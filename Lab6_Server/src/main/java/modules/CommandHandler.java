package modules;

import entities.Paper;
import entities.Pen;
import messages.Command;
import messages.Response;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CommandHandler {

    private InetAddress inetAddress;
    private int port;
    public static String fileName;

    public CommandHandler() {

    }


    public DatagramPacket handleCommand(DatagramPacket in, LinkedList<Pen> storage) {
        synchronized (CommandHandler.class) {
            Command com;
            try (ByteArrayInputStream bais = new ByteArrayInputStream(in.getData());
                 ObjectInputStream ois = new ObjectInputStream(bais);) {
                com = (Command) ois.readObject();
            } catch (Exception e) {
                System.err.println("Ошибка при получении пакета");
                return null;
            }
            String command = com.getCommand();
            Object data = com.getData();

            byte[] buffer = null;

            switch (command.toLowerCase()) {
                case "connecting":
                    buffer = "connected".getBytes();
                    break;
                case "show":
                    buffer = show(storage);
                    break;
                case "save":
                    buffer = save(storage);
                    break;
                case "add":
                    buffer = add(storage, (Pen) data);
                    break;
                case "import":
                    buffer = _import(storage, (LinkedList<Pen>) data);
                    break;
                case "info":
                    buffer = info(storage);
                    break;
                case "remove":
                    buffer = remove(storage, (Pen) data);
                    break;
                case "remove_first":
                    buffer = remove_first(storage);
                    break;
                case "remove_last":
                    buffer = remove_last(storage);
                    break;
                case "help":
                    buffer = help();
                    break;
                default:
                    buffer = "Незиветная команда, попробуйте еще раз".getBytes();
            }
            Response response = new Response(buffer);
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                oos.writeObject(response);
                oos.flush();
                byte[] output = baos.toByteArray();
                return new DatagramPacket(output, output.length, in.getAddress(), in.getPort());
            } catch (Exception e) {
                System.err.println("Возникла ошибка при сериализации ответа");
                return null;
            }

        }
    }

    /**
     * <p>Показывает все данные, содержащиеся в коллекции</p>
     *
     * @param storage - ссылка на коллекцию с объектами
     */
    public byte[] show(LinkedList<Pen> storage) {

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(outputStream)){
            oos.writeObject(storage);
            oos.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            System.err.println("Произошла ошибка");
        }

        return null;

    }

    public byte[] save(LinkedList<Pen> storage) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = document.createElement("world");
            document.appendChild(root);
            for (Pen pen: storage) {
                Element element = document.createElement("pen");

                Element name = document.createElement("name");
                name.appendChild(document.createTextNode(pen.getName()));
                element.appendChild(name);

                Element ink = document.createElement("ink");
                ink.appendChild(document.createTextNode(String.valueOf(pen.getInk())));
                element.appendChild(ink);


                if (pen.getPaper() != null) {
                    Element paper = document.createElement("paper");

                    Paper hat = pen.getPaper();

                    Element size = document.createElement("size");
                    size.appendChild(document.createTextNode(String.valueOf(hat.getSize())));

                    Element height = document.createElement("height");
                    height.appendChild(document.createTextNode(String.valueOf(hat.getHeight())));

                    Element wide = document.createElement("wide");
                    wide.appendChild(document.createTextNode(String.valueOf(hat.getWide())));

                    paper.appendChild(size);
                    paper.appendChild(height);
                    paper.appendChild(wide);

                    element.appendChild(paper);
                }

                root.appendChild(element);
            }
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            DOMSource dom = new DOMSource(document);

            StringWriter sw = new StringWriter();
            StreamResult sr = new StreamResult(sw);
            transformer.transform(dom, sr);
            String xmlOutputString = sw.toString();

            File outputXml = new File(fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(outputXml);
            fileOutputStream.write(xmlOutputString.getBytes(StandardCharsets.UTF_8));

            fileOutputStream.flush();
            fileOutputStream.close();

        } catch (Exception e) {
            System.err.println("Невозможно записать в файл, проверьте права доступа.");
            return "Коллекция не была сохранена".getBytes();
        }
        sortCollection(storage);
        return "Коллекция сохранена".getBytes();
    }

    public byte[] add(LinkedList<Pen> storage, Pen pen) {
        boolean exist = false;

        for (Pen current: storage) {
            if (current.equals(pen)) {
                exist = true;
                break;
            }
        }

        if (!exist) {
            storage.add(pen);
            sortCollection(storage);
            return "Element successfully added".getBytes();
        } else {
            return "Duplicate element".getBytes();
        }
    }

    public byte[] _import(LinkedList<Pen> storage, LinkedList<Pen> importing) {
        for (Pen pen: importing) {
            add(storage, pen);
        }

        return "Команда import выполнена".getBytes();
    }

    public byte[] info(LinkedList<Pen> storage) {
        return ("Информация о коллекции\n" +
                "Тип коллекции: " + storage.getClass() + "\n" +
                "Количество элементов в коллекции: " + storage.size()).getBytes();
    }

    public byte[] remove(LinkedList<Pen> storage, Pen pen) {
        for (Pen current : storage) {
            if (current.equals(pen)) {
                storage.remove(current);
                return "Элемент удален".getBytes();
            }
        }
        return "Элемент не был найден".getBytes();
    }

    public byte[] remove_first(LinkedList<Pen> storage) {
        if (storage.size() > 0) {
            storage.removeFirst();
            return "Первый элемент удален".getBytes();
        } else {
            return "Количество элементов в коллекции меньше одного, невозможно ничего удалить".getBytes();
        }
    }

    public byte[] remove_last(LinkedList<Pen> storage) {
        if (storage.size() > 0) {
            storage.removeLast();
            return "Последний элемент удален".getBytes();
        } else {
            return "Количество элементов в коллекции меньше одного, невозможно ничего удалить".getBytes();
        }
    }

    /**
     * <p>Выводит информацию о всех доступных командах</p>
     */
    public byte[] help() {

        return ("Доступные команды:" +
                "\nadd {element} - добавляет элемент в коллекцию, element - строка в формате json" +
                "\nВозможные записи комады add:" +
                "\n- {\"name\": \"test\", \"ink\": 1.0, \"paper\": {\"size\": 3, \"height\": 3, \"wide\": 3}}" +
                "\n- {\"name\": \"test\", \"ink\": 1.0}" +
                "\n- {\"name\": \"test\"}" +
                "\nshow - выводит список всех элементов коллекции" +
                "\nsave - сохраняет текущую в исходный файл" +
                "\nimport {path} - добавляет в коллекцию все элементы из файла в формате json, path - путь до .json файла" +
                "\ninfo - выводит информацию о коллекции" +
                "\nremove {element} - удаляет элемент из коллекции, element - строка json, запись элементов как в команде add" +
                "\nremove_first - удаляет первый элемент из коллекции" +
                "\nremove_last - удаляет последний элемент из коллекции" +
                "\nhelp - выводит список доступных команд" +
                "\nexit - выход из программы").getBytes();
    }

    /**
     * <p>Сортирует коллекцию</p>
     *
     * @param storage - ссылка на коллекцию с объектами
     */
    private void sortCollection(LinkedList<Pen> storage) {
        Collections.sort(storage);
    }
}