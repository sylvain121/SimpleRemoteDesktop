import { XImage } from "node-x11";
import { AvcodecH264Encoder, encodeFrame, initSync, YUV_420P, EncoderCallback, encodeFrameSync } from 'node-avcodec-h264-encoder';

export class H264Encoder {
    private firstframe = true;

    constructor(private inputWidth: number,
        private inputHeight: number,
        private outputWidth: number,
        private outputHeight: number,
        private bitrate: number,
        private fps: number) {

        const options: AvcodecH264Encoder = {
            inputWidth: this.inputWidth,
            inputHeight: this.inputHeight,
            outputWidth: this.outputWidth,
            outputHeight: this.outputHeight,
            bit_rate: this.bitrate,
            fps: this.fps,
            sample: YUV_420P
        }
        console.log(options);
        initSync(options);

    }

    public compress(image: XImage, cb: EncoderCallback) {
        const frame = encodeFrameSync(image.data);
        cb(new Error(), frame);
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
