package factories;

import entities.Paper;

import java.util.Map;

/**
 * <p></p>
 *
 */
public class PaperFactory {
    /**
     * <p></p>
     * @param paperParams
     * @return
     */
    public static Paper newInstance(Map<String, Object> paperParams) {
        Paper paper = new Paper();
        if (paperParams.containsKey("height")) {
            try {
                paper.setHeight(Integer.parseInt(paperParams.get("height").toString()));
            } catch (NumberFormatException e) {
                paper.setHeight((double)paperParams.get("height"));
            }
        }
        if(paperParams.containsKey("size")){
            try {
                paper.setSize(Integer.parseInt(paperParams.get("size").toString()));
            }catch (NumberFormatException e){
                paper.setSize((double)paperParams.get("size"));
            }
        }

        if(paperParams.containsKey("wide")){
            try {
                paper.setWide(Integer.parseInt(paperParams.get("wide").toString()));
            }catch (NumberFormatException e){
                paper.setWide((double)paperParams.get("wide"));
            }
        }
        return paper;
    }
}