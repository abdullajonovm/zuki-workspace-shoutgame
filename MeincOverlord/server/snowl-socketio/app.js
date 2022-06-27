var http = require('http');
var fs = require('fs');
var masterClientSocketId;
var twitchClientSocketId;
var primaryIdHashToSocket = {};
var socketToPrimaryIdHash = {};

var primaryIdHashToMonitor = '105bc0e0e7f8659e5e95fe28ba5680b5c592d75e49cddba6f6b2e198f7b0e2d4';

var server = http.createServer(function(req, res) {
    res.writeHead(200, {"Content-Type": "text/html"});
    res.end('<html><body></body></html>');
});

var io = require('socket.io').listen(server);

//handle incoming messages from clients
io.sockets.on('connection', function (socket) {

    //console.log('client is connected: ' + socket.id);

    socket.on('disconnect', function() {
        //console.log('client is disconnected: ' + socket.id);

        try {
            var primaryIdHash = socketToPrimaryIdHash[socket.id];

            if (primaryIdHashToMonitor === primaryIdHash) {
                console.log('monitored client has disconnected');
            } else {
                console.log('client has disconnected. socket.id: ' + socket.id);
            }

            //remove the reverse lookup
            delete socketToPrimaryIdHash[socket.id];

            //remove from the list of clients 
            var clientsForThisHash = primaryIdHashToSocket[primaryIdHash];
            var index=-1;
            if (clientsForThisHash) {
                for (i=0; i<clientsForThisHash.length; i++) {
                    if (socket.id == clientsForThisHash[i].id) {
                        index = i;
                        break;
                    }
                }
            }
            if (index != -1) {
                clientsForThisHash.splice(index, 1);
            }

            //if there are no other connected clients, remove the entry
            if (clientsForThisHash && clientsForThisHash.length == 0) {
                delete primaryIdHashToSocket[primaryIdHash];
            }
        } catch (err) {
            console.log('ERR while disconnecting: ' + err);
        }
    });

    socket.on('set_as_controller', function(msg) {
        //TODO cryptographically secure this
        masterClientSocketId = socket.id;
        console.log('controller socket is now: ' + socket.id);
    });

    socket.on('twitch_client_checkin', function(primaryIdHash) {
        //TODO: cryptographically secure this
        twitchClientSocketId = socket.id
        console.log('twitch socket is now: ' + socket.id);
    });

    socket.on('client_checkin', function(primaryIdHash) {
        try {
            //grab (create if necessary) the list of sockets currently connected for this primaryIdHash
            var clientsForThisHash = primaryIdHashToSocket[primaryIdHash];
            if (clientsForThisHash == null) {
                clientsForThisHash = [];
                primaryIdHashToSocket[primaryIdHash] = clientsForThisHash;
            }

            //add the new socket
            clientsForThisHash.push(socket);

            //add the reverse lookup (for disconnect later on)
            socketToPrimaryIdHash[socket.id] = primaryIdHash;

            if (primaryIdHashToMonitor === primaryIdHash) {
                console.log('monitored client has checked in (socket.id: ' + socket.id + ')');
            }
            
            
            //console.log('client_checkin, socket: ' + socket.id + ', primaryIdHash: ' + primaryIdHash);
        } catch (err) {
            console.log('ERR during client_checkin: ' + err);
        }
    });


    socket.on('send_twitch_message', function(msg) {
        if (socket.id !== masterClientSocketId) {
            console.log('ignoring send_twitch_message message from non-controller client: ' + socket.id);
            return;
        }

        if (twitchClientSocketId) {
            twitchClientSocketId.emit('twitch_message', msg);
        }
    });

    /*
       The msg param will be a json like so:
       {
           "recipient": "[primaryIdHash]",
           "message": { [sync message] }
       }
    */
    socket.on('send_sync_message', function(msg) {
        if (socket.id !== masterClientSocketId) {
            console.log('ignoring send_sync_message message from non-controller client: ' + socket.id);
            return;
        }

        try {
            var jsonMsg = JSON.parse(msg);

            var primaryIdHash = jsonMsg.recipient;

            if (primaryIdHashToMonitor === primaryIdHash) {
                console.log('emitting sync_message to monitored client: ' + jsonMsg.message.messageType);
            }

            //console.log('emitting sync_message: ' + jsonMsg.messageType + ', to: ' + primaryIdHash);

            var clientsForThisHash = primaryIdHashToSocket[primaryIdHash];
            if (clientsForThisHash == null) {
                clientsForThisHash = [];
            }

            for (i=0; i<clientsForThisHash.length; i++) {
                var clientSocket = clientsForThisHash[i];
                clientSocket.emit('sync_message', JSON.stringify(jsonMsg.message));
            }
        } catch (err) {
            console.log('ERR during send_sync_message: ' + err);
        }
    });

    socket.on('send_current_rank', function(msg) {
        if (socket.id !== masterClientSocketId) {
            console.log('ignoring send_current_rank message from non-controller client: ' + socket.id);
            return;
        }

        var jsonMsg = JSON.parse(msg);

        var primaryIdHash = jsonMsg.recipient;

        if (primaryIdHashToMonitor === primaryIdHash) {
            console.log('emitting current_rank to monitored client');
        }

        //console.log('emitting current_rank: ' + jsonMsg.messageType + ', to: ' + primaryIdHash);

        var clientsForThisHash = primaryIdHashToSocket[primaryIdHash];
        if (clientsForThisHash == null) {
            clientsForThisHash = [];
        }

        for (i=0; i<clientsForThisHash.length; i++) {
            var clientSocket = clientsForThisHash[i];
            clientSocket.emit('current_rank', JSON.stringify(jsonMsg.message));
        }
    });

    socket.on('send_arbitrary_msg', function(msg) {
        if (socket.id != masterClientSocketId) {
            console.log('ignoring send_arbitrary_msg message from non-controller client: ' + socket.id);
            return;
        //} else {
        //    console.log('processing send_sync_message message from controller client');
        }

        var jsonMsg = JSON.parse(msg);
        var primaryIdHash = jsonMsg.recipient;
        //console.log('received send_arbitrary_msg: ' + msg + ", to: " + primaryIdHash);

        var clientsForThisHash = primaryIdHashToSocket[primaryIdHash];
        if (clientsForThisHash == null) {
            clientsForThisHash = [];
        }

        for (i=0; i<clientsForThisHash.length; i++) {
            var clientSocket = clientsForThisHash[i];
            clientSocket.emit('arbitrary_message', JSON.stringify(jsonMsg.message));
        }
    });

    socket.on('send_playercount', function(msg) {
        if (socket.id !== masterClientSocketId) {
            console.log('ignoring send_playercount message from non-controller client: ' + socket.id);
            return;
        }

        try {
            for (key in primaryIdHashToSocket) {
                if (primaryIdHashToMonitor === key) {
                    console.log('emitting player_count to monitored client: ' + JSON.parse(msg).count);
                }                
                
                var clientsForThisHash = primaryIdHashToSocket[key];
                if (clientsForThisHash == null) {
                    clientsForThisHash = [];
                }

                for (i=0; i<clientsForThisHash.length; i++) {
                    var clientSocket = clientsForThisHash[i];
                    if (clientSocket != null) {
                        clientSocket.emit('player_count', msg);
                    } 
                }
            }

        } catch (err) {
            console.log('ERR during send_playercount: ' + err);
        }
    });

    socket.on('send_tiebreaker', function(msg) {
        if (socket.id !== masterClientSocketId) {
            console.log('ignoring send_tiebreaker message from non-controller client: ' + socket.id);
            return;
        }

        try {
            var jsonMsg = JSON.parse(msg);
            //console.log('>>> tiebreaker_coming payload: ' + msg);

            var primaryIdHash = jsonMsg.recipient;

            if (primaryIdHashToMonitor === primaryIdHash) {
                console.log('emitting tiebreaker_coming to monitored client');
            }                
            
            var clientsForThisHash = primaryIdHashToSocket[primaryIdHash];
            if (clientsForThisHash == null) {
                clientsForThisHash = [];
            }

            for (i=0; i<clientsForThisHash.length; i++) {
                var clientSocket = clientsForThisHash[i];
                //console.log('emitting tiebreaker_coming, to: ' + primaryIdHash);
                clientSocket.emit('tiebreaker_coming', JSON.stringify(jsonMsg.message));
            }

        } catch (err) {
            console.log('ERR during send_tiebreaker: ' + err);
        }
    });

    socket.on('pong', function(msg) {
        //no-op
    });
});

//https://stackoverflow.com/questions/12008120/console-log-timestamps-in-chrome
//see answer by JSmyth
console.logCopy = console.log.bind(console);
console.log = function(data)
{
    var currentDate = '[' + new Date().toUTCString() + '] ';
    this.logCopy(currentDate, data);
};

server.listen(8080);

//a ping-pong workaround to keep the socket from disconnecting constantly.
//see: https://github.com/socketio/socket.io-client-java/issues/123
function sendPingToAll()
{
    for (key in primaryIdHashToSocket) {
        var clientsForThisHash = primaryIdHashToSocket[key];
        if (clientsForThisHash == null) {
            clientsForThisHash = [];
        }

        for (i=0; i<clientsForThisHash.length; i++) {
            var socket = clientsForThisHash[i];
            if (socket != null) {
                socket.emit('ping');
            } 
        }
    }

    setTimeout(sendPingToAll, 5000);
}

setTimeout(sendPingToAll, 5000);

