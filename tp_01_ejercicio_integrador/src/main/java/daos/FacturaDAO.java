package repositories;

import repositories.interfaces.DAO;
import dtos.ClienteConFacturacionDTO;
import entities.Factura;
import factories.MySqlConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 * =====================================================================================
 * SUGERENCIAS DE MEJORA Y EFICIENCIA (FacturaDAO):
 * =====================================================================================
 * 1. Gestión de Conexiones y Connection Pool:
 *    - Cada método cierra la conexión ('conn.close()'), destruyendo el socket físico subyacente
 *      en el Singleton 'MySqlConnectionFactory'. Esto degrada el rendimiento por renegociación
 *      continua de conexiones. Se debe emplear un Connection Pool (como HikariCP) con try-with-resources.
 *
 * 2. Inserción Masiva en Lote (Batch Processing):
 *    - En 'DatabaseLoader', se cargan 512 facturas ejecutando 512 sentencias 'INSERT' individuales,
 *      con 512 round-trips de red y 512 escrituras a disco ('commit()').
 *    - Mejora de eficiencia: Implementar 'insertBatch(List<Factura> facturas)' usando 'ps.addBatch()'
 *      y 'ps.executeBatch()' en una sola transacción, lo cual reduce el tiempo de inserción en más de un 90%.
 *
 * 3. Definición de Esquema e Índices Secundarios en Base de Datos:
 *    - En 'createTable()', la tabla 'Factura' no define clave foránea ni índice sobre 'idCliente'.
 *    - Impacto en Eficiencia: Como la clave primaria es 'idFactura', cualquier consulta o JOIN que filtre
 *      o una por 'idCliente' (como la agregación en 'ClienteDAO.obtenerClientesPorMayorFacturacionDesc')
 *      requerirá un escaneo completo de la tabla si no existe un índice en 'idCliente':
 *      'CREATE INDEX idx_factura_cliente ON Factura(idCliente);'.
 *
 * 4. Corrección Crítica en 'select(int id)':
 *    - 'ps.executeQuery()' se invoca en el bloque try-with-resources antes de setear el parámetro
 *      'ps.setInt(1, id)' y no se avanza el cursor con 'rs.next()'. Además, el mensaje de error
 *      indica erróneamente "Cliente" en lugar de "Factura".
 *
 * 5. Proyecciones Explícitas y Capacidad de Colecciones:
 *    - En 'selectAll()', reemplazar 'SELECT *' por 'SELECT idFactura, idCliente'.
 *    - Inicializar la lista con una capacidad estimada ('new ArrayList<>(capacidadEstimada)')
 *      para evitar el redimensionamiento dinámico del arreglo interno.
 * =====================================================================================
 */
// Patrón Singleton
public class FacturaDAO implements DAO<Factura> {
    private static FacturaDAO unicaInstancia;

    private FacturaDAO() throws SQLException {

    }

    public static FacturaDAO getInstance() throws SQLException {
        if (unicaInstancia == null) {
            unicaInstancia = new FacturaDAO();
        }

        return unicaInstancia;
    }

    @Override
    public void dropTable() throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        String drop_table = "DROP TABLE IF EXISTS Factura";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente
        try (PreparedStatement ps = conn.prepareStatement(drop_table)) {
            ps.executeUpdate();

            conn.commit();
            conn.close();
        } catch (SQLException e) {
            conn.rollback(); // Rollback en caso de error
            throw new SQLException("Error al eliminar la tabla Factura.", e);
        }
    }

    @Override
    public void createTable() throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        // Sugerencia de eficiencia/integridad: Agregar clave foránea e índice secundario sobre 'idCliente'
        // para optimizar los JOINs con la tabla 'Cliente':
        // "INDEX idx_factura_cliente (idCliente), FOREIGN KEY (idCliente) REFERENCES Cliente(idCliente)"
        String table = "CREATE TABLE IF NOT EXISTS Factura(" +
                "idFactura INT," +
                "idCliente INT," +
                "PRIMARY KEY(idFactura))";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente
        try (PreparedStatement ps = conn.prepareStatement(table)) {
            ps.executeUpdate();

            conn.commit();
            conn.close();
        } catch (SQLException e) {
            conn.rollback(); // Rollback en caso de error
            throw new SQLException("Error al crear la tabla Factura.", e);
        }
    }

    @Override
    public void insert (Factura f) throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        String query = "INSERT INTO Factura(idFactura, idCliente) VALUES (?, ?)";

        // Sugerencia de eficiencia: Usar 'insertBatch(List<Factura>)' con 'addBatch()' / 'executeBatch()'
        // para evitar un commit y round-trip individual por cada una de las 500+ facturas.
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, f.getIdFactura());
            ps.setInt(2, f.getIdCliente());
            ps.executeUpdate();

            conn.commit();
            conn.close();
        } catch (SQLException e) {
            conn.rollback(); // Rollback en caso de error
            throw new SQLException("Error al insertar Factura!", e);
        }
    }

    @Override
    public Factura select (int id) throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        Factura f = null;
        String query = "SELECT * FROM Factura WHERE idFactura=?";

        // NOTA / SUGERENCIA DE CORRECCIÓN:
        // 'ps.executeQuery()' se ejecuta antes de 'ps.setInt(1, id)' y no se invoca 'rs.next()'.
        // Debe reestructurarse asignando parámetros antes del executeQuery() y validando rs.next().
        try (PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            ps.setInt(1, id);

            f = new Factura(rs.getInt(1), rs.getInt(2));

            conn.close();
        } catch (SQLException e) {
            throw new SQLException("Error al seleccionar Cliente con id=" + id + "!", e);
        }

        return f;
    }

    @Override
    public List<Factura> selectAll () throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        // Sugerencia de eficiencia: Inicializar con capacidad estimada y proyectar columnas explícitas
        List<Factura> facturas = new ArrayList<>();
        String query = "SELECT * FROM Factura";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente
        try (PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                facturas.add(new Factura(rs.getInt(1), rs.getInt(2)));
            }

            conn.close();
        } catch (SQLException e) {
            throw new SQLException("Error al obtener Facturas!", e);
        }

        return facturas;
    }

    @Override
    public boolean update (Factura f) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean delete (int id) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
