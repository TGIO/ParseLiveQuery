## Parse LiveQuery Implementation for Android
Very simple and modern implementation, it lacks tests and extra functionality at this moment but i'm working on it.

#### Import guide

```
dependencies {
    compile 'com.github.tgio:parse-livequery:1.0.1'
}
```

#### Server-Setup

1. Make sure u have node and npm installed.
2. cd Server && npm install
3. node server.js


#### Usage


  ```java
//Do initialization, for example in App.java
LiveQueryClient.init(WS_URL, MY_APP_ID);

//Connect
LiveQueryClient.connect();

//Subscribe for parse object "Message" where "text" equals "asd" and include "text" field in response

RxBus.subscribe(new Action1<LiveQueryEvent>() {
    @Override
    public void call(final LiveQueryEvent event) {
        if (event.op.equals(Constants.CONNECTED)) {
            LiveQueryClient.executeQuery(
                            new BaseQuery.Builder(Constants.SUBSCRIBE, "Message")
                            .where("text", "asd")
                            .addField("text")
                            .build());
        } else {
            System.out.println(event.object.toString());
        }
    }
});

  ```