var capture = require('./capture');
var socket = require('./socket');
var discovery = require("./DiscoveryService.js");

socket.registerMessageHandler(onNewMessage);
socket.registerdisconnectHander(capture.free);

function onNewMessage(message) {

    switch (message.type) {
        case 1:
            capture.toggleKeyDown(message.keycode);
            break;
        case 2:
            capture.toggleKeyUp(message.keycode);
            break;
        case 3:
            capture.mouseMove(message.x, message.y);
            break;
        case 4:
            capture.mouseToggle(message.button, "down");
            break;
        case 5:
            capture.mouseToggle(message.button);
            break;
        case 6:
            capture.start(message.width, message.height, message.codecWidth, message.codecHeight, message.bandwidth, message.fps, message.sdl);
            break;
        case 7:
            capture.stop();
            break;
        default:
            break;
    }

}
