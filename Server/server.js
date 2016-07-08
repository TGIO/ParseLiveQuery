var express = require('express');
var ParseServer = require('parse-server').ParseServer;
var ParseDashboard = require('parse-dashboard');

var app = express();

var api = new ParseServer({
    databaseURI: 'mongodb://localhost:27017/dev', // Connection string for your MongoDB database
    appId: 'myAppId',
    masterKey: 'myMasterKey', // Keep this key secret!
    fileKey: 'optionalFileKey',
    serverURL: 'http://localhost:1337/parse', // Don't forget to change to https if needed
    liveQuery: {
        classNames: ['Message']
    }
});

var dashboard = new ParseDashboard({
    "apps": [
        {
            "serverURL": "http://localhost:1337/parse",
            "appId": "myAppId",
            "masterKey": "myMasterKey",
            "appName": "MyApp"
        }
    ]
});

// Serve the Parse API on the /parse URL prefix
app.use('/parse', api);
app.use('/dashboard', dashboard);

var httpServer = require('http').createServer(app);
httpServer.listen(4040);
var parseLiveQueryServer = ParseServer.createLiveQueryServer(httpServer);

app.listen(1337, function() {
    console.log('parse-server-example running on port 1337.');
});
