package tgio.parselivequery.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import rx.functions.Action1;
import tgio.parselivequery.Constants;
import tgio.parselivequery.LiveQueryClient;
import tgio.parselivequery.LiveQueryEvent;
import tgio.parselivequery.RxBus;
import tgio.parselivequery.queries.BaseQuery;

public class MainActivity extends AppCompatActivity {
    TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resultView = (TextView) findViewById(R.id.resultView);


        LiveQueryClient.connect();

        RxBus.subscribe(new Action1<LiveQueryEvent>() {
            @Override
            public void call(final LiveQueryEvent event) {
                if (event.op.equals(Constants.CONNECTED)) {
                    LiveQueryClient.executeQuery(
                            new BaseQuery.Builder(Constants.SUBSCRIBE, "Message")
                                    .where("text", "asd")
                                    .addField("text")
                                    .build());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultView.append(event.object.toString() + "\n");
                    }
                });
            }
        });
    }
}
