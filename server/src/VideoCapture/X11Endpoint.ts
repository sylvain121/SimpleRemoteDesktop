import { getImageSync, init, keyPressWithKeysym, mouseButton, mouseMove, XImage } from 'node-x11'
import { ScreenOffsetOption } from '../core/SimpleRemoteDesktop';

export class X11Endpoint {
    private width!: number;
    private height!: number;

    constructor(display?: string, private screenOffset?: ScreenOffsetOption) {
        init();
    }

    public getImage(): XImage {
        let image;
        if (this.screenOffset) {
            image = getImageSync(
                this.screenOffset.xoffset,
                this.screenOffset.yoffset,
                this.screenOffset.width,
                this.screenOffset.height);

        } else {
            image = getImageSync();
        }
        this.width = image.width;
        this.height = image.height;

        return image;
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
        mouseButton(button, isDown);
    }

    public keyDown(keycode: number) {
        keyPressWithKeysym(keycode, true);
    }

    public keyUp(keycode: number) {
        keyPressWithKeysym(keycode, false)
    }
}
