package modules;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class Sender {

    private DatagramSocket socket;

    public Sender(DatagramSocket socket) {
        this.socket = socket;
    }

    public Sender(int port) {
        try {
            this.socket = new DatagramSocket(port);
        } catch (SocketException e) {
            System.err.println("Ошибка при открытии сокета");
        }

    }

    public void sendBack(DatagramPacket packet) {
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("Возникла ошибка при отправке пакета");
        }
    }
}
