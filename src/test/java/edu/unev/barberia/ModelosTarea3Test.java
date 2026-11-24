package edu.unev.barberia;

import edu.unev.barberia.model.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ModelosTarea3Test {
    private Empleado empleado() {
        return new Empleado(1,new Usuario("Carlos","Demo","barbero","barbero@example.com","Clave123!",Rol.BARBERO),"Cortes");
    }
    @Test void onceModelosTienenConstructorVacioYAtributosPrivados() throws Exception {
        for (Class<?> tipo:List.of(Usuario.class,Cliente.class,Empleado.class,Servicio.class,Cita.class,Pago.class,
                Agenda.class,HorarioTrabajo.class,Factura.class,DetalleFactura.class,CategoriaServicio.class)) {
            assertNotNull(tipo.getConstructor().newInstance());
            for (var campo:tipo.getDeclaredFields()) {
                if (!campo.isSynthetic()) assertTrue(java.lang.reflect.Modifier.isPrivate(campo.getModifiers()),campo.toString());
            }
        }
        Cliente cliente=new Cliente(); cliente.setId(4);cliente.setNombre("Ana");cliente.setTelefono("809-555-0101");
        assertEquals("Ana",cliente.toString()); assertEquals(4,cliente.getId());
    }
    @Test void agendaRespetaJornadaYCitasContiguas() {
        Empleado empleado=empleado();LocalDate dia=LocalDate.now().plusDays(1);
        HorarioTrabajo horario=new HorarioTrabajo(1,empleado,dia.getDayOfWeek(),LocalTime.of(9,0),LocalTime.of(12,0));
        empleado.setHorarios(List.of(horario)); Agenda agenda=new Agenda(1,empleado);
        Servicio corte=new Servicio(1,"Corte",new BigDecimal("350.00"),30,new CategoriaServicio(1,"Cortes","Cabello"));
        Cliente cliente=new Cliente(1,"Ana","809-555-0101");
        assertFalse(agenda.estaDisponible(dia.atTime(8,30),30));
        Cita cita=agenda.agendar(cliente,corte,dia.atTime(9,0));
        assertFalse(agenda.estaDisponible(dia.atTime(9,15),30));
        assertTrue(agenda.estaDisponible(dia.atTime(9,30),30));
        assertFalse(agenda.estaDisponible(dia.atTime(11,45),30));
        cita.cancelar();assertTrue(agenda.estaDisponible(dia.atTime(9,0),30));
        assertThrows(IllegalArgumentException.class,()->horario.setIntervalo(LocalTime.NOON,LocalTime.of(9,0)));
    }
    @Test void facturaConservaPrecioEImpideCambiosTrasEmitir() {
        Cliente cliente=new Cliente(1,"Ana","809-555-0101");Servicio servicio=new Servicio(1,"Corte",new BigDecimal("350.00"),30);
        Cita cita=new Cita(1,cliente,empleado(),servicio,LocalDateTime.now().plusDays(1));cita.completar();
        Factura factura=new Factura(1,"F-001",cliente);factura.agregarCita(cita);
        assertThrows(IllegalArgumentException.class,()->factura.agregarCita(cita));
        servicio.setPrecio(new BigDecimal("900.00"));assertEquals(new BigDecimal("350.00"),factura.calcularTotal());
        factura.emitir();assertEquals(EstadoFactura.EMITIDA,factura.getEstado());assertNotNull(factura.getFechaEmision());
        assertThrows(IllegalStateException.class,()->factura.setNumero("F-002"));
        assertThrows(IllegalStateException.class,()->factura.getDetalles().getFirst().setDescripcionServicio("Otro"));
        assertThrows(UnsupportedOperationException.class,()->factura.getDetalles().clear());
    }
    @Test void facturaRechazaCitaPendienteOtroClienteYEmisionVacia() {
        Cliente uno=new Cliente(1,"Ana","809-555-0101"),dos=new Cliente(2,"Luis","809-555-0102");
        Cita cita=new Cita(1,dos,empleado(),new Servicio(1,"Corte",new BigDecimal("350.00"),30),LocalDateTime.now());
        Factura factura=new Factura(1,"F-001",uno);
        assertThrows(IllegalStateException.class,factura::emitir);
        assertThrows(IllegalArgumentException.class,()->factura.agregarCita(cita));cita.completar();
        assertThrows(IllegalArgumentException.class,()->factura.agregarCita(cita));
    }
}
