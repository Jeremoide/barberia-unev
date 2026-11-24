# Preparar MariaDB en otro equipo

El equipo actual ya tiene la base `barberia_unev`, sus once tablas, los triggers y un usuario propio configurado. No hace falta repetir la instalación aquí.

## Crear el esquema en otro servidor

Con una cuenta administradora de MariaDB, ejecutar en orden:

```bash
sudo mariadb < database/01_esquema.sql
sudo mariadb < database/02_reglas.sql
sudo mariadb --table barberia_unev < database/03_verificacion.sql
```

Si el servidor usa otra autenticación, entrar con las credenciales administrativas configuradas. También se pueden abrir y ejecutar 01 y 02 desde el gestor SQL conectado al servidor correcto.

Crear un usuario local propio con una contraseña elegida para ese equipo. En este ejemplo se debe sustituir `TU_CLAVE_LOCAL` antes de ejecutar:

```sql
CREATE USER 'barberia_unev_app'@'127.0.0.1' IDENTIFIED BY 'TU_CLAVE_LOCAL';
GRANT SELECT, INSERT, UPDATE, DELETE ON barberia_unev.* TO 'barberia_unev_app'@'127.0.0.1';
```

Copiar `config/database.properties.example` a `config/database.properties` y colocar esa contraseña. Restringir los permisos con `chmod 600 config/database.properties`. Si el usuario ya existe, utilizar sus credenciales o crear otro nombre y ajustar la configuración; no cambiar su contraseña automáticamente.

## Probar desde Java

```bash
mvn compile exec:java -Dexec.mainClass=edu.unev.barberia.database.PruebaConexion
```

No se utiliza root desde Java. La cuenta de la aplicación no tiene permisos para administrar usuarios ni crear/eliminar bases de datos. Las pruebas de integración utilizan transacciones que se revierten al terminar.

El esquema no es una migración automática de bases previas: CREATE TABLE IF NOT EXISTS conserva una tabla existente sin cambiar sus columnas. Las futuras modificaciones deben gestionarse mediante migraciones explícitas.
