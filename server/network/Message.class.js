/**
 * this file will decoder input message 
 * and encoder output message for transmission over tcp socket
 */




class InputMessage {

	constructor() {
		this.length = 48;
		this.typeList = [ null,
			'KEY_DOWN',
			'KEY_UP',
			'MOUSE_MOTION',
			'MOUSE_DOWN',
			'MOUSE_UP',
			'ENCODER_START',
			'ENCODER_STOP'
		]
	}

	getType(value) {
		return this.typeList[value];

	}

	messageBuffer(buffer) {
		this.buffer = buffer;
		this.type  = this.getType(buffer.readInt32LE(0));
		this.x = buffer.readInt32LE(4);
		this.y = buffer.readInt32LE(8);
		this.button = buffer.readInt32LE(12);
		this.keycode = buffer.readInt32LE(16);
		this.width = buffer.readInt32LE(20);
		this.height = buffer.readInt32LE(24);
		this.codecWidth = buffer.readInt32LE(28);
		this.codecHeight = buffer.readInt32LE(32);
		this.bandwidth = buffer.readInt32LE(36);
		this.fps = buffer.readInt32LE(40);
		this.sdl = buffer.readInt32LE(44);
	}



}


class OutputMessage {

	constructor(buffer) {
		this.buffer = Buffer.from(buffer);
		this.length =Buffer.alloc(4);
		this.counter = Buffer.alloc(4);
		this.length.writeInt32BE(this.buffer.length);
		this.counter.writeInt32BE(1);
		this.out = Buffer.concat([this.counter, this.length, this.buffer], 8+this.buffer.length);
	}

	getData() {
		return this.out;
	}

}


module.exports.InputMessage = InputMessage;
module.exports.OutputMessage = OutputMessage;


