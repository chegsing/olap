# OLAP Analytics API

Sistema de análisis OLAP con arquitectura hexagonal para consultas agregadas sobre cubos de datos.

## 🏗️ Arquitectura

- **Arquitectura Hexagonal**: Separación clara entre dominio, aplicación e infraestructura
- **Clean Code**: Métodos máximo 20 líneas, máximo 3 parámetros
- **SOLID Principles**: Aplicación de principios de diseño
- **Zero Dependencies**: Dominio libre de dependencias externas

## 🚀 Tecnologías

- Java 21
- Spring Boot 4.0.0
- Spring WebFlux (Reactive)
- SQL Server JDBC Driver
- Swagger/OpenAPI 3
- JUnit 5 + Mockito
- Gradle 9.2.1
- Jacoco (Cobertura 90%+)
- H2 Database (Desarrollo)

## 📋 Funcionalidades

### Consultas OLAP
- ✅ Consultas SQL agregadas
- ✅ Consultas MDX (XMLA)
- ✅ Filtros dinámicos
- ✅ Validación de columnas autorizadas
- ✅ Manejo reactivo de respuestas

### Seguridad
- ✅ Validación de parámetros de entrada
- ✅ Sanitización de consultas SQL
- ✅ Prevención de inyección SQL
- ✅ Configuración segura de XML parsing

## 🚀 Inicio Rápido con H2

```bash
# 1. Clonar y ejecutar
git clone <repo>
cd olap
./gradlew bootRun --args='--spring.profiles.active=h2'

# 2. Probar API
curl "http://localhost:8080/api/olap/aggregate?dimensions=Region&measures=Sales"

# 3. Ver Swagger
# http://localhost:8080/swagger-ui.html
```

## 🔧 Configuración

### Base de datos SQL Server
```yaml
app:
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=OlapDb
    username: sa
    password: your_password
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
    name: vw_olap_cube
  allowed-columns: Region,Product,Sales,Quantity,Year
```

### Ejecutar aplicación

#### Con SQL Server (Producción)
```bash
./gradlew bootRun
```

#### Con H2 (Desarrollo/Demo)
```bash
# Opción 1: Parámetro en línea
./gradlew bootRun --args='--spring.profiles.active=h2'

# Opción 2: Variable de entorno
set SPRING_PROFILES_ACTIVE=h2
./gradlew bootRun
```

## 📚 API Documentation

Una vez iniciada la aplicación, accede a:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs

### Con H2 (Desarrollo)
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:olapdb`
  - Username: `sa`
  - Password: (vacío)
- **Datos precargados**: Region, Product, Sales, Quantity, Year (2024)

## 🔐 Endpoints

### Consulta OLAP Agregada
```http
GET /api/olap/aggregate?dimensions=Region,Product&measures=Sales,Quantity&filters=Year:2024|Region:EMEA
```

#### Ejemplos con H2
```http
# Todas las regiones
GET /api/olap/aggregate?dimensions=Region&measures=Sales

# Por producto
GET /api/olap/aggregate?dimensions=Product&measures=Sales,Quantity

# Filtrado por región
GET /api/olap/aggregate?dimensions=Region,Product&measures=Sales&filters=Region:EMEA
```

**Parámetros:**
- `dimensions`: Dimensiones para agrupar (separadas por comas)
- `measures`: Medidas a agregar (separadas por comas)  
- `filters`: Filtros en formato `clave:valor|clave2:valor2`

**Respuesta:**
```json
{
  "rows": [
    {
      "values": {
        "Region": "EMEA",
        "Product": "ProductA",
        "Sales": 1000,
        "Quantity": 50
      }
    }
  ],
  "rowCount": 1
}
```

## 🏛️ Estructura del proyecto

```
src/main/java/com/prueba/olap/
├── domain/
│   └── entity/           # Entidades de dominio (Query, QueryType)
├── port/                 # Interfaces del dominio (OlapQueryPort)
├── application/          # Casos de uso (en service/)
├── adapter/              # Adaptadores de infraestructura
│   ├── JdbcOlapAdapter   # Adaptador para SQL Server
│   └── XmlaOlapAdapter   # Adaptador para XMLA/MDX
├── service/              # Servicios de aplicación
│   ├── OlapService       # Lógica de negocio OLAP
│   └── dto/              # DTOs de transferencia
├── controller/           # Controladores REST
├── config/               # Configuraciones Spring
└── OlapApplication       # Clase principal
```

## 🧪 Testing

```bash
# Ejecutar todos los tests
./gradlew test

# Generar reporte de cobertura
./gradlew jacocoTestReport

# Verificar cobertura mínima (90%)
./gradlew jacocoTestCoverageVerification
```

### Tipos de Tests
- **Unitarios**: Lógica de negocio y adaptadores
- **Integración**: Base de datos H2 en memoria
- **DTOs**: Validación de objetos de transferencia

## 🔒 Seguridad Implementada

- Validación de parámetros de entrada
- Escape de caracteres especiales en SQL
- Prevención de XXE en parsing XML
- Límite de filtros por consulta (máximo 3)
- Validación de columnas autorizadas

## 📊 Monitoreo

- Cobertura de código: 90%+ requerida
- Métricas de Jacoco habilitadas
- Reportes HTML y XML generados

## 🚀 Mejoras Implementadas

### Arquitectura
- ✅ Separación clara de capas (Dominio, Aplicación, Infraestructura)
- ✅ Implementación de puertos y adaptadores
- ✅ Entidades de dominio sin dependencias externas

### Código
- ✅ Métodos con máximo 20 líneas
- ✅ Máximo 3 parámetros por método
- ✅ Validaciones exhaustivas
- ✅ Manejo de errores robusto
- ✅ Documentación Javadoc completa

### Testing
- ✅ Cobertura de tests 90%+
- ✅ Tests unitarios y de integración
- ✅ Mocks apropiados con Mockito
- ✅ Verificación de comportamiento

### Seguridad
- ✅ Prevención de inyección SQL
- ✅ Configuración segura de XML
- ✅ Validación de entrada
- ✅ Manejo seguro de excepciones

## 📝 Notas de desarrollo

- Todos los métodos respetan el límite de 20 líneas
- Máximo 3 parámetros por método
- Sin código hardcoded (uso de properties)
- Documentación Javadoc estándar
- Principios SOLID aplicados
- Arquitectura hexagonal implementada correctamente