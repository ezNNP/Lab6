import java.io.Serializable;

public class Command implements Serializable {
    private String command;
    private Object data;

    public Command(String command) {
        this.command = command;
        this.data = "";
    }

    public Command(String command, String data) {
        this.command = command;
        this.data = data;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Command{" +
                "command='" + command + '\'' +
                ", data=" + data +
                '}';
    }
}
