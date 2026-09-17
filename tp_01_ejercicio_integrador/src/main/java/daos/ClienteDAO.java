package repositories;

import repositories.interfaces.DAO;
import dtos.ClienteConFacturacionDTO;
import entities.Cliente;
import factories.MySqlConnectionFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 * =====================================================================================
 * SUGERENCIAS DE MEJORA Y EFICIENCIA (ClienteDAO):
 * =====================================================================================
 * 1. Gestión de Conexiones (Cierre Prematuro y Falta de Connection Pool):
 *    - En cada método se invoca 'conn.close()'. Dado que 'MySqlConnectionFactory' mantiene
 *      una única instancia estática de 'Connection', cada llamada destruye la conexión TCP física.
 *      Esto obliga a 'DriverManager.getConnection(...)' a negociar un nuevo socket TCP y
 *      handshake de autenticación en la siguiente operación.
 *    - Impacto en Eficiencia: Crear una conexión TCP a MySQL puede demorar entre 15ms y 50ms.
 *      En un bucle de inserción masiva, esto degrada catastróficamente el rendimiento.
 *    - Mejora recomendada: Emplear un Connection Pool (como HikariCP) y obtener/cerrar
 *      conexiones lógicas mediante bloques try-with-resources: 'try (Connection conn = ds.getConnection())'.
 *
 * 2. Ausencia de Inserción Masiva (Batch Processing):
 *    - El método 'insert(Cliente c)' realiza un único 'ps.executeUpdate()' seguido de un 'commit()'.
 *    - Para la carga inicial o inserciones múltiples, se debería proveer 'insertBatch(List<Cliente> clientes)'
 *      utilizando 'ps.addBatch()' y 'ps.executeBatch()' dentro de una única transacción sin commits intermedios.
 *
 * 3. Corrección Crítica y Eficiencia en 'select(int id)':
 *    - En la línea 95 del código original:
 *      'try (PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery())'
 *      Se invoca 'ps.executeQuery()' en la declaración del try ANTES de setear el parámetro 'ps.setInt(1, id)',
 *      y además no se invoca 'rs.next()' antes de leer las columnas. Esto arroja 'SQLException' en ejecución.
 *    - Debe prepararse la sentencia, asignar el parámetro 'ps.setInt(1, id)' y recién ejecutar la consulta.
 *
 * 4. Optimización de Proyecciones ('SELECT *' vs Columnas Explícitas):
 *    - En 'select' y 'selectAll', se utiliza 'SELECT * FROM Cliente'.
 *    - Especificar explícitamente las columnas ('SELECT idCliente, nombre, email FROM Cliente')
 *      reduce el tráfico de red, evita transferencias innecesarias de metadatos y optimiza el plan de ejecución.
 *
 * 5. Optimización de Consultas Complejas ('obtenerClientesPorMayorFacturacionDesc'):
 *    - Indexación en BD: Este cuádruple JOIN ('Cliente', 'Factura', 'Factura_Producto', 'Producto')
 *      requiere índices sobre las claves foráneas ('Factura.idCliente', 'Factura_Producto.idFactura',
 *      'Factura_Producto.idProducto') para evitar escaneos de tabla completos (Full Table Scan).
 *    - Pre-dimensionamiento de colecciones: Pre-asignar la capacidad de 'new ArrayList<>(expectedSize)'
 *      evita múltiples redimensionamientos internos ('grow()') y copias de arreglos en memoria heap.
 * =====================================================================================
 */
// Patrón Singleton
public class ClienteDAO implements DAO<Cliente> {
    private static ClienteDAO unicaInstancia;

    private ClienteDAO() throws SQLException {}

    public static ClienteDAO getInstance() throws SQLException {
        if (unicaInstancia == null) {
            unicaInstancia = new ClienteDAO();
        }

        return unicaInstancia;
    }

    @Override
    public void dropTable() throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        String drop_table = "DROP TABLE IF EXISTS Cliente";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente
        try (PreparedStatement ps = conn.prepareStatement(drop_table)) {
            ps.executeUpdate();

            conn.commit();
            // Sugerencia de eficiencia: No cerrar la conexión estática compartida; usar Connection Pool
            conn.close();
        } catch (SQLException e) {
            conn.rollback(); // Rollback en caso de error
            throw new SQLException("Error al eliminar la tabla Cliente.", e);
        }
    }

    @Override
    public void createTable() throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        String table = "CREATE TABLE IF NOT EXISTS Cliente(" +
                "idCliente INT," +
                "nombre VARCHAR(500)," +
                "email VARCHAR(150)," +
                "PRIMARY KEY(idCliente))";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente
        try (PreparedStatement ps = conn.prepareStatement(table)) {
            ps.executeUpdate();

            conn.commit();
            conn.close();
        } catch (SQLException e) {
            conn.rollback(); // Rollback en caso de error
            throw new SQLException("Error al crear la tabla Cliente.", e);
        }
    }

    @Override
    public void insert (Cliente c) throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        String query = "INSERT INTO Cliente(idCliente, nombre, email) VALUES (?, ?, ?)";

        // Sugerencia de eficiencia: Implementar 'insertBatch(List<Cliente> clientes)' con 'addBatch()'
        // y 'executeBatch()' para evitar round-trips de red individuales y múltiples fsync de disco por commit.
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, c.getIdCliente());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getEmail());
            ps.executeUpdate();

            conn.commit();
            conn.close();
        } catch (SQLException e) {
            conn.rollback(); // Rollback en caso de error
            throw new SQLException("Error al insertar Cliente!", e);
        }
    }

    @Override
    public Cliente select (int id) throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        Cliente c = null;
        // Sugerencia de eficiencia: Proyectar columnas explícitas 'SELECT idCliente, nombre, email'
        String query = "SELECT * FROM Cliente WHERE idCliente=?";

        // NOTA / SUGERENCIA DE CORRECCIÓN:
        // 'ps.executeQuery()' se declara en el try-with-resources antes de setear 'ps.setInt(1, id)'.
        // Debe estructurarse como:
        // try (PreparedStatement ps = conn.prepareStatement(query)) {
        //     ps.setInt(1, id);
        //     try (ResultSet rs = ps.executeQuery()) {
        //         if (rs.next()) {
        //             c = new Cliente(rs.getInt(1), rs.getString(2), rs.getString(3));
        //         }
        //     }
        // }
        try (PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            ps.setInt(1, id);

            c = new Cliente(rs.getInt(1), rs.getString(2), rs.getString(3));

            conn.close();
        } catch (SQLException e) {
            throw new SQLException("Error al seleccionar Cliente con id=" + id + "!", e);
        }

        return c;
    }

    @Override
    public List<Cliente> selectAll () throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        // Sugerencia de eficiencia: Si se conoce el tamaño estimado, inicializar con capacidad 'new ArrayList<>(tam)'
        List<Cliente> clientes = new ArrayList<>();
        // Sugerencia de eficiencia: Evitar 'SELECT *' y proyectar columnas explícitas
        String query = "SELECT * FROM Cliente";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente
        try (PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(new Cliente(rs.getInt(1), rs.getString(2), rs.getString(3)));
            }

            conn.close();
        } catch (SQLException e) {
            throw new SQLException("Error al obtener Clientes!", e);
        }

        return clientes;
    }

    @Override
    public boolean update(Cliente c) throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        String query = "UPDATE Cliente SET nombre = ?, email = ? WHERE idCliente = ?";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getEmail());
            ps.setInt(3, c.getIdCliente());

            int affectedRows = ps.executeUpdate(); // Devuelve el número de filas afectadas

            conn.commit();
            conn.close();

            return affectedRows > 0; // Retorna true si se actualizó al menos una fila
        } catch (SQLException e) {
            conn.rollback(); // Rollback en caso de error
            throw new SQLException("Error al actualizar Cliente!", e);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        String query = "DELETE FROM Cliente WHERE idCliente = ?";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);

            int affectedRows = ps.executeUpdate(); // Devuelve el número de filas afectadas

            conn.commit();
            conn.close();

            return affectedRows > 0; // Retorna true si se eliminó al menos una fila
        } catch (SQLException e) {
            conn.rollback(); // Rollback en caso de error
            throw new SQLException("Error al eliminar Cliente con id=" + id, e);
        }
    }

    public List<ClienteConFacturacionDTO> obtenerClientesPorMayorFacturacionDesc () throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        // Sugerencia de eficiencia: Inicializar ArrayList con capacidad esperada para evitar realocaciones
        List<ClienteConFacturacionDTO> clientesFacturadosDesc = new ArrayList<>();
        // Sugerencia de eficiencia (Consulta SQL e Índices):
        // 1. Claves foráneas e índices secundarios en 'Factura(idCliente)', 'Factura_Producto(idFactura)'
        //    y 'Factura_Producto(idProducto)' son esenciales para que el motor resuelva los JOINs en tiempo lineal.
        // 2. En configuraciones estrictas de SQL ('ONLY_FULL_GROUP_BY'), agrupar por 'c.idCliente, c.nombre, c.email'.
        String query = "SELECT c.idCliente, c.nombre, c.email, SUM(fp.cantidad) AS cantidad, SUM(p.valor * fp.cantidad) AS totalFacturado "
                + "FROM Cliente c "
                + "JOIN Factura f USING (idCliente) "
                + "JOIN Factura_Producto fp USING (idFactura) "
                + "JOIN Producto p USING (idProducto) "
                + "GROUP BY c.idCliente "
                + "ORDER BY totalFacturado DESC";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                clientesFacturadosDesc.add(new ClienteConFacturacionDTO(rs.getInt("idCliente"),
                        rs.getString("nombre"),
                        rs.getString("email"),
                        rs.getFloat("totalFacturado")));
            }

            conn.close();
        } catch(SQLException e){
            throw new SQLException("Error al obtener Clientes por facturación desc!", e);
        }

        return clientesFacturadosDesc;
    }
}
