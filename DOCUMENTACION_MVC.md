# Sistema de Gestión con Persistencia Políglota

Sistema web desarrollado con Spring Boot 3.3.x que implementa una arquitectura MVC tradicional con Thymeleaf, integrando tres tipos de bases de datos (PostgreSQL, MongoDB y Redis) para diferentes casos de uso.

## 🏗️ Arquitectura

### Patrón MVC con Thymeleaf
- **Controllers Web**: Manejo de solicitudes HTTP y lógica de presentación
- **Services**: Lógica de negocio
- **Repositories**: Acceso a datos
- **Views**: Templates Thymeleaf con Bootstrap 5.3.2

### Bases de Datos
- **PostgreSQL**: Datos relacionales (usuarios, facturas, pagos, procesos, etc.)
- **MongoDB**: Datos IoT (sensores, mediciones, alertas)
- **Redis**: Sesiones, notificaciones y métricas en tiempo real

## 📋 Módulos del Sistema

### 🏠 Dashboard
- **Ruta**: `/dashboard`
- **Vista principal** con métricas generales del sistema

### 🌡️ Módulo IoT

#### Sensores
- **Ruta**: `/sensores`
- **CRUD completo**: listar, crear, editar, eliminar sensores
- **Gestión de estados**: activar/desactivar sensores
- **Templates**: `sensores/lista.html`, `sensores/formulario.html`, `sensores/detalles.html`

#### Mediciones
- **Ruta**: `/mediciones`
- **Funciones**: registrar mediciones, filtrar por sensor, visualizar histórico
- **Templates**: `mediciones/lista.html`, `mediciones/formulario.html`, `mediciones/detalles.html`

#### Alertas
- **Ruta**: `/alertas`
- **Funciones**: visualizar alertas del sistema, filtrar por nivel/estado, resolver alertas
- **Templates**: `alertas/lista.html`, `alertas/detalles.html`

### 👥 Módulo de Usuarios
- **Ruta**: `/usuarios`
- **CRUD completo**: gestión de usuarios del sistema
- **Gestión de roles** y permisos
- **Templates**: `usuarios/lista.html`, `usuarios/formulario.html`, `usuarios/detalles.html`

### 💰 Módulo Financiero

#### Facturas
- **Ruta**: `/facturas`
- **Funciones**: emisión, consulta, pago y anulación de facturas
- **Estados**: pendiente, pagada, vencida, anulada
- **Templates**: `facturas/lista.html`, `facturas/detalles.html`

#### Pagos
- **Ruta**: `/pagos`
- **Funciones**: registro de pagos, múltiples métodos (efectivo, transferencia, tarjetas)
- **Templates**: `pagos/lista.html`, `pagos/formulario.html`, `pagos/detalles.html`

#### Cuentas Corrientes
- **Ruta**: `/cuentas`
- **Funciones**: gestión de saldos, registro de débitos y créditos
- **Templates**: `cuentas/lista.html`, `cuentas/detalles.html`

### 📄 Módulo Administrativo

#### Procesos
- **Ruta**: `/procesos`
- **CRUD completo**: definición y gestión de procesos del negocio
- **Templates**: `procesos/lista.html`, `procesos/formulario.html`, `procesos/detalles.html`

#### Solicitudes de Proceso
- **Ruta**: `/solicitudes`
- **Funciones**: crear solicitudes, aprobar/rechazar
- **Estados**: pendiente, aprobada, rechazada
- **Templates**: `solicitudes/lista.html`, `solicitudes/formulario.html`, `solicitudes/detalles.html`

### 💬 Módulo de Comunicación

#### Conversaciones
- **Ruta**: `/conversaciones`
- **Funciones**: mensajería entre usuarios, chat en tiempo real
- **Templates**: `conversaciones/lista.html`, `conversaciones/chat.html`, `conversaciones/formulario.html`

### 🔐 Módulo de Seguridad

#### Autenticación
- **Ruta**: `/login`, `/logout`
- **Sistema de autenticación** con Spring Security
- **Template**: `auth/login.html`

## 🎨 Frontend

### Tecnologías
- **Bootstrap 5.3.2**: Framework CSS
- **Bootstrap Icons**: Iconografía
- **Thymeleaf**: Motor de plantillas
- **CSS personalizado**: `static/css/style.css`
- **JavaScript**: `static/js/main.js`

### Características del Diseño
- Diseño responsivo
- Gradientes personalizados
- Animaciones CSS
- Tarjetas con efectos hover
- Sistema de notificaciones
- Validación de formularios

## 🚀 Cómo Ejecutar

### Prerrequisitos
- Java 17 o superior
- PostgreSQL
- MongoDB
- Redis
- Maven

### Configuración

1. **Configurar base de datos** en `application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tu_bd
    username: usuario
    password: contraseña
  
  data:
    mongodb:
      uri: mongodb://localhost:27017/tu_bd
  
  redis:
    host: localhost
    port: 6379
```

2. **Compilar el proyecto**:
```bash
mvn clean install
```

3. **Ejecutar la aplicación**:
```bash
mvn spring-boot:run
```

4. **Acceder a la aplicación**:
```
http://localhost:8080
```

## 📁 Estructura del Proyecto

```
src/main/
├── java/com/tp/persistencia/persistencia_poliglota/
│   ├── config/              # Configuraciones
│   ├── controller/web/      # Controladores MVC
│   ├── dto/                 # Data Transfer Objects
│   ├── model/               # Entidades
│   │   ├── nosql/          # MongoDB (Sensor, Medicion, Alerta)
│   │   ├── redis/          # Redis (Sesion, Notificacion)
│   │   └── sql/            # PostgreSQL (Usuario, Factura, etc.)
│   ├── repository/          # Repositorios
│   ├── security/            # Seguridad
│   └── service/             # Servicios
│
└── resources/
    ├── static/
    │   ├── css/            # Estilos personalizados
    │   └── js/             # JavaScript
    └── templates/          # Vistas Thymeleaf
        ├── auth/
        ├── dashboard/
        ├── sensores/
        ├── mediciones/
        ├── alertas/
        ├── usuarios/
        ├── facturas/
        ├── pagos/
        ├── cuentas/
        ├── procesos/
        ├── solicitudes/
        ├── conversaciones/
        └── layout/         # Template base
```

## 🛠️ Tecnologías Utilizadas

- **Backend**: Spring Boot 3.3.x, Spring MVC, Spring Data JPA, Spring Data MongoDB, Spring Data Redis
- **Frontend**: Thymeleaf, Bootstrap 5.3.2, Bootstrap Icons
- **Bases de Datos**: PostgreSQL, MongoDB, Redis
- **Seguridad**: Spring Security
- **Build Tool**: Maven
- **Java**: 17+

## 📝 Características Técnicas

- ✅ **Sin API REST**: Aplicación MVC tradicional pura
- ✅ **Sin JSON**: Toda la comunicación mediante formularios HTML
- ✅ **Multi-base de datos**: Persistencia políglota
- ✅ **Responsive Design**: Compatible con dispositivos móviles
- ✅ **Validación de formularios**: Cliente y servidor
- ✅ **Mensajes flash**: Feedback al usuario
- ✅ **Templates reutilizables**: Base layout común
- ✅ **CRUD completo**: Para todas las entidades

## 👨‍💻 Desarrollo

Este proyecto fue convertido completamente de una arquitectura REST API a una aplicación MVC tradicional con Thymeleaf, eliminando todo rastro de APIs REST y respuestas JSON.

## 📄 Licencia

Este proyecto es de código abierto y está disponible bajo la licencia especificada en el repositorio.
