package entities;

/*
 * =====================================================================================
 * SUGERENCIAS DE MEJORA Y EFICIENCIA (FacturaProducto Entity):
 * =====================================================================================
 * 1. Inmutabilidad de la Clave Compuesta:
 *    - Los campos 'idFactura' e 'idProducto' conforman la clave primaria compuesta de la
 *      relación N:M. Al no cambiar su identidad, deberían declararse 'final'
 *      ('private final int idFactura;', 'private final int idProducto;').
 *
 * 2. Implementación de 'equals()' y 'hashCode()':
 *    - Al modelar una tabla de unión (asociativa) con clave compuesta, la falta de
 *      'equals' y 'hashCode' impide detectar duplicados o realizar búsquedas $O(1)$ en
 *      colecciones tipo 'HashSet' o 'HashMap' durante procesos de carga o consolidación.
 *    - Deben implementarse considerando la tupla '(idFactura, idProducto)'.
 *
 * 3. Diseño Compacto en Memoria:
 *    - El uso de enteros primitivos 'int' para los tres campos optimiza el empaquetado
 *      en memoria del heap al evitar el boxing de objetos wrappers.
 * =====================================================================================
 */
public class FacturaProducto {
    // Sugerencia: Declarar 'final' los componentes de la clave primaria compuesta
    private int idFactura;
    private int idProducto;
    private int cantidad;

    public FacturaProducto(int idFactura, int idProducto, int cantidad) {
        this.idFactura = idFactura;
        this.idProducto = idProducto;
        this.cantidad = cantidad;
    }

    public int getIdFactura() {
        return idFactura;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    @Override
    public String toString() {
        return "FacturaProducto{" +
                "idFactura=" + idFactura +
                ", idProducto=" + idProducto +
                ", cantidad=" + cantidad +
                '}';
    }
}
