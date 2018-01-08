const x11 = require("node-x11");
const SDLKey = require('./SDLKeysymToX11Keysym');
const { log } = require('../logger.js');

var screen = {
	width: 0,
	height: 0
}
var x_ratio = 0;
var y_ratio = 0;
var sdl = false;

module.exports.start = function(distantWidth, distantHeight, sdl_val) {
	
	sdl = sdl_val === 1;
	x11.init();
	var img = x11.getImage();
	screen.width = img.width;
	screen.height = img.height;
	if(sdl) SDLKey.SDLKeyToKeySym_init();
	setMouseDistantScreenSize(distantWidth, distantHeight);
}

module.exports.stop = function() {
	freeDesktop();
}

function freeDesktop() {
	log.info( "freeing desktop capture" );
	var x11ModuleName = require.resolve("node-x11");
	delete require.cache[x11ModuleName];
}
module.exports.handleAction = (message) => {
	switch(message.type) {
		case 'KEY_DOWN':
			toggleKeyDown(message.keyCode);
			break;
		case 'KEY_UP':
			toggleKeyUp(message.keyCode);
			break;
		case 'MOUSE_MOTION':
			mouseMove(message.x, message.y);
			break;
		case 'MOUSE_DOWN':
			mouseToggle(message.button, "down");
			break;
		case 'MOUSE_UP':
			mouseToggle(message.button);
			break;
	}
}

function setMouseDistantScreenSize(width, height) {
	x_ratio = screen.width / width;
	y_ratio = screen.height / height;
}

function mouseMove(x, y) {
		x11.mouseMove(x * x_ratio, y * y_ratio);
}

/**
 *@params newStat => "up", "down"
 *@params button => "left", "right", "middle"
 */
function mouseToggle(button, newStat) {
		var isDown = false;
		isDown = (newStat === "down") ? true : false;
		log.trace("button id : "+button+" isDown ? : "+isDown);
		x11.mouseButton(button, isDown);
}


/**
 * 
 *  keyboard
 * 
 */


function toggleKeyDown(keyCode) {
		if (keyCode <= 0) return log.warn("unknow keyCode : " + keyCode);
		if(sdl) keyCode = SDLKey.SDLKeyToKeySym(keyCode);
		x11.keyPressWithKeysym(keyCode, true);
}


function toggleKeyUp(keyCode) {
		if (keyCode <= 0) return log.warn("unknow keyCode : " + keyCode);
		if(sdl) keyCode = SDLKey.SDLKeyToKeySym(keyCode);
		x11.keyPressWithKeysym(keyCode, false);
}
