const {start, stop, handleAction } = require('./input.js');

process.on('message', (message) => {

	switch(message.type) {
		case 'start':
			start(message.distantWidth, message.distantHeight);
			break;
		case 'stop':
			stop();
			break;
		case 'action':
			handleAction(message.message);
			break;
	}
});

