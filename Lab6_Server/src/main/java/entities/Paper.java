package entities;

import java.io.Serializable;

public class Paper implements Serializable {
    private double size;
    private double height;
    private double wide;

    public double getWide() {
        return wide;
    }

    public void setWide(double wide) {
        this.wide = wide;
    }

    public double getSize() {
        return size;
    }

    public void setSize(double size) {
        this.size = size;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "entities.Paper{" +
                "size=" + size +
                ", height=" + height +
                ", wide=" + wide +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Paper paper = (Paper) o;

        if (size != paper.size) return false;
        if (height != paper.height) return false;
        return wide == paper.wide;
    }

    @Override
    public int hashCode() {
        int result = (int) size;
        result = (int) (31 * result + height);
        result = (int) (31 * result + wide);
        return result;
    }
}