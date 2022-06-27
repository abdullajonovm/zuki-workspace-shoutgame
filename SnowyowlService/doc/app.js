var http = require('http');
var fs = require('fs');
var masterClientSocketId;
var primaryIdHashToSocket = [];
var socketToPrimaryIdHash = [];
var clientASocket;
var clientBSocket;

var server = http.createServer(function(req, res) {
    //fs.readFile('./index.html', 'utf-8', function(error, content) {
    //    res.writeHead(200, {"Content-Type": "text/html"});
    //    res.end(content);
    //});

    res.writeHead(200, {"Content-Type": "text/html"});
	res.end('<html><body></body></html>');
});

var io = require('socket.io').listen(server);

//handle incoming messages from clients
io.sockets.on('connection', function (socket) {

	//console.log('client is connected: ' + socket.id);

    socket.on('disconnect', function() {
		var primaryIdHash = socketToPrimaryIdHash[socket];
		socketToPrimaryIdHash[socket] = null;
		primaryIdHashToSocket[primaryIdHash] = null;
    });

    socket.on('set_as_controller', function(msg) {
        masterClientSocketId = socket.id;
        //console.log("Master socket is now: " + socket.id);
    });	

    socket.on('client_checkin', function(msg) {
		primaryIdHashToSocket[msg.primaryIdHash] = socket;
		socketToPrimaryIdHash[socket] = msg.primaryIdHash;
    });	

	/*
	   The msg param will be a json like so:
	   {
			"recipient": "[primaryIdHash]",
			"message": { [sync message] }
	   }
	*/
    socket.on('sync_message', function(msg) {
        if (socket.id != masterClientSocketId) {
            console.log('ignoring sync_message message from non-master client');
            return;
        }

		var primaryIdHash = msg.recipient;
		var clientSocket = primaryIdHashToSocket[primaryIdHash];
		if (clientSocket != null) {
			clientSocket.emit('sync_message', JSON.stringify(msg.message));
		}
    });	
});

server.listen(45730);
