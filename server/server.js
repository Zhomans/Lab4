var sys = require('util');
var journey = require('journey');
my_http = require('http');
var fs = require('fs');

var privateKey = fs.readFileSync('../certs/privatekey.pem');
var certificate = fs.readFileSync('../certs/certificate.pem');

var options = {
	key: privateKey,
	cert: certificate
};

path = require("path");
url = require("url");
filesys = require("fs");

my_http.createServer(function(request,response){
	sys.puts("I got kicked");
	response.writeHeader(200,{"Content-Type":"text/plain"});
	response.write("Hello World");
	response.end();
}).listen(8080);
sys.puts("Server Running on 8080");

