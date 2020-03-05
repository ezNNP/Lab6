package entities;

import java.io.Serializable;

public class Pen extends Tools implements Comparable<Pen>, Serializable {
    private String name = " Чёрная ручка,";
    private double ink;
    private Paper paper;

    public Pen(Paper paper) {
        this.paper=paper;
    }

    public Pen(String name) {
        this.name = name;
        paper = null;
    }

    public Pen() {
        ink = 0.0;
        paper = null;
    }

    @Override
    String dosomething(Tools tool) {
        return null;
    }

    public double fill() {
        this.ink = 2;
        return ink;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getInk() {
        return ink;
    }

    public void setInk(double ink) {
        this.ink = ink;
    }

    public Paper getPaper() {
        return paper;
    }

    public void setPaper(Paper paper) {
        this.paper = paper;
    }

    public String draft() {
        return ("писать ");
    }

    @Override
    public int compareTo(Pen o) {
        return o.name.compareTo(name);
    }

    @Override
    public String toString() {
        return "entities.Pen{" +
                "name='" + name + '\'' +
                ", ink=" + ink +
                ", paper=" + paper +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Pen pen = (Pen) o;

        if (Double.compare(pen.ink, ink) != 0) return false;
        if (!name.equals(pen.name)) return false;
        return paper != null ? paper.equals(pen.paper) : pen.paper == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        result = 31 * result + name.hashCode();
        temp = Double.doubleToLongBits(ink);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (paper != null ? paper.hashCode() : 0);
        return result;
    }
}