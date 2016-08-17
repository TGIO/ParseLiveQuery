[![Jit Pack](https://img.shields.io/badge/JitPack-ParseLiveQUery-green.svg)](https://jitpack.io/#khirr/ParseLiveQuery/-SNAPSHOT)
## Parse LiveQuery Implementation for Android
Simple ParseLiveQuery with subscribe, unsubscribe and listen events.
Based on [ParseLiveQuery](https://github.com/TGIO/ParseLiveQuery)

#### Import guide

```
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    compile 'com.github.khirr:ParseLiveQuery:e1a4d67cd2'
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

//  Subscription
final Subscription subscription = new BaseQuery.Builder("Message")
        .where("text", "asd")
        .addField("text")
        .build()
        .subscribe();

//  Listen
subscription.on(Subscription.CREATE, new OnListener() {
    @Override
    public void on(final JSONObject object) {
        Log.e("CREATED", object.toString());
    }
});

//  Unsubscribe
//subscription.unsubscribe();

  ```
