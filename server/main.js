var capture = require('./capture');






















var socket = require('./socket');


socket.registerMessageHandler(onNewMessage);
socket.registerdisconnectHander(capture.free);





function onNewMessage(message) {

    console.log(message.type);
    switch (message.type) {
        case 1:
            console.log("keydown : " + message.keycode);
            capture.toggleKeyDown(message.keycode);
            break;
        case 2:
            console.log("keyup : " + message.keycode);
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
            console.log("encoder start, width : " + message.width + ", height: " + message.height);
            capture.start(message.width, message.height);
            break;
        case 7:
            capture.stop();
            break;
        default:
            console.log(message);
            break;
    }

}