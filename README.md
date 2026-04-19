# Product Catalog Application

This is a Spring Boot application for managing a product catalog with AI-powered chatbot features.

## Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd product_catalog
   ```

2. **Environment Configuration**
   - Copy `.env.example` to `.env`
   - Fill in your actual secret values in `.env`

   Required environment variables:
   - Database credentials (MySQL)
   - Google OAuth credentials
   - Google AI API keys
   - Redis password

3. **Local Development**
   - Make sure MySQL and Redis are running locally
   - Run the application:
     ```bash
     mvn spring-boot:run
     ```

4. **Docker Development**
   - Build and run with Docker Compose:
     ```bash
     docker-compose up --build
     ```

## Security

All sensitive configuration (API keys, passwords, secrets) are externalized to the `.env` file, which is:
- Not committed to version control (added to .gitignore)
- Loaded automatically by the application using dotenv-java
- Used by Docker Compose for containerized deployment

Never commit actual secrets to the repository. Use `.env.example` as a template for required environment variables.