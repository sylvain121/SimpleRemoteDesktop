const { fork } = require('child_process');
const videoProc = fork('./video/videoRunnable');
const { log } = require('../logger.js');
module.exports.registerFrameCallBack = (cb) => {
	videoProc.on('message', (frame)=>{
		cb(frame);	
	});
}

module.exports.start = function(codecWidth, codecHeight, bandwidth, fps) {
	log.debug("sending start request to video capture thread");
	videoProc.send({
		type: "start",
		codecWidth: codecWidth,
		codecHeight: codecHeight,
		bandwidth: bandwidth,
		fps: fps
	});
}

module.exports.stop = function() {
	videoProc.send({type: "stop"});
}

