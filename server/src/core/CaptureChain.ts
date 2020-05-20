import {X11Endpoint} from "../VideoCapture/X11Endpoint";
import {H264Encoder} from "../VideoCapture/H264Encoder";
import {APPLICATION_EVENT, SimpleRemoteDesktop, StreamingInformation} from "./SimpleRemoteDesktop";
import Timeout = NodeJS.Timeout;

export class CaptureChain {

    private interval!: Timeout;
    public capture!: X11Endpoint;
    private encoder!: H264Encoder;
    private msInterval!: number;

    constructor() {


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

        this.msInterval = 1 / message.fps;
        this.capture = new X11Endpoint();
        this.encoder = new H264Encoder(message.codecWidth, message.codecHeight, message.bitrate, message.fps);
        console.log("start interval");
        this.interval = setInterval(() => {
            console.log("grabbing desktop");
            const image = this.capture.getImage();
            console.log("compressing frame");
            const encoded = this.encoder.compress(image);

            SimpleRemoteDesktop.App_event_bus.emit(APPLICATION_EVENT.NEW_ENCODED_FRAME, encoded);

        }, this.msInterval);
    }

    private stop() {
        clearInterval(this.interval);
        this.capture.stop();
        this.encoder.stop();
    }
}
