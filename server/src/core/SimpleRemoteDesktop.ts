import { EventEmitter } from "events";
import { IncomingTCPMessage, IMessage } from "../NetworkProviders/IncomingTCPMessage";
import { TcpSocket } from "../NetworkProviders/TcpSocket";
import { CaptureChain } from "./CaptureChain";
import { KeyLogger } from "../VideoCapture/KeyLogger";
import { Http } from "../NetworkProviders/webrtc/Http";

const { SDLKeyToKeySym_init, SDLKeyToKeySym } = require('../VideoCapture/SDLKeysymToX11Keysym.js')

export class StreamingInformation {
    constructor(public readonly codecWidth: number,
        public readonly codecHeight: number,
        public readonly bitrate: number,
        public readonly fps: number) {
    }
}

export class ScreenOffsetOption {
    constructor(
        public xoffset: number,
        public yoffset: number,
        public width: number,
        public height: number) { }
}

export enum APPLICATION_EVENT {
    NEW_ENCODED_FRAME = 'new_encoded_frame',
    NEW_INCOMING_MESSAGE = 'new_incoming_message',
    NETWORK_CLOSE = 'network_close',
    STREAMING_START = 'streaming_start',
    STREAMING_STOP = 'streaming_stop'
}

export class SimpleRemoteDesktop {
    public static App_event_bus = new EventEmitter();
    private isSdlClient = false;
    private network!: TcpSocket;
    private videoCapture: CaptureChain;
    private keylogger: KeyLogger;
    private http!: Http;

    constructor(port: number, enableWebrtc: boolean, password: string, private readonly: boolean, screenOffset?: ScreenOffsetOption | undefined) {
        this.keylogger = new KeyLogger();
        SDLKeyToKeySym_init();
        SimpleRemoteDesktop.App_event_bus.on(APPLICATION_EVENT.NEW_INCOMING_MESSAGE,
            (message: IMessage) => {
                this.dispathIncomingEvent(message);
            });

        SimpleRemoteDesktop.App_event_bus.on(APPLICATION_EVENT.NETWORK_CLOSE,
            () => {
                this.stop();
            })
        if (enableWebrtc) {
            this.http = new Http(port, password);
        } else {
            this.network = new TcpSocket(port);
        }
        this.videoCapture = new CaptureChain(screenOffset);
    }


    private dispathIncomingEvent(message: IMessage) {
        switch (message.type) {
            case IncomingTCPMessage.TYPE_KEYDOWN:
                if (this.readonly)
                    return;
                let keyDowncode = message.keycode;
                if (this.isSdlClient) {
                    keyDowncode = SDLKeyToKeySym(keyDowncode);
                }
                this.keylogger.keyDown(keyDowncode);
                this.videoCapture.capture.keyDown(keyDowncode);
                break;
            case IncomingTCPMessage.TYPE_KEYUP:
                if (this.readonly)
                    return;
                let keyUpcode = message.keycode;
                if (this.isSdlClient) {
                    keyUpcode = SDLKeyToKeySym(keyUpcode);
                }
                this.keylogger.keyDown(keyUpcode);
                this.videoCapture.capture.keyDown(keyUpcode);
                break;
            case IncomingTCPMessage.TYPE_MOUSE_MOVE:
                if (this.readonly)
                    return;
                this.videoCapture.capture.mouseMove(message.x, message.y);
                break;
            case IncomingTCPMessage.TYPE_MOUSE_DOWN:
                if (this.readonly)
                    return;
                this.videoCapture.capture.mousebutton(message.button, true);
                break;
            case IncomingTCPMessage.TYPE_MOUSE_UP:
                if (this.readonly)
                    return;
                this.videoCapture.capture.mousebutton(message.button, false);
                break;
            case IncomingTCPMessage.TYPE_STREAM_START:

                if (message.sdl) {
                    this.isSdlClient = true;
                }

                const streamingMessage = new StreamingInformation(
                    message.codecWidth,
                    message.codecHeight,
                    message.bandwidth,
                    message.fps
                );

                SimpleRemoteDesktop.App_event_bus.emit(APPLICATION_EVENT.STREAMING_START, streamingMessage);
                break;

            case IncomingTCPMessage.TYPE_STREAM_STOP:
                this.stop();
                break;
            default:
                console.log(`Unknown message id ${message.type}`);
                break;
        }
    }

    private stop() {
        console.log("Receiving stop request !!!!!");
        SimpleRemoteDesktop.App_event_bus.emit(APPLICATION_EVENT.STREAMING_STOP);
        this.isSdlClient = false;
    }
}
