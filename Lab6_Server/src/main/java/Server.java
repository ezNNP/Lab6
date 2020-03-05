import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import entities.Pen;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.LinkedList;
import java.util.Vector;

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

        CommandHandler handler;

        while (true) {
            byte[] input = new byte[8192];
            DatagramPacket in = new DatagramPacket(input, input.length);
            this.udpSocket.receive(in);

            Command command;

            try (ByteArrayInputStream bais = new ByteArrayInputStream(input);
                 ObjectInputStream ois = new ObjectInputStream(bais);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                command = (Command) ois.readObject();
                System.out.println("- Client input: " + command.getCommand());

                handler = new CommandHandler();
                Response response = new Response(handler.handleCommand(command, storage));

                oos.writeObject(response);
                oos.flush();
                byte[] output = baos.toByteArray();
                DatagramPacket out = new DatagramPacket(output, output.length, in.getAddress(), in.getPort());
                udpSocket.send(out);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
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