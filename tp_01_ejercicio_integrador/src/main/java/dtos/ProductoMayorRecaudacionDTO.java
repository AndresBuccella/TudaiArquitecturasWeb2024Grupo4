package dtos;

/*
 * =====================================================================================
 * SUGERENCIAS DE MEJORA Y EFICIENCIA (ProductoMayorRecaudacionDTO):
 * =====================================================================================
 * 1. Adopción de Java 17 Records:
 *    - Al modelar un resultado de agregación de sólo lectura, esta clase es una candidata
 *      ideal para convertirse en un 'record':
 *      'public record ProductoMayorRecaudacionDTO(int idProducto, String nombre, float valor, double recaudacion) {}'
 *    - Ventajas de Eficiencia:
 *      * Reduce la huella de memoria (menor overhead de objeto en el heap).
 *      * Facilita optimizaciones de compilación JIT e inmutabilidad garantizada.
 *      * Implementa automáticamente métodos canónicos ('equals', 'hashCode', 'toString').
 *
 * 2. Precisión Numérica en Tipos Monetarios:
 *    - Los campos 'valor' y 'recaudacion' están definidos como 'float'.
 *    - Los cálculos financieros requieren evitar errores de redondeo inherentes al estándar IEEE 754.
 *      Se recomienda usar 'double', 'BigDecimal' o enteros en centavos ('long').
 * =====================================================================================
 */
public class ProductoMayorRecaudacionDTO {
    private int idProducto;
    private String nombre;
    // Sugerencia: Migrar a double o BigDecimal para garantizar precisión en valores monetarios
    private float valor;
    private float recaudacion;

    public ProductoMayorRecaudacionDTO(int idProducto, String nombre, float valor, float recaudacion) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.valor = valor;
        this.recaudacion = recaudacion;
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

    public float getRecaudacion() {
        return recaudacion;
    }

    public void setRecaudacion(float recaudacion) {
        this.recaudacion = recaudacion;
    }

    @Override
    public String toString() {
        return "ProductoMayorRecaudacionDTO{" +
                "idProducto=" + idProducto +
                ", nombre='" + nombre + '\'' +
                ", valor=" + valor +
                ", recaudacion=" + recaudacion +
                '}';
    }
}
