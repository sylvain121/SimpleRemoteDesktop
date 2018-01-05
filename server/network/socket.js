var net = require('net');
var userSocket = null;
var port = 8001;
const EventEmitter = require('events');
var buffer = new Buffer(0);
const { InputMessage, OutputMessage } = require("./Message.class.js");
const { log } = require('../logger.js');

class InputMessageEvent extends EventEmitter {}
const inputMessageEventEmitter = new InputMessageEvent();

module.exports = {
	inputMessageEventEmitter
}


inputMessageEventEmitter.on('output_message', (message)=> {
	log.debug("socket : receiving new message ");
	if(userSocket !== null) {
		const buf = new OutputMessage(message);
		log.debug("send frame to client, length : "+ buf.buffer.length);
		userSocket.write(buf.getData());

	}
});



var server = net.createServer(function(socket) {
	log.info("new socket connected");
	if (!userSocket) {
		log.info("set new userSocket");
		userSocket = socket;


	} else {
		socket.end();
	}

	socket.on('close', function() {
		log.info("Socket Disconnected");
		if (userSocket === socket) {
			userSocket = null;
			inputMessageEventEmitter.emit("socket_closed",true);
		}
	})
		.on('data', function(data) {
			log.debug("new message from client");
			messageHandler(data);
		})
		.on('error', function() {
			log.error("error on socket");
			inputMessageEventEmitter.emit("socket_closed",true);
		});


});



server.listen(port, function() {
	log.info("tcp socket open on port " + port);
});

function addInputDataToqueue(data) {
	buffer = Buffer.concat([buffer, data]);
}
function getMessageFromInputQueue(length) {
	var data = new Buffer(length);
	buffer.copy(data, 0, 0, length);
	buffer = buffer.slice(length, buffer.length);
	return data;

}

function messageHandler(data) {

	addInputDataToqueue(data);
	var message = new InputMessage();
	log.debug("buffer length : "+buffer.length+ " message length : "+message.length);
	if(buffer.length >= message.length) {
		var buf = getMessageFromInputQueue(message.length);
		message.messageBuffer(buf);
		//log.debug(message);
		inputMessageEventEmitter.emit('input_message', message);
		
	}
}

