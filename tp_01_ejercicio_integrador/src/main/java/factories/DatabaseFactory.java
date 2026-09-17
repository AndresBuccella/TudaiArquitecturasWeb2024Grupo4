package factories;

import repositories.ClienteDAO;
import repositories.FacturaDAO;
import repositories.FacturaProductoDAO;
import repositories.ProductoDAO;

import java.sql.Connection;
import java.sql.SQLException;

/*
 * =====================================================================================
 * SUGERENCIAS DE MEJORA Y EFICIENCIA (DatabaseFactory):
 * =====================================================================================
 * 1. Desacoplamiento de Interfaces (Inversión de Dependencias - DIP):
 *    - Los métodos abstractos declaran retornar implementaciones concretas ('ClienteDAO',
 *      'ProductoDAO', etc.) en lugar de sus interfaces genéricas ('DAO<Cliente>', 'DAO<Producto>').
 *    - Sugerencia: Retornar las interfaces de acceso a datos para permitir cambiar de tecnología
 *      subyacente (ej. de JDBC a JPA/Hibernate o a una base en memoria para testing) sin modificar
 *      el código consumidor.
 *
 * 2. Gestión de Conexiones vs DataSource:
 *    - Exponer 'getConnection()' y 'closeConnection()' directamente en la fábrica abstracta promueve
 *      el diseño con una única conexión compartida o fugas de recursos.
 *    - Sugerencia: Reemplazar el manejo manual de 'Connection' por la provisión de un 'DataSource'
 *      con Connection Pooling (ej. HikariCP) o encapsular la gestión transaccional.
 *
 * 3. Seguridad de Tipos (Enum vs Enteros Mágicos):
 *    - El uso de enteros ('public static final int MYSQL_JDBC = 1') para seleccionar la factoría
 *      es propenso a errores. Es preferible utilizar una enumeración tipada ('enum DatabaseType').
 * =====================================================================================
 */
public abstract class DatabaseFactory {
    public static final int MYSQL_JDBC = 1;

    public abstract Connection getConnection() throws SQLException;
    public abstract void closeConnection() throws SQLException;

    // Sugerencia de diseño: Retornar DAO<Cliente> en lugar de ClienteDAO concreto
    public abstract ClienteDAO getClienteDAO() throws SQLException;
    public abstract FacturaDAO getFacturaDAO() throws SQLException;
    public abstract FacturaProductoDAO getFacturaProductoDAO() throws SQLException;
    public abstract ProductoDAO getProductoDAO() throws SQLException;

    public static DatabaseFactory getDAOFactory(int whichFactory) throws SQLException {
        switch(whichFactory) {
            case MYSQL_JDBC:
                return MySqlConnectionFactory.getInstance();
            default:
                return null;
        }
    }
}
