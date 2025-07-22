package org.techm.samples.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.techm.samples.entity.Reviews;

public interface ReviewsRepository extends JpaRepository<Reviews, Long> {

}
