package repositories;

import repositories.interfaces.DAO;
import dtos.ProductoMayorRecaudacionDTO;
import entities.Producto;
import factories.MySqlConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/*
 * =====================================================================================
 * SUGERENCIAS DE MEJORA Y EFICIENCIA (ProductoDAO):
 * =====================================================================================
 * 1. Optimización de la Consulta 'obtenerProductoMayorRecaudacion()':
 *    - La consulta calcula 'SUM(fp.cantidad * p.valor) AS recaudacion' dentro de la agregación.
 *      Como 'p.valor' es un atributo fijo por cada 'p.idProducto', multiplicar en cada fila
 *      individual antes de la suma agrega costo de CPU redundante en el motor de base de datos.
 *    - Mejora matemática: Utilizar 'p.valor * SUM(fp.cantidad) AS recaudacion'.
 *    - Mejora de Plan de Ejecución (Subconsulta / Pre-agregación):
 *      Hacer el JOIN de toda la tabla 'Producto' con 'Factura_Producto' previo a agrupar
 *      produce un conjunto intermedio grande. Se puede obtener primero el 'idProducto' con
 *      mayor facturación calculada desde 'Factura_Producto' (o una tabla derivada) y luego
 *      hacer un JOIN simple de 1 fila contra 'Producto', aprovechando un índice covering en
 *      'Factura_Producto(idProducto, cantidad)'.
 *
 * 2. Gestión de Conexiones y Connection Pooling:
 *    - Invocar 'conn.close()' destruye la conexión singleton, forzando la apertura de un
 *      nuevo socket TCP en la siguiente llamada. Debe sustituirse por un pool como HikariCP.
 *
 * 3. Inserción Masiva en Lote (Batch Processing):
 *    - Para la inserción de múltiples productos, implementar 'insertBatch(List<Producto>)'
 *      con 'addBatch()' / 'executeBatch()' y un único commit.
 *
 * 4. Corrección Crítica en 'select(int id)':
 *    - 'ps.executeQuery()' se declara en el try-with-resources antes de asignar 'ps.setInt(1, id)'
 *      y no se verifica 'rs.next()'.
 *
 * 5. Tipos de Datos y Casting en 'update':
 *    - 'ps.setDouble(2, p.getValor())' realiza una conversión implícita de 'float' a 'double'.
 *      Se debe utilizar 'ps.setFloat(2, p.getValor())' o preferentemente migrar a 'BigDecimal'
 *      o 'double' para valores monetarios tanto en Java como en MySQL ('DECIMAL(10,2)').
 * =====================================================================================
 */
// Patrón Singleton
public class ProductoDAO implements DAO<Producto> {
    private static ProductoDAO unicaInstancia;

    private ProductoDAO() throws SQLException {

    }

    public static ProductoDAO getInstance() throws SQLException {
        if (unicaInstancia == null) {
            unicaInstancia = new ProductoDAO();
        }

        return unicaInstancia;
    }

    @Override
    public void dropTable() throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        String drop_table = "DROP TABLE IF EXISTS Producto";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente
        try (PreparedStatement ps = conn.prepareStatement(drop_table)) {
            ps.executeUpdate();

            conn.commit();
            conn.close();
        } catch (SQLException e) {
            conn.rollback(); // Rollback en caso de error
            throw new SQLException("Error al eliminar la tabla Producto.", e);
        }
    }

    @Override
    public void createTable() throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        // Sugerencia de precisión/eficiencia: Para montos monetarios, 'valor DECIMAL(10,2)' es
        // preferible a 'FLOAT' para evitar imprecisiones de redondeo en cálculos agregados.
        String table = "CREATE TABLE IF NOT EXISTS Producto(" +
                "idProducto INT," +
                "nombre VARCHAR(45)," +
                "valor FLOAT," +
                "PRIMARY KEY(idProducto))";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente
        try (PreparedStatement ps = conn.prepareStatement(table)) {
            ps.executeUpdate();

            conn.commit();
            conn.close();
        } catch (SQLException e) {
            conn.rollback(); // Rollback en caso de error
            throw new SQLException("Error al crear la tabla Producto.", e);
        }
    }

    @Override
    public void insert (Producto p) throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        String query = "INSERT INTO Producto(idProducto, nombre, valor) VALUES (?, ?, ?)";

        // Sugerencia de eficiencia: Implementar inserción en batch ('insertBatch') para evitar
        // commits y round-trips individuales por cada producto.
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, p.getIdProducto());
            ps.setString(2, p.getNombre());
            ps.setFloat(3, p.getValor());
            ps.executeUpdate();

            conn.commit();
            conn.close();
        } catch (SQLException e) {
            conn.rollback(); // Rollback en caso de error
            throw new SQLException("Error al insertar Producto!", e);
        }
    }

    @Override
    public Producto select (int id) throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        Producto p = null;
        // Sugerencia de eficiencia: Proyectar columnas explícitas en lugar de 'SELECT *'
        String query = "SELECT * FROM Producto WHERE idProducto=?";

        // NOTA / SUGERENCIA DE CORRECCIÓN:
        // 'ps.executeQuery()' se ejecuta antes de setear el parámetro y no se llama a 'rs.next()'.
        // Debe reestructurarse con parámetros configurados antes de la ejecución.
        try (PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            ps.setInt(1, id);

            p = new Producto(rs.getInt(1), rs.getString(2), rs.getFloat(3));

            conn.close();
        } catch (SQLException e) {
            throw new SQLException("Error al seleccionar Producto con id=" + id + "!", e);
        }

        return p;
    }

    @Override
    public List<Producto> selectAll () throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        // Sugerencia de eficiencia: Pre-asignar capacidad inicial en el ArrayList
        List<Producto> productos = new ArrayList<>();
        String query = "SELECT * FROM Producto";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente
        try (PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                productos.add(new Producto(rs.getInt(1), rs.getString(2), rs.getFloat(3)));
            }

            conn.close();
        } catch (SQLException e) {
            throw new SQLException("Error al obtener Productos!", e);
        }

        return productos;
    }

    @Override
    public boolean update(Producto p) throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        String query = "UPDATE Producto SET nombre = ?, valor = ? WHERE idProducto = ?";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, p.getNombre());
            // Sugerencia: Usar setFloat si el atributo es float o migrar consistentemente a double/BigDecimal
            ps.setDouble(2, p.getValor());
            ps.setInt(3, p.getIdProducto());

            int affectedRows = ps.executeUpdate(); // Devuelve el número de filas afectadas

            conn.commit();
            conn.close();

            return affectedRows > 0; // Retorna true si se actualizó al menos una fila
        } catch (SQLException e) {
            conn.rollback(); // Rollback en caso de error
            throw new SQLException("Error al actualizar Producto!", e);
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        String query = "DELETE FROM Producto WHERE idProducto = ?";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, id);

            int affectedRows = ps.executeUpdate(); // Devuelve el número de filas afectadas

            conn.commit();
            conn.close();

            return affectedRows > 0; // Retorna true si se eliminó al menos una fila
        } catch (SQLException e) {
            conn.rollback(); // Rollback en caso de error
            throw new SQLException("Error al eliminar Producto con id=" + id, e);
        }
    }

    public ProductoMayorRecaudacionDTO obtenerProductoMayorRecaudacion () throws SQLException {
        Connection conn = MySqlConnectionFactory.getInstance().getConnection();

        ProductoMayorRecaudacionDTO productoMayorRecaudacion = null;
        // Sugerencia de eficiencia (Cálculo SQL y Covering Index):
        // 1. Reemplazar 'SUM(fp.cantidad * p.valor)' por 'p.valor * SUM(fp.cantidad)' evita
        //    multiplicar en cada fila antes de agregar.
        // 2. Un índice en 'Factura_Producto(idProducto, cantidad)' permite a MySQL computar
        //    la suma directamente en el índice (Using index).
        String query = "SELECT p.idProducto, p.nombre, p.valor, "
                + "SUM(fp.cantidad * p.valor) AS recaudacion "
                + "FROM Producto p "
                + "JOIN Factura_Producto fp ON p.idProducto = fp.idProducto "
                + "GROUP BY p.idProducto "
                + "ORDER BY recaudacion DESC "
                + "LIMIT 1";

        // try-with-resources asegura que PreparedStatement y ResultSet se cierren automáticamente
        try (PreparedStatement ps = conn.prepareStatement(query); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                productoMayorRecaudacion = new ProductoMayorRecaudacionDTO(rs.getInt("idProducto"),
                        rs.getString("nombre"),
                        rs.getFloat("valor"),
                        rs.getFloat("recaudacion"));
            }

            conn.close();
        } catch(SQLException e){
            throw new SQLException("Error al obtener Producto con mayor recaudación!", e);
        }

        return productoMayorRecaudacion;
    }
}
