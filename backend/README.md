# NutriFit Backend (Spring Boot)

Pasos rápidos para desarrollar y arrancar el backend localmente.

Requisitos:
- JDK 17 (o superior)
- Maven 3.6+

1) Construir:

```powershell
mvn -f backend -DskipTests clean package
```

2) Arrancar usando perfil `local` y variables de entorno (PowerShell ejemplo):

```powershell
$env:SPRING_DATASOURCE_URL = 'jdbc:mariadb://localhost:3306/nutrifit'
$env:SPRING_DATASOURCE_USERNAME = 'root'
$env:SPRING_DATASOURCE_PASSWORD = 'secret'

mvn -f backend spring-boot:run -Dspring-boot.run.profiles=local
```

O usando el JAR empaquetado:

```powershell
java -jar backend\target\nutrifit-backend-0.1.0-SNAPSHOT.jar --spring.profiles.active=local
```

Notas:
- El fichero `application-local.properties` está configurado para leer las credenciales
  desde variables de entorno (ver claves `SPRING_DATASOURCE_*`). No incluyas credenciales
  en el repositorio. Si prefieres, copia `application-local.properties` y añade valores
  en tu máquina local (este repo ignora `application-local.properties`).
