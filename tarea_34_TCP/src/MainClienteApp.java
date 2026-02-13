import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class MainClienteApp {
    public static void main(String[] args) {

        int puerto = 2121;
        String ip = "localhost";
        String nombreArchivo = "server.properties";
        Socket socketCliente = null;
        Scanner scanner = new Scanner(System.in);
        BufferedReader in = null;
        PrintWriter out = null;
        String mensaje = null;

        try (BufferedReader lector = new BufferedReader(new FileReader(nombreArchivo))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                System.out.println(linea);
                if (linea.startsWith("puerto")) {
                    String[] lineaPuerto = linea.trim().split("=");
                    puerto = Integer.parseInt(lineaPuerto[1]);
                }
            }
            if (args.length < 1) {
                System.err.println("Al introducir menos de 1 argumento se usará la ip por defecto");
            } else if (args.length == 1) {
                if (esIpValida(args[0])) {
                    ip = args[0];
                } else {
                    System.err.println("No has introducido una ip válida, se usará la por defecto");
                }
            } else {
                System.err.println("Al introducir más de 1 argumento se usará la ip por defecto");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            socketCliente = new Socket(ip, puerto);
            in = new BufferedReader(new InputStreamReader(socketCliente.getInputStream()));
            out = new PrintWriter(socketCliente.getOutputStream(), true);

            buclePrincipal:
            while (true) {
                System.out.println("Introduce uno de los siguientes comandos: \n" +
                        "- list + ruta\n" +
                        "- show + ruta\n" +
                        "- delete + ruta\n" +
                        "- quit\n"
                );
                mensaje = scanner.nextLine();

                out.println(mensaje);

                String respuesta = null;

                bucleSecundario:
                while (true) {
                    respuesta = in.readLine();

                    if (respuesta == null) {
                        break buclePrincipal;
                    }

                    System.out.println(respuesta);

                    if ("OK QUIT".equals(respuesta)) {
                        break buclePrincipal;
                    }

                    if("FIN".equals(respuesta)){
                        break bucleSecundario;
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean esIpValida(String ip) {
        try {
            InetAddress.getByName(ip);
            return ip.matches("^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$") || ip.contains(":");
        } catch (UnknownHostException e) {
            return false;
        }
    }
}