package helpers;

import entities.Cliente;
import entities.Factura;
import entities.FacturaProducto;
import entities.Producto;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * =====================================================================================
 * SUGERENCIAS DE MEJORA Y EFICIENCIA (CSVreader):
 * =====================================================================================
 * 1. Consumo de Memoria Heap y Falta de Streaming / Ingesta por Lotes:
 *    - Cada método lee el archivo CSV completo e instancia y almacena la totalidad de las entidades
 *      en una lista 'ArrayList<T>' en memoria heap.
 *    - Impacto en Eficiencia: Con datasets grandes (cientos de miles o millones de registros),
 *      retener todas las entidades simultáneamente provoca alta presión en el Garbage Collector y
 *      riesgo de 'OutOfMemoryError'.
 *    - Solución recomendada: Procesar en Streaming o mediante un patrón productor-consumidor
 *      (ej. procesar y cargar en la base de datos en bloques de 1.000 registros mediante un 'Consumer<T>'
 *      o 'Iterator<T>'), descartando los objetos ya persistidos para mantener constante el uso de RAM.
 *
 * 2. Optimización de Entrada/Salida con 'BufferedReader':
 *    - Se utiliza directamente 'InputStreamReader' sin envoltorio de buffer.
 *    - Envolver el flujo en 'new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))'
 *      reduce significativamente las llamadas al sistema operativo al leer bloques de memoria (ej. 8KB)
 *      en lugar de fragmentos pequeños de caracteres.
 *
 * 3. Acceso por Nombre de Columna vs Acceso por Índice en 'CSVRecord':
 *    - 'row.get("idCliente")' ejecuta una búsqueda por hash/string en el mapa de headers en cada fila.
 *    - En archivos voluminosos, acceder por posición ordinal ('row.get(0)', 'row.get(1)') o cachear
 *      los índices previamente ahorra millones de operaciones de hashing y comparaciones de cadenas.
 *
 * 4. Eliminación de Código Redundante (Principio DRY):
 *    - Los cuatro métodos repiten exactamente la misma lógica de lectura y parseo.
 *      Se sugiere unificar en un método genérico parametrizado:
 *      'private <T> List<T> leerArchivo(String fileName, Function<CSVRecord, T> mapper)'
 *      lo que facilita aplicar optimizaciones de I/O de manera centralizada.
 * =====================================================================================
 */
public class CSVreader {
    private static final Logger logger = Logger.getLogger(CSVreader.class.getName());

    public List<Cliente> leerArchivoClientes() {
        List<Cliente> clientes = new ArrayList<>();
        String fileName = "clientes.csv";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("csv_files/" + fileName);
        if (inputStream == null) {
            logger.warning("No se encontró el archivo: " + fileName + " en la carpeta resources.");
            return clientes;
        }
        CSVFormat formatoCsv = CSVFormat.DEFAULT.builder()
                .setHeader()
                .build();

        // Sugerencia de eficiencia: Envolver en 'new BufferedReader(...)' y acceder por índice numérico
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser parser = formatoCsv.parse(reader)) {
            for (CSVRecord row : parser) {
                Cliente c = new Cliente(
                        Integer.parseInt(row.get("idCliente")),
                        row.get("nombre"),
                        row.get("email"));
                clientes.add(c);
            }
            logger.info(fileName + " leído correctamente.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error de entrada/salida al procesar el CSV: " + fileName, e);
        }
        return clientes;
    }

    public List<Factura> leerArchivoFacturas() {
        List<Factura> facturas = new ArrayList<>();
        String fileName = "facturas.csv";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("csv_files/"+fileName);
        if (inputStream == null) {
            logger.warning("No se encontró el archivo: " + fileName + " en la carpeta resources.");
            return facturas;
        }
        CSVFormat formatoCsv = CSVFormat.DEFAULT.builder()
                .setHeader()
                .build();

        // Sugerencia de eficiencia: Usar BufferedReader para amortiguar operaciones de lectura I/O
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser parser = formatoCsv.parse(reader)) {
            for (CSVRecord row : parser) {
                Factura f = new Factura(
                        Integer.parseInt(row.get("idFactura")),
                        Integer.parseInt(row.get("idCliente")));
                facturas.add(f);
            }
            logger.info(fileName + " leído correctamente.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error de entrada/salida al procesar el CSV: " + fileName, e);
        }
        return facturas;
    }

    public List<FacturaProducto> leerArchivoFacturasProductos() {
        List<FacturaProducto> facturas_productos = new ArrayList<>();
        String fileName = "facturas-productos.csv";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("csv_files/" + fileName);
        if(inputStream == null){
            logger.warning("No se encontró el archivo: " + fileName + " en la carpeta resources.");
            return facturas_productos;
        }
        CSVFormat formatoCsv = CSVFormat.DEFAULT.builder()
                .setHeader()
                .build();

        // Sugerencia de eficiencia: Al tener 2590+ filas, el uso de BufferedReader y acceso por índice es clave
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser parser = formatoCsv.parse(reader)) {
            for (CSVRecord row : parser) {
                FacturaProducto fp = new FacturaProducto(
                        Integer.parseInt(row.get("idFactura")),
                        Integer.parseInt(row.get("idProducto")),
                        Integer.parseInt(row.get("cantidad")));
                facturas_productos.add(fp);
            }
            logger.info(fileName + " leído correctamente.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error de entrada/salida al procesar el CSV: " + fileName, e);
        }

        return facturas_productos;
    }

    public List<Producto> leerArchivoProductos() {
        List<Producto> productos = new ArrayList<>();
        String fileName = "productos.csv";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("csv_files/" + fileName);
        if(inputStream == null){
            logger.warning("No se encontró el archivo: " + fileName + " en la carpeta resources.");
            return productos;
        }
        CSVFormat formatoCsv = CSVFormat.DEFAULT.builder()
                .setHeader()
                .build();

        // Sugerencia de eficiencia: Usar BufferedReader para amortiguar lecturas
        try (Reader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             CSVParser parser = formatoCsv.parse(reader)) {
            for (CSVRecord row : parser) {
                Producto p = new Producto(
                        Integer.parseInt(row.get("idProducto")),
                        row.get("nombre"),
                        Float.parseFloat(row.get("valor")));
                productos.add(p);
            }
            logger.info(fileName + " leído correctamente.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error de entrada/salida al procesar el CSV: " + fileName, e);
        }

        return productos;
    }
}
