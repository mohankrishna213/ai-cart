# AGENTS.md

## Architecture Overview

This is a Spring Boot 3.4.3 application (Java 21) for managing a product catalog with AI-powered chatbot features. Key components:

- **Backend**: Spring Boot with JPA/Hibernate, MySQL database, Redis caching
- **Security**: Spring Security with JWT tokens and Google OAuth2 login
- **AI Integration**: Spring AI with Google GenAI (Gemma 4) for chat and embeddings, vector store for RAG
- **Frontend**: Thymeleaf templates for web UI, REST APIs for data
- **Data Flow**: Products → Document loader → Vector store → Chatbot service → LLM → UI cards

## Critical Workflows

### Development Setup
- Copy `.env.example` to `.env` and fill secrets (DB, Redis, Google AI/OAuth keys)
- Local: `mvn spring-boot:run` (requires MySQL/Redis running)
- Docker: `docker-compose up --build` (includes MySQL/Redis healthchecks)
- Build: `mvn clean package -DskipTests` (multi-stage Docker build)

### Testing
- Unit tests: `mvn test` (H2 in-memory DB for tests)
- Integration tests: API endpoints with test data
- Data initialization: `DataInitializer` creates sample users/products/reviews on startup

### Debugging
- Logs: Spring Boot default logging, custom error handling in controllers
- Cache: Redis-backed with `@Cacheable/@CacheEvict` annotations
- AI: Chatbot responses logged, vector store queries traceable

## Project Conventions

### OpenSpec Change Management
- Changes managed via `openspec/` directory with structured artifacts
- Workflow: proposal → design → tasks → specs → implementation
- Use skills like `openspec-apply-change` for guided implementation
- Example: `add-promotional-pricing` change with detailed design/tasks/specs

### Promotional Pricing
- Products have optional `promotionalPrice` (Double) and `isPromoActive` (boolean)
- Validation in `ProductServiceImpl.updateProduct()`: promo price < standard price, >= 0
- Cache eviction: specific product + all search results on update
- DTO mapping: manual `toProductsDTO/toProductsEntity` methods in controllers

### AI Chatbot Patterns
- RAG implementation: products/categories loaded as documents with metadata
- Vector search with similarity threshold (0.5), max results (5)
- LLM prompt enforces JSON response for UI cards display
- Document metadata: type, id, name, price, etc. for recommendations

### Caching Strategy
- Redis cache buckets: "products", "searchResults", "product-suggestions"
- Keys: specific IDs, pageable params, search terms
- Eviction: targeted for updates, allEntries for searches

### Security & Auth
- JWT tokens for API auth, Google OAuth for web login
- Roles: ADMIN/CUSTOMER with `@PreAuthorize` annotations
- Secrets: externalized to `.env`, loaded via dotenv-java

### Database Migrations
- Flyway scripts in `db/migration/` (e.g., V001__add_promotional_pricing.sql)
- Entity fields use `@Column` for custom names if needed
- Relationships: Products ↔ Categories (ManyToOne), Products ↔ Reviews/Wishlist (OneToMany)

## Integration Points

### External Dependencies
- Google AI Studio: chat (Gemma 4) and embeddings (text-embedding-004)
- Google OAuth2: client registration in `application.properties`
- MySQL: local/dev DB with Docker healthchecks
- Redis: caching and session store

### Cross-Component Communication
- Controllers inject services, services inject repos
- Chatbot: ProductDocumentLoader → VectorStore → ChatbotService
- Cache: AOP annotations on service methods
- Events: None explicit, but cache eviction simulates reactivity

## Key Files

- `Product_catalog.java`: Main class with .env loading
- `DataInitializer.java`: Sample data creation (users, products, category-specific reviews)
- `ChatbotService.java`: RAG implementation with LLM prompting
- `ProductServiceImpl.java`: Business logic with validation/caching
- `ProductsController.java`: CRUD APIs with DTO mapping
- `openspec/changes/add-promotional-pricing/`: Example change artifacts
