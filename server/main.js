var videoCapture = require('./video/captureThread.js');
var socket = require('./socket');
var discovery = require("./DiscoveryService.js");

socket.registerMessageHandler(onNewMessage);
socket.registerdisconnectHander(videoCapture.free);

const CONTROL_PORT = 8001;
const DISCOVERY_PORT = 8002;
const VIDEO_PORT = 8003;

discovery.start(DISCOVERY_PORT);


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
            videoCapture.start(message.codecWidth, message.codecHeight, message.bandwidth, message.fps, VIDEO_PORT);
            break;
        case 7:
            videoCapture.stop();
            break;
        default:
            break;
    }

}
