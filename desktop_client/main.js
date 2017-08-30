var net = require('net');
var client = new net.Socket();
client.setNoDelay(true);

client.connect(8001, '127.0.0.1', function() {
    console.log('Connected');

    /**
     * request stream start
     */

    client.write(ObjToBuffer({
        type: 5,
        x: 0,
        y: 0,
        button: 0,
        keycode: 0,
        width: 1280,
        height: 720,
        fps: 25
    }));


    /**
     *  Keypress  down a
     */

    client.write(ObjToBuffer({
        type: 1,
        x: 0,
        y: 0,
        button: 0,
        keycode: 97,
        width: 0,
        height: 0,
        fps: 0
    }));


    /**
     *  Keypress  up a
     */

    client.write(ObjToBuffer({
        type: 2,
        x: 0,
        y: 0,
        button: 0,
        keycode: 97,
        width: 0,
        height: 0,
        fps: 0
    }));

    /**
     *  Keypress  down a
     */

    client.write(ObjToBuffer({
        type: 1,
        x: 0,
        y: 0,
        button: 0,
        keycode: 97,
        width: 0,
        height: 0,
        fps: 0
    }));

    /**
     *  Keypress  up a
     */

    client.write(ObjToBuffer({
        type: 2,
        x: 0,
        y: 0,
        button: 0,
        keycode: 97,
        width: 0,
        height: 0,
        fps: 0
    }));

});




function ObjToBuffer(obj) {
    var buffer = new Buffer(32);
    buffer.writeInt32LE(obj.type)
    buffer.writeInt32LE(obj.x, 4);
    buffer.writeInt32LE(obj.y, 8);
    buffer.writeInt32LE(obj.button, 12);
    buffer.writeInt32LE(obj.keycode, 16);
    buffer.writeInt32LE(obj.width, 20);
    buffer.writeInt32LE(obj.height, 24);
    buffer.writeInt32LE(obj.fps, 28);

    console.log(buffer);
    return buffer;
}