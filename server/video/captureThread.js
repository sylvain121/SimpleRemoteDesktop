const { fork } = require('child_process');
const videoProc = fork('./captureRunnable');


module.exports.start = function(codecWidth, codecHeight, bandwidth, fps) {
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


videoProc.on('message', (frame)=>{
// to video buffer fifo
});
