import {IncomingMessage} from "./IncomingMessage";
import {createServer, Server, Socket} from "net";
import {EventEmitter} from "events";
import {APPLICATION_EVENT, SimpleRemoteDesktop} from "../core/SimpleRemoteDesktop";

export class TcpSocket {
    private server: Server;
    private userSocket!: Socket;
    private incomingBuffer: Buffer = new Buffer(0);

    constructor(private listenPort: number) {
        this.server = createServer((socket) => {
            console.log("new socket connected");
            if (!this.userSocket) {
                console.log("set new userSocket");
                this.userSocket = socket;


            } else {
                socket.end();
                console.log("a socket is already connected");
            }

            this.userSocket.on('close', () => {
                console.log("Socket Disconnected");
                SimpleRemoteDesktop.App_event_bus.emit(APPLICATION_EVENT.NETWORK_CLOSE);
            })
                .on('data', (data) => {
                    this.incomingBuffer = Buffer.concat([this.incomingBuffer, data]);
                    while (this.incomingBuffer.length >= IncomingMessage.MESSAGE_LENGTH) {
                        const data = new Buffer(IncomingMessage.MESSAGE_LENGTH);
                        this.incomingBuffer.copy(data, 0, 0, IncomingMessage.MESSAGE_LENGTH);
                        this.incomingBuffer = this.incomingBuffer.slice(IncomingMessage.MESSAGE_LENGTH, this.incomingBuffer.length);

                        const message = new IncomingMessage(data);
                        SimpleRemoteDesktop.App_event_bus.emit(APPLICATION_EVENT.NEW_INCOMING_MESSAGE, message);
                    }
                })
                .on('error', () => {
                    SimpleRemoteDesktop.App_event_bus.emit(APPLICATION_EVENT.NETWORK_CLOSE);
                    console.log("error on socket");
                });


        });

        SimpleRemoteDesktop.App_event_bus.on(APPLICATION_EVENT.NEW_ENCODED_FRAME, (frame: Buffer) => {
            this.send(frame);
        });

        this.server.listen(listenPort, () => {
            console.log("tcp socket open on port " + listenPort);
        });
    }

    private send(data: Buffer) {
        console.log(`frame length : ${data.length}`);
        const frameCounter = Buffer.alloc(4);
        frameCounter.writeInt32BE(0);
        const frameLength = Buffer.alloc(4)
        frameLength.writeInt32BE(data.length);
        this.userSocket.write(frameCounter, () => {
            this.userSocket.write(frameLength, () => {
                this.userSocket.write(data, () => {

                })
            })
        })

    }
}

