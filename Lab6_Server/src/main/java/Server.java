import entities.Pen;
import modules.CommandHandler;
import modules.ConnectionListener;
import modules.Sender;

import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class Server {

    private DatagramSocket udpSocket;
    private LinkedList<Pen> storage;
    private String filename;


    public Server(int port) throws IOException {
        this.udpSocket = new DatagramSocket(port);
        System.out.println("Server is running on");
        System.out.println("address: " + InetAddress.getLocalHost());
        System.out.println("port: " + port);

    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void loadCollection() {
        System.out.println("Initializing collection...");
        try {
            storage = Loaders.loadXML(Loaders.readStrings(filename));
            CommandHandler.fileName = filename;
        } catch (FileNotFoundException e) {
            System.err.println("Backup file not found");
            System.exit(1);
        }
    }

    private void listen() throws Exception {
        System.out.println("-- Running Server at " + InetAddress.getLocalHost() + " --");

        ConnectionListener listener = new ConnectionListener(udpSocket);
        CommandHandler handler = new CommandHandler();
        Sender sender = new Sender(udpSocket);

        while (true) {
            DatagramPacket in = listener.receive();
            DatagramPacket out = handler.handleCommand(in, storage);
            sender.sendBack(out);
        }
    }

    public static void showUsage() {
        System.out.println("To run server properly follow this syntax");
        System.out.println("java Server <port> <path to backup file>");
        System.exit(1);
    }

    public static void main(String[] args) throws Exception {
        int input_port = -1;
        if (args.length == 0) {
            showUsage();
        }

        try {
            input_port = Integer.parseInt(args[0]);
        } catch (IllegalArgumentException e) {
            showUsage();
        }

        Server server = new Server(input_port);

        if (args.length == 2) {
            server.setFilename(args[1]);
        }
        server.loadCollection();
        server.listen();
    }
}