import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import entities.Pen;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.LinkedList;
import java.util.Scanner;

public class Client {
    private DatagramChannel udpChannel;
    private InetSocketAddress inetSocketAddress;
    private int port;
    private Scanner scanner;

    private Client(String destinationAddr, int port) throws IOException {
        this.port = port;
        this.inetSocketAddress = new InetSocketAddress(InetAddress.getByName(destinationAddr), port);
        this.udpChannel = DatagramChannel.open();
        scanner = new Scanner(System.in);
    }

    public static void showUsage() {
        System.out.println("To run client properly you need to follow this syntax");
        System.out.println("java Client <host> <port>");
        System.exit(1);
    }

    public void testServerConnection() throws IOException {
        System.out.println("Подключение к серверу");
        for (int i = 1; i < 11; ++i) {
            System.out.println("Попытка №" + i);
            ByteBuffer response = ByteBuffer.allocate(8192);
            ByteBuffer request = createRequest("connecting", "");
            this.udpChannel.connect(inetSocketAddress);
            this.udpChannel.send(request, inetSocketAddress);
            try {
                this.udpChannel.receive(response);
            } catch (PortUnreachableException ignored) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
                this.udpChannel.disconnect();
                continue;
            }
            try(ByteArrayInputStream bais = new ByteArrayInputStream(response.array());
                ObjectInputStream ois = new ObjectInputStream(bais)) {
                Response responseObj = (Response) ois.readObject();
                if ((new String((byte[])responseObj.getResponse())).equals("connected")){
                    System.out.println("Подключение выполнено успешно");
                    return;
                } else {
                    this.udpChannel.disconnect();
                }
            } catch (Exception e) {
                System.err.println("Произошла ошибка");
                System.exit(0);
            }
        }
        System.out.println("Подключение не выполнено");
        System.exit(0);
    }

    private void start() throws IOException {
        System.out.println("Client started");
        System.out.print("Enter command > ");
        String input;
        String lastCommand = "";
        String addStr = "";
        boolean commandEnd = true;
        boolean correctCommand = false;
        int nestingJSON = 0;
        ByteBuffer request = null;

        while (!(input = scanner.nextLine().trim()).toLowerCase().equals("exit")) {
            if (!input.equals("")) {
                String command = input.split(" ")[0].toLowerCase();
                if (nestingJSON < 0) {
                    nestingJSON = 0;
                    lastCommand = "";
                    addStr = "";
                    commandEnd = true;
                }

                if (!commandEnd && (lastCommand.equals("add") || lastCommand.equals("remove"))) {

                    nestingJSON += charCounter(input, '{');
                    nestingJSON -= charCounter(input, '}');
                    correctCommand = true;
                    addStr += input;
                    if (nestingJSON == 0) {
                        commandEnd = true;

                    }

                } else if (command.equals("remove") && commandEnd) {

                    lastCommand = "remove";
                    commandEnd = false;
                    correctCommand = true;
                    addStr = input.substring(6).trim();
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
                } else if (lastCommand.equals("remove") && commandEnd && correctCommand) {
                    request = createRequest("remove", addStr);
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
                        this.udpChannel.send(request, inetSocketAddress);
                    } else {
                        throw new Exception();
                    }
                    ByteBuffer responseBuffer = ByteBuffer.allocate(8192);
                    try {
                        this.udpChannel.receive(responseBuffer);
                    } catch (PortUnreachableException e) {
                        System.err.println("Произошло отключение от хоста");
                        this.udpChannel.disconnect();
                        testServerConnection();
                    }

                    try(ByteArrayInputStream bais = new ByteArrayInputStream(responseBuffer.array());
                        ObjectInputStream ois = new ObjectInputStream(bais)) {
                        Response response = (Response) ois.readObject();
                        String output = new String(decodeResponse(lastCommand, response));
                        if (!output.equals("show")) {
                            System.out.println(output);
                        }
                    } catch (IOException e) {
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    System.err.println("Некорректные данные, попробуйте снова");
                }
                System.out.print("> ");
            } else if (commandEnd) {
                if (!input.trim().equals("")) {
                    System.err.println("Неизвестная команда");
                }
                System.out.print("> ");
            }
        }

    }

    private ByteBuffer createRequest(String command, String data) throws IOException {
        ByteBuffer send = ByteBuffer.allocate(8192);
        send.clear();
        byte[] sending;
        Command c = new Command(command, data);

        if (command.equals("add") || command.equals("remove")) {
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
            send.put(sending);
            send.flip();
            return send;
        } catch (IOException e) {
            throw new IOException();
        }
    }

    private byte[] decodeResponse(String command, Response response) {
        if (command.equals("show")) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream((byte[])response.getResponse());
                ObjectInputStream ois = new ObjectInputStream(bais)) {
                LinkedList<Pen> storage = (LinkedList<Pen>) ois.readObject();
                for (Pen pen : storage) {
                    System.out.println(pen.toString());
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
            e.printStackTrace();
            System.err.println("Произошел троллинг");
            showUsage();
        }
    }
}