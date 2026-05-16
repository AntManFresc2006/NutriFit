#!/bin/sh
# Render DATABASE_URL: postgresql://user:pass@host/db
# JDBC needs: jdbc:postgresql://host/db (credentials via separate properties)
if [ -n "$DATABASE_URL" ]; then
  AFTER_AT=$(echo "$DATABASE_URL" | sed 's|postgresql://[^@]*@||')
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${AFTER_AT}"
fi
exec java -jar app.jar
