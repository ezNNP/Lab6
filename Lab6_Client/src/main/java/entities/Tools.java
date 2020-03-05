package entities;

import java.io.Serializable;
import java.util.Objects;

abstract class Tools implements Serializable {

    private String consist = "";
    private String nameOfTool;

    public Tools() {

    }


    public Tools(String nameOfTool) {
        this.nameOfTool = nameOfTool;
    }

    abstract String dosomething(Tools tool);

    public String getConsist() {
        return consist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tools tools = (Tools) o;
        return Objects.equals(consist, tools.consist) &&
                Objects.equals(nameOfTool, tools.nameOfTool);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consist, nameOfTool);
    }

    @Override
    public String toString() {
        return "entities.Tools{" +
                "consist='" + consist + '\'' +
                ", nameOfTool='" + nameOfTool + '\'' +
                '}';
    }
}