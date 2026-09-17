package factories;

import repositories.ClienteDAO;
import repositories.FacturaDAO;
import repositories.FacturaProductoDAO;
import repositories.ProductoDAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
 * =====================================================================================
 * SUGERENCIAS DE MEJORA Y EFICIENCIA (MySqlConnectionFactory):
 * =====================================================================================
 * 1. Concurrencia y Thread-Safety (Problema Crítico de Eficiencia y Diseño):
 *    - El uso de una única conexión estática ('private static Connection conn;') NO es seguro
 *      para hilos (Thread-Safe). Si múltiples hilos intentan ejecutar transacciones o consultas
 *      simultáneamente, colisionarán en el estado del socket y de la transacción.
 *    - Como los DAOs llaman a 'conn.close()', cada operación destruye la conexión física, forzando
 *      a 'DriverManager.getConnection(...)' a negociar un nuevo socket TCP y protocolo de
 *      autenticación cada vez (15-50ms de latencia por query).
 *
 * 2. Implementación de un Pool de Conexiones (HikariCP / DBCP):
 *    - Solución óptima: Reemplazar el Singleton de conexión estática por un DataSource con
 *      Connection Pooling (ej. HikariCP).
 *    - Beneficio: Mantiene un conjunto de conexiones físicas precalentadas y reutilizables.
 *      'getConnection()' se resuelve en microsegundos y 'conn.close()' devuelve la conexión al pool
 *      en lugar de cerrarla a nivel de sistema operativo.
 *
 * 3. Parámetros de Rendimiento en la URL de MySQL (JDBC Connection Flags):
 *    - La URL actual no incluye optimizaciones de MySQL Connector/J.
 *    - Flags indispensables para alta eficiencia:
 *      * '?rewriteBatchedStatements=true': Transforma sentencias 'addBatch()' en un único multi-insert
 *        'INSERT INTO ... VALUES (...), (...)', multiplicando la velocidad de inserción hasta por 50x.
 *      * '&cachePrepStmts=true': Habilita el cacheo de sentencias preparadas en el cliente JDBC.
 *      * '&prepStmtCacheSize=250': Número de sentencias preparadas que el driver retiene en memoria.
 *      * '&prepStmtCacheSqlLimit=2048': Límite de longitud SQL para cachear.
 *      * '&useServerPrepStmts=true': Prepara sentencias del lado del servidor MySQL.
 *
 * 4. Externalización de Credenciales y Configuración:
 *    - Las credenciales y la URL están hardcodeadas en constantes privadas. Deben obtenerse
 *      desde un archivo 'application.properties', variables de entorno ('System.getenv') o
 *      propiedades del sistema, mejorando seguridad y portabilidad entre entornos.
 *
 * 5. Manejo de Errores en Constructor:
 *    - 'System.exit(1)' detiene la JVM de forma abrupta si falla la carga del driver.
 *      Se debe relanzar una excepción ('IllegalStateException' o 'SQLException') para permitir
 *      un manejo de fallos controlado o ejecución en entornos de testing.
 * =====================================================================================
 */
// Patrón Singleton
public class MySqlConnectionFactory extends DatabaseFactory {
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    // Sugerencia de eficiencia: Agregar ?rewriteBatchedStatements=true&cachePrepStmts=true&prepStmtCacheSize=250&prepStmtCacheSqlLimit=2048
    private static final String DB_URI = "jdbc:mysql://localhost:3306/db_integrador_01";
    // Sugerencia de seguridad y arquitectura: Externalizar credenciales a properties o variables de entorno
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "password";

    private static MySqlConnectionFactory unicaInstancia = null;
    // Sugerencia de concurrencia/eficiencia: Reemplazar conexión estática por HikariDataSource
    private static Connection conn;

    private MySqlConnectionFactory() {
        try {
            Class.forName(DRIVER);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static MySqlConnectionFactory getInstance() throws SQLException {
        if (unicaInstancia == null) {
            unicaInstancia = new MySqlConnectionFactory();
        }

        return unicaInstancia;
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            if (conn == null || conn.isClosed()) {
                conn = DriverManager.getConnection(DB_URI, DB_USER, DB_PASSWORD);
                conn.setAutoCommit(false);
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }

        return conn;
    }

    @Override
    public void closeConnection() throws SQLException {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public ClienteDAO getClienteDAO() throws SQLException {
        return ClienteDAO.getInstance();
    }

    @Override
    public FacturaDAO getFacturaDAO() throws SQLException {
        return FacturaDAO.getInstance();
    }

    @Override
    public FacturaProductoDAO getFacturaProductoDAO() throws SQLException {
        return FacturaProductoDAO.getInstance();
    }

    @Override
    public ProductoDAO getProductoDAO() throws SQLException {
        return ProductoDAO.getInstance();
    }
}
