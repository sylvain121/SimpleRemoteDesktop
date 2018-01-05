const x11 = require("node-x11");
const encoder = require('node-avcodec-h264-encoder');
const { log } = require('../logger.js');
var running = false;

const EventEmitter = require('events');
class VideoCaptureEvent extends EventEmitter {}
const videoCaptureEventEmitter = new VideoCaptureEvent();

module.exports.videoCaptureEventEmitter = videoCaptureEventEmitter;


var options = {
	inputWidth: 0,
	inputHeight: 0,
	outputWidth: 0,
	outputHeight: 0,
	bit_rate: 1000000,
	fps: 30,
	sample: encoder.YUV_420P
};

module.exports.free = free;

function freeDesktop() {
	log.info( "freeing desktop capture" );
	var x11ModuleName = require.resolve("node-x11");
	delete require.cache[x11ModuleName];
}

function freeEncoder() {
	log.info("freeing encoder");
	var encoderModuleName = require.resolve('node-avcodec-h264-encoder');
	delete require.cache[encoderModuleName];
}

function free() {
	log.info("stopping capture thread");
	running = false;
	freeDesktop();
	freeEncoder();

}


module.exports.start = function(codecWidth, codecHeight, bandwidth, fps) {

	options.outputWidth = codecWidth;
	options.outputHeight = codecHeight;
	options.bit_rate = bandwidth;
	options.fps = fps;
	options.period = 1000 / options.fps;
	log.info("Starting new capture session");
	log.debug(options);

	x11.init();
	var img = x11.getImage();
	options.inputWidth = img.width;
	options.inputHeight = img.height;
	encoder.initSync(options);
	running = true;


	getFrame();
}

module.exports.stop = function() {
	running = false;
	free();
}


function getFrame() {
	log.debug("requesting new frame");
	var initTime = new Date();
	var img = x11.getImage();
	var frame = encoder.encodeFrameSync(img.data);

	if (frame !== undefined) {
		videoCaptureEventEmitter.emit('frame', frame);
		var frameTime = new Date();
		var t = new Date() - initTime;
		if(running) {
			setTimeout(getFrame, options.period - t);
		}
	}
}

