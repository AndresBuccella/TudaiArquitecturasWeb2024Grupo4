package entities;

/*
 * =====================================================================================
 * SUGERENCIAS DE MEJORA Y EFICIENCIA (Factura Entity):
 * =====================================================================================
 * 1. Inmutabilidad de Atributos (Thread-Safety y Optimización JIT):
 *    - La entidad 'Factura' no dispone de métodos setters, por lo que su estado es inmutable.
 *    - Declarar 'private final int idFactura;' y 'private final int idCliente;' permite
 *      al compilador y a la JVM realizar optimizaciones de inlining y garantiza seguridad
 *      ante accesos concurrentes sin bloqueos de memoria.
 *
 * 2. Sobrescribir 'equals()' y 'hashCode()':
 *    - Implementar 'equals' y 'hashCode' basados en 'idFactura' es crítico si se manipulan
 *      colecciones (ej. evitar facturas duplicadas en un 'HashSet' o indexar en un 'HashMap').
 *
 * 3. Eficiencia en Memoria:
 *    - El uso de enteros primitivos 'int' minimiza el consumo de memoria en el heap al evitar
 *      el encapsulado en objetos 'Integer'.
 * =====================================================================================
 */
public class Factura {
    // Sugerencia: Declarar campos como 'final' para garantizar inmutabilidad
    private int idFactura;
    private int idCliente;

    public Factura(int idFactura, int idCliente) {
        this.idFactura = idFactura;
        this.idCliente = idCliente;
    }

    public int getIdFactura() {
        return idFactura;
    }

    public int getIdCliente() {
        return idCliente;
    }

    @Override
    public String toString() {
        return "Factura{" +
                "idFactura=" + idFactura +
                ", idCliente=" + idCliente +
                '}';
    }
}
