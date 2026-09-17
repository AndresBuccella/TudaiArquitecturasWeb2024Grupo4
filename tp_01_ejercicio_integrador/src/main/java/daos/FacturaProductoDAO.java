package repositories;

import repositories.interfaces.DAO;
import entities.Factura;
import entities.FacturaProducto;
import factories.MySqlConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/*
 * =====================================================================================
 * SUGERENCIAS DE MEJORA Y EFICIENCIA (FacturaProductoDAO):
 * =====================================================================================
 * 1. Cuello de Botella Crítico en Inserción Masiva (2.590 registros):
 *    - Esta tabla contiene el mayor volumen del dataset. El método 'insert(FacturaProducto fp)'
 *      se ejecuta 2.590 veces desde 'DatabaseLoader', abriendo y cerrando la conexión física
 *      y realizando un 'commit()' individual por cada fila.
 *    - Impacto en Eficiencia: 2.590 operaciones de red individuales y 2.590 fsync de disco.
 *    - Mejora prioritaria: Implementar 'insertBatch(List<FacturaProducto> items)' con
 *      'ps.addBatch()' y 'ps.executeBatch()' en bloques de 500 o 1.000 filas dentro de una única
 *      transacción. Junto con la propiedad JDBC 'rewriteBatchedStatements=true', esta mejora
 *      reduce el tiempo de carga en varios órdenes de magnitud (de ~40 segundos a <100 milisegundos).
 *
 * 2. Optimización de Índices en Base de Datos:
 *    - La clave primaria 'PRIMARY KEY(idFactura, idProducto)' crea un índice B-Tree cuyo prefijo
 *      es 'idFactura'. Esto optimiza búsquedas por factura, pero las búsquedas o JOINs que filtran
 *      por 'idProducto' (como en 'ProductoDAO.obtenerProductoMayorRecaudacion') NO pueden usar
 *      dicho índice de forma óptima, provocando un escaneo de índice completo o escaneo de tabla.
 *    - Sugerencia: Agregar un índice secundario explícito sobre 'idProducto':
 *      'CREATE INDEX idx_factura_producto_idProducto ON Factura_Producto(idProducto, cantidad);'
 *      (Covering Index que cubre 'idProducto' y 'cantidad', resolviendo agregaciones sin acceder a las páginas de datos).
 *
 * 3. Gestión de Conexiones:
 *    - Al igual que en otros DAOs, 'conn.close()' destruye la conexión singleton. Es necesario
 *      utilizar un Connection Pool (ej. HikariCP) y scopes de conexión cerrados lógicamente.
 *
 * 4. Corrección Crítica en 'select(int idFactura, int idProducto)':
 *    - 'ps.executeQuery()' se declara en el try-with-resources antes de setear 'ps.setInt(1, idFactura)'
 *      y 'ps.setInt(2, idProducto)', y falta verificar 'rs.next()'.
 *
 * 5. Adaptabilidad de Clave Primaria Compuesta:
 *    - 'select(int id)' lanza 'UnsupportedOperationException' porque la interfaz 'DAO<T>' asume un 'int'.
 *      Se recomienda modelar una clase de clave compuesta 'FacturaProductoPK(int idFactura, int idProducto)'
 *      con interfaz 'DAO<T, ID>'.
 * =====================================================================================
 */
// Patrón Singleton
public class FacturaProductoDAO implements DAO<FacturaProducto> {
    private static FacturaProductoDAO unicaInstancia;

    private FacturaProductoDAO() throws SQLException {

    }

    public static FacturaProductoDAO getInstance() throws SQLException {
        if (unicaInstancia == null) {
            unicaInstancia = new FacturaProductoDAO();
        }

        return unicaInstancia;
    }

    @Override
    public void dropTable() throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        String drop_table = "DROP TABLE IF EXISTS Factura_Producto";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente
        try (PreparedStatement ps = conn.prepareStatement(drop_table)) {
            ps.executeUpdate();

            conn.commit();
            conn.close();
        } catch (SQLException e) {
            conn.rollback(); // Rollback en caso de error
            throw new SQLException("Error al eliminar la tabla Factura_Producto.", e);
        }
    }

    @Override
    public void createTable() throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        // Sugerencia de eficiencia: Agregar índice secundario en 'idProducto' (o covering index con 'cantidad')
        // para optimizar el JOIN en 'obtenerProductoMayorRecaudacion':
        // ", INDEX idx_producto_cantidad (idProducto, cantidad)"
        String table = "CREATE TABLE IF NOT EXISTS Factura_Producto(" +
                "idFactura INT," +
                "idProducto INT," +
                "cantidad INT," +
                "PRIMARY KEY(idFactura, idProducto)," +
                "FOREIGN KEY(idFactura) REFERENCES Factura(idFactura)," +
                "FOREIGN KEY(idProducto) REFERENCES Producto(idProducto))";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente
        try (PreparedStatement ps = conn.prepareStatement(table);) {
            ps.executeUpdate();

            conn.commit();
            conn.close();
        } catch (SQLException e) {
            conn.rollback(); // Rollback en caso de error
            throw new SQLException("Error al crear la tabla Factura_Producto.", e);
        }
    }

    @Override
    public void insert(FacturaProducto fp) throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        String query = "INSERT INTO Factura_Producto(idFactura, idProducto, cantidad) VALUES (?, ?, ?)";

        // Sugerencia crítica de eficiencia: Usar 'insertBatch(List<FacturaProducto>)' con 'addBatch()'
        // y 'executeBatch()' en transacciones agrupadas para procesar los 2590 registros eficientemente.
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, fp.getIdFactura());
            ps.setInt(2, fp.getIdProducto());
            ps.setInt(3, fp.getCantidad());
            ps.executeUpdate();

            conn.commit();
            conn.close();
        } catch (SQLException e) {
            conn.rollback();
            throw new SQLException("Error al insertar FacturaProducto!", e);
        }
    }

    @Override
    public FacturaProducto select (int id) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public FacturaProducto select(int idFactura, int idProducto) throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        FacturaProducto fp = null;
        String query = "SELECT * FROM Factura_Producto WHERE idFactura=? AND idProducto=?";

        // NOTA / SUGERENCIA DE CORRECCIÓN:
        // 'ps.executeQuery()' se ejecuta antes de setear parámetros y no se invoca 'rs.next()'.
        // Debe reestructurarse con parámetros seteados antes de ejecutar la consulta.
        try (PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            ps.setInt(1, idFactura);
            ps.setInt(2, idProducto);

            fp = new FacturaProducto(rs.getInt(1), rs.getInt(2), rs.getInt(3));

            conn.close();
        } catch (SQLException e) {
            throw new SQLException("Error al seleccionar FacturaProducto!");
        }

        return fp;
    }

    @Override
    public List<FacturaProducto> selectAll() throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        // Sugerencia de eficiencia: Pre-asignar capacidad 'new ArrayList<>(2600)' y proyectar columnas explícitas
        List<FacturaProducto> facturas_productos = new ArrayList<>();
        String query = "SELECT * FROM Factura_Producto";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente.
        try (PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                facturas_productos.add(new FacturaProducto(rs.getInt(1), rs.getInt(2), rs.getInt(3)));
            }

            conn.close();
        } catch (SQLException e) {
            throw new SQLException("Error al obtener FacturasProductos!", e);
        }

        return facturas_productos;
    }

    @Override
    public boolean update(FacturaProducto fp) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean delete(int id) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
