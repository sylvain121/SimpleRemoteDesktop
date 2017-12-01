const { fork } = require('child_process');
const videoProc = fork('./captureRunnable');


module.exports.start = function(codecWidth, codecHeight, bandwidth, fps, tcp_port) {
	videoProc.send({
		type: "start",
		codecWidth: codecWidth,
		codecHeight: codecHeight,
		bandwidth: bandwidth,
		fps: fps,
		tcp_port: tcp_port
	});
}

module.exports.stop = function() {
	videoProc.send({type: "stop"});
}


videoProc.on('message', (m)=>{
 //TODO implements ?
});
