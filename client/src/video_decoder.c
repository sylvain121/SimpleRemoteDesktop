#include <SDL2/SDL_net.h>

#include "config.h"
#include "video_decoder.h"
#include "video_surface.h"

// compatibility with newer API
#if LIBAVCODEC_VERSION_INT < AV_VERSION_INT(55,28,1)
#define av_frame_alloc avcodec_alloc_frame
#define av_frame_free avcodec_free_frame
#endif

int init_video_decoder(int codec_width, int codec_height)
{
	AVCodec         *pCodec = NULL;
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "starting with codec resolution %dx%d", codec_width, codec_height);
	av_register_all();
	avformat_network_init(); //FIXME alway use ?

	// Find the decoder for the video stream
	pCodec=avcodec_find_decoder(AV_CODEC_ID_H264);
	if(pCodec==NULL) {
		SDL_LogError(SDL_LOG_CATEGORY_APPLICATION, "Unsupported codec!\n");
		return -1; // Codec not found
	}

	// Copy context
	pCodecCtx = avcodec_alloc_context3(pCodec);
	pCodecCtx->width = codec_width; //TODO set value
	pCodecCtx->height = codec_height; // TODO set value
	pCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;


	pCodecCtx->flags|= CODEC_FLAG_TRUNCATED; // FIXME not sure is used;


	// Open codec
	if(avcodec_open2(pCodecCtx, pCodec, NULL)<0)
		return -1; // Could not open codec

	// Allocate video frame
	pFrame=av_frame_alloc();


	// initialize SWS context for software scaling
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "initialize SWS context for software scaling\n ");
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "input w: %d h: %d", pCodecCtx->width, pCodecCtx->height);
	sws_ctx = sws_getContext(pCodecCtx->width,
			pCodecCtx->height,
			pCodecCtx->pix_fmt,
			pCodecCtx->width, 
			pCodecCtx->height,
			AV_PIX_FMT_YUV420P,
			SWS_FAST_BILINEAR,
			NULL,
			NULL,
			NULL
			);


	// received data loop
	packet.data = NULL;
	packet.size = 0;

	parser = av_parser_init(pCodecCtx->codec_id);
	parser->flags |= PARSER_FLAG_ONCE;
	return 1; // TODO
}

void destroy_decoder()
{
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, " Closing decoder");
	avcodec_close(pCodecCtx);
	av_free(pCodecCtx);
	sws_freeContext(sws_ctx);
}

int decode_video_frame(uint8_t *frame,int frame_length, Configuration *conf) 
{


	//Video decode part





	packet.data = frame;
	packet.size = frame_length;	

	while(packet.size > 0) {

		int lenght;
		// Decode video frame
		int frameFinished;
		lenght = avcodec_decode_video2(pCodecCtx, pFrame, &frameFinished, &packet);


		if( lenght < 0 ) {
			SDL_LogError(SDL_LOG_CATEGORY_APPLICATION, "Error while decoding frame\n");
		}
		// Did we get a video frame?
		if(frameFinished) {
			AVPicture pict;
			pict.data[0] = yPlane;
			pict.data[1] = uPlane;
			pict.data[2] = vPlane;
			pict.linesize[0] = conf->screen->width;
			pict.linesize[1] = uvPitch;
			pict.linesize[2] = uvPitch;		     

			// Convert the image into YUV format that SDL uses
			sws_scale(sws_ctx, (uint8_t const * const *)pFrame->data,
					pFrame->linesize, 0, pCodecCtx->height,
					pict.data, pict.linesize);
			
		update_video_surface(); //FIXME args

		}
		if(packet.data)
		{
			packet.size -=lenght;
			packet.data +=lenght;
		}
		av_free_packet(&packet);
	}

return 1; //TODO
}


void free_video_decoder() //FIXME need to clean ?
{
	// Free the YUV frame
	av_frame_free(&pFrame);

	// Close the codec
	avcodec_close(pCodecCtx);

	// Close the video file
	avformat_close_input(&pFormatCtx);


}


