package khirr.parselivequery;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.ws.WebSocket;
import okhttp3.ws.WebSocketCall;
import okhttp3.ws.WebSocketListener;
import okio.Buffer;
import rx.functions.Action1;
import khirr.parselivequery.interfaces.OnListener;

public class LiveQueryClient {
    static final int INFINITE = 0;

    static String baseUrl;
    static String applicationId;
    static WebSocket webSocket;
    static boolean autoConnect = false;
    static boolean isOpened = false;
    static boolean isConnected = false;
    static int lastRequestID = -1;

    public static LiveQueryClient instance;
    private ArrayList<Event> mEvents = new ArrayList<>();

    private static ArrayList<Subscription> mSubscriptions = new ArrayList<>();

    LiveQueryClient (String _baseUrl, String _applicationId){
        baseUrl = _baseUrl;
        applicationId = _applicationId;
        OkHttpClient client = new OkHttpClient()
                .newBuilder()
                .readTimeout(INFINITE, TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(baseUrl)
                .build();
        WebSocketCall.create(client, request).enqueue(webSocketListener);

        // Trigger shutdown of the dispatcher's executor so this process can exit cleanly.
        client.dispatcher().executorService().shutdown();

        //  Listen events
        listenEvents();
    }

    private static LiveQueryClient getInstance(){
        if(instance == null) {
            instance = new  LiveQueryClient(baseUrl, applicationId);
        }
        return instance;

    }

    public static void init(String _baseUrl, String _applicationId) {
        baseUrl = _baseUrl;
        applicationId = _applicationId;
        getInstance();
    }

    public static int getNewRequestId(){
        lastRequestID++;
        return lastRequestID;
    }

    private void connectInternal(){
        if(!isOpened) {
            autoConnect = true;
            return;
        }
        try {
            webSocket.sendMessage(RequestBody.create(WebSocket.TEXT, getConnectMessage()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void connect() {
        getInstance().connectInternal();
    }

    private void executeQueryInternal(String query) {
        if (webSocket != null) {
            try {
                webSocket.sendMessage(RequestBody.create(WebSocket.TEXT, query));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected static void executeQuery(BaseQuery baseQuery) {
        getInstance().executeQueryInternal(baseQuery.toString());
    }

    protected static void executeQuery(String query) {
        getInstance().executeQueryInternal(query);
    }

    static String getConnectMessage() {
        return String.format("{ \"op\": \"%s\", \"applicationId\": \"%s\" }", "connect", applicationId);
    }

    private static synchronized boolean isConnected() {
        return isConnected;
    }

    private static synchronized void setIsConnected(boolean connected) {
        isConnected = connected;
    }


    WebSocketListener webSocketListener = new WebSocketListener() {
        @Override
        public void onOpen(WebSocket _webSocket, Response response) {
            isOpened = true;
            if (_webSocket != null) {
                webSocket = _webSocket;
                if (autoConnect) {
                    connect();
                }
            }
        }

        @Override
        public void onFailure(IOException e, Response response) {
            e.printStackTrace();
        }

        @Override
        public void onMessage(ResponseBody responseBody) throws IOException {
            try {
                JSONObject jsonObject = new JSONObject(responseBody.string());
                @BaseQuery.op String op = jsonObject.optString(Constants.OP);
                RxBus.broadCast(new LiveQueryEvent(op, jsonObject));
                //  Create server subscriptions
                if (op.equals(LiveQueryEvent.CONNECTED)) {
                    setIsConnected(true);
                    registerExistingSubscriptions();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onPong(Buffer buffer) {

        }

        @Override
        public void onClose(int i, String s) {

        }
    };

    // Connect && Disconnect events
    public static void on(String op, OnListener listener) {
        getInstance().mEvents.add(new Event(op, listener));
    }

    private void listenEvents() {
        RxBus.subscribe(new Action1<LiveQueryEvent>() {
            @Override
            public void call(final LiveQueryEvent event) {
                for (Event ev : mEvents) {
                    if (event.op.equals(ev.getOp())) {
                        ev.getListener().on(event.object);
                    }
                }
            }
        });
    }

    //  Register subscriptions
    public static void registerSubscription(Subscription subscription) {
        mSubscriptions.add(subscription);
        if (isConnected()) {
            executeQuery(subscription.getQuery());
        }
    }

    //  Remove subscription
    public static void removeSubscription(Subscription subscription) {
        mSubscriptions.remove(subscription);
        LiveQueryClient.executeQuery(subscription.getQuery().unsubscribeQueryToString());
    }

    //  Register existing subscription after server connection
    private synchronized void registerExistingSubscriptions() {
        for (Subscription subscription : mSubscriptions) {
            executeQuery(subscription.getQuery());
        }
    }

}
