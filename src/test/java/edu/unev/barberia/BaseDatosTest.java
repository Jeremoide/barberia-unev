package edu.unev.barberia;

import edu.unev.barberia.database.ConexionBD;
import edu.unev.barberia.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import java.sql.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Prueba real en MariaDB. Inserta un grafo de 11 entidades y revierte todos sus datos. */
@EnabledIfEnvironmentVariable(named="BARBERIA_DB_TEST",matches="true")
class BaseDatosTest {
    private int insertar(Connection c,String sql,Object... valores) throws SQLException {
        try(var ps=c.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS)) {
            for(int i=0;i<valores.length;i++) ps.setObject(i+1,valores[i]);
            ps.executeUpdate();try(var rs=ps.getGeneratedKeys()) {return rs.next()?rs.getInt(1):0;}
        }
    }
    @Test void mapeaOnceEntidadesYCompruebaRestricciones() throws Exception {
        try(var c=ConexionBD.getConexion()) {
            c.setAutoCommit(false);
            try {
                String sufijo=UUID.randomUUID().toString().substring(0,8);
                Usuario usuario=new Usuario("Prueba","JDBC","test_"+sufijo,sufijo+"@example.com","Prueba123!",Rol.BARBERO);
                insertar(c,"INSERT INTO usuarios VALUES (?,?,?,?,?,?,?,?)",(Object[])usuario.datosPersistentes());
                Empleado empleado=new Empleado(insertar(c,"INSERT INTO empleados(id_usuario,especialidad) VALUES (?,?)",usuario.getId(),"Cortes"),usuario,"Cortes");
                Cliente cliente=new Cliente(insertar(c,"INSERT INTO clientes(nombre,telefono) VALUES (?,?)","Cliente prueba","809-555-0101"),"Cliente prueba","809-555-0101");
                CategoriaServicio categoria=new CategoriaServicio(insertar(c,"INSERT INTO categorias_servicio(nombre,descripcion) VALUES (?,?)","Cortes-"+sufijo,"Prueba"),"Cortes-"+sufijo,"Prueba");
                Servicio servicio=new Servicio(insertar(c,"INSERT INTO servicios(nombre,precio,duracion_minutos,id_categoria) VALUES (?,?,?,?)","Corte",350,30,categoria.getId()),"Corte",new BigDecimal("350.00"),30,categoria);
                Agenda agenda=new Agenda(insertar(c,"INSERT INTO agendas(id_empleado) VALUES (?)",empleado.getId()),empleado);
                LocalDate dia=LocalDate.now().plusDays(1);
                HorarioTrabajo horario=new HorarioTrabajo(insertar(c,"INSERT INTO horarios_trabajo(id_empleado,dia_semana,hora_inicio,hora_fin) VALUES (?,?,?,?)",empleado.getId(),dia.getDayOfWeek().getValue(),"09:00:00","12:00:00"),empleado,dia.getDayOfWeek(),LocalTime.of(9,0),LocalTime.NOON);
                empleado.setHorarios(java.util.List.of(horario));Cita cita=agenda.agendar(cliente,servicio,dia.atTime(9,0));
                cita.setId(insertar(c,"INSERT INTO citas(id_cliente,id_empleado,id_servicio,id_agenda,fecha,precio_acordado,duracion_acordada) VALUES (?,?,?,?,?,?,?)",cliente.getId(),empleado.getId(),servicio.getId(),agenda.getId(),cita.getFecha(),cita.getPrecioAcordado(),cita.getDuracionAcordada()));
                assertThrows(SQLException.class,()->insertar(c,"INSERT INTO pagos(id_cita,monto,fecha) VALUES (?,?,NOW())",cita.getId(),350));
                cita.completar();insertar(c,"UPDATE citas SET estado='COMPLETADA' WHERE id_cita=?",cita.getId());
                Pago pago=new Pago(insertar(c,"INSERT INTO pagos(id_cita,monto,fecha) VALUES (?,?,NOW())",cita.getId(),350),cita,new BigDecimal("350.00"));
                assertEquals(new BigDecimal("350.00"),pago.getMonto());
                assertThrows(SQLException.class,()->insertar(c,"INSERT INTO pagos(id_cita,monto,fecha) VALUES (?,?,NOW())",cita.getId(),350));
                Factura factura=new Factura(insertar(c,"INSERT INTO facturas(numero,id_cliente) VALUES (?,?)","PRUEBA-"+sufijo,cliente.getId()),"PRUEBA-"+sufijo,cliente);
                assertThrows(SQLException.class,()->insertar(c,"UPDATE facturas SET estado='EMITIDA',fecha_emision=NOW() WHERE id_factura=?",factura.getId()));
                factura.agregarCita(cita);DetalleFactura detalle=factura.getDetalles().getFirst();
                detalle.setId(insertar(c,"INSERT INTO detalles_factura(id_factura,id_cita,descripcion_servicio,importe) VALUES (?,?,?,?)",factura.getId(),cita.getId(),detalle.getDescripcionServicio(),detalle.getImporte()));
                factura.emitir();insertar(c,"UPDATE facturas SET estado='EMITIDA',fecha_emision=? WHERE id_factura=?",factura.getFechaEmision(),factura.getId());
                assertThrows(SQLException.class,()->insertar(c,"UPDATE detalles_factura SET descripcion_servicio='Cambio' WHERE id_detalle=?",detalle.getId()));
                assertThrows(SQLException.class,()->insertar(c,"UPDATE citas SET precio_acordado=400 WHERE id_cita=?",cita.getId()));
                assertThrows(SQLException.class,()->insertar(c,"INSERT INTO servicios(nombre,precio,duracion_minutos,id_categoria) VALUES ('Huérfano',350,30,?)",-999));
                try(var ps=c.prepareStatement("SELECT SUM(importe) FROM detalles_factura WHERE id_factura=?")) {
                    ps.setInt(1,factura.getId());try(var rs=ps.executeQuery()) {assertTrue(rs.next());assertEquals(factura.calcularTotal(),rs.getBigDecimal(1));}
                }
            } finally { c.rollback(); }
        }
    }
}
