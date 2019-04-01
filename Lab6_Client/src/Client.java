import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.Vector;

public class Client {
    private DatagramSocket udpSocket;
    private InetAddress serverAddress;
    private int port;
    private Scanner scanner;
    private Client(String destinationAddr, int port) throws IOException {

        this.serverAddress = InetAddress.getByName(destinationAddr);
        this.port = port;
        this.udpSocket = new DatagramSocket();
        scanner = new Scanner(System.in);
    }

    public static void showUsage() {
        System.out.println("To run client properly you need to follow this syntax");
        System.out.println("java Client <host> <port>");
        System.exit(1);
    }

    public void testServerConnection() throws IOException {
        System.out.println("Trying to reach remote host...");
        DatagramPacket testRequest = createRequest("connecting", "");

        byte[] buf = new byte[256];
        DatagramPacket testResponse = new DatagramPacket(buf, buf.length);

        boolean connected = false;
        this.udpSocket.setSoTimeout(1000);
        String connectString;
        for (int i = 1; i < 11; i++) {
            System.out.println("Attempt " + i);
            this.udpSocket.send(testRequest);
            try {
                this.udpSocket.receive(testResponse);
            } catch (SocketTimeoutException e) {
                continue;
            }


            try(ByteArrayInputStream bais = new ByteArrayInputStream(buf);
                ObjectInputStream ois = new ObjectInputStream(bais)) {
                Response response = (Response) ois.readObject();
                connectString = new String(decodeResponse("connecting", response));
            } catch (IOException | ClassNotFoundException e) {
                connectString = "Not connected";
                e.printStackTrace();
            }

            //System.out.println(response);
            if (connectString.equals("connected")) {
                connected = true;
                break;
            }
        }

        if (connected) {
            System.out.println("Connection with server is established");
        } else {
            System.err.println("Server is unreachable at this moment");
            System.exit(1);
        }

    }

    private int start() throws IOException {
        System.out.println("Client started");
        System.out.print("Enter command > ");
        String input;
        String lastCommand = "";
        String addStr = "";
        boolean commandEnd = true;
        boolean correctCommand = false;
        int nestingJSON = 0;
        DatagramPacket request = null;

        while (!(input = scanner.nextLine().trim()).toLowerCase().equals("exit")) {
            if (!input.equals("")) {
                String command = input.split(" ")[0].toLowerCase();
                if (nestingJSON < 0) {
                    nestingJSON = 0;
                    lastCommand = "";
                    addStr = "";
                    commandEnd = true;
                }

                if (!commandEnd && (lastCommand.equals("add") || lastCommand.equals("add_if_min"))) {

                    nestingJSON += charCounter(input, '{');
                    nestingJSON -= charCounter(input, '}');
                    correctCommand = true;
                    addStr += input;
                    if (nestingJSON == 0) {
                        commandEnd = true;

                    }

                } else if (command.equals("add_if_min") && commandEnd) {

                    lastCommand = "add_if_min";
                    commandEnd = false;
                    correctCommand = true;
                    addStr = input.substring(10).trim();
                    nestingJSON += charCounter(addStr, '{');
                    nestingJSON -= charCounter(addStr, '}');
                    if (nestingJSON == 0) {
                        commandEnd = true;
                    }

                } else if (command.equals("add") && commandEnd) {

                    lastCommand = "add";
                    commandEnd = false;
                    correctCommand = true;
                    addStr = input.substring(3).trim();
                    nestingJSON += charCounter(addStr, '{');
                    nestingJSON -= charCounter(addStr, '}');
                    if (nestingJSON == 0) {
                        commandEnd = true;
                    }

                } else if (command.equals("show") && commandEnd) {
                    lastCommand = "show";
                    correctCommand = true;
                    request = createRequest("show", null);
                } else if (command.equals("save") && commandEnd) {
                    lastCommand = "save";
                    correctCommand = true;
                    request = createRequest("save", null);
                } else if (command.equals("import") && commandEnd) {
                    lastCommand = "import";
                    correctCommand = true;
                    request = createRequest("import", input.substring(6).trim());
                } else if (command.equals("info") && commandEnd) {
                    lastCommand = "info";
                    correctCommand = true;
                    request = createRequest("info", null);
                } else if (command.equals("remove") && commandEnd) {
                    lastCommand = "remove";
                    correctCommand = true;
                    request = createRequest("remove", input.substring(6).trim());
                } else if (command.equals("help") && commandEnd) {
                    lastCommand = "help";
                    correctCommand = true;
                    request = createRequest("help", null);
                } else {
                    correctCommand = false;
                    commandEnd = true;
                }

                if (lastCommand.equals("add") && commandEnd && correctCommand) {
                    request = createRequest("add", addStr);
                    addStr = "";
                    correctCommand = true;
                } else if (lastCommand.equals("add_if_min") && commandEnd && correctCommand) {
                    request = createRequest("add_if_min", addStr);
                    addStr = "";
                    correctCommand = true;
                }
            } else {
                if (commandEnd)
                    correctCommand = false;
            }

            if (commandEnd && correctCommand) {
                try {
                    if (request != null) {
                        this.udpSocket.send(request);
                    } else {
                        throw new Exception();
                    }
                    byte[] resp = new byte[8192];
                    DatagramPacket responsePacket = new DatagramPacket(resp, resp.length);
                    try {
                        this.udpSocket.receive(responsePacket);
                    } catch (SocketTimeoutException e) {
                        System.err.println("Disconnected from host");
                        testServerConnection();
                    }

                    try(ByteArrayInputStream bais = new ByteArrayInputStream(resp);
                        ObjectInputStream ois = new ObjectInputStream(bais)) {
                        Response response = (Response) ois.readObject();
                        String output = new String(decodeResponse(lastCommand, response));
                        if (!output.equals("show")) {
                            System.out.println(output);
                        }
                    } catch (IOException e) {

                    }

                } catch (Exception e) {
                    System.err.println("Некорректные данные, попробуйте снова");
                }
                System.out.print("> ");
            } else if (commandEnd) {
                if (!input.trim().equals("")) {
                    System.err.println("Unknown command");
                }
                System.out.print("> ");
            }
        }

        return 0;
    }

    private DatagramPacket createRequest(String command, String data) throws IOException {
        byte[] sending;
        Command c = new Command(command, data);

        if (command.equals("add") || command.equals("add_if_min")) {
            try {
                c.setData(Helper.getFromJson(data));
            } catch (Exception e) {
                return null;
            }
        } else if (command.equals("import")) {
            c.setData(Helper._import(data));
            if (c.getData() == null) {
                return null;
            }
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(outputStream)){
            oos.writeObject(c);
            oos.flush();
            sending = outputStream.toByteArray();
            sending = HexBin.encode(sending).getBytes();
            return new DatagramPacket(sending, sending.length, serverAddress, port);
        } catch (IOException e) {
            throw new IOException();
        }


    }

    private byte[] decodeResponse(String command, Response response) {
        if (command.equals("show")) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream((byte[])response.getResponse());
                ObjectInputStream ois = new ObjectInputStream(bais)) {
                Vector<Human> storage = (Vector<Human>) ois.readObject();
                for (Human human : storage) {
                    System.out.println(human.toString());
                }
            } catch (IOException | ClassNotFoundException e) {

            }
        } else {
            return (byte[])response.getResponse();
        }
        return "show".getBytes();
    }

    /**
     * <p>Считает количество символов в строке</p>
     *
     * @param in - Исходная строка
     * @param c - Символ, который мы ищем
     * @return Количество символов в строке
     */
    private int charCounter(String in, char c) {
        int count = 0;
        for (char current: in.toCharArray())
            if (current == c)
                count++;
        return count;
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            showUsage();
        }

        try {
            Client sender = new Client(args[0], Integer.parseInt(args[1]));
            System.out.println("-- Running UDP Client at " + InetAddress.getLocalHost() + " --");
            System.out.println("-- UDP client settings --");
            System.out.println("-- UDP connection to " + args[0] + " host --");
            System.out.println("-- UDP port " + args[1] + " --");
            sender.testServerConnection();
            sender.start();
        } catch (Exception e) {
            System.err.println("Произошел троллинг");
             showUsage();
        }
    }
}