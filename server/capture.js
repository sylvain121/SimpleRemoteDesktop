const x11 = require("node-x11");
const encoder = require('node-avcodec-h264-encoder');
const socket = require("./socket");
const SDLKey = require('./SDLKeysymToX11Keysym');

var lb = Buffer.allocUnsafe(4);
var timer = null;
var running = false;

var frameSend = true;

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

function free() {
	var x11ModuleName = require.resolve("node-x11");
	var encoderModuleName = require.resolve('node-avcodec-h264-encoder');

	delete require.cache[x11ModuleName];
	delete require.cache[encoderModuleName];
	try {
		clearInterval(timer);
	} catch (e) {
		console.error(e);
	}

	running = false;

}


module.exports.start = function(distantWidth, distantHeight, codecWidth, codecHeight, bandwidth, fps, sdl) {

	options.outputWidth = codecWidth;
	options.outputHeight = codecHeight;
	options.distantDisplayWidth = distantWidth;
	options.distantDisplayHeight = distantHeight;
	options.bit_rate = bandwidth;
	options.fps = fps;
	options.sdl = sdl;
	timer = setInterval(getFrame, 1000 / options.fps);
}

module.exports.stop = function() {
	try {
		clearInterval(timer);
	} catch (e) {
		console.error(e);
	}

	running = false;
}


function getFrame() {
	var initTime = new Date();
	if (!running) {
		x11.init();
		SDLKey.SDLKeyToKeySym_init();

	}

	var img = x11.getImage();
	var getImageTime = new Date();
	options.inputWidth = img.width;
	options.inputHeight = img.height;

	if (!running) {
		setMouseDistantScreenSize(options.distantDisplayWidth, options.distantDisplayHeight);
		console.log("init video stream");
		console.log(options);
		encoder.initSync(options);
		running = true;
	}

	if (frameSend) {
		var frame = encoder.encodeFrameSync(img.data);

		if (frame !== undefined) {
			frameSend = false;
			var frameTime = new Date();

			if (socket.getSocket() == null) {
				free();
			} else {

				//TODO android patch 
				lb.writeInt32BE(frame.length);
				socket.getSocket().write(lb, function() {
					console.log(frame.length);
					socket.getSocket().write(frame, function() {
						frameSend = true;
					});
				});


			}
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
