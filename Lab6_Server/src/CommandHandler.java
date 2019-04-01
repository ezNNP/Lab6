import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class CommandHandler extends Thread {

    private InetAddress inetAddress;
    private int port;
    public static String fileName;

    public CommandHandler(InetAddress inetAddress, int port) {
        this.inetAddress = inetAddress;
        this.port = port;
    }

    public CommandHandler() {

    }

    @Override
    public void run() {
    }



    /**
     * <p>Ищет исполняемую команду и исполняет её</p>
     * @param command - команда
     * @param storage - ссылка на коллекцию с объектами
     * @param data - специальная строка, которая может понадобиться командеH
     */
    //public DatagramPacket handleCommand(String command, Vector<Human> storage, String data) {
    public Object handleCommand(Command com, Vector<Human> storage) {
        synchronized (CommandHandler.class) {
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
                    buffer = add(storage, (Human) data);
                    break;
                case "add_if_min":
                    buffer = add_if_min(storage, (Human) data);
                    break;
                case "import":
                    buffer = _import(storage, (Vector<Human>) data);
                    break;
                case "info":
                    buffer = info(storage);
                    break;
                case "remove":
                    buffer = remove(storage, (String) data);
                    break;
                case "help":
                    buffer = help();
                    break;
                default:
                    buffer = "Незиветная команда, попробуйте еще раз".getBytes();
            }
            //return new DatagramPacket(buffer, buffer.length, inetAddress, port);
            return buffer;
        }
    }

    /**
     * <p>Показывает все данные, содержащиеся в коллекции</p>
     *
     * @param storage - ссылка на коллекцию с объектами
     */
    public byte[] show(Vector<Human> storage) {

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

    /**
     * <p>Сохраняет данные работы программы в файл исходник</p>
     *
     * @param storage - ссылка на коллекцию с объектами
     */
    public byte[] save(Vector<Human> storage) {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = document.createElement("world");
            document.appendChild(root);
            for (Human human: storage) {
                Element hElement = document.createElement("human");

                Element hName = document.createElement("name");
                hName.appendChild(document.createTextNode(human.getName()));
                hElement.appendChild(hName);

                Element hAge = document.createElement("age");
                hAge.appendChild(document.createTextNode(String.valueOf(human.getAge())));
                hElement.appendChild(hAge);

                Element hTall = document.createElement("tall");
                hTall.appendChild(document.createTextNode(String.valueOf(human.getTall())));
                hElement.appendChild(hTall);

                if (human.getCloth() != null) {
                    Element hCloth = document.createElement("cloth");
                    Element cHat = document.createElement("hat");

                    Hat hat = (Hat)human.getCloth();

                    Element hDiametr = document.createElement("diametr");
                    hDiametr.appendChild(document.createTextNode(String.valueOf(hat.getDiametr())));

                    Element hHeight = document.createElement("height");
                    hHeight.appendChild(document.createTextNode(String.valueOf(hat.getHeight())));

                    Element hHatType = document.createElement("hatType");
                    hHatType.appendChild(document.createTextNode(String.valueOf(hat.getHatType())));

                    cHat.appendChild(hDiametr);
                    cHat.appendChild(hHeight);
                    cHat.appendChild(hHatType);

                    hCloth.appendChild(cHat);
                    hElement.appendChild(hCloth);
                }

                Element hCharism = document.createElement("charism");
                hCharism.appendChild(document.createTextNode(String.valueOf(human.getCharism())));
                hElement.appendChild(hCharism);

                Element hHeadDiametr = document.createElement("headDiametr");
                hHeadDiametr.appendChild(document.createTextNode(String.valueOf(human.getHeadDiametr())));
                hElement.appendChild(hHeadDiametr);

                Element hX = document.createElement("x");
                hX.appendChild(document.createTextNode(String.valueOf(human.getX())));
                hElement.appendChild(hX);

                Element hY = document.createElement("y");
                hY.appendChild(document.createTextNode(String.valueOf(human.getY())));
                hElement.appendChild(hY);
                root.appendChild(hElement);
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

        } catch (ParserConfigurationException e) {
            System.err.println("Парсеру, как бы Вам намекнуть, кабзда. Обратитесь в службу поддержки");
            return "Collection has not been saved".getBytes();
        } catch (TransformerConfigurationException e) {
            System.err.println("Как можно здесь словить исключение я не понимаю");
            return "Collection has not been saved".getBytes();
        } catch (TransformerException e) {
            System.err.println("При записи в строку произошла ошибка");
            return "Collection has not been saved".getBytes();
        } catch (FileNotFoundException e) {
            System.err.println("Не найден файл для записи, что очень странно, видимо меня решили добить.\nПоместите его обратно, туда где он был.");
            return "Collection has not been saved".getBytes();
        } catch (IOException e) {
            System.err.println("Невозможно записать в файл, проверьте права доступа.");
            return "Collection has not been saved".getBytes();
        }
        sortCollection(storage);
        return "File successfully saved".getBytes();
    }

    /**
     * <p>Добавляет элемент в коллекцию</p>
     *
     * @param storage - ссылка на коллекцию с объектами
     * @param json - строка формата json
     */
    public byte[] add(Vector<Human> storage, Human human) {
        boolean exist = false;

        for (Human current: storage) {
            if (current.getName().toLowerCase().equals(human.getName().toLowerCase())) {
                exist = true;
                break;
            }
        }

        if (!exist) {
            storage.add(human);
            sortCollection(storage);
            return "Element successfully added".getBytes();
        } else {
            return "Duplicate element".getBytes();
        }
    }

    /**
     * <p>Добавляет элемент в коллекцию если он является уникальным</p>
     *
     * @param storage - ссылка на коллекцию с объектами
     * @param json - строка формата json
     */
    public byte[] add_if_min(Vector<Human> storage, Human human) {
        if (storage.size() > 0) {
            Human min = storage.stream().min(Human::compareTo).get();
            if (human.compareTo(min) < 0) {
                return add(storage, human);
            } else {
                return "This element is not minimal".getBytes();
            }
        } else {
            return add(storage, human);
        }
    }

    /**
     * <p>Импортирует все объекты из заданного json файла</p>
     *
     * @param storage - ссылка на коллекцию с объектом
     * @param pathToFile - путь до файла json
     */
    public byte[] _import(Vector<Human> storage, Vector<Human> importing) {
        for (Human human: importing) {
            add(storage, human);
        }

        return "Команда import выполнена".getBytes();
    }

    /**
     * <p>Выводит информацию о коллекции</p>
     *
     * @param storage - ссылка на коллекцию с объектами
     */
    public byte[] info(Vector<Human> storage) {
        return ("Информация о коллекции\n" +
                "Тип коллекции: " + storage.getClass() + "\n" +
                "Количество элементов в коллекции: " + storage.size()).getBytes();
    }

    /**
     * <p>Удаляет элемент из коллекции</p>
     *
     * @param storage - ссылка на коллекцию с объектами
     * @param name - уникальное имя объекта
     */
    public byte[] remove(Vector<Human> storage, String name) {
        for (Human human: storage) {
            if (human.getName().toLowerCase().equals(name.toLowerCase())) {
                System.out.println("Удален человек по имени \"" + human.getName() + "\"");
                storage.remove(human);
                return ("Удален человек по имени \"" + human.getName() + "\"").getBytes();
            }
        }

        return"Такого объекта не было найдено".getBytes();
    }

    /**
     * <p>Выводит информацию о всех доступных командах</p>
     */
    public byte[] help() {

        return ("Доступные команды:" +
                "\nadd {element} - добавляет элемент в коллекцию, element - строка в формате json" +
                "\nshow - выводит список всех элементов коллекции" +
                "\nsave - сохраняет текущую в исходный файл" +
                "\nimport {path} - добавляет в коллекцию все элементы из файла в формате json, path - путь до .json файла" +
                "\ninfo - выводит информацию о коллекции" +
                "\nremove {name} - удаляет элемент из коллекции, name - уникальное имя" +
                "\nadd_if_min {element} - добавляет элемент в коллекцию если он минимальный, element - строка в формате json" +
                "\nhelp - выводит список доступных команд").getBytes();
    }

    /**
     * <p>Сортирует коллекцию</p>
     *
     * @param storage - ссылка на коллекцию с объектами
     */
    private static void sortCollection(Vector<Human> storage) {
        Collections.sort(storage);
    }
}