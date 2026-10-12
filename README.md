# SkillSwap Backend

## Deploy on Render

Create a Render Blueprint from this repository and use the included `render.yaml`.
The Dockerfile builds the Spring Boot application with Java 17. Render generates
a JWT signing secret and prompts for the database connection settings.

For TiDB Cloud, set:

- `SPRING_DATASOURCE_URL`: `jdbc:mysql://<cluster-host>:4000/<database>?sslMode=VERIFY_IDENTITY`
- `SPRING_DATASOURCE_USERNAME`: the TiDB database username
- `SPRING_DATASOURCE_PASSWORD`: the TiDB database password
- `FRONTEND_URL`: the allowed frontend origin. It defaults to `http://localhost:5173`
  for local development and is set to the deployed Vercel origin in `render.yaml`.

Keep database credentials and other secrets in Render environment variables, not
in source control. The default H2 database remains available for local development.
