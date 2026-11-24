-- Reglas que implican varias tablas; ejecutar después de 01_esquema.sql.
USE barberia_unev;
DELIMITER //
CREATE OR REPLACE TRIGGER validar_pago_insert BEFORE INSERT ON pagos FOR EACH ROW
BEGIN
 IF NOT EXISTS (SELECT 1 FROM citas WHERE id_cita=NEW.id_cita AND estado='COMPLETADA' AND precio_acordado=NEW.monto) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Pago requiere cita completada e importe acordado';
 END IF;
END//
CREATE OR REPLACE TRIGGER validar_pago_update BEFORE UPDATE ON pagos FOR EACH ROW
BEGIN
 IF NOT EXISTS (SELECT 1 FROM citas WHERE id_cita=NEW.id_cita AND estado='COMPLETADA' AND precio_acordado=NEW.monto) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Pago requiere cita completada e importe acordado';
 END IF;
END//
CREATE OR REPLACE TRIGGER validar_detalle_insert BEFORE INSERT ON detalles_factura FOR EACH ROW
BEGIN
 IF NOT EXISTS (SELECT 1 FROM citas c JOIN facturas f ON f.id_cliente=c.id_cliente
  WHERE c.id_cita=NEW.id_cita AND f.id_factura=NEW.id_factura AND c.estado='COMPLETADA'
    AND f.estado='BORRADOR' AND c.precio_acordado=NEW.importe) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Detalle requiere factura borrador, mismo cliente y cita completada al precio acordado';
 END IF;
END//
CREATE OR REPLACE TRIGGER validar_detalle_update BEFORE UPDATE ON detalles_factura FOR EACH ROW
BEGIN
 IF EXISTS (SELECT 1 FROM facturas WHERE id_factura=OLD.id_factura AND estado='EMITIDA') THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='No se modifica una factura emitida';
 END IF;
 IF NOT EXISTS (SELECT 1 FROM citas c JOIN facturas f ON f.id_cliente=c.id_cliente
  WHERE c.id_cita=NEW.id_cita AND f.id_factura=NEW.id_factura AND c.estado='COMPLETADA'
    AND f.estado='BORRADOR' AND c.precio_acordado=NEW.importe) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Detalle inconsistente';
 END IF;
END//
CREATE OR REPLACE TRIGGER validar_detalle_delete BEFORE DELETE ON detalles_factura FOR EACH ROW
BEGIN
 IF EXISTS (SELECT 1 FROM facturas WHERE id_factura=OLD.id_factura AND estado='EMITIDA') THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='No se elimina un detalle emitido';
 END IF;
END//
CREATE OR REPLACE TRIGGER validar_factura_insert BEFORE INSERT ON facturas FOR EACH ROW
BEGIN
 IF NEW.estado<>'BORRADOR' THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Primero cree el borrador y sus detalles';
 END IF;
END//
CREATE OR REPLACE TRIGGER validar_factura_update BEFORE UPDATE ON facturas FOR EACH ROW
BEGIN
 IF OLD.estado='EMITIDA' THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='No se modifica una factura emitida';
 END IF;
 IF EXISTS (SELECT 1 FROM detalles_factura d JOIN citas c ON c.id_cita=d.id_cita
  WHERE d.id_factura=OLD.id_factura AND (c.id_cliente<>NEW.id_cliente OR c.estado<>'COMPLETADA' OR c.precio_acordado<>d.importe)) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Factura y detalles inconsistentes';
 END IF;
 IF NEW.estado='EMITIDA' AND NOT EXISTS (SELECT 1 FROM detalles_factura WHERE id_factura=OLD.id_factura) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Factura emitida requiere al menos un detalle';
 END IF;
END//
CREATE OR REPLACE TRIGGER validar_factura_delete BEFORE DELETE ON facturas FOR EACH ROW
BEGIN
 IF OLD.estado='EMITIDA' THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='No se elimina una factura emitida';
 END IF;
END//
CREATE OR REPLACE TRIGGER proteger_cita_documentada BEFORE UPDATE ON citas FOR EACH ROW
BEGIN
 IF (EXISTS (SELECT 1 FROM pagos WHERE id_cita=OLD.id_cita) OR EXISTS (SELECT 1 FROM detalles_factura WHERE id_cita=OLD.id_cita))
 AND (NEW.estado<>OLD.estado OR NEW.id_cliente<>OLD.id_cliente OR NEW.id_servicio<>OLD.id_servicio OR NEW.precio_acordado<>OLD.precio_acordado) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='No se alteran condiciones de una cita cobrada o facturada';
 END IF;
END//
DELIMITER ;
