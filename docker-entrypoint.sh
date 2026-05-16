#!/bin/sh
# Render provides DATABASE_URL as postgresql://... — JDBC needs jdbc: prefix
if [ -n "$DATABASE_URL" ]; then
  export SPRING_DATASOURCE_URL="jdbc:${DATABASE_URL}"
fi
exec java -jar app.jar
