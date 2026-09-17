package helpers;

//import repositories.*;
import repositories.interfaces.DAO;
import entities.Cliente;
import entities.Factura;
import entities.FacturaProducto;
import entities.Producto;
import factories.DatabaseFactory;

import java.sql.SQLException;
import java.util.List;

/*
 * =====================================================================================
 * SUGERENCIAS DE MEJORA Y EFICIENCIA (DatabaseLoader):
 * =====================================================================================
 * 1. Cuello de Botella Crítico en Carga Masiva (Inserción Unitaria vs Batch Processing):
 *    - El método 'cargarListaEnBaseDeDatos' itera cada lista llamando a 'dao.insert(entidad)'.
 *    - Con más de 3.300 registros en total, esto genera:
 *      * Más de 3.300 reconexiones TCP físicas (debido al 'conn.close()' en cada DAO).
 *      * Más de 3.300 round-trips de red cliente-servidor.
 *      * Más de 3.300 operaciones 'commit()' con sincronización a disco ('fsync') en MySQL.
 *    - Impacto en Eficiencia: La carga tarda decenas de segundos cuando debería tardar menos
 *      de 100 milisegundos.
 *    - Solución: Implementar 'dao.insertBatch(lista)' usando 'PreparedStatement.addBatch()' y
 *      'executeBatch()' por bloques (ej. cada 500-1.000 filas) dentro de una única transacción
 *      con 'setAutoCommit(false)' y un solo 'commit()' final por tabla.
 *
 * 2. Retención Acumulada de Objetos en Memoria Heap:
 *    - Se leen y retienen en memoria simultáneamente las 4 listas ('clientes', 'facturas',
 *      'productos', 'facturasProductos') antes de insertar la primera fila.
 *    - En entornos de producción con millones de registros, esto causa picos de consumo de RAM
 *      y pausas prolongadas por Garbage Collection.
 *    - Solución: Procesar entidad por entidad de forma secuencial (leer clientes -> insertar ->
 *      liberar referencia; leer facturas -> insertar...) o canalizar directamente en streaming
 *      desde el archivo CSV a la base de datos sin acumular en memoria intermedia.
 *
 * 3. Ejecución Destructiva de DDL en Tiempo de Ejecución:
 *    - Invocar 'dropTable()' y 'createTable()' en cada ejecución bloquea el diccionario de datos
 *      de MySQL y destruye cualquier índice o dato preexistente.
 *    - Se sugiere gestionar el ciclo de vida del esquema mediante herramientas de migración
 *      (Flyway / Liquibase) o scripts DDL desacoplados de la lógica de carga.
 * =====================================================================================
 */
public class DatabaseLoader {
    // Método para cargar todos los datos de los CSVs a la base de datos
    public static void cargarDatos(CSVreader reader) throws SQLException {
        // Sugerencia de memoria: En vez de cargar las 4 listas a la vez en RAM,
        // cargar e insertar cada tabla secuencialmente para permitir que el GC recolecte objetos.
        List<Cliente> clientes = reader.leerArchivoClientes();
        List<Factura> facturas = reader.leerArchivoFacturas();
        List<Producto> productos = reader.leerArchivoProductos();
        List<FacturaProducto> facturasProductos = reader.leerArchivoFacturasProductos();

        // Obtener instancias de los DAOs
        DatabaseFactory dbF = DatabaseFactory.getDAOFactory(1);
        DAO<Cliente> clienteDAO = dbF.getClienteDAO();
        DAO<Producto> productoDAO = dbF.getProductoDAO();
        DAO<FacturaProducto> facturaProductoDAO = dbF.getFacturaProductoDAO();
        DAO<Factura> facturaDAO = dbF.getFacturaDAO();

        // Eliminar las tablas si existen y luego recrearlas
        clienteDAO.dropTable();
        clienteDAO.createTable();

        facturaProductoDAO.dropTable(); // Eliminar primero por las FK referenciadas!

        facturaDAO.dropTable();
        facturaDAO.createTable();

        productoDAO.dropTable();
        productoDAO.createTable();

        facturaProductoDAO.createTable();

        // Cargar datos en las tablas correspondientes
        cargarListaEnBaseDeDatos(clientes, clienteDAO);
        cargarListaEnBaseDeDatos(facturas, facturaDAO);
        cargarListaEnBaseDeDatos(productos, productoDAO);
        cargarListaEnBaseDeDatos(facturasProductos, facturaProductoDAO);
    }

    // Método genérico para cargar entidades en la base de datos usando cualquier DAO que implemente la interfaz DAO
    // Sugerencia crítica de eficiencia: Reemplazar el bucle de inserción unitaria por inserción en lote (Batch).
    public static <T> void cargarListaEnBaseDeDatos(List<T> lista, DAO<T> dao) throws SQLException {
        for (T entidad : lista) {
            dao.insert(entidad);
        }
    }
}
