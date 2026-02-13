import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * Cliente de archivos que mantiene una sesión persistente con el servidor.
 * <p>
 * A diferencia de {@code MainUniqueFileClient}, esta clase establece una única conexión
 * y permite solicitar múltiples archivos secuencialmente usando {@link DataInputStream}
 * y {@link DataOutputStream}.
 * </p>
 */
public class MainFileClientApp {

    /**
     * Inicia el cliente, establece la conexión y gestiona el bucle de peticiones.
     *
     * @param args Argumentos de configuración: [IP] [Puerto]
     */
    public static void main(String[] args) {

        int port = 4321;
        String ip = "localhost";
        Socket socketCliente = null;
        DataInputStream in = null;
        DataOutputStream out = null;
        String ruta = null;
        Scanner scanner = new Scanner(System.in);

        // Validación de parámetros
        try {
            if (args.length == 0) {
                System.out.println("Usando configuración por defecto: " + ip + ":" + port);
            } else if (args.length == 1) {
                if (esIpValida(args[0])) {
                    ip = args[0];
                } else {
                    System.err.println("IP no válida, usando localhost");
                }
            } else if (args.length == 2) {
                if (esIpValida(args[0])) {
                    ip = args[0];
                }
                port = Integer.parseInt(args[1]);
            } else {
                System.err.println("Demasiados argumentos.");
                return;
            }
        } catch (NumberFormatException e) {
            System.err.println("El puerto debe ser un número válido.");
            return;
        }

        // Lógica de conexión y petición
        try {
            socketCliente = new Socket(ip, port);
            // Uso de DataStreams para enviar Strings UTF y primitivos (long)
            in = new DataInputStream(socketCliente.getInputStream());
            out = new DataOutputStream(socketCliente.getOutputStream());

            System.out.println("Introduce la ruta del archivo a buscar con nombre de este");
            ruta = scanner.nextLine();

            // Bucle persistente sobre el MISMO socket
            while (!ruta.isEmpty()) {
                out.writeUTF(ruta);
                out.flush();

                String respuesta = in.readUTF();

                if (respuesta.startsWith("OK")) {
                    long tamaño = in.readLong();
                    long recibido = 0;

                    byte[] buffer = new byte[8192];
                    int bytesLeidos;

                    System.out.println("Descargando " + tamaño + " bytes...");

                    // Guardado del archivo.
                    // Se usa Math.min para no leer bytes de más que pertenezcan a una futura respuesta
                    try (FileOutputStream fos = new FileOutputStream("archivo_recibido.dat")) {
                        while (recibido < tamaño && (bytesLeidos = in.read(buffer, 0, (int) Math.min(buffer.length, tamaño - recibido))) != -1) {
                            fos.write(buffer, 0, bytesLeidos);
                            recibido += bytesLeidos;
                        }
                    }
                    System.out.println("¡Descarga completada!");
                } else {
                    System.out.println("El servidor respondió con un error: " + respuesta);
                }

                System.out.println("Introduce la ruta del archivo a buscar con nombre de este");
                ruta = scanner.nextLine();
            }

        } catch (Exception e) {
            System.err.println("No se puede establecer la conexion");
            e.printStackTrace();
            return;
        }
    }

    /**
     * Valida si la cadena proporcionada es una dirección IP válida (v4) o un nombre de host.
     *
     * @param ip Dirección IP o Hostname a validar.
     * @return {@code true} si el formato es correcto, {@code false} si no se puede resolver.
     */
    public static boolean esIpValida(String ip) {
        try {
            // Este método intenta resolver la cadena como una dirección IP
            InetAddress.getByName(ip);
            // Verificamos que sea formato numérico y no un nombre de dominio (como google.com)
            // Nota: Esta expresión regular es una validación básica de formato IPv4
            return ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$") || ip.contains(":");
        } catch (UnknownHostException e) {
            return false;
        }
    }
}