import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Scanner;

/**
 * Cliente de descarga de archivos que utiliza una conexión TCP única por cada solicitud.
 * <p>
 * Esta clase implementa un cliente que solicita archivos al servidor enviando la ruta
 * como bytes crudos y cerrando la salida del socket (shutdownOutput) para indicar el fin de la solicitud.
 * </p>
 */
public class MainUniqueFileClient {

    /**
     * Punto de entrada de la aplicación cliente.
     * <p>
     * Maneja la entrada del usuario, la conexión con el servidor y el flujo de descarga del archivo.
     * </p>
     *
     * @param args Argumentos de línea de comandos.
     * <br>args[0]: (Opcional) IP del servidor.
     * <br>args[1]: (Opcional) Puerto del servidor.
     */
    public static void main(String[] args) {

        int port = 4321;
        String ip = "localhost";

        // Validación de argumentos de entrada
        try {
            if (args.length == 1) {
                if (esIpValida(args[0])) ip = args[0];
            } else if (args.length == 2) {
                if (esIpValida(args[0])) ip = args[0];
                port = Integer.parseInt(args[1]);
            }
        } catch (NumberFormatException e) {
            System.err.println("El puerto debe ser un número válido.");
            return;
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("--- CLIENTE DE ARCHIVOS (Modo Stream) ---");
        System.out.println("Introduce la ruta del archivo a buscar (o Enter para salir):");
        String ruta = scanner.nextLine();

        // Bucle principal: solicita archivos hasta que el usuario pulse Enter vacío
        while (!ruta.isEmpty()) {

            // Se crea un NUEVO socket para cada petición (try-with-resources asegura el cierre)
            try (Socket socketCliente = new Socket(ip, port);
                 InputStream in = socketCliente.getInputStream();
                 OutputStream out = socketCliente.getOutputStream()) {

                // 1. Envío de la solicitud
                out.write(ruta.getBytes());
                out.flush();

                // Indicar al servidor que no se enviarán más datos, pero se espera respuesta
                socketCliente.shutdownOutput();

                // 2. Lectura de la respuesta del servidor (Cabecera)
                byte[] respuestaBuffer = new byte[1024];
                int leidosRespuesta = in.read(respuestaBuffer);
                String respuesta = new String(respuestaBuffer, 0, leidosRespuesta);

                if (respuesta.startsWith("OK")) {
                    // 3. Lectura del tamaño del archivo (8 bytes para long)
                    byte[] sizeBytes = new byte[8];
                    in.read(sizeBytes);
                    long tamaño = ByteBuffer.wrap(sizeBytes).getLong();

                    System.out.println("Descargando " + tamaño + " bytes...");

                    String nombreArchivoLocal = new File(ruta).getName();
                    if (nombreArchivoLocal.isEmpty()) nombreArchivoLocal = "descarga_recibida.dat";

                    // 4. Descarga del contenido binario
                    try (FileOutputStream fos = new FileOutputStream(nombreArchivoLocal)) {
                        byte[] buffer = new byte[8192];
                        int bytesLeidos;
                        long totalRecibido = 0;

                        while (totalRecibido < tamaño && (bytesLeidos = in.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesLeidos);
                            totalRecibido += bytesLeidos;
                        }
                    }
                    System.out.println("¡Descarga completada en: " + nombreArchivoLocal + "!");

                } else {
                    System.out.println("El servidor respondió con error: " + respuesta);
                }

            } catch (Exception e) {
                System.err.println("Error de conexión: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("\nIntroduce otro archivo a buscar (o Enter para salir):");
            ruta = scanner.nextLine();
        }

        System.out.println("Saliendo del cliente...");
    }

    /**
     * Verifica si una cadena de texto tiene el formato de una dirección IP válida.
     *
     * @param ip La cadena que representa la dirección IP.
     * @return {@code true} si la IP es válida o es un nombre de host resoluble, {@code false} en caso contrario.
     */
    public static boolean esIpValida(String ip) {
        try {
            InetAddress.getByName(ip);
            return ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$") || ip.contains(":");
        } catch (UnknownHostException e) {
            return false;
        }
    }
}