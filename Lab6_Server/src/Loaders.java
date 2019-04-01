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

    /**
     * <p>Возвращает vector по строке xml</p>
     *
     * @param xmlStr - Строка xml
     * @return vector где каждый элемент - объект типа human
     *
     */
    public static Vector<Human> loadXML(String xmlStr) {
        Vector<Human> humans = new Vector<>();
        try {

            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = docBuilder.parse(new ByteArrayInputStream(xmlStr.getBytes(StandardCharsets.UTF_8)));
            NodeList humanList = doc.getElementsByTagName("human");
            for (int i = 0; i < humanList.getLength(); i++) {
                HashMap<String, Object> hParams = new HashMap<>();
                Element humanElement = (Element) humanList.item(i);

                NodeList currentList = humanElement.getElementsByTagName("name");
                String hName = currentList.item(0).getFirstChild().getNodeValue();
                currentList = humanElement.getElementsByTagName("age");
                int hAge = Integer.parseInt(currentList.item(0).getFirstChild().getNodeValue());

                currentList = humanElement.getElementsByTagName("tall");
                long hTall = Long.parseLong(currentList.item(0).getFirstChild().getNodeValue());

                NodeList hatList = humanElement.getElementsByTagName("cloth");
                if (hatList.getLength() > 0) {
                    for (int j = 0; j < hatList.getLength(); j++) {
                        HashMap<String, Object> hatParams = new HashMap<>();
                        Element hatElement = (Element) hatList.item(j);
                        NodeList currentHatList = hatElement.getElementsByTagName("diametr");
                        String diametr = currentHatList.item(0).getFirstChild().getNodeValue();
                        currentHatList = hatElement.getElementsByTagName("height");
                        String height = currentHatList.item(0).getFirstChild().getNodeValue();
                        currentHatList = hatElement.getElementsByTagName("hatType");
                        String hatType = currentHatList.item(0).getFirstChild().getNodeValue();

                        hatParams.put("hatType", hatType);
                        hatParams.put("diametr", diametr);
                        hatParams.put("height", height);

                        hParams.put("cloth", HatFactory.newInstance(hatParams));
                    }
                }

                currentList = humanElement.getElementsByTagName("charism");
                int hCharism = Integer.parseInt(currentList.item(0).getFirstChild().getNodeValue());
                currentList = humanElement.getElementsByTagName("headDiametr");
                float hHeadDiametr = Float.parseFloat(currentList.item(0).getFirstChild().getNodeValue());

                currentList = humanElement.getElementsByTagName("x");
                int hX = Integer.parseInt(currentList.item(0).getFirstChild().getNodeValue());
                currentList = humanElement.getElementsByTagName("y");
                int hY = Integer.parseInt(currentList.item(0).getFirstChild().getNodeValue());

                hParams.put("name", hName);
                hParams.put("age", hAge);
                hParams.put("tall", hTall);
                hParams.put("charism", hCharism);
                hParams.put("headDiametr", hHeadDiametr);
                hParams.put("x", hX);
                hParams.put("y", hY);
                humans.add(HumanFactory.newInstance(hParams));

            }
        } catch (ParserConfigurationException e) {
            System.err.println("Ух ты, вы сломали парсер, написанный отцами-основателями");
        } catch (SAXException | IOException e) {
            System.err.println("Да хватит уже");
        } catch (Exception e) {
            System.err.println("Неверный формат XML");
        }
        return humans;
    }
}
