import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Vector;

public class Server {

    private DatagramChannel udpChannel;
    private int port;
    private Vector<Human> storage;
    private String filename;


    public Server(int port) throws IOException {
        this.port = port;
        this.udpChannel = DatagramChannel.open().bind(new InetSocketAddress("192.168.3.24", 9090));
        System.out.println("Server is running on");
        System.out.println("address: " + InetAddress.getLocalHost());
        System.out.println("port: " + this.port);

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
        String input;

        CommandHandler handler;

        while (true) {
            // Receiving udp package
            ByteBuffer buffer = ByteBuffer.allocate(8192);
            buffer.clear();
            InetSocketAddress clientAddress = (InetSocketAddress) udpChannel.receive(buffer);

            // Decoding udp package
            input = new String(buffer.array()).trim();
            byte[] request = HexBin.decode(input);

            Command command;

            try (ByteArrayInputStream bais = new ByteArrayInputStream(request);
                 ObjectInputStream ois = new ObjectInputStream(bais);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                command = (Command) ois.readObject();
                System.out.println("- Client input: " + command.getCommand());

                // creating response for client
                /*Thread thread = new Thread(() -> {

                });*/
                handler = new CommandHandler();
                handler.start();
                Response response = new Response(handler.handleCommand(command, storage));

                oos.writeObject(response);
                oos.flush();
                buffer.clear();
                buffer.put(baos.toByteArray());
                buffer.flip();

                udpChannel.send(buffer, clientAddress);
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