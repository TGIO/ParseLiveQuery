Deprecated, please use https://github.com/parse-community/ParseLiveQuery-Android

[![Jit Pack](https://img.shields.io/badge/JitPack-ParseLiveQUery-green.svg)](https://jitpack.io/#tgio/ParseLiveQuery/-SNAPSHOT)
## Parse LiveQuery Implementation for Android
Simple ParseLiveQuery with subscribe, unsubscribe and listen events.
Based on [ParseLiveQuery](https://github.com/TGIO/ParseLiveQuery)

#### Import guide

```
dependencies {
    compile 'com.github.tgio:parse-livequery:1.0.3'
}
```

#### Server-Setup

1. Make sure u have node and npm installed.
2. cd Server && npm install
3. node server.js


#### Usage


  ```java
//Do initialization, for example in App.java
LiveQueryClient.init(WS_URL, MY_APP_ID, true);

//Connect
LiveQueryClient.connect();

//Subscribe for parse object "Message" where "body" equals "asd" and include "body" field in response

//  Subscription
final Subscription subscription = new BaseQuery.Builder("Message")
        .where("body", "asd")
        .addField("body")
        .build()
        .subscribe();

//  Listen
subscription.on(LiveQueryEvent.CREATE, new OnListener() {
    @Override
    public void on(final JSONObject object) {
        Log.e("CREATED", object.toString());
    }
});

//  Unsubscribe
//subscription.unsubscribe();

  ```
  
#### Contributors
[Khirr] (https://github.com/khirr)
