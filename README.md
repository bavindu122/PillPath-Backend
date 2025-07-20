## Local Development Setup

1. Copy `application.properties(example)` to `src/main/resources/application.properties`
2. Create a `.env` file in the project root with your actual values (see example above)
3. Run the application with `mvn spring-boot:run`

## Production Deployment

Set the environment variables in your deployment environment:
- For Docker: Use `-e` flags or `docker-compose.yml`
- For cloud platforms: Use their environment variable configuration

## Testing Configuration

To verify your configuration is working:
```bash
curl http://localhost:8080/actuator/health