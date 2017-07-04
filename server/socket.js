var net = require('net');
var userSocket = null;
var port = 8001;
var onNewMessageHandler = null;

var buffer = new Buffer(0);

module.exports.getSocket = function() {
    return userSocket;
};

module.exports.getPort = function() {
    return port;
}

module.exports.registerMessageHandler = function(mh) {
    onNewMessageHandler = mh;
}

module.exports.getWriteFn = function() {
    if (userSocket) return userSocket.write();
    console.log("!!! WARN no socket");
    return null;
}

var server = net.createServer(function(socket) {
    console.log("new socket connected");
    if (!userSocket) {
        console.log("set new userSocket");
        userSocket = socket;


    } else {
        socket.end();
    }

    socket.on('close', function() {
            console.log("Socket Disconnected");
            if (userSocket === socket) {
                userSocket = null;
            }
        })
        .on('data', function(data) {
            console.log("new tcp message");
            messageHandler(data);
        });


});



server.listen(port, function() {
    console.log("tcp socket open on port " + port);
});

function messageHandler(data) {

    buffer = Buffer.concat([buffer, data]);

    while (buffer.length > 32) {


        var message = {
            type: parseInt(read(4).readInt32LE().toString(), 10),
            x: parseInt(read(4).readInt32LE().toString(), 10),
            y: parseInt(read(4).readInt32LE().toString(), 10),
            button: parseInt(read(4).readInt32LE().toString(), 10),
            keycode: parseInt(read(4).readInt32LE().toString(), 10),
            width: parseInt(read(4).readInt32LE().toString(), 10),
            height: parseInt(read(4).readInt32LE().toString(), 10),
            fps: parseInt(read(4).readInt32LE().toString(), 10)
        }

        //console.log(message);
        onNewMessageHandler(message);
    }
}


function read(length, cb) {
    var data = new Buffer(length);
    buffer.copy(data, 0, 0, length);
    buffer = buffer.slice(length, buffer.length);
    return data;

}