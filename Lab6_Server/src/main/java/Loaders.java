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
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

public class Loaders {
    private Loaders() {}

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

    public static LinkedList<Pen> loadXML(String xmlStr) {
        LinkedList<Pen> pens = new LinkedList<>();
        try {

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
            System.err.println("Да хватит уже");
        } catch (Exception e) {
            System.err.println("Неверный формат XML");
        }
        return pens;
    }
}
