package messages;

import java.io.Serializable;

public class Response implements Serializable {
    private Object response;

    public Response(Object response) {
        this.response = response;
    }

    public Object getResponse() {
        return response;
    }

}
