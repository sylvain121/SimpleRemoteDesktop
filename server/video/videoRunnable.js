const {start, stop } = require('./capture.js');

process.on('message', (m)=>{

	switch(m){
		case "start":
			start(m.codecWidth, m.codecHeight, m.bandwidth, m.fps, m.tcp_port);
			break;
		case "stop":
			stop();
	}
});


// process.send({ foo: 'bar', baz: NaN  });
