package tgio.parselivequery;

import org.json.JSONObject;

import tgio.parselivequery.queries.BaseQuery;

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
