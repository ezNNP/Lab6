import java.io.Serializable;

public class Human extends Creature implements Comparable<Human>, Serializable {
	
	private String name;
	private int age;
	private long tall;
	private Cloth cloth;
	private int charism;
	private float headDiametr;
	private int x;
	private int y;
	private long timeID;

	@Override
	public int compareTo(Human o) {
		return name.compareTo(o.name);
	}

	public Human(String name, int x, int y, int age) {
		super(name, x, y, 3, Fear.CALM);
		this.name = name;
		this.age = age;
		this.tall = 10;
		this.cloth = null;
		this.charism = 2;
		this.headDiametr = 20.0F;
		this.x = x;
		this.y = y;
		this.timeID = System.currentTimeMillis();
	}

	public Human(String name, int x, int y, int speed, int age, long tall, int charism, Fear fear, float headDiametr, Cloth cloth) {
		super(name, x, y, speed, fear);
		this.name = name;
		this.age = age;
		this.tall = tall;
		this.cloth = cloth;
		this.charism = charism;
		this.headDiametr = headDiametr;
		this.x = x;
		this.y = y;
		this.timeID = System.currentTimeMillis();
	}

	public Human(String name, int x, int y, int speed, int age, long tall, int charism, Fear fear, float headDiametr) {
		super(name, x, y, speed, fear);
		this.name = name;
		this.age = age;
		this.tall = tall;
		this.cloth = null;
		this.charism = charism;
		this.headDiametr = headDiametr;
		this.x = x;
		this.y = y;
		this.timeID = System.currentTimeMillis();
	}

	public Human(String name, int x, int y, int age, Cloth cloth) {
		super(name, x, y, 3, Fear.CALM);
		this.name = name;
		this.age = age;
		this.tall = 10;
		this.cloth = cloth;
		this.headDiametr = 20.0F;
		this.charism = 2;
		this.x = x;
		this.y = y;
		this.timeID = System.currentTimeMillis();
	}

	public Human(String name) {
		super(name, 0, 0, 1, Fear.CALM);
		this.cloth = null;
		this.age = 10;
		this.name = name;
		this.headDiametr = 20.0F;
		this.charism = 2;
		this.tall = 10;
		this.x = 0;
		this.y = 0;
		this.timeID = System.currentTimeMillis();
	}

	public void putOnCloth() {
		if (cloth != null) {
			cloth.putOn(this);
		} else {
            System.out.println("У вас нет одежды");
        }
	}

	public void takeOffCloth() {
		if (cloth != null) {
			cloth.takeOff(this);
		} else {
            System.out.println("У вас и так не одежды");
        }
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public long getTall() {
		return tall;
	}

	public void setTall(long tall) {
		this.tall = tall;
	}

	public Cloth getCloth() {
		return cloth;
	}

	public void setCloths(Cloth cloth) {
		this.cloth = cloth;
	}

	public int getCharism() {
		return charism;
	}

	public void setCharism(int charism) {
		this.charism = charism;
	}

	public float getHeadDiametr() {
		return headDiametr;
	}

	public void setHeadDiametr(float headDiametr) {
		this.headDiametr = headDiametr;
	}

	public void setCloth(Cloth cloth) {
		this.cloth = cloth;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}

	public long getTimeID() {
		return timeID;
	}

	public void setTimeID(long timeID) {
		this.timeID = timeID;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Human human = (Human) o;

		if (age != human.age) return false;
		if (tall != human.tall) return false;
		if (charism != human.charism) return false;
		if (Float.compare(human.headDiametr, headDiametr) != 0) return false;
		if (!name.equals(human.name)) return false;
		return cloth.equals(human.cloth);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + age;
		result = 31 * result + (int) (tall);
		result = 31 * result + (cloth != null ? cloth.hashCode() : 0);
		result = 31 * result + charism;
		result = 31 * result + (Math.round(headDiametr));
		return result;
	}

	@Override
	public String toString() {
		if (cloth != null)
			return "Human{" +
					"name='" + name + '\'' +
					", age=" + age +
					", tall=" + tall +
					", cloth=" + cloth +
					", charism=" + charism +
					", headDiametr=" + headDiametr +
					'}';
		else
			return "Human{" +
					"name='" + name + '\'' +
					", age=" + age +
					", tall=" + tall +
					", charism=" + charism +
					", headDiametr=" + headDiametr +
					'}';
	}
}