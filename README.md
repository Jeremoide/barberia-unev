# Barbería UNEV

Aplicación de escritorio para gestionar el acceso del personal, las citas, los servicios y los pagos de una barbería. Utiliza Java 25, JavaFX, FXML, Maven y MariaDB mediante JDBC.

## Ejecutar

Abrir esta carpeta en Visual Studio Code y esperar la importación del proyecto Maven. Se requiere JDK 25.

```bash
mvn test
mvn javafx:run
```

Maven descarga las dependencias en la primera ejecución.

Cuenta inicial de demostración: usuario `admin`, contraseña `Admin123!`. Se crea cuando no hay cuentas guardadas. Las cuentas nuevas se registran desde el formulario de Registro.

## Estructura

```text
src/main/java/edu/unev/barberia/
├── App.java          Inicio de la aplicación JavaFX
├── model/            Once entidades y sus tipos auxiliares
├── servicio/         Autenticación, agenda y reglas de negocio
├── controlador/      Controladores de Login, Registro y panel
└── database/         Conexión JDBC y prueba ejecutable
src/main/resources/   FXML y estilos de la interfaz
src/test/             Pruebas de modelos, servicios, interfaz y base de datos
database/             Scripts SQL e instrucciones de instalación
config/               Configuración local de JDBC y plantilla de ejemplo
datos/                Cuentas locales, excluidas de Git
target/               Archivos de compilación y resultados de pruebas generados por Maven
.vscode/              Configuración de desarrollo
```

El modelo contiene Usuario, Cliente, Empleado, Servicio, Cita, Pago, Agenda, HorarioTrabajo, Factura, DetalleFactura y CategoriaServicio. Los enums describen roles y estados.

## Funciones y persistencia

- Login y Registro con validación de campos, correo, contraseñas y cuentas duplicadas.
- Agenda de demostración con validación de fechas futuras y cruces de horarios.
- Completar o cancelar citas, registrar un pago por cita y consultar el total cobrado.
- Modelos de horarios, categorías y facturación, con pruebas de sus reglas.
- Base MariaDB con once tablas y prueba de conexión JDBC.

Las cuentas utilizadas por Login y Registro se guardan en `datos/usuarios.tsv`; las contraseñas se protegen con PBKDF2-HMAC-SHA256 y sal aleatoria. El resto de la interfaz utiliza datos de muestra en memoria: las citas y los pagos de la pantalla no se guardan todavía en MariaDB. La conexión y el esquema están preparados para incorporar esa persistencia.

El rol de usuario es descriptivo; los permisos diferenciados todavía no se aplican. Las cuentas de demostración están destinadas al desarrollo.

## Base de datos

Consultar [las instrucciones de MariaDB](database/README.md) y ejecutar los scripts de `database/` en orden. En este equipo la base `barberia_unev` ya está creada.

La conexión lee `config/database.properties`, excluido de Git. Para configurar otro equipo, copiar `config/database.properties.example` y completar las credenciales de un usuario propio de la aplicación. También se admiten `BARBERIA_DB_URL`, `BARBERIA_DB_USER` y `BARBERIA_DB_PASSWORD`. Java no necesita acceder como root.

```bash
mvn compile exec:java -Dexec.mainClass=edu.unev.barberia.database.PruebaConexion
```

## Pruebas

```bash
# Pruebas de reglas de negocio y modelos
mvn test

# Añadir integración real con MariaDB
BARBERIA_DB_TEST=true mvn test

# Añadir también la interfaz JavaFX, con sesión gráfica disponible
BARBERIA_DB_TEST=true BARBERIA_UI=true mvn clean package
```

Las pruebas de base de datos revierten sus inserciones mediante rollback. Las capturas de la prueba gráfica se generan en `target/capturas-pruebas/`. Los informes se generan en `target/surefire-reports/`.

## Editar las vistas

Los formularios están en `src/main/resources/vistas/`. Se pueden abrir en Scene Builder; la lógica de sus eventos está en los controladores Java.

```bash
flatpak run com.gluonhq.SceneBuilder
```
