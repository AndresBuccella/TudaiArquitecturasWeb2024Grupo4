package entities;

/*
 * =====================================================================================
 * SUGERENCIAS DE MEJORA Y EFICIENCIA (Cliente Entity):
 * =====================================================================================
 * 1. Implementación de 'equals()' y 'hashCode()':
 *    - La entidad no implementa 'equals()' ni 'hashCode()'.
 *    - Impacto en Eficiencia: Si se almacenan entidades 'Cliente' en colecciones basadas en hash
 *      ('HashSet', 'HashMap'), la JVM recurre a la identidad de referencia ('Object.hashCode()').
 *      Esto impide deduplicar clientes en memoria de forma eficiente ($O(1)$) y dificulta
 *      comparaciones en operaciones masivas.
 *    - Sugerencia: Implementar 'equals' y 'hashCode' basados en la clave primaria ('idCliente').
 *
 * 2. Inmutabilidad del Identificador (Thread-Safety y Optimizaciones JIT):
 *    - El campo 'idCliente' no posee setter, pero no está declarado 'final'.
 *    - Declararlo 'private final int idCliente' garantiza la inmutabilidad de la identidad
 *      del objeto y habilita optimizaciones en tiempo de compilación/ejecución (JIT).
 *
 * 3. Eficiencia en Memoria:
 *    - El uso de primitivos ('int idCliente') en vez del wrapper 'Integer' es una buena práctica
 *      que ahorra 16-24 bytes de encabezado de objeto por instancia y evita costos de boxing/unboxing.
 * =====================================================================================
 */
public class Cliente {
    // Sugerencia: Declarar 'final' para garantizar inmutabilidad de la clave primaria
    private int idCliente;
    private String nombre;
    private String email;

    public Cliente(int idCliente, String nombre, String email) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.email = email;
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

    @Override
    public String toString() {
        return "Cliente{" +
                "idCliente=" + idCliente +
                ", nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
