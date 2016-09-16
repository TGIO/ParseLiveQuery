package khirr.parselivequery;

import org.json.JSONObject;

public class LiveQueryEvent {

    public static final String CONNECTED = Constants.CONNECTED;
    public static final String SUBSCRIBED = Constants.SUBSCRIBED;
    public static final String CREATE = Constants.CREATE;
    public static final String ENTER = Constants.ENTER;
    public static final String UPDATE = Constants.UPDATE;
    public static final String LEAVE = Constants.LEAVE;
    public static final String DELETE = Constants.DELETE;
    public static final String ALL = Constants.ERROR;

    public @BaseQuery.op String op;
    public JSONObject object;

    public LiveQueryEvent(String op, JSONObject object) {
        this.op = op;
        this.object = object;
    }
}
