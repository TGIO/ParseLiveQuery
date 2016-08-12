package khirr.parselivequery.example;

import com.parse.Parse;

import rx.functions.Action1;
import khirr.parselivequery.LiveQueryClient;
import khirr.parselivequery.RxBus;

/**
 * Created by pro on 16-07-08.
 */
public class Application extends android.app.Application {
    public static final String WS_URL = "ws://192.168.0.31:4040/";
    public static final String MY_APP_ID = "myAppId";
    public static String SERVER = "http://192.168.0.31:1337/parse/";
    public static String CLIENT_KEY = "2ead5328dda34e688816040a0e78948a";

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId(MY_APP_ID)
                .server(SERVER)
                .clientKey(CLIENT_KEY)
                .build()
        );

        LiveQueryClient.init(WS_URL, MY_APP_ID);

        RxBus.subscribe(new Action1() {
            @Override
            public void call(Object o) {

            }
        });
    }
}
