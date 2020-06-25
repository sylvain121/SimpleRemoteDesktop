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
        onKeyPress(event);
    }

    function onKeyUp(event) {
        if (event.repeat) return;
        onKeyPress(event);
    }

    function onKeyPress(event) {
        console.log(event);
    }

    function onMouseButton(event) {
        //console.log(event);
        const button = event.buttons ^ Button.prevState;
        Button.prevState = event.buttons;
        const isDown = event.type === "mousedown";

        //console.log(button, isDown);
        socket.send(JSON.stringify({type: "button", button: button, isDown: isDown}));

    }

    function onMouseMove(event) {
        var x = event.pageX - element.offsetLeft + document.documentElement.scrollLeft;
        var y = event.pageY - element.offsetTop + document.documentElement.scrollTop;

        var fx = x / element.offsetWidth;
        var fy = y / element.offsetHeight;

        socket.send(JSON.stringify({type: "move", x: fx, y: fy}));
    }
}

