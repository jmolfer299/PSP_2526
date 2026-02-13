import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Servidor de archivos concurrente TCP.
 * <p>
 * Utiliza un {@link ExecutorService} para manejar múltiples conexiones de clientes de forma paralela.
 * Implementa un protocolo simple de petición-respuesta usando {@link DataInputStream} y {@link DataOutputStream}.
 * </p>
 */
public class MainFileServerApp {

    /**
     * Método principal que inicia el servidor.
     *
     * @param args Argumentos de línea de comandos.
     * <br>args[0]: (Opcional) Puerto de escucha.
     * @throws UnknownHostException Si hay errores relacionados con la dirección de host.
     */
    public static void main(String[] args) throws UnknownHostException {

        ServerSocket servidor = null;
        int port = 4321;

        // Configuración del puerto mediante argumentos
        if(args.length == 1){
            try{
                port = Integer.parseInt(args[0]);
            } catch (Exception e) {
                System.err.println("Debes introducir un puerto valido");
                return;
            }
        } else if (args.length > 1) {
            System.err.println("Debes introducir o no solo el puerto");
            return;
        }

        // Pool de hilos para manejar hasta 10 clientes simultáneos
        ExecutorService pool = Executors.newFixedThreadPool(10);
        String respuesta = null;

        try {
            servidor = new ServerSocket(port);
            System.out.println("Servidor iniciado en puerto " + port);

            while(true) {
                // Bloqueante: espera conexión de cliente
                Socket socket = servidor.accept();
                System.out.println("Cliente conectado");

                // Delegar la gestión del cliente a un hilo del pool
                pool.execute(() -> {
                    // Usamos try-with-resources para asegurar el cierre de flujos y socket
                    try (DataInputStream in = new DataInputStream(socket.getInputStream());
                         DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                         Socket s = socket) {

                        while (true) {
                            try {
                                // 1. Esperamos el siguiente mensaje (nombre del archivo)
                                String mensaje = in.readUTF();

                                // Protocolo de salida limpia
                                if (mensaje.equalsIgnoreCase("SALIR")) {
                                    break;
                                }

                                System.out.println("Solicitud recibida para: " + mensaje);
                                File archivo = new File(mensaje);

                                // 2. Procesamos la petición
                                if (archivo.exists() && archivo.isFile()) {
                                    // Respuesta positiva: OK + Tamaño + Bytes
                                    out.writeUTF("OK \n\r");
                                    out.writeLong(archivo.length());
                                    out.flush(); // Importante enviar cabecera antes del contenido
                                    enviarFichero(archivo, out);
                                } else {
                                    // Respuesta negativa
                                    out.writeUTF("KO\n\r");
                                    out.flush();
                                }

                            } catch (EOFException e) {
                                System.out.println("El cliente ha terminado la conexión.");
                                break;
                            }
                        }

                    } catch (IOException e) {
                        System.err.println("Error en la conexión con cliente: " + e.getMessage());
                    }
                });
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Lee un archivo del disco y lo escribe en el flujo de salida del socket.
     *
     * @param fichero Objeto {@link File} que apunta al archivo físico.
     * @param out     Flujo de salida {@link OutputStream} (generalmente del socket).
     * @throws IOException Si ocurre un error de lectura o escritura.
     */
    private static void enviarFichero(File fichero, OutputStream out) throws IOException {
        try (FileInputStream fis = new FileInputStream(fichero)) {
            byte[] buffer = new byte[8192]; // Buffer de 8KB para transferencia eficiente
            int leidos;
            while ((leidos = fis.read(buffer)) != -1) {
                out.write(buffer, 0, leidos);
            }
            out.flush();
            System.out.println("Fichero enviado con éxito.");
        }
    }
}