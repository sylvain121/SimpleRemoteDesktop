var videoCapture = require('./video/captureThread.js');
var socket  = require('./network/socketThread.js');
var input = require('./input/inputThread.js');
const { log } = require('./logger.js');

var discovery = require("./DiscoveryService.js");

const CONTROL_PORT = 8001;
const DISCOVERY_PORT = 8002;
var isInit = false;

log.info("starting discovery service");
discovery.start(DISCOVERY_PORT);

videoCapture.registerFrameCallBack( (frame) => {
	log.info("new frame");
	socket.push(frame);
});


socket.registerCallback( (message) =>{

	switch(message.type) {
		case 'control':
			handleControlMessage(message.data);
			break;
		case 'socket':
			handleSocketMessage(message.data);
	}




});

function handleSocketMessage(message) {
	switch(message) {
		case 'closed':
			stopEncoder();
			break;
	}
}


function handleControlMessage(message) {

	switch(message.type) {
		case 'KEY_DOWN':
		case 'KEY_UP':
		case 'MOUSE_MOTION':
		case 'MOUSE_DOWN':
		case 'MOUSE_UP':
			log.info("new action message");
			if(isInit) input.handleInputAction(message);
			break;
		case 'ENCODER_START':
			log.info("start encoder");
			startEncoder(message);
			break;
		case 'ENCODER_STOP':
			log.info("stop Encoder");
			stopEncoder(message);
			break;
	}
}

function startEncoder(message) {
	input.initInput(message.width, message.height);
	videoCapture.start(message.codecWidth, message.codecHeight, message.bandwidth, message.fps);
	isInit = true;
}

function stopEncoder(message) {
	input.stopInput();
	videoCapture.stop();
	isInit = false;
}

