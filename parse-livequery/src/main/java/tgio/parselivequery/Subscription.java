package tgio.parselivequery;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;
import tgio.parselivequery.interfaces.OnListener;

public class Subscription {

    private BaseQuery mBaseQuery;
    private List<Event> mEvents = new ArrayList<>();

    private rx.Subscription mRxSubscription;

    public Subscription(BaseQuery baseQuery) {
        mBaseQuery = baseQuery;
    }

    protected void subscribe() {
        LiveQueryClient.registerSubscription(this);
        listen();
    }

    public void unsubscribe() {
        LiveQueryClient.removeSubscription(this);
        if (!mRxSubscription.isUnsubscribed()) {
            mRxSubscription.unsubscribe();
        }
    }

    public boolean isSubscribed() {
        return !mRxSubscription.isUnsubscribed();
    }

    protected BaseQuery getQuery() {
        return mBaseQuery;
    }

    public void on(@NonNull final String op, @NonNull final OnListener listener) {
        mEvents.add(new Event(op, listener));
    }

    private void listen() {
        mRxSubscription = RxBus.subscribe(new Action1<LiveQueryEvent>() {
            @Override
            public void call(final LiveQueryEvent event) {
                for (Event ev : mEvents) {
                    if (ev.getOp().equals(LiveQueryEvent.ALL)) {
                        ev.getListener().on(event.object);
                    } else if (event.op.equals(ev.getOp())) {
                        ev.getListener().on(event.object);
                    }
                }
            }
        });
    }



}
