import { getImage as X11GetImage, init, keyPressWithKeysym, mouseButton, mouseMove, GetImageCallback, XImage } from 'node-x11'
import { ScreenOffsetOption } from '../core/SimpleRemoteDesktop';

export class X11Endpoint {
    public width!: number;
    public height!: number;

    constructor(display?: string, private screenOffset?: ScreenOffsetOption) {
        const {width, height} = init();
        this.width = width;
        this.height = height;
    }

    public getImage(cb: GetImageCallback) {
        if (this.screenOffset) {
            X11GetImage(
                this.screenOffset.xoffset,
                this.screenOffset.yoffset,
                this.screenOffset.width,
                this.screenOffset.height,
                cb);

        } else {
            X11GetImage(undefined, undefined, undefined, undefined, cb);
        }
    }

    stop() {
        console.log("releasing x11 capture module");
        const x11ModuleName = require.resolve("node-x11");
        delete require.cache[x11ModuleName];
    }

    public mouseMove(x: number, y: number) {
        mouseMove(x * this.width, y * this.height);
    }

    public mousebutton(button: number, isDown: boolean) {
        if (button  === 1 || button === 3) { //FIXME currently only support left and right click
            mouseButton(button, isDown);
        }
    }

    public keyDown(keycode: number) {
        console.log("keydown", keycode);
        keyPressWithKeysym(keycode, true);
    }

    public keyUp(keycode: number) {
        console.log("keyup", keycode);
        keyPressWithKeysym(keycode, false)
    }
}
