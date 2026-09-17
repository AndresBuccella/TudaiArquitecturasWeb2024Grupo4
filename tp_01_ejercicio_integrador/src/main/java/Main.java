import helpers.CSVreader;
import helpers.DatabaseLoader;
import repositories.ClienteDAO;
import repositories.ProductoDAO;
import dtos.ClienteConFacturacionDTO;
import dtos.ProductoMayorRecaudacionDTO;

import java.sql.SQLException;
import java.util.List;

/*
 * =====================================================================================
 * SUGERENCIAS DE MEJORA Y EFICIENCIA (Main):
 * =====================================================================================
 * 1. Desacoplamiento y uso del patrón Factory:
 *    - En lugar de invocar 'ClienteDAO.getInstance()' y 'ProductoDAO.getInstance()'
 *      directamente (lo que acopla el código a implementaciones concretas), se debería
 *      obtener la fábrica mediante 'DatabaseFactory.getDAOFactory(DatabaseFactory.MYSQL_JDBC)'
 *      y solicitar los DAOs a través de sus interfaces 'DAO<Cliente>' y 'DAO<Producto>'.
 *
 * 2. Eficiencia en operaciones de Entrada/Salida (I/O en Consola):
 *    - El recorrido de la lista 'clientesFacturados' imprime en consola mediante
 *      'System.out.println(cliente)' en cada iteración.
 *    - 'System.out.println' es una operación sincronizada y bloqueante que interactúa con
 *      el descriptor de salida estándar del sistema operativo. Si la consulta retorna miles
 *      de registros, este bucle genera una penalización severa de I/O.
 *    - Mejora de eficiencia: Acumular la salida en un 'StringBuilder' o utilizar un
 *      'BufferedWriter' / salida formateada en bloque para emitir una única llamada al sistema.
 *
 * 3. Gestión y Cierre de Recursos (Connection / Pool):
 *    - No se realiza una liberación o cierre explícito de la fábrica o del pool de conexiones
 *      al finalizar la ejecución en 'main'. Es una buena práctica invocar un método de cierre
 *      o 'shutdown' de recursos de la base de datos en un bloque 'finally'.
 *
 * 4. Manejo Robusto de Excepciones:
 *    - Se captura 'SQLException' imprimiendo únicamente la traza con 'e.printStackTrace()'.
 *      Para mayor robustez y observabilidad, se recomienda el uso de un Logger (SLF4J / Log4j2)
 *      y la terminación controlada del proceso ('System.exit(1)' o código de error específico).
 * =====================================================================================
 */
public class Main {
    public static void main(String[] args) {
        CSVreader reader = new CSVreader();

        try {
            // Cargar todos los datos desde los archivos CSV a la base de datos
            DatabaseLoader.cargarDatos(reader);

            // Obtener instancias de DAOs para las consultas
            ClienteDAO clienteDAO = ClienteDAO.getInstance();
            ProductoDAO productoDAO = ProductoDAO.getInstance();

            // Obtiene y muestra el producto con mayor recaudación
            ProductoMayorRecaudacionDTO productoMayorRecaudacion = productoDAO.obtenerProductoMayorRecaudacion();

            if (productoMayorRecaudacion != null) {
                System.out.println("Producto con mayor recaudación: ");
                System.out.println(productoMayorRecaudacion);
            } else {
                System.out.println("No se encontraron productos.");
            }

            // Obtiene y muestra la lista de clientes ordenada por facturación
            List<ClienteConFacturacionDTO> clientesFacturados = clienteDAO.obtenerClientesPorMayorFacturacionDesc();

            if (clientesFacturados.isEmpty()) {
                System.out.println("\nNo se encontraron clientes.");
            } else {
                System.out.println("\nClientes ordenados por mayor facturación: ");

                // Sugerencia de eficiencia: Si la lista es extensa, amortiguar I/O usando StringBuilder
                for (ClienteConFacturacionDTO cliente : clientesFacturados) {
                    System.out.println(cliente);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
