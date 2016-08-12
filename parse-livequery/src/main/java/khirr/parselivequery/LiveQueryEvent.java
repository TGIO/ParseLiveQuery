package khirr.parselivequery;

import org.json.JSONObject;

/**
 * Created by pro on 16-07-08.
 */
public class LiveQueryEvent {
    public @BaseQuery.op String op;
    public JSONObject object;

    public LiveQueryEvent(String op, JSONObject object) {
        this.op = op;
        this.object = object;
    }
}
