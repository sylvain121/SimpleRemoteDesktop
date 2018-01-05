const { fork } = require('child_process');
const socketProc = fork('./network/socketRunnable');

module.exports.registerCallback = function(callback) {
	socketProc.on('message', (message) => {
		if(callback !== null) {
			callback(message);
		}
	});
}

module.exports.push = function(outputData) {
	socketProc.send(outputData);
}




