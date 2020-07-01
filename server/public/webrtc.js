let el;
let controlDataChannel;
let commandDataChannel;


function onload() {
    initwebrtc();
}

function initwebrtc() {


    const pc = new RTCPeerConnection({
        iceServers: [
            {
                urls: 'stun:stun.l.google.com:19302'
            }
        ]
    });
    pc.createDataChannel("test"); //if not datachannel are not enabled on server side //FIXME
    pc.ondatachannel = (event) => {
        console.log("new datachannel");
        console.log(event);
        if (event.channel.label === 'control') {
            controlDataChannel = event.channel;
        }

        if(event.channel.label === 'command') {
            commandDataChannel = event.channel;
        }
    }

    pc.onicecandidate = event => {
        if (event.candidate === null) {
            console.log('=============== LOCAL OFFER =====================');
            console.log(pc.localDescription);
            const data = {offer: pc.localDescription}
            fetch("/connect", {
                method: 'POST',
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            })
                .then(response => response.json())
                .then(offer => {
                    console.log("================ DISTANT OFFER ===========")
                    console.log(offer);
                    pc.setRemoteDescription(new RTCSessionDescription(offer));
                })
        }
    };

    let log = msg => {
        //document.getElementById('div').innerHTML += msg + '<br>'
        //FIXME
    };

    pc.ontrack = function (event) {
        el = document.getElementById('remoteVideo');
        el.srcObject = event.streams[0];
        el.autoplay = false;
        el.controls = true;
        el.addEventListener('play', () => {
            el.controls = false;
            //fetch('/start');  //TODO

            function send() {
                setTimeout(() => {
                    if(!sendCommand({command: "start"})) {
                        send();
                    }
                },1000);
            }

            send();


        })

        addInputEventHandler(el);
    };

    pc.oniceconnectionstatechange = e => log(pc.iceConnectionState);


// Offer to receive 1 audio, and 2 video tracks
    //pc.addTransceiver('audio', {'direction': 'recvonly'});
    pc.addTransceiver('video', {'direction': 'recvonly'});
    //pc.addTransceiver('video', {'direction': 'recvonly'});
    pc.createOffer().then(d => pc.setLocalDescription(d)).catch(log);
}

const Button = {
    LEFT: 1,
    RIGHT: 2,
    MIDDLE: 4,
    prevState: 0
};

const SRD_MOUSE_BUTTON = {
    LEFT: 1,
    RIGHT: 2
};

function addInputEventHandler(element) {
    element.addEventListener('contextmenu', function (event) {
        event.preventDefault();
        return false;
    });

    element.addEventListener("mouseup", onMouseButton, false);
    element.addEventListener("mousedown", onMouseButton, false);
    element.addEventListener('mousemove', onMouseMove, false);
    document.addEventListener('keydown', onKeyDown, false);
    document.addEventListener('keyup', onKeyUp, false);


    function onKeyDown(event) {
        if (event.repeat) return;
        onKeyPress(event, true);

    }

    function onKeyUp(event) {
        if (event.repeat) return;
        onKeyPress(event, false);
    }

    function onKeyPress(event, isDown) {
        const message = {type: "keycode", keycode: event.keyCode, isDown};
        console.log(message);
        sendControl(message);
    }

    function onMouseButton(event) {
        const button = event.buttons ^ Button.prevState;
        Button.prevState = event.buttons;
        const isDown = event.type === "mousedown";
        sendControl({type: "mouseButton", button, isDown})

    }

    function onMouseMove(event) {
        var x = event.pageX - element.offsetLeft + document.documentElement.scrollLeft;
        var y = event.pageY - element.offsetTop + document.documentElement.scrollTop;

        var fx = x / element.offsetWidth;
        var fy = y / element.offsetHeight;

        sendControl({type: "mouseMove", x: fx, y: fy});

    }


}

function sendControl(data) {
    if (controlDataChannel && controlDataChannel.readyState === 'open') {
        controlDataChannel.send(JSON.stringify(data));
    }
}


function sendCommand(data) {
    if (commandDataChannel && commandDataChannel.readyState === 'open') {
        commandDataChannel.send(JSON.stringify(data));
        return true;
    }
    return false;
}
