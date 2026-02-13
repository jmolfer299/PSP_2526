import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainFileServerApp {
    public static void main(String[] args) {

        int puerto = 2121;
        String nombreArchivo = "server.properties";
        ServerSocket servidor = null;

        try (BufferedReader lector = new BufferedReader(new FileReader(nombreArchivo))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                System.out.println(linea);
                if (linea.contains("puerto")) {
                    String[] lineaPuerto = linea.trim().split("=");
                    puerto = Integer.parseInt(lineaPuerto[1]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ExecutorService pool = Executors.newFixedThreadPool(10);

        try {
            servidor = new ServerSocket(puerto);
            System.out.println("Servidor iniciado en puerto " + puerto);

            while (true) {
                Socket socket = servidor.accept();
                System.out.println("Cliente conectado");

                pool.execute(() -> {
                    try (Socket s = socket;
                         BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                         PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {

                        String mensaje;
                        while ((mensaje = in.readLine()) != null) {

                            if (mensaje.isEmpty()) {
                                System.out.println("Mensaje vacio, finalizando por seguridad");
                                break;
                            }
                            System.out.println("Solicitud recibida para: " + mensaje);

                            String ruta = null;
                            String respuesta = null;

                            if (mensaje.toLowerCase().startsWith("list")) {

                                ruta = mensaje.substring(5).trim();
                                respuesta = UtilsServer.fileLister(ruta);
                                String[] mensajeDividido = respuesta.split("\n");
                                for (String linea : mensajeDividido) {
                                    out.println(linea);
                                }
                                out.println("FIN");

                            } else if (mensaje.toLowerCase().startsWith("show")) {

                                ruta = mensaje.substring(4).trim();
                                respuesta = UtilsServer.fileShow(ruta);
                                String[] mensajeDividido = respuesta.split("\n");
                                for (String linea : mensajeDividido) {
                                    out.println(linea);
                                }
                                out.println("FIN");

                            } else if (mensaje.toLowerCase().startsWith("delete")) {

                                ruta = mensaje.substring(6).trim();
                                respuesta = UtilsServer.fileDelete(ruta);
                                String[] mensajeDividido = respuesta.split("\n");
                                for (String linea : mensajeDividido) {
                                    out.println(linea);
                                }
                                out.println("FIN");

                            } else if (mensaje.equalsIgnoreCase("quit")) {
                                out.println("OK QUIT");
                                break;
                            } else {
                                out.println("KO");
                            }
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        System.out.println("Cliente fuera");
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}