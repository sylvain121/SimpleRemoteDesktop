import express from "express";
import * as bodyParser from "body-parser";
import {init as webRtcInit, writeFrame as webRtcWriteFrame} from 'node-pion-webrtc';
import {APPLICATION_EVENT, SimpleRemoteDesktop, StreamingInformation} from "../../core/SimpleRemoteDesktop";
import {IncomingTCPMessage, IncomingWebRTCMessage} from "../IncomingTCPMessage";

interface IOffer {
    authentication: string;
    offer: RTCSessionDescription;
}

export class Http {
    private app: express.Application;

    constructor(port: number, private password: string) {

        this.app = express();
        this.app.use(express.static('public'));
        this.app.use(bodyParser.json())

        this.app.get('/start', (req: express.Request, res: express.Response) => {
            const streamingInformation = new StreamingInformation(
                1280,
                720,
                100000000,
                25
            );

            SimpleRemoteDesktop.App_event_bus.emit(APPLICATION_EVENT.STREAMING_START, streamingInformation);
        });

        this.app.get('/connect', (req: express.Request, res: express.Response) => {
            const authenticationRequired = !!this.password
            res.status(200).json({authenticationRequired});
        })

        this.app.post('/connect', (req: express.Request, res: express.Response) => {
            const data = req.body as IOffer
            console.log('========== DATA ================');
            console.log(data);
            if (!!this.password && data.authentication !== this.password) {
                return res.status(401).json("bad password");
            }
            console.log(data.offer);
            const response = webRtcInit(data.offer,
                (msg: string) => {
                const m = JSON.parse(msg);
                const message = new IncomingWebRTCMessage();
                if(m.command === 'start') {
                    message.type = IncomingTCPMessage.TYPE_STREAM_START;
                    message.codecWidth = 1280;
                    message.codecHeight = 720;
                    message.bandwidth = 10000000;
                    message.fps = 30;
                    message.sdl = 0;
                }

                SimpleRemoteDesktop.App_event_bus.emit(APPLICATION_EVENT.NEW_INCOMING_MESSAGE, message);

                },
                (msg: string) => console.log(msg),
                (msg: string) => console.log(msg));
            res.status(200).json(response);

            SimpleRemoteDesktop.App_event_bus.on(APPLICATION_EVENT.NEW_ENCODED_FRAME, (frame: Buffer) => {
                webRtcWriteFrame(frame);
            });

            return;

        })

        this.app.listen(port, () => {
            console.log(`web server listening on port ${port}!`)
        });
    }
}
