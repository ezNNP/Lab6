package factories;

import entities.Paper;
import entities.Pen;

import java.util.Map;

public class PenFactory {
    /**
     * <p>создает entities.Pen из принимаемой коллекции параметров </p>
     * @param penParams коллекция параметров
     * @return entities.Pen
     * @throws Exception
     */
    public static Pen newInstance(Map<String, Object> penParams) throws Exception {
        if (penParams.containsKey("name")) {
            Pen pen = new Pen();
            pen.setName(penParams.get("name").toString());
            if (penParams.containsKey("ink")) {
                try {
                    pen.setInk(Double.parseDouble(penParams.get("ink").toString()));
                } catch (NumberFormatException e) {
                    pen.setInk((int) Math.round((double) penParams.get("ink")));
                }
            }
            if (penParams.containsKey("paper")) {
                pen.setPaper((Paper) penParams.get("paper"));
            }
            return pen;
        } else {
            throw new Exception();
        }
    }
}