import { XImage } from 'node-x11';
import { X11Endpoint } from "../VideoCapture/X11Endpoint";
import { H264Encoder } from "../VideoCapture/H264Encoder";
import { APPLICATION_EVENT, SimpleRemoteDesktop, StreamingInformation, ScreenOffsetOption } from "./SimpleRemoteDesktop";
import Timeout = NodeJS.Timeout;

export class CaptureChain {

    private interval!: Timeout;
    public capture!: X11Endpoint;
    private encoder!: H264Encoder;
    private msInterval!: number;

    constructor(private screenOffset: ScreenOffsetOption | undefined) {


        SimpleRemoteDesktop.App_event_bus.on(APPLICATION_EVENT.STREAMING_STOP,
            () => {
                this.stop();
            });

        SimpleRemoteDesktop.App_event_bus.on(APPLICATION_EVENT.STREAMING_START,
            (message: StreamingInformation) => {
                this.start(message)
            });
    }

    private start(message: StreamingInformation) {
        let i = 0;
        this.msInterval = Math.trunc((1 / message.fps) * 1000);
        this.capture = new X11Endpoint(undefined, this.screenOffset);
        this.encoder = new H264Encoder(this.capture.width, this.capture.height, message.codecWidth, message.codecHeight, message.bitrate, message.fps);
        console.log("start interval", this.msInterval);
        this.interval = setInterval(() => {
            this.capture.getImage((err: Error, image: XImage) => {
                this.encoder.compress(image, (err: Error, encoded: Buffer) => {
                    SimpleRemoteDesktop.App_event_bus.emit(APPLICATION_EVENT.NEW_ENCODED_FRAME, encoded);
                });
            });


        }, this.msInterval);
    }

    private stop() {
        clearInterval(this.interval);
        this.capture.stop();
        this.encoder.stop();
    }
}
