const {start, stop } = require('./capture.js');

process.on('message', (m)=>{

	switch(m){
		case "start":
			start(m.codecWidth, m.codecHeight, m.bandwidth, m.fpsi, videoCallback);
			break;
		case "stop":
			stop();
	}
});

function videoCallback(frame) {
 process.send(frame);
}

// process.send({ foo: 'bar', baz: NaN  });
