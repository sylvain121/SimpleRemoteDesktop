const x11 = require("node-x11");
const encoder = require('node-avcodec-h264-encoder');
const socket = require("./socket");
const SDLKey = require('./SDLKeysymToX11Keysym');

var lb = Buffer.allocUnsafe(4);
var running = false;
var frameNumberBuffer = Buffer.allocUnsafe(4);
var frameCounter = 0;

var options = {
inputWidth: 0,
	    inputHeight: 0,
	    outputWidth: 0,
	    outputHeight: 0,
	    distantDisplayWidth: 0,
	    distantDisplayHeight: 0,
	    bit_rate: 10000000,
	    fps: 60,
	    sdl: 0,
	    sample: encoder.YUV_420P
};

module.exports.free = free;

function freeDesktop() {
	console.log( "freeing desktop capture" );
	var x11ModuleName = require.resolve("node-x11");
	delete require.cache[x11ModuleName];
}

function freeEncoder() {
	console.log("freeing encoder");
	var encoderModuleName = require.resolve('node-avcodec-h264-encoder');
	delete require.cache[encoderModuleName];
}

function free() {
	running = false;
	freeDesktop();
	freeEncoder();

}


module.exports.start = function(distantWidth, distantHeight, codecWidth, codecHeight, bandwidth, fps, sdl) {

	console.log("Starting new capture session");
	options.outputWidth = codecWidth;
	options.outputHeight = codecHeight;
	options.distantDisplayWidth = distantWidth;
	options.distantDisplayHeight = distantHeight;
	options.bit_rate = bandwidth;
	options.fps = fps;
	options.sdl = sdl;
	options.period = 1000 / options.fps;

	frameCounter = 0;
	getFrame();
}

module.exports.stop = function() {
	running = false;
	free();
}


function getFrame() {
	var initTime = new Date();
	if (!running) {
		x11.init();
		if(options.sdl == 1) SDLKey.SDLKeyToKeySym_init();
	}

	var img = x11.getImage();

	if (options.inputWidth !== img.width || options.inputHeight !== img.height) {
		console.log("resolution changed : reconfiguring encoder");
		freeEncoder();
		running = false;
	}
	if (!running) {
		options.inputWidth = img.width;
		options.inputHeight = img.height;
		setMouseDistantScreenSize(options.distantDisplayWidth, options.distantDisplayHeight);
		console.log("init video stream");
		console.log(options);
		encoder.initSync(options);
		running = true;
	}

	var frame = encoder.encodeFrameSync(img.data);

	if (frame !== undefined) {
		frameSend = false;
		var frameTime = new Date();

		if (socket.getSocket() == null) {
			free();
		} else {

			//TODO android patch 
			lb.writeInt32BE(frame.length);
			frameNumberBuffer.writeInt32BE(frameCounter++);
			socket.getSocket().write(frameNumberBuffer, function(){
					console.log('sending frame number : '+frameCounter);
					socket.getSocket().write(lb, function() {
							socket.getSocket().write(frame, function() {
									var t = new Date() - initTime;
									if(running) {
									setTimeout(getFrame, options.period - t);
									}
									});
							});
					});



		}
	}
}


/**
 * 
 * 
 * 
 * 
 */


var x_ratio = 0;
var y_ratio = 0;


module.exports.isConfigured = function() {
	return x_ratio > 0 && y_ratio > 0;
}

function setMouseDistantScreenSize(width, height) {
	x_ratio = options.inputWidth / width;
	y_ratio = options.inputHeight / height;
}

module.exports.mouseMove = function(x, y) {
	if (running) {
		x11.mouseMove(x * x_ratio, y * y_ratio);
	};
}

/**
 *@params newStat => "up", "down"
 *@params button => "left", "right", "middle"
 */
module.exports.mouseToggle = function(button, newStat) {

	if (running) {
		var isDown = false;
		isDown = (newStat === "down") ? true : false;
		console.log("button id : "+button+" isDown ? : "+isDown);
		x11.mouseButton(button, isDown);
	};
}


/**
 * 
 *  keyboard
 * 
 */


module.exports.toggleKeyDown = function(keyCode) {
	if (running) {
		if (keyCode <= 0) return console.log("unknow keyCode : " + keyCode);
		if(options.sdl === 1) keyCode = SDLKey.SDLKeyToKeySym(keyCode);
		x11.keyPressWithKeysym(keyCode, true);
	};
}


module.exports.toggleKeyUp = function(keyCode) {
	if (running) {
		if (keyCode <= 0) return console.log("unknow keyCode : " + keyCode);
		if(options.sdl === 1) keyCode = SDLKey.SDLKeyToKeySym(keyCode);
		x11.keyPressWithKeysym(keyCode, false);
	}
}
