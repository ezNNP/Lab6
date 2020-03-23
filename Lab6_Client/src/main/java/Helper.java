import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import entities.Paper;
import entities.Pen;
import factories.PaperFactory;
import factories.PenFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Helper {

    private Helper() {}

    public static Pen getFromJson(String json) throws Exception {
        Gson gson = new Gson();
        LinkedTreeMap<String, Object> map;
        try {
            Type type = new TypeToken<LinkedTreeMap<String, Object>>(){}.getType();
            map = gson.fromJson(json, type);

            if (map.containsKey("paper")) {
                LinkedTreeMap<String, Object> paperMap = (LinkedTreeMap<String, Object>) map.get("paper");
                Paper paper = PaperFactory.newInstance(paperMap);
                map.remove("paper");
                map.put("paper", paper);
            }
            return PenFactory.newInstance(map);
        } catch (Exception e) {
            System.err.println("Возникла ошибка при создании объекта, проверьте вашу json строку");
            throw new Exception();
        }
    }

    /**
     * <p>Импортирует все объекты из заданного json файла</p>
     *
     * @param pathToFile - путь до файла json
     */
    public static LinkedList<Pen> _import(String pathToFile) {
        LinkedList<Pen> pens = new LinkedList<>();
        try {
            Scanner scanner = new Scanner(new File(pathToFile));
            StringBuilder xmlStringBuilder = new StringBuilder();
            while (scanner.hasNextLine()) {
                xmlStringBuilder.append(scanner.nextLine());
            }
            String xmlStr = xmlStringBuilder.toString();
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.parse(new ByteArrayInputStream(xmlStr.getBytes(StandardCharsets.UTF_8)));
            NodeList humanList = doc.getElementsByTagName("pen");
            for (int i = 0; i < humanList.getLength(); i++) {
                HashMap<String, Object> penParams = new HashMap<>();
                Element humanElement = (Element) humanList.item(i);

                NodeList currentList = humanElement.getElementsByTagName("name");
                String name = currentList.item(0).getFirstChild().getNodeValue();
                currentList = humanElement.getElementsByTagName("ink");
                double ink = Double.parseDouble(currentList.item(0).getFirstChild().getNodeValue());


                NodeList hatList = humanElement.getElementsByTagName("paper");
                if (hatList.getLength() > 0) {
                    for (int j = 0; j < hatList.getLength(); j++) {
                        Map<String, Object> paperParams = new HashMap<>();
                        Element hatElement = (Element) hatList.item(j);
                        NodeList currentHatList = hatElement.getElementsByTagName("size");
                        double size = Double.parseDouble(currentHatList.item(0).getFirstChild().getNodeValue());
                        currentHatList = hatElement.getElementsByTagName("height");
                        double height = Double.parseDouble(currentHatList.item(0).getFirstChild().getNodeValue());
                        currentHatList = hatElement.getElementsByTagName("wide");
                        double wide = Double.parseDouble(currentHatList.item(0).getFirstChild().getNodeValue());

                        paperParams.put("size", size);
                        paperParams.put("height", height);
                        paperParams.put("wide", wide);

                        penParams.put("paper", PaperFactory.newInstance(paperParams));
                    }
                }

                penParams.put("name", name);
                penParams.put("ink", ink);
                pens.add(PenFactory.newInstance(penParams));

            }
        } catch (ParserConfigurationException e) {
            System.err.println("Ух ты, вы сломали парсер, написанный отцами-основателями");
        } catch (SAXException | IOException e) {
            System.err.println("Произошлая ошибка при чтении XML");
        } catch (Exception e) {
            System.err.println("Неверный формат XML");
        }
        return pens;
    }

    /**
     * <p>Читает файл</p>
     *
     * @param path - Путь до файла
     * @return массив строк, который содержит все символы файла
     *
     */
    public static String readStrings(String path) throws FileNotFoundException {
        String in = "";

        File file = new File(path);
        if (file.exists()) {
            try (BufferedInputStream fileInput = new BufferedInputStream(new FileInputStream(file))) {
                String currentLine = "";
                int i;
                while ((i = fileInput.read()) != -1) {
                    char c = (char) i;
                    in += c;
                }
            } catch (FileNotFoundException e) {
                throw new FileNotFoundException();
            } catch (IOException e) {
                System.err.println("Ой-ой-ой");
            }
        } else {
            throw new FileNotFoundException();
        }

        return in.trim();
    }
}
