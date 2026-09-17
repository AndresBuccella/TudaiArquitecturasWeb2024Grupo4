package entities;

/*
 * =====================================================================================
 * SUGERENCIAS DE MEJORA Y EFICIENCIA (Producto Entity):
 * =====================================================================================
 * 1. Tipos de Datos para Valores Monetarios (Precisión y Rendimiento):
 *    - El campo 'valor' está tipado como 'float' (32 bits IEEE 754), susceptible a pérdida
 *      de precisión binaria al operar con centavos o multiplicaciones masivas.
 *    - Recomendación:
 *      * Para máxima precisión contable: 'BigDecimal'.
 *      * Para alto rendimiento numérico sin overhead de objetos en memoria: 'double' o 'long'
 *        (almacenando el importe en centavos/unidades mínimas).
 *
 * 2. Inmutabilidad de la Clave Primaria ('final'):
 *    - El campo 'idProducto' no tiene setter; debe declararse 'private final int idProducto;'
 *      para garantizar la integridad de la identidad y favorecer optimizaciones del compilador.
 *
 * 3. Implementación de 'equals()' y 'hashCode()':
 *    - Sobrescribir ambos métodos basados en 'idProducto' permite búsquedas y agrupaciones
 *      en tiempo constante $O(1)$ en colecciones basadas en tablas hash ('HashMap', 'HashSet').
 * =====================================================================================
 */
public class Producto {
    // Sugerencia: Declarar 'final' para garantizar la inmutabilidad de la clave primaria
    private int idProducto;
    private String nombre;
    // Sugerencia: Migrar a double, BigDecimal o long (centavos) para evitar errores de precisión monetaria
    private float valor;

    public Producto(int idProducto, String nombre, float valor) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.valor = valor;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public float getValor() {
        return valor;
    }

    public void setValor(float valor) {
        this.valor = valor;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "idProducto=" + idProducto +
                ", nombre='" + nombre + '\'' +
                ", valor=" + valor +
                '}';
    }
}
