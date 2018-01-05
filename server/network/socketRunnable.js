const { inputMessageEventEmitter } = require('./socket.js');

process.on('message', (outputData) => {
	inputMessageEventEmitter.emit('output_message', outputData);
});


inputMessageEventEmitter.on('input_message', (message) => {

	process.send({
		type: "control",
		data: message
	});
});

inputMessageEventEmitter.on('socket_closed', (isClosed) => {
	if(isClosed) {
		process.send({
			type:"socket",
			data: "closed"
		});
	}
});
