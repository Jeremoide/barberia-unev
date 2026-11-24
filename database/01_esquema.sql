-- Tarea 3 - Jeremy Peguero Almonte, 2022-3200298.
-- No elimina bases, tablas ni datos existentes. Motor seleccionado: MariaDB.
CREATE DATABASE IF NOT EXISTS barberia_unev CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE barberia_unev;

CREATE TABLE IF NOT EXISTS usuarios (
 id_usuario CHAR(36) PRIMARY KEY,
 nombre VARCHAR(100) NOT NULL,
 apellido VARCHAR(100) NOT NULL,
 usuario VARCHAR(30) NOT NULL UNIQUE,
 correo VARCHAR(254) NOT NULL UNIQUE,
 rol VARCHAR(20) NOT NULL CHECK (rol IN ('ADMINISTRADOR','RECEPCIONISTA','BARBERO')),
 sal VARCHAR(24) NOT NULL,
 hash VARCHAR(44) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS clientes (
 id_cliente INT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(150) NOT NULL,
 telefono VARCHAR(30) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS empleados (
 id_empleado INT AUTO_INCREMENT PRIMARY KEY,
 id_usuario CHAR(36) NOT NULL UNIQUE,
 especialidad VARCHAR(150) NOT NULL,
 CONSTRAINT fk_empleado_usuario FOREIGN KEY (id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS categorias_servicio (
 id_categoria INT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(100) NOT NULL UNIQUE,
 descripcion VARCHAR(255)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS servicios (
 id_servicio INT AUTO_INCREMENT PRIMARY KEY,
 nombre VARCHAR(100) NOT NULL,
 precio DECIMAL(10,2) NOT NULL CHECK (precio>0),
 duracion_minutos INT NOT NULL CHECK (duracion_minutos>0),
 id_categoria INT NOT NULL,
 CONSTRAINT fk_servicio_categoria FOREIGN KEY (id_categoria) REFERENCES categorias_servicio(id_categoria)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS agendas (
 id_agenda INT AUTO_INCREMENT PRIMARY KEY,
 id_empleado INT NOT NULL UNIQUE,
 UNIQUE KEY uq_agenda_empleado (id_agenda,id_empleado),
 CONSTRAINT fk_agenda_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS horarios_trabajo (
 id_horario INT AUTO_INCREMENT PRIMARY KEY,
 id_empleado INT NOT NULL,
 dia_semana TINYINT NOT NULL CHECK (dia_semana BETWEEN 1 AND 7),
 hora_inicio TIME NOT NULL,
 hora_fin TIME NOT NULL,
 CHECK (hora_fin>hora_inicio),
 CONSTRAINT fk_horario_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS citas (
 id_cita INT AUTO_INCREMENT PRIMARY KEY,
 id_cliente INT NOT NULL,
 id_empleado INT NOT NULL,
 id_servicio INT NOT NULL,
 id_agenda INT NOT NULL,
 fecha DATETIME NOT NULL,
 precio_acordado DECIMAL(10,2) NOT NULL CHECK (precio_acordado>0),
 duracion_acordada INT NOT NULL CHECK (duracion_acordada>0),
 estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE','COMPLETADA','CANCELADA')),
 CONSTRAINT fk_cita_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente),
 CONSTRAINT fk_cita_empleado FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
 CONSTRAINT fk_cita_servicio FOREIGN KEY (id_servicio) REFERENCES servicios(id_servicio),
 -- Evita asignar a la cita una agenda perteneciente a otro empleado.
 CONSTRAINT fk_cita_agenda FOREIGN KEY (id_agenda,id_empleado) REFERENCES agendas(id_agenda,id_empleado),
 INDEX ix_cita_empleado_fecha (id_empleado,fecha),
 INDEX ix_cita_cliente_fecha (id_cliente,fecha)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pagos (
 id_pago INT AUTO_INCREMENT PRIMARY KEY,
 id_cita INT NOT NULL UNIQUE,
 monto DECIMAL(10,2) NOT NULL CHECK (monto>0),
 fecha DATETIME NOT NULL,
 CONSTRAINT fk_pago_cita FOREIGN KEY (id_cita) REFERENCES citas(id_cita)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS facturas (
 id_factura INT AUTO_INCREMENT PRIMARY KEY,
 numero VARCHAR(40) NOT NULL UNIQUE,
 id_cliente INT NOT NULL,
 fecha_emision DATETIME NULL,
 estado VARCHAR(12) NOT NULL DEFAULT 'BORRADOR' CHECK (estado IN ('BORRADOR','EMITIDA')),
 CHECK ((estado='BORRADOR' AND fecha_emision IS NULL) OR (estado='EMITIDA' AND fecha_emision IS NOT NULL)),
 CONSTRAINT fk_factura_cliente FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detalles_factura (
 id_detalle INT AUTO_INCREMENT PRIMARY KEY,
 id_factura INT NOT NULL,
 id_cita INT NOT NULL UNIQUE,
 descripcion_servicio VARCHAR(150) NOT NULL,
 importe DECIMAL(10,2) NOT NULL CHECK (importe>0),
 CONSTRAINT fk_detalle_factura FOREIGN KEY (id_factura) REFERENCES facturas(id_factura) ON DELETE CASCADE,
 CONSTRAINT fk_detalle_cita FOREIGN KEY (id_cita) REFERENCES citas(id_cita)
) ENGINE=InnoDB;
