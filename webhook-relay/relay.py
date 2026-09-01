#!/usr/bin/env python3
"""
Webhook Relay: Polls Elasticsearch for ERROR logs, sends GitHub repository_dispatch webhooks.
Replaces Kibana's paid Webhook connector for the PoC.
"""

import json
import os
import sys
import time
import hashlib
from datetime import datetime, timezone
from urllib.request import Request, urlopen
from urllib.error import URLError, HTTPError

# --- Configuration from environment ---
ES_HOST = os.getenv("ES_HOST", "http://host.docker.internal:9200")
ES_INDEX = os.getenv("ES_INDEX", "filebeat-*")
POLL_INTERVAL = int(os.getenv("POLL_INTERVAL", "30"))  # seconds
GITHUB_REPO = os.getenv("GITHUB_REPO", "mohankrishna213/ai-cart")
GITHUB_TOKEN = os.getenv("GITHUB_TOKEN", "")

# State: track last poll timestamp to avoid re-sending alerts
STATE_FILE = "/tmp/relay_state.json"


def log(msg):
    ts = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    print(f"[{ts}] {msg}", flush=True)


def load_state():
    try:
        with open(STATE_FILE, "r") as f:
            return json.load(f)
    except (FileNotFoundError, json.JSONDecodeError):
        return {"last_poll_ts": None, "sent_alert_ids": []}


def save_state(state):
    with open(STATE_FILE, "w") as f:
        json.dump(state, f)


def query_es_errors(state):
    """Query Elasticsearch for ERROR logs since last poll."""
    now_iso = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%S.000Z")
    last_poll = state.get("last_poll_ts")

    if last_poll:
        time_range = {"range": {"@timestamp": {"gt": last_poll, "lte": now_iso}}}
    else:
        # First run: look back 5 minutes
        time_range = {"range": {"@timestamp": {"gte": "now-5m"}}}

    query = {
        "bool": {
            "must": [
                {"match": {"level": "ERROR"}},
                {"match": {"service_name": "ai-cart"}},
                time_range,
            ]
        }
    }

    payload = json.dumps({"query": query, "size": 10, "sort": [{"@timestamp": {"order": "desc"}}]}).encode()

    url = f"{ES_HOST}/{ES_INDEX}/_search"
    req = Request(url, data=payload, headers={"Content-Type": "application/json"})

    try:
        with urlopen(req, timeout=10) as resp:
            data = json.loads(resp.read())
            return data.get("hits", {}).get("hits", []), now_iso
    except (URLError, HTTPError) as e:
        log(f"ES query failed: {e}")
        return [], now_iso


def build_alert_payload(hit):
    """Build GitHub repository_dispatch payload from an ES hit."""
    src = hit.get("_source", {})
    alert_id = hashlib.md5(hit.get("_id", "").encode()).hexdigest()[:12]

    return {
        "event_type": "kibana_anomaly_alert",
        "client_payload": {
            "rule_name": "High Error Rate - ai-cart",
            "error_message": src.get("message", "Unknown error"),
            "stack_trace": src.get("stack_trace", src.get("error.stack_trace", "N/A")),
            "affected_service": src.get("service_name", "ai-cart"),
            "alert_id": alert_id,
            "timestamp": src.get("@timestamp", datetime.now(timezone.utc).isoformat()),
        },
    }


def send_github_api(url, payload, description):
    """Send a POST request to GitHub API."""
    body = json.dumps(payload).encode()
    req = Request(
        url,
        data=body,
        headers={
            "Content-Type": "application/json",
            "Accept": "application/vnd.github.v3+json",
            "Authorization": f"token {GITHUB_TOKEN}",
        },
        method="POST",
    )
    try:
        with urlopen(req, timeout=15) as resp:
            log(f"{description} sent (HTTP {resp.getcode()})")
            return True
    except HTTPError as e:
        body_err = e.read().decode() if e.fp else ""
        log(f"{description} failed (HTTP {e.code}): {body_err[:200]}")
        return False
    except URLError as e:
        log(f"{description} connection failed: {e}")
        return False


def send_webhooks(alert_data):
    """Send both repository_dispatch (CLI workflow) and workflow_dispatch (Cloud Agent)."""
    base_url = f"https://api.github.com/repos/{GITHUB_REPO}"

    # 1. repository_dispatch → triggers predictive-healing.yml (CLI workflow)
    dispatch_payload = {
        "event_type": "kibana_anomaly_alert",
        "client_payload": alert_data,
    }
    send_github_api(f"{base_url}/dispatches", dispatch_payload, "repository_dispatch (CLI)")

    # 2. workflow_dispatch → triggers predictive-healing-cloud.yml (Cloud Agent)
    workflow_payload = {
        "ref": "main",
        "inputs": {
            "rule_name": alert_data.get("rule_name", ""),
            "error_message": alert_data.get("error_message", ""),
            "stack_trace": alert_data.get("stack_trace", ""),
            "affected_service": alert_data.get("affected_service", ""),
            "alert_id": alert_data.get("alert_id", ""),
            "timestamp": alert_data.get("timestamp", ""),
        },
    }
    send_github_api(f"{base_url}/actions/workflows/predictive-healing-cloud.yml/dispatches", workflow_payload, "workflow_dispatch (Cloud Agent)")


def main():
    if not GITHUB_TOKEN:
        log("ERROR: GITHUB_TOKEN not set. Set it in .env or as environment variable.")
        sys.exit(1)

    log(f"Webhook Relay started. ES={ES_HOST}, Repo={GITHUB_REPO}, Interval={POLL_INTERVAL}s")

    state = load_state()

    while True:
        try:
            hits, poll_ts = query_es_errors(state)
            new_alerts = []

            for hit in hits:
                hit_id = hit.get("_id", "")
                if hit_id not in state.get("sent_alert_ids", []):
                    new_alerts.append(hit)

            if new_alerts:
                log(f"Found {len(new_alerts)} new ERROR alert(s)")

                for hit in new_alerts:
                    alert_data = build_alert_payload(hit)["client_payload"]
                    log(f"Sending alert: {alert_data['error_message'][:80]}")
                    send_webhooks(alert_data)

                    # Track sent alert (keep last 100)
                    state.setdefault("sent_alert_ids", []).append(hit.get("_id", ""))
                    state["sent_alert_ids"] = state["sent_alert_ids"][-100:]
            else:
                log("No new errors")

            state["last_poll_ts"] = poll_ts
            save_state(state)

        except Exception as e:
            log(f"Poll cycle error: {e}")

        time.sleep(POLL_INTERVAL)


if __name__ == "__main__":
    main()
