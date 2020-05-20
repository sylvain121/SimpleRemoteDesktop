export class IncomingMessage {
    public static readonly MESSAGE_LENGTH = 40;
    public static readonly TYPE_KEYDOWN = 1;
    public static readonly TYPE_KEYUP = 2;
    public static readonly TYPE_MOUSE_MOVE = 3;
    public static readonly TYPE_MOUSE_DOWN = 4;
    public static readonly TYPE_MOUSE_UP = 5;
    public static readonly TYPE_STREAM_START = 6;
    public static readonly TYPE_STREAM_STOP = 7;

    constructor(private chunk: Buffer) {
    }

    public get type(): number {
        return parseInt(this.chunk.readInt32LE().toString(), 10)
    }

    public get x(): number {
        return this.chunk.readFloatLE(4);
    }

    public get y(): number {
        return this.chunk.readFloatLE(8);
    }

    public get button(): number {
        return parseInt(this.chunk.readInt32LE(12).toString(), 10);
    }

    public get keycode(): number {
        return parseInt(this.chunk.readInt32LE(16).toString(), 10);
    }

    public get codecWidth(): number {
        return parseInt(this.chunk.readInt32LE(20).toString(), 10);
    }

    public get codecHeight(): number {
        return parseInt(this.chunk.readInt32LE(24).toString(), 10);
    }

    public get bandwidth(): number {
        return parseInt(this.chunk.readInt32LE(28).toString(), 10);
    }

    public get fps(): number {
        return parseInt(this.chunk.readInt32LE(32).toString(), 10);
    }

    public get sdl(): number {
        return parseInt(this.chunk.readInt32LE(36).toString(), 10);
    }
}
