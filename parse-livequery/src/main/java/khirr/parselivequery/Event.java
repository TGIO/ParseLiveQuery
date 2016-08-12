package khirr.parselivequery;

import khirr.parselivequery.interfaces.OnListener;

public class Event {

    private OnListener mListener;
    private String mOp;

    public Event(String op, OnListener listener) {
        mOp = op;
        mListener = listener;
    }

    public OnListener getListener() {
        return mListener;
    }

    public String getOp() {
        return mOp;
    }
}