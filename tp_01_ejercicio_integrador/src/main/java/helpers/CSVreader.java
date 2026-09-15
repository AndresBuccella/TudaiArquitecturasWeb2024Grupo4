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

public class CSVreader {
    private static final Logger logger = Logger.getLogger(CSVreader.class.getName());
    public List<Cliente> leerArchivoClientes() {
        List<Cliente> clientes = new ArrayList<>();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("csv_files/clientes.csv");
        if (inputStream == null) {
            System.err.println("No se encontró el archivo CSV en la carpeta resources.");
            return clientes;
        }
        CSVFormat formatoCsv = CSVFormat.DEFAULT.builder()
                .setHeader()
                .build();
        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = formatoCsv.parse(reader)) {
            for (CSVRecord row : parser) {
                Cliente c = new Cliente(
                        Integer.parseInt(row.get("idCliente")),
                        row.get("nombre"),
                        row.get("email"));
                clientes.add(c);
            }
            logger.info("Archivo CSV leído correctamente.");
            System.out.println("Archivo CSV clientes.csv leído correctamente.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error de entrada/salida al procesar el CSV", e);
        }
        return clientes;
    }

    public List<Factura> leerArchivoFacturas() {
        List<Factura> facturas = new ArrayList<>();
        String csvFile = ".\\src\\main\\resources\\csv_files\\facturas.csv";

        try (CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(new FileReader(csvFile))) {
            for (CSVRecord row : parser) {
                Factura f = new Factura(
                        Integer.parseInt(row.get("idFactura")),
                        Integer.parseInt(row.get("idCliente")));
                facturas.add(f);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return facturas;
    }

    public List<FacturaProducto> leerArchivoFacturasProductos() {
        List<FacturaProducto> facturas_productos = new ArrayList<>();
        String csvFile = ".\\resources\\csv_files\\facturas-productos.csv";

        try (CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(new FileReader(csvFile))) {
            for (CSVRecord row : parser) {
                FacturaProducto fp = new FacturaProducto(
                        Integer.parseInt(row.get("idFactura")),
                        Integer.parseInt(row.get("idProducto")),
                        Integer.parseInt(row.get("cantidad")));
                facturas_productos.add(fp);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return facturas_productos;
    }

    public List<Producto> leerArchivoProductos() {
        List<Producto> productos = new ArrayList<>();
        String csvFile = ".\\src\\main\\resources\\csv_files\\productos.csv";

        try (CSVParser parser = CSVFormat.DEFAULT.withHeader().parse(new FileReader(csvFile))) {
            for (CSVRecord row : parser) {
                Producto p = new Producto(
                        Integer.parseInt(row.get("idProducto")),
                        row.get("nombre"),
                        Float.parseFloat(row.get("valor")));
                productos.add(p);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return productos;
    }
}
