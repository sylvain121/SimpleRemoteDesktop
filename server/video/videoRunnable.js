const {start, stop, videoCaptureEventEmitter } = require('./capture.js');
const { log } = require('../logger.js');

process.on('message', (m)=>{
	log.debug("new message from main thread for video");
	log.debug(m);
	switch(m.type){
		case "start":
			start(m.codecWidth, m.codecHeight, m.bandwidth, m.fps);
			break;
		case "stop":
			stop();
			break;
	}
});

videoCaptureEventEmitter.on('frame', (frame) => {
log.debug("sending frame to main process");	
	process.send(frame)
});


