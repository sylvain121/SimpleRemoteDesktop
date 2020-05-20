import {XImage} from "node-x11";
import {AvcodecH264Encoder, encodeFrameSync, initSync, YUV_420P} from 'node-avcodec-h264-encoder';

export class H264Encoder {
    private firstframe = true;

    constructor(private outputWidth: number,
                private outputHeight: number,
                private bitrate: number,
                private fps: number) {

    }

    public compress(image: XImage): Buffer {
        if (this.isFirstFrame()) {
            const options: AvcodecH264Encoder = {
                inputWidth: image.width,
                inputHeight: image.height,
                outputWidth: this.outputWidth,
                outputHeight: this.outputHeight,
                bit_rate: this.bitrate,
                fps: this.fps,
                sample: YUV_420P
            }
            console.log(options);
            initSync(options);
        }
        return encodeFrameSync(image.data);
    }


    private isFirstFrame(): boolean {
        if (this.firstframe) {
            this.firstframe = false;
            return true;
        }
        return false
    }

    stop() {
        console.log("freeing avcodec-h264 encoder");
        const encoderModuleName = require.resolve('node-avcodec-h264-encoder');
        delete require.cache[encoderModuleName];
    }
}
