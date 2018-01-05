const { fork } = require('child_process');
const inputProc = fork('./input/inputRunnable.js');

module.exports.initInput = (distantWidth, distantHeight) => {
	inputProc.send({
		type: 'start',
		distantWidth,
		distantHeight
	});
}

module.exports.stopInput = () => {
	inputProc.send({
		type: 'stop'
	});
}

module.exports.handleInputAction = (message) => {
	inputProc.send({
		type: "action",
		message
	});
}

