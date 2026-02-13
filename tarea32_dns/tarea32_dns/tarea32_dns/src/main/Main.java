package main;

import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servidor DNS concurrente implementado sobre el protocolo UDP.
 * <p>
 * Este servidor escucha peticiones en el puerto 2222 y utiliza un pool de hilos
 * para manejar múltiples solicitudes de clientes de forma simultánea.
 * La resolución de nombres se realiza consultando un archivo de propiedades externo.
 * </p>
 * <p>
 * <b>Requisitos:</b>
 * </p>
 * <ul>
 * <li>Debe existir un archivo <b>dns.properties</b> en la raíz del proyecto.</li>
 * </ul>
 *
 * @author TuNombre
 * @version 1.0
 */
public class Main {

    /**
     * Constructor por defecto del servidor.
     */
    public Main() {
        // Constructor vacío implícito
    }

    /**
     * Punto de entrada principal del servidor DNS.
     * <p>
     * El flujo de trabajo es el siguiente:
     * </p>
     * <ol>
     * <li>Inicializa un pool de hilos de tamaño fijo (10 hilos).</li>
     * <li>Carga la base de datos de dominios desde el archivo {@code dns.properties}.</li>
     * <li>Abre un {@code DatagramSocket} en el puerto 2222.</li>
     * <li>Entra en un bucle infinito esperando peticiones.</li>
     * <li>Al recibir un paquete, delega su procesamiento a un hilo del pool.</li>
     * </ol>
     *
     * @param args Argumentos de línea de comandos (no utilizados).
     */
    public static void main(String[] args) {
        int port = 2222;
        ExecutorService pool = Executors.newFixedThreadPool(10);
        try {
            Properties dnsRecords = new Properties();
            dnsRecords.load(new FileInputStream("dns.properties"));

            DatagramSocket socket = new DatagramSocket(port);
            System.out.println("--- Servidor DNS Activo en puerto " + port + " ---");

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket peticion = new DatagramPacket(buffer, buffer.length);
                socket.receive(peticion);

                pool.execute(() -> {
                    try {
                        String nombreBuscado = new String(peticion.getData(), 0, peticion.getLength()).trim();

                        System.out.println("Cliente pregunta por: " + nombreBuscado);

                        String respuesta = dnsRecords.getProperty(nombreBuscado, "IP NO ENCONTRADA");

                        byte[] dataRespuesta = respuesta.getBytes();
                        InetAddress ipCliente = peticion.getAddress();
                        int puertoCliente = peticion.getPort();

                        DatagramPacket paqueteRespuesta = new DatagramPacket(
                                dataRespuesta,
                                dataRespuesta.length,
                                ipCliente,
                                puertoCliente
                        );

                        socket.send(paqueteRespuesta);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            }

        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}