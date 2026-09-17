package dtos;

/*
 * =====================================================================================
 * SUGERENCIAS DE MEJORA Y EFICIENCIA (ClienteConFacturacionDTO):
 * =====================================================================================
 * 1. Adopción de Java 17 Records (Inmutabilidad y Menor Footprint de Memoria):
 *    - Este DTO es un transporte de datos puramente de lectura originado por una consulta agregada.
 *    - En Java 17 (versión del proyecto), se recomienda transformarlo en un 'record':
 *      'public record ClienteConFacturacionDTO(int idCliente, String nombre, String email, double totalFacturado) {}'
 *    - Beneficios de Eficiencia:
 *      * Reduce el boilerplate a una sola línea.
 *      * Garantiza inmutabilidad y seguridad entre hilos (thread-safety).
 *      * Permite mejores optimizaciones del compilador JIT (análisis de escape y asignación en registros).
 *      * Genera implementaciones óptimas de 'equals()', 'hashCode()' y 'toString()' sin costo de mantenimiento.
 *
 * 2. Precisión Numérica en Tipos Monetarios:
 *    - El uso de 'float totalFacturado' puede acumular pérdidas de precisión debido a la
 *      representación binaria de punto flotante (IEEE 754).
 *    - Para importes de facturación se recomienda utilizar 'double', 'BigDecimal' o representar
 *      los centavos en un tipo entero primitivo 'long', garantizando exactitud contable.
 * =====================================================================================
 */
public class ClienteConFacturacionDTO {
    private int idCliente;
    private String nombre;
    private String email;
    // Sugerencia: Migrar a double, BigDecimal o long (centavos) para evitar imprecisión monetaria
    private float totalFacturado;

    public ClienteConFacturacionDTO(int idCliente, String nombre, String email, float totalFacturado) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.email = email;
        this.totalFacturado = totalFacturado;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public float getTotalFacturado() {
        return totalFacturado;
    }

    public void setTotalFacturado(float totalFacturado) {
        this.totalFacturado = totalFacturado;
    }

    @Override
    public String toString() {
        return "ClienteConFacturacionDTO{" +
                "idCliente=" + idCliente +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", totalFacturado=" + totalFacturado +
                '}';
    }
}
