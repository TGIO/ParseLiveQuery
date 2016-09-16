package khirr.parselivequery;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
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

    static final String CLASS_NAME = "LiveQueryClient";

    static String baseUrl;
    static String applicationId;
    static WebSocket webSocket;
    static boolean isOpened = false;
    static boolean isConnected = false;
    static int lastRequestID = -1;

    static boolean autoReConnect = false;

    public static LiveQueryClient instance;
    private ArrayList<Event> mEvents = new ArrayList<>();

    private static ArrayList<Subscription> mSubscriptions = new ArrayList<>();

    private static ScheduledExecutorService mScheduleTaskExecutor;

    private static String mSessionToken;

    LiveQueryClient (String _baseUrl, String _applicationId) {
        baseUrl = _baseUrl;
        applicationId = _applicationId;
        //  Listen events
        listenEvents();
    }

    private void connectToServer() {
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
    }

    private static LiveQueryClient getInstance() {
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

    public static void init(String _baseUrl, String _applicationId, boolean _autoReConnect) {
        baseUrl = _baseUrl;
        applicationId = _applicationId;
        autoReConnect = _autoReConnect;
        getInstance();
    }

    public static void init(String _baseUrl, String _applicationId, String _sessionToken, boolean _autoReConnect) {
        baseUrl = _baseUrl;
        applicationId = _applicationId;
        autoReConnect = _autoReConnect;
        mSessionToken = _sessionToken;
        getInstance();
    }

    public static void connect() {
        if (isConnected()) {
            Log.i(CLASS_NAME, CLASS_NAME + " is already connected");
            return;
        }
        getInstance().connectToServer();
    }

    public static void disconnect() {
        destroyConnection();
        getInstance().removeTryToReConnect();
        Log.i(CLASS_NAME, CLASS_NAME + " disconnected");
    }

    private static void destroyConnection() {
        try {
            if (webSocket != null) {
                webSocket.close(1000, "Connection closed");
            }
            webSocket = null;
            setIsConnected(false);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getNewRequestId(){
        lastRequestID++;
        return lastRequestID;
    }

    private void connectInternal() {
        try {
            webSocket.sendMessage(RequestBody.create(WebSocket.TEXT, getConnectMessage()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void validateConnection() {
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

    public static synchronized boolean isConnected() {
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
                validateConnection();
            }
        }

        @Override
        public void onFailure(IOException e, Response response) {
            e.printStackTrace();
            destroyConnection();
            tryToReConnect();
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

    //  Reconnection
    private synchronized void tryToReConnect() {
        if (!autoReConnect) return;
        mScheduleTaskExecutor = Executors.newSingleThreadScheduledExecutor();
        mScheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    connectToServer();
                    removeTryToReConnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 3, 3, TimeUnit.SECONDS);
    }

    private void removeTryToReConnect() {
        if (mScheduleTaskExecutor != null) {
            try {
                mScheduleTaskExecutor.shutdownNow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //  Token
    protected static String getSessionToken() {
        return mSessionToken;
    }

}
