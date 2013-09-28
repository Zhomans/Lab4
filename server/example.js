//module dependencies

var express = require('express')
  , http = require('http')
  , path = require('path')
  , mongojs = require('mongojs')
  , MongoStore = require('connect-mongo')(express);

var app = express(), db;

app.configure(function () {
  db = mongojs(process.env.MONGOLAB_URI || 'data', ['data']);
  app.set('port', 3000);
  app.set('views', __dirname + '/views');
  app.set('view engine', 'jade');
  app.set('secret', process.env.SESSION_SECRET || 'terrible, terrible secret')
  app.use(express.favicon());
  app.use(express.logger('dev'));
  app.use(express.bodyParser());
  app.use(express.methodOverride());
  app.use(express.cookieParser(app.get('secret')));
  app.use(app.router);
  app.use(express.static(path.join(__dirname, 'public')));
});

app.configure('development', function () {
  app.set('host', '0:0:0:0:3000');
  app.use(express.errorHandler());
});

// routes

app.get('/', function (req, res) {
  res.redirect('https://github.com/sihrc/MobileProto-Lab4');
})

app.get('/data', function (req, res) {
  var query = { };  
  db.data.find(query);
});

app.post('/putdata', function (req, res) {
  if (req.body.user) {
    db.data.save({
      latitude: req.body.latitude,
      longitude: req.body.longitude,
      user: req.body.user
    }, res.redirect.bind(res, '/'));
  } else {
    res.json({error: true, message: 'Invalid quote'}, 500);
  }
})

/**
* Launch
*/

http.createServer(app).listen(app.get('port'), function(){
  console.log("Express server listening on http://" + app.get('host'));
});
