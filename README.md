# OLAP Aggregation API

Proyecto demo que expone un endpoint REST para consultar datos agregados desde un cubo OLAP (implementado sobre SQL Server) siguiendo arquitectura hexagonal y buenas prácticas.

Contenido
- `src/main/java`: Código fuente (controller, service, ports, adapters, config)
- `src/test/java`: Tests unitarios
- `src/main/resources`: Configuración por perfiles (YAML)

Requisitos
- Java 21
- Gradle wrapper (incluido)
- SQL Server JDBC driver ya incluido en `build.gradle` (mssql-jdbc)

Preparación
1. Clona o abre el proyecto en tu máquina.
2. Configura las propiedades de conexión a la base de datos por perfil (ver `src/main/resources/application-{dev,test,stage,prod}.yml`).
   - En desarrollo: puedes editar `application-dev.yml` directamente.
   - En producción: usa variables de entorno para las credenciales (v. `application-prod.yml`).

Variables de entorno (ejemplos)
- `SPRING_PROFILES_ACTIVE` — perfil activo: `dev`, `test`, `stage` o `prod`.
- Para `prod` (ejemplos):
  - `APP_DATASOURCE_URL` — p. ej. `jdbc:sqlserver://host:1433;databaseName=OlapProdDb`
  - `APP_DATASOURCE_USERNAME`
  - `APP_DATASOURCE_PASSWORD`
  - `APP_DATASOURCE_DRIVER_CLASS_NAME` (opcional; por defecto es `com.microsoft.sqlserver.jdbc.SQLServerDriver`)
  - `APP_DATASOURCE_NAME` — nombre lógico de la vista/tabala del cubo (ej: `cube_view_prod`)
  - `APP_ALLOWED_COLUMNS` — lista de columnas permitidas (coma-separadas)

Ejecutar localmente (desarrollo)
1. Establece el perfil `dev` (opcional — `application.yml` por defecto activa `dev`):

```cmd
set SPRING_PROFILES_ACTIVE=dev
```

2. Ejecuta la aplicación con Gradle (desde la raíz del proyecto):

```cmd
gradlew.bat bootRun
```

3. Accede a Swagger UI para explorar el endpoint:

http://localhost:8080/swagger-ui.html

Endpoints principales
- GET `/api/olap/aggregate` — parámetros de consulta: `dimensions`, `measures`, `filters`.
  - `dimensions`: columnas a agrupar (coma-separadas)
  - `measures`: medidas a agregar (coma-separadas)
  - `filters`: filtros en formato `key:value|key2:value2`

Ejemplo de llamada:

```http
GET /api/olap/aggregate?dimensions=Region&measures=Sales&filters=Year:2024
```

Tests y cobertura
- Ejecutar tests:

```cmd
gradlew.bat test
```

- Generar reporte JaCoCo (se genera automáticamente tras `test`):
  - HTML: `build/reports/jacoco/test/html/index.html`
  - XML: `build/reports/jacoco/test/jacocoTestReport.xml`

Cobertura mínima
- El proyecto está configurado con JaCoCo y una verificación que exige 90% de cobertura. Si la verificación falla, el build fallará. Añade tests unitarios según sea necesario.

Buenas prácticas y notas
- NO versionar credenciales en el repositorio. Usa variables de entorno o un vault.
- `app.allowed-columns` controla la whitelist de columnas permitidas para evitar inyección a nivel de columnas.
- Para evitar inyección en valores de filtro se recomienda parametrizar queries (PreparedStatements) — puedo implementarlo como mejora si lo deseas.

Soporte
- Si quieres que ejecute los tests o genere el reporte aquí, indícalo y ejecutaré `gradlew.bat test` y te pasaré los logs y el HTML de cobertura.
