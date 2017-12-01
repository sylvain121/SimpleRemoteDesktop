const x11 = require("node-x11");
const encoder = require('node-avcodec-h264-encoder');
const net = require('net');

var lb = Buffer.allocUnsafe(4);
var running = false;
var frameNumberBuffer = Buffer.allocUnsafe(4);
var frameCounter = 0;
var socket = null;

var options = {
	inputWidth: 0,
	inputHeight: 0,
	outputWidth: 0,
	outputHeight: 0,
	bit_rate: 1000000,
	fps: 30,
	sample: encoder.YUV_420P,
	port: 8003
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


module.exports.start = function(codecWidth, codecHeight, bandwidth, fps, tcp_port) {

	console.log("Starting new capture session");
	options.outputWidth = codecWidth;
	options.outputHeight = codecHeight;
	options.bit_rate = bandwidth;
	options.fps = fps;
	options.period = 1000 / options.fps;
	options.port = tcp_port;
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
		console.log("init video stream");
		console.log(options);
		encoder.initSync(options);
		running = true;
	}

	var frame = encoder.encodeFrameSync(img.data);

	if (frame !== undefined) {
		frameSend = false;
		var frameTime = new Date();

		if (socket == null) {
			free();
		} else {

			lb.writeInt32BE(frame.length);
			frameNumberBuffer.writeInt32BE(frameCounter++);
			socket.write(frameNumberBuffer, function(){
				//console.log('sending frame number : '+frameCounter);
				socket.write(lb, function() {
					socket.write(frame, function() {
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


function startSocket() {
	var server = net.createServer(function(s) {
		console.log("new socket connected");
		socket = s;
		s.on('close', function() {
			console.log("Socket Disconnected");
			socket = null;	
			free();
		})
			.on('data', function(data) {
				// Do nothing acctualy
				// eventually can receive request idr frame
			})
			.on('error', function() {
				console.log("error on socket");
				free();
			});


	});


}
