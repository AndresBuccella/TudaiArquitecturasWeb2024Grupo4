package repositories.interfaces;

import java.sql.SQLException;
import java.util.List;

/*
 * =====================================================================================
 * SUGERENCIAS DE MEJORA Y EFICIENCIA (DAO Interface):
 * =====================================================================================
 * 1. Soporte para Operaciones Masivas (Batch Processing):
 *    - La interfaz carece de métodos como 'void insertBatch(List<T> list)' o
 *      'default void insertAll(Collection<T> items)'.
 *    - Impacto en Eficiencia: Obliga a los consumidores a iterar colecciones ejecutando
 *      llamadas individuales a 'insert(t)', lo que provoca múltiples round-trips de red
 *      hacia el motor de base de datos. Implementar inserción por lotes ('addBatch()' /
 *      'executeBatch()') reduce el tiempo de inserción de miles de registros de varios
 *      segundos a pocos milisegundos.
 *
 * 2. Flexibilidad y Tipado Genérico de Clave Primaria (DAO<T, ID>):
 *    - Los métodos 'select(int id)' y 'delete(int id)' asumen un identificador primitivo 'int'.
 *    - Esto genera problemas de diseño y viola el principio de sustitución de Liskov (LSP) en
 *      entidades con claves compuestas (como 'FacturaProducto', cuya PK es 'idFactura, idProducto')
 *      o identificadores de tipo 'Long', 'UUID' o 'String', forzando a lanzar
 *      'UnsupportedOperationException'.
 *    - Sugerencia: Parametrizar la interfaz como 'public interface DAO<T, ID>' con firmas como
 *      'T select(ID id)' y 'boolean delete(ID id)'.
 *
 * 3. Segregación de Responsabilidades (DDL vs DML):
 *    - 'dropTable()' y 'createTable()' son operaciones de definición de esquema (DDL).
 *      No corresponden al contrato de un Data Access Object (DML/CRUD).
 *    - En motores como MySQL, las sentencias DDL provocan un commit implícito inmediato,
 *      impidiendo el control transaccional e introduciendo overhead de recreación de esquema.
 *      La creación de tablas debería delegarse a un inicializador o herramienta de migración.
 *
 * 4. Contexto Transaccional / Conexión Compartida:
 *    - La interfaz no provee sobrecargas que acepten un objeto 'Connection' externo.
 *      Esto impide coordinar múltiples operaciones DAO bajo una única transacción atómica
 *      (patrón Unit of Work).
 * =====================================================================================
 */
public interface DAO<T> {
    void dropTable() throws SQLException;

    void createTable() throws SQLException;

    // Sugerencia de eficiencia: Incorporar 'void insertBatch(List<T> entities) throws SQLException;'
    void insert(T t) throws SQLException;

    // Sugerencia de diseño: Utilizar clave genérica ID en lugar de forzar 'int'
    T select(int id) throws SQLException;

    List<T> selectAll() throws SQLException;

    boolean update(T t) throws SQLException;

    boolean delete(int id) throws SQLException;
}
