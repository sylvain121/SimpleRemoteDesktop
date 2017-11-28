package com.github.sylvain121.SimpleRemoteDesktop.player.video;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodec.CodecException;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.util.Log;
import android.view.SurfaceHolder;

import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.codecs.h264.io.model.SeqParameterSet;
import org.jcodec.codecs.h264.io.model.VUIParameters;

import java.nio.ByteBuffer;
import java.util.Locale;

public class MediaCodecDecoderRenderer {

    private static final boolean USE_FRAME_RENDER_TIME = false;
    private static final boolean FRAME_RENDER_TIME_ONLY = USE_FRAME_RENDER_TIME && false;

    private static final String TAG = "DECODER_RENDERER";

    // Used on versions < 5.0
    private ByteBuffer[] legacyInputBuffers;

    private MediaCodecInfo avcDecoder;

    // Used for HEVC only
    private byte[] vpsBuffer;
    private byte[] spsBuffer;

    private MediaCodec videoDecoder;
    private Thread rendererThread;
    private boolean needsSpsBitstreamFixup, isExynos4;
    private boolean adaptivePlayback, directSubmit;
    private boolean constrainedHighProfile;
    private boolean refFrameInvalidationAvc, refFrameInvalidationHevc;
    private boolean refFrameInvalidationActive;
    private int initialWidth, initialHeight;
    private SurfaceHolder renderTarget;
    private volatile boolean stopping;

    private boolean reportedCrash;

    private boolean needsBaselineSpsHack;
    private SeqParameterSet savedSps;

    private RendererException initialException;
    private long initialExceptionTimestamp;
    private static final int EXCEPTION_REPORT_DELAY_MS = 3000;

    private long lastTimestampUs;
    private long decoderTimeMs;
    private long totalTimeMs;
    private int totalFrames;
    private int frameLossEvents;
    private int framesLost;
    private int lastFrameNumber;
    private int refreshRate;


    private int numSpsIn;
    private int numPpsIn;
    private int numVpsIn;
    private int previous_idc;

    private MediaCodecInfo findAvcDecoder() {
        MediaCodecInfo decoder = MediaCodecHelper.findProbableSafeDecoder("video/avc", MediaCodecInfo.CodecProfileLevel.AVCProfileHigh);
        if (decoder == null) {
            decoder = MediaCodecHelper.findFirstDecoder("video/avc");
        }
        return decoder;
    }

    public void setRenderTarget(SurfaceHolder renderTarget) {
        this.renderTarget = renderTarget;
    }

    public MediaCodecDecoderRenderer() {
        //dumpDecoders();

        avcDecoder = findAvcDecoder();
        if (avcDecoder != null) {
            Log.d(TAG,"Selected AVC decoder: "+avcDecoder.getName());
        }
        else {
            Log.d(TAG,"No AVC decoder found");
        }

        // Set attributes that are queried in getCapabilities(). This must be done here
        // because getCapabilities() may be called before setup() in current versions of the common
        // library. The limitation of this is that we don't know whether we're using HEVC or AVC, so
        // we just assume AVC. This isn't really a problem because the capabilities are usually
        // shared between AVC and HEVC decoders on the same device.
        if (avcDecoder != null) {
            directSubmit = MediaCodecHelper.decoderCanDirectSubmit(avcDecoder.getName());
            adaptivePlayback = MediaCodecHelper.decoderSupportsAdaptivePlayback(avcDecoder.getName());
            refFrameInvalidationAvc = MediaCodecHelper.decoderSupportsRefFrameInvalidationAvc(avcDecoder.getName());
            refFrameInvalidationHevc = MediaCodecHelper.decoderSupportsRefFrameInvalidationHevc(avcDecoder.getName());

            if (directSubmit) {
                Log.d(TAG,"Decoder "+avcDecoder.getName()+" will use direct submit");
            }
            if (refFrameInvalidationAvc) {
                Log.d(TAG,"Decoder "+avcDecoder.getName()+" will use reference frame invalidation for AVC");
            }
            if (refFrameInvalidationHevc) {
                Log.d(TAG,"Decoder "+avcDecoder.getName()+" will use reference frame invalidation for HEVC");
            }
        }
    }

    public int setup(int width, int height) {
        this.initialWidth = width;
        this.initialHeight = height;

        String mimeType;
        String selectedDecoderName;

            mimeType = "video/avc";
            selectedDecoderName = avcDecoder.getName();

            if (avcDecoder == null) {
                Log.d(TAG,"No available AVC decoder!");
                return -1;
            }

            // These fixups only apply to H264 decoders
            needsSpsBitstreamFixup = MediaCodecHelper.decoderNeedsSpsBitstreamRestrictions(selectedDecoderName);
            needsBaselineSpsHack = MediaCodecHelper.decoderNeedsBaselineSpsHack(selectedDecoderName);
            constrainedHighProfile = MediaCodecHelper.decoderNeedsConstrainedHighProfile(selectedDecoderName);
            isExynos4 = MediaCodecHelper.isExynos4Device();
            if (needsSpsBitstreamFixup) {
                Log.d(TAG,"Decoder "+selectedDecoderName+" needs SPS bitstream restrictions fixup");
            }
            if (needsBaselineSpsHack) {
                Log.d(TAG,"Decoder "+selectedDecoderName+" needs baseline SPS hack");
            }
            if (constrainedHighProfile) {
                Log.d(TAG,"Decoder "+selectedDecoderName+" needs constrained high profile");
            }
            if (isExynos4) {
                Log.d(TAG,"Decoder "+selectedDecoderName+" is on Exynos 4");
            }

            refFrameInvalidationActive = refFrameInvalidationAvc;


        // Codecs have been known to throw all sorts of crazy runtime exceptions
        // due to implementation problems
        try {
            videoDecoder = MediaCodec.createByCodecName(selectedDecoderName);
        } catch (Exception e) {
            e.printStackTrace();
            return -4;
        }

        MediaFormat videoFormat = MediaFormat.createVideoFormat(mimeType, width, height);

        // Adaptive playback can also be enabled by the whitelist on pre-KitKat devices
        // so we don't fill these pre-KitKat
        if (adaptivePlayback && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            videoFormat.setInteger(MediaFormat.KEY_MAX_WIDTH, width);
            videoFormat.setInteger(MediaFormat.KEY_MAX_HEIGHT, height);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Operate at maximum rate to lower latency as much as possible on
            // some Qualcomm platforms. We could also set KEY_PRIORITY to 0 (realtime)
            // but that will actually result in the decoder crashing if it can't satisfy
            // our (ludicrous) operating rate requirement.
            videoFormat.setInteger(MediaFormat.KEY_OPERATING_RATE, Short.MAX_VALUE);
        }

        try {
            videoDecoder.configure(videoFormat, renderTarget.getSurface(), null, 0);
            videoDecoder.setVideoScalingMode(MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT);

            if (USE_FRAME_RENDER_TIME && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                videoDecoder.setOnFrameRenderedListener(new MediaCodec.OnFrameRenderedListener() {
                    @Override
                    public void onFrameRendered(MediaCodec mediaCodec, long presentationTimeUs, long renderTimeNanos) {
                        long delta = (renderTimeNanos / 1000000L) - (presentationTimeUs / 1000);
                        if (delta >= 0 && delta < 1000) {
                            if (USE_FRAME_RENDER_TIME) {
                                totalTimeMs += delta;
                            }
                        }
                    }
                }, null);
            }

            Log.d(TAG,"Using codec "+selectedDecoderName+" for hardware decoding "+mimeType);

            // Start the decoder
            videoDecoder.start();

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                legacyInputBuffers = videoDecoder.getInputBuffers();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -5;
        }

        return 0;
    }

    private void handleDecoderException(Exception e, ByteBuffer buf, int codecFlags, boolean throwOnTransient) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (e instanceof CodecException) {
                CodecException codecExc = (CodecException) e;

                if (codecExc.isTransient() && !throwOnTransient) {
                    // We'll let transient exceptions go
                    Log.d(TAG,codecExc.getDiagnosticInfo());
                    return;
                }

                Log.d(TAG,codecExc.getDiagnosticInfo());
            }
        }

        // Only throw if we're not stopping
        if (!stopping) {
            //
            // There seems to be a race condition with decoder/surface teardown causing some
            // decoders to to throw IllegalStateExceptions even before 'stopping' is set.
            // To workaround this while allowing real exceptions to propagate, we will eat the
            // first exception. If we are still receiving exceptions 3 seconds later, we will
            // throw the original exception again.
            //
            if (initialException != null) {
                // This isn't the first time we've had an exception processing video
                if (System.currentTimeMillis() - initialExceptionTimestamp >= EXCEPTION_REPORT_DELAY_MS) {
                    // It's been over 3 seconds and we're still getting exceptions. Throw the original now.
                    if (!reportedCrash) {
                        reportedCrash = true;
                    }
                    throw initialException;
                }
            }
            else {
                // This is the first exception we've hit
                if (buf != null || codecFlags != 0) {
                    initialException = new RendererException(this, e, buf, codecFlags);
                }
                else {
                    initialException = new RendererException(this, e);
                }
                initialExceptionTimestamp = System.currentTimeMillis();
            }
        }
    }

    private void startRendererThread()
    {
        rendererThread = new Thread() {
            @Override
            public void run() {
                BufferInfo info = new BufferInfo();
                while (!stopping) {
                    try {
                        // Try to output a frame
                        int outIndex = videoDecoder.dequeueOutputBuffer(info, 50000);
                        if (outIndex >= 0) {
                            long presentationTimeUs = info.presentationTimeUs;
                            int lastIndex = outIndex;

                            // Get the last output buffer in the queue
                            while ((outIndex = videoDecoder.dequeueOutputBuffer(info, 0)) >= 0) {
                                videoDecoder.releaseOutputBuffer(lastIndex, false);

                                lastIndex = outIndex;
                                presentationTimeUs = info.presentationTimeUs;
                            }

                            // Render the last buffer
                            videoDecoder.releaseOutputBuffer(lastIndex, true);

                            // Add delta time to the totals (excluding probable outliers)
                            long delta = MediaCodecHelper.getMonotonicMillis() - (presentationTimeUs / 1000);
                            if (delta >= 0 && delta < 1000) {
                                decoderTimeMs += delta;
                                if (!USE_FRAME_RENDER_TIME) {
                                    totalTimeMs += delta;
                                }
                            }
                        } else {
                            switch (outIndex) {
                                case MediaCodec.INFO_TRY_AGAIN_LATER:
                                    break;
                                case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                                    Log.d(TAG,"Output format changed");
                                    Log.d(TAG,"New output Format: " + videoDecoder.getOutputFormat());
                                    break;
                                default:
                                    break;
                            }
                        }
                    } catch (Exception e) {
                        handleDecoderException(e, null, 0, false);
                    }
                }
            }
        };
        rendererThread.setName("Video - Renderer (MediaCodec)");
        rendererThread.setPriority(Thread.NORM_PRIORITY + 2);
        rendererThread.start();
    }

    private int dequeueInputBuffer() {
        int index = -1;
        long startTime, queueTime;

        startTime = MediaCodecHelper.getMonotonicMillis();

        try {
            while (index < 0 && !stopping) {
                index = videoDecoder.dequeueInputBuffer(10000);
            }
        } catch (Exception e) {
            handleDecoderException(e, null, 0, true);
            return MediaCodec.INFO_TRY_AGAIN_LATER;
        }

        if (index < 0) {
            return index;
        }

        queueTime = MediaCodecHelper.getMonotonicMillis();

        if (queueTime - startTime >= 20) {
            Log.d(TAG,"Queue input buffer ran long: " + (queueTime - startTime) + " ms");
        }

        return index;
    }


    public void start() {
        startRendererThread();
    }

    // !!! May be called even if setup()/start() fails !!!
    public void prepareForStop() {
        // Let the decoding code know to ignore codec exceptions now
        stopping = true;

        // Halt the rendering thread
        if (rendererThread != null) {
            rendererThread.interrupt();
        }
    }


    public void stop() {
        // May be called already, but we'll call it now to be safe
        prepareForStop();

        try {
            // Invalidate pending decode buffers
            videoDecoder.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Wait for the renderer thread to shut down
        try {
            rendererThread.join();
        } catch (InterruptedException ignored) { }

        try {
            // Stop the video decoder
            videoDecoder.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void cleanup() {
        videoDecoder.release();
    }

    private boolean queueInputBuffer(int inputBufferIndex, int offset, int length, long timestampUs, int codecFlags) {
        try {
            videoDecoder.queueInputBuffer(inputBufferIndex,
                    offset, length,
                    timestampUs, codecFlags);
            return true;
        } catch (Exception e) {
            handleDecoderException(e, null, codecFlags, true);
            return false;
        }
    }

    // Using the new getInputBuffer() API on Lollipop allows
    // the framework to do some performance optimizations for us
    private ByteBuffer getEmptyInputBuffer(int inputBufferIndex) {
        ByteBuffer buf;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                buf = videoDecoder.getInputBuffer(inputBufferIndex);
            } catch (Exception e) {
                handleDecoderException(e, null, 0, true);
                return null;
            }
        }
        else {
            buf = legacyInputBuffers[inputBufferIndex];

            // Clear old input data pre-Lollipop
            buf.clear();
        }

        return buf;
    }

    private void doProfileSpecificSpsPatching(SeqParameterSet sps) {
        // Some devices benefit from setting constraint flags 4 & 5 to make this Constrained
        // High Profile which allows the decoder to assume there will be no B-frames and
        // reduce delay and buffering accordingly. Some devices (Marvell, Exynos 4) don't
        // like it so we only set them on devices that are confirmed to benefit from it.
        if (sps.profileIdc == 100 && constrainedHighProfile) {
            Log.d(TAG,"Setting constraint set flags for constrained high profile");
            sps.constraintSet4Flag = true;
            sps.constraintSet5Flag = true;
        }
        else {
            // Force the constraints unset otherwise (some may be set by default)
            sps.constraintSet4Flag = false;
            sps.constraintSet5Flag = false;
        }
    }

    public int submitDecodeUnit(byte[] frameData, int frameLength, int frameNumber, long receiveTimeMs) {
        totalFrames++;

        // We can receive the same "frame" multiple times if it's an IDR frame.
        // In that case, each frame start NALU is submitted independently.
        if (frameNumber != lastFrameNumber && frameNumber != lastFrameNumber + 1) {
            framesLost += frameNumber - lastFrameNumber - 1;
            frameLossEvents++;
        }

        lastFrameNumber = frameNumber;

        int inputBufferIndex;
        ByteBuffer buf;

        long timestampUs = System.nanoTime() / 1000;

        if (!FRAME_RENDER_TIME_ONLY) {
            // Count time from first packet received to decode start
            totalTimeMs += (timestampUs / 1000) - receiveTimeMs;
        }

        if (timestampUs <= lastTimestampUs) {
            // We can't submit multiple buffers with the same timestamp
            // so bump it up by one before queuing
            timestampUs = lastTimestampUs + 1;
        }

        lastTimestampUs = timestampUs;

        int codecFlags = 0;
        boolean needsSpsReplay = false;

        // H264 SPS
        if (frameData[4] == 0x67) {
            numSpsIn++;
            codecFlags |= MediaCodec.BUFFER_FLAG_CODEC_CONFIG;

            ByteBuffer spsBuf = ByteBuffer.wrap(frameData);

            // Skip to the start of the NALU data
            spsBuf.position(5);

            // The H264Utils.readSPS function safely handles
            // Annex B NALUs (including NALUs with escape sequences)
            SeqParameterSet sps = H264Utils.readSPS(spsBuf);

            // Some decoders rely on H264 level to decide how many buffers are needed
            // Since we only need one frame buffered, we'll set the level as low as we can
            // for known resolution combinations. Reference frame invalidation may need
            // these, so leave them be for those decoders.

            if (!refFrameInvalidationActive) {
                if (initialWidth == 1280 && initialHeight == 720) {
                    // Max 5 buffered frames at 1280x720x60
                    Log.d(TAG,"Patching level_idc to 32");
                    sps.levelIdc = 32;
                }
                else if (initialWidth == 1920 && initialHeight == 1080) {
                    // Max 4 buffered frames at 1920x1080x64
                    Log.d(TAG,"Patching level_idc to 42");
                    sps.levelIdc = 42;
                }
                else {
                    // Leave the profile alone (currently 5.0)
                }
            }

            // TI OMAP4 requires a reference frame count of 1 to decode successfully. Exynos 4
            // also requires this fixup.
            //
            // I'm doing this fixup for all devices because I haven't seen any devices that
            // this causes issues for. At worst, it seems to do nothing and at best it fixes
            // issues with video lag, hangs, and crashes.
            //
            // It does break reference frame invalidation, so we will not do that for decoders
            // where we've enabled reference frame invalidation.
            if (!refFrameInvalidationActive) {
                Log.d(TAG,"Patching num_ref_frames in SPS");
                sps.numRefFrames = 1;
            }

            // GFE 2.5.11 changed the SPS to add additional extensions
            // Some devices don't like these so we remove them here.
            sps.vuiParams.videoSignalTypePresentFlag = false;
            sps.vuiParams.colourDescriptionPresentFlag = false;
            sps.vuiParams.chromaLocInfoPresentFlag = false;

            if ((needsSpsBitstreamFixup || isExynos4) && !refFrameInvalidationActive) {
                // The SPS that comes in the current H264 bytestream doesn't set bitstream_restriction_flag
                // or max_dec_frame_buffering which increases decoding latency on Tegra.

                // GFE 2.5.11 started sending bitstream restrictions
                if (sps.vuiParams.bitstreamRestriction == null) {
                    Log.d(TAG,"Adding bitstream restrictions");
                    sps.vuiParams.bitstreamRestriction = new VUIParameters.BitstreamRestriction();
                    sps.vuiParams.bitstreamRestriction.motionVectorsOverPicBoundariesFlag = true;
                    sps.vuiParams.bitstreamRestriction.log2MaxMvLengthHorizontal = 16;
                    sps.vuiParams.bitstreamRestriction.log2MaxMvLengthVertical = 16;
                    sps.vuiParams.bitstreamRestriction.numReorderFrames = 0;
                }
                else {
                    Log.d(TAG,"Patching bitstream restrictions");
                }

                // Some devices throw errors if max_dec_frame_buffering < num_ref_frames
                sps.vuiParams.bitstreamRestriction.maxDecFrameBuffering = sps.numRefFrames;

                // These values are the defaults for the fields, but they are more aggressive
                // than what GFE sends in 2.5.11, but it doesn't seem to cause picture problems.
                sps.vuiParams.bitstreamRestriction.maxBytesPerPicDenom = 2;
                sps.vuiParams.bitstreamRestriction.maxBitsPerMbDenom = 1;

                // log2_max_mv_length_horizontal and log2_max_mv_length_vertical are set to more
                // conservative values by GFE 2.5.11. We'll let those values stand.
            }
            else {
                // Devices that didn't/couldn't get bitstream restrictions before GFE 2.5.11
                // will continue to not receive them now
                sps.vuiParams.bitstreamRestriction = null;
            }

            // If we need to hack this SPS to say we're baseline, do so now
            if (needsBaselineSpsHack) {
                Log.d(TAG,"Hacking SPS to baseline");
                sps.profileIdc = 66;
                savedSps = sps;
            }

            // Patch the SPS constraint flags
            doProfileSpecificSpsPatching(sps);

            inputBufferIndex = dequeueInputBuffer();
            if (inputBufferIndex < 0) {
                Log.d(TAG, "NEED_IDR_1!!!!");
                return 1;
            }

            buf = getEmptyInputBuffer(inputBufferIndex);
            if (buf == null) {
                Log.d(TAG,"NEEED_IDR_2 !!!!");
                return 1;
            }

            // Write the annex B header
            buf.put(frameData, 0, 5);

            // The H264Utils.writeSPS function safely handles
            // Annex B NALUs (including NALUs with escape sequences)
            ByteBuffer escapedNalu = H264Utils.writeSPS(sps, frameLength);
            buf.put(escapedNalu);

            if (queueInputBuffer(inputBufferIndex,
                    0, buf.position(),
                    timestampUs, codecFlags)) {
                Log.d(TAG, "DR_OK_1");
                return 1;
            }
            else {
                Log.d(TAG,"NEED_IDR_3");
                return 1;
            }

            // H264 PPS
        } else if (frameData[4] == 0x68) {
            numPpsIn++;
            codecFlags |= MediaCodec.BUFFER_FLAG_CODEC_CONFIG;

            inputBufferIndex = dequeueInputBuffer();
            if (inputBufferIndex < 0) {
                // We're being torn down now
                Log.d(TAG, "NEED_IDR_4");
                return 1;
            }

            buf = getEmptyInputBuffer(inputBufferIndex);
            if (buf == null) {
                // We're being torn down now
                Log.d(TAG, "NEED_IDR_5");
                return 1;
            }

            if (needsBaselineSpsHack) {
                Log.d(TAG,"Saw PPS; disabling SPS hack");
                needsBaselineSpsHack = false;

                // Give the decoder the SPS again with the proper profile now
                needsSpsReplay = true;
            }
        }
        else if (frameData[4] == 0x40) {
            numVpsIn++;

            // Batch this to submit together with SPS and PPS per AOSP docs
            vpsBuffer = new byte[frameLength];
            System.arraycopy(frameData, 0, vpsBuffer, 0, frameLength);
            Log.d(TAG, "DR OK_2");
            return 1;
        }
        else if (frameData[4] == 0x42) {
            numSpsIn++;

            // Batch this to submit together with VPS and PPS per AOSP docs
            spsBuffer = new byte[frameLength];
            System.arraycopy(frameData, 0, spsBuffer, 0, frameLength);
            Log.d(TAG, "DR OK_3");
            return 1;
        }
        else if (frameData[4] == 0x44) {
            numPpsIn++;

            inputBufferIndex = dequeueInputBuffer();
            if (inputBufferIndex < 0) {
                // We're being torn down now
                Log.d(TAG, "NEED_IDR_6");
                return 1;
            }

            buf = getEmptyInputBuffer(inputBufferIndex);
            if (buf == null) {
                // We're being torn down now
                Log.d(TAG, "NEED_IDR_7");
                return 1;
            }

            // When we get the PPS, submit the VPS and SPS together with
            // the PPS, as required by AOSP docs on use of HEVC and MediaCodec.
            if (vpsBuffer != null) {
                buf.put(vpsBuffer);
            }
            if (spsBuffer != null) {
                buf.put(spsBuffer);
            }

            // This is the HEVC CSD blob
            codecFlags |= MediaCodec.BUFFER_FLAG_CODEC_CONFIG;
        }
        else {
            inputBufferIndex = dequeueInputBuffer();
            if (inputBufferIndex < 0) {
                // We're being torn down now
                Log.d(TAG, "NEED_IDR_8");
                return 1;
            }

            buf = getEmptyInputBuffer(inputBufferIndex);
            if (buf == null) {
                // We're being torn down now
                Log.d(TAG, "NEED_IDR_9");
                return 1;
            }
        }

        // Copy data from our buffer list into the input buffer
        buf.put(frameData, 0, frameLength);

        if (!queueInputBuffer(inputBufferIndex,
                0, buf.position(),
                timestampUs, codecFlags)) {
            Log.d(TAG, "NEED_IDR_10");
        }

        if (needsSpsReplay) {
            if (!replaySps()) {
                Log.d(TAG, "NEED_IDR_11");
                return 1;
            }

            Log.d(TAG, "SPS replay complete");
            return 1;
        }

        Log.d(TAG, "DR OK_1");
        return 1;
    }

    private boolean replaySps() {
        int inputIndex = dequeueInputBuffer();
        if (inputIndex < 0) {
            return false;
        }

        ByteBuffer inputBuffer = getEmptyInputBuffer(inputIndex);
        if (inputBuffer == null) {
            return false;
        }

        // Write the Annex B header
        inputBuffer.put(new byte[]{0x00, 0x00, 0x00, 0x01, 0x67});

        // Switch the H264 profile back to previous idc
        savedSps.profileIdc = previous_idc;

        // Patch the SPS constraint flags
        doProfileSpecificSpsPatching(savedSps);

        // The H264Utils.writeSPS function safely handles
        // Annex B NALUs (including NALUs with escape sequences)
        ByteBuffer escapedNalu = H264Utils.writeSPS(savedSps, 128);
        inputBuffer.put(escapedNalu);

        // No need for the SPS anymore
        savedSps = null;

        // Queue the new SPS
        return queueInputBuffer(inputIndex,
                0, inputBuffer.position(),
                System.nanoTime() / 1000,
                MediaCodec.BUFFER_FLAG_CODEC_CONFIG);
    }

    public int getCapabilities() {
        int capabilities = 0;

        /*// We always request 4 slices per frame to speed up decoding on some hardware
        capabilities |= MoonBridge.CAPABILITY_SLICES_PER_FRAME((byte) 4);

        // Enable reference frame invalidation on supported hardware
        if (refFrameInvalidationAvc) {
            capabilities |= MoonBridge.CAPABILITY_REFERENCE_FRAME_INVALIDATION_AVC;
        }
        if (refFrameInvalidationHevc) {
            capabilities |= MoonBridge.CAPABILITY_REFERENCE_FRAME_INVALIDATION_HEVC;
        }

        // Enable direct submit on supported hardware
        if (directSubmit) {
            capabilities |= MoonBridge.CAPABILITY_DIRECT_SUBMIT;
        }*/

        return capabilities;
    }

    public int getAverageEndToEndLatency() {
        if (totalFrames == 0) {
            return 0;
        }
        return (int)(totalTimeMs / totalFrames);
    }

    public int getAverageDecoderLatency() {
        if (totalFrames == 0) {
            return 0;
        }
        return (int)(decoderTimeMs / totalFrames);
    }

    public class RendererException extends RuntimeException {
        private static final long serialVersionUID = 8985937536997012406L;

        private final Exception originalException;
        private final MediaCodecDecoderRenderer renderer;
        private ByteBuffer currentBuffer;
        private int currentCodecFlags;

        public RendererException(MediaCodecDecoderRenderer renderer, Exception e) {
            this.renderer = renderer;
            this.originalException = e;
        }

        public RendererException(MediaCodecDecoderRenderer renderer, Exception e, ByteBuffer currentBuffer, int currentCodecFlags) {
            this.renderer = renderer;
            this.originalException = e;
            this.currentBuffer = currentBuffer;
            this.currentCodecFlags = currentCodecFlags;
        }

        public String toString() {
            String str = "";

            str += "AVC Decoder: "+((renderer.avcDecoder != null) ? renderer.avcDecoder.getName():"(none)")+"\n";
            str += "Build fingerprint: "+Build.FINGERPRINT+"\n";
            str += "Initial video dimensions: "+renderer.initialWidth+"x"+renderer.initialHeight+"\n";
            str += "In stats: "+renderer.numVpsIn+", "+renderer.numSpsIn+", "+renderer.numPpsIn+"\n";
            str += "Total frames: "+renderer.totalFrames+"\n";
            str += "Frame losses: "+renderer.framesLost+" in "+frameLossEvents+" loss events\n";
            str += "Average end-to-end client latency: "+getAverageEndToEndLatency()+"ms\n";
            str += "Average hardware decoder latency: "+getAverageDecoderLatency()+"ms\n";

            if (currentBuffer != null) {
                str += "Current buffer: ";
                currentBuffer.flip();
                while (currentBuffer.hasRemaining() && currentBuffer.position() < 10) {
                    str += String.format((Locale)null, "%02x ", currentBuffer.get());
                }
                str += "\n";
                str += "Buffer codec flags: "+currentCodecFlags+"\n";
            }

            str += "Is Exynos 4: "+renderer.isExynos4+"\n";

            str += "/proc/cpuinfo:\n";
            try {
                str += MediaCodecHelper.readCpuinfo();
            } catch (Exception e) {
                str += e.getMessage();
            }

            str += "Full decoder dump:\n";
            try {
                str += MediaCodecHelper.dumpDecoders();
            } catch (Exception e) {
                str += e.getMessage();
            }

            str += originalException.toString();

            return str;
        }
    }
}
