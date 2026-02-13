package main;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Scanner;

/**
 * Clase que implementa un cliente DNS sencillo utilizando el protocolo UDP.
 * <p>
 * Este cliente se conecta a un servidor local en el puerto 2222 para resolver
 * nombres de dominio. Permite al usuario introducir dominios por consola de manera
 * interactiva y muestra la dirección IP devuelta por el servidor.
 * </p>
 *
 * @author JoseM
 * @version 1.0
 */
public class ClienteDns {

    /**
     * Constructor por defecto del cliente DNS.
     */
    public ClienteDns() {
        // Constructor vacío implícito
    }

    /**
     * Método principal que ejecuta la lógica del cliente UDP.
     * <p>
     * El flujo de ejecución es el siguiente:
     * </p>
     * <ol>
     * <li>Se crea un {@code DatagramSocket} para la comunicación.</li>
     * <li>Se establece un bucle infinito para solicitar dominios al usuario.</li>
     * <li>Se envía el nombre del dominio al servidor (localhost:2222) mediante un {@code DatagramPacket}.</li>
     * <li>Se espera y recibe la respuesta del servidor con la IP resuelta.</li>
     * <li>El bucle termina si el usuario introduce una cadena vacía.</li>
     * </ol>
     *
     * @param args Argumentos de la línea de comandos (no utilizados en esta aplicación).
     */
    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress ipServidor = InetAddress.getByName("localhost");
            int puertoServidor = 2222;
            Scanner sc = new Scanner(System.in);

            while (true) {
                System.out.print("Introduce dominio a consultar: ");
                String dominio = sc.nextLine();

                if (dominio.trim().isEmpty()) {
                    break;
                }

                byte[] dataEnvio = dominio.getBytes();
                DatagramPacket envio = new DatagramPacket(
                        dataEnvio,
                        dataEnvio.length,
                        ipServidor,
                        puertoServidor
                );

                socket.send(envio);

                byte[] buffer = new byte[1024];
                DatagramPacket recepcion = new DatagramPacket(buffer, buffer.length);
                socket.receive(recepcion);

                String ipRespuesta = new String(recepcion.getData(), 0, recepcion.getLength());
                System.out.println("Respuesta del servidor: " + ipRespuesta);
            }

            socket.close();
            sc.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}