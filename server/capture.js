const x11 = require("node-x11");
const encoder = require('node-avcodec-h264-encoder');
const socket = require("./socket");

var timer = null;
var running = false;

var options = {
    screen: {
        width: 0,
        height: 0,
        bpp: 0
    },
    distant: {
        width: 0,
        height: 0
    },
    fps: 25
}



module.exports.start = function(distantWidth, distantHeight) {

    options.distant.width = distantWidth;
    options.distant.height = distantHeight;
    timer = setInterval(getFrame, 1000 / options.fps);
}

module.exports.stop = function() {
    try {
        clearInterval(timer);
    } catch (e) {
        console.error(e);
    }

    running = false;
}


function getFrame() {
    if (!running) {
        x11.init();
    }
    var img = x11.getImage();

    options.screen.width = img.width;
    options.screen.height = img.height;
    options.screen.bpp = img.bits_per_pixel;

    if (!running) {
        setMouseDistantScreenSize(options.distant.width, options.distant.height);
        console.log("init with parameters : ", options.screen.width, options.screen.height, options.distant.width, options.distant.height, options.screen.bpp);
        encoder.initSync(options.screen.width, options.screen.height, options.distant.width, options.distant.height, options.screen.bpp);
        running = true;
    }

    var frame = encoder.encodeFrameSync(img.data);
    if (frame !== undefined) {
        socket.getSocket().write(frame);
    }
}


/**
 * 
 * 
 * 
 * 
 */


var x_ratio = 0;
var y_ratio = 0;


module.exports.isConfigured = function() {
    return x_ratio > 0 && y_ratio > 0;
}

function setMouseDistantScreenSize(width, height) {
    x_ratio = options.screen.width / width;
    y_ratio = options.screen.height / height;



}

module.exports.mouseMove = function(x, y) {
    if (running) {
        console.log(x, x_ratio, y, y_ratio)
        x11.mouseMove(x * x_ratio, y * y_ratio);
    };
}

/**
 *@params newStat => "up", "down"
 *@params button => "left", "right", "middle"
 */
module.exports.mouseToggle = function(button, newStat) {

    if (running) {
        var isDown = false;
        isDown = (newStat === "down") ? true : false;
        x11.mouseButton(button, isDown);
    };


}


/**
 * 
 *  keyboard
 * 
 */


module.exports.toggleKeyDown = function(keyCode) {
    if (running && keyCode < 1000) {
        console.log("keycode", "down", keyCode, typeof keyCode);
        if (keyCode <= 0) return console.log("unknow keyCode : " + keyCode);
        x11.keyPressWithKeysym(keyCode, true);
    };
}


module.exports.toggleKeyUp = function(keyCode) {
    if (running && keyCode < 1000) {
        if (keyCode <= 0) return console.log("unknow keyCode : " + keyCode);
        x11.keyPressWithKeysym(keyCode, false);
    }
}