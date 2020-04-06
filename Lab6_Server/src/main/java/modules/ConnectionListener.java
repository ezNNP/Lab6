package modules;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ConnectionListener {

    private DatagramSocket udpSocket;

    public ConnectionListener(int port) {
        try {
            this.udpSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.err.println("Возникла ошибка при открытии сокета");
        }
    }

    public ConnectionListener(DatagramSocket socket) {
        this.udpSocket = socket;
    }

    public DatagramPacket receive() {
        byte[] input = new byte[8192];
        DatagramPacket in = new DatagramPacket(input, input.length);
        try {
            this.udpSocket.receive(in);
            return in;
        } catch (IOException e) {
            System.err.println("Ошибка при получении пакета");
            return null;
        }
    }
}
