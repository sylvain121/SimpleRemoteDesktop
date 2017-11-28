// remote desktop sdl client
#include <stdbool.h>
#include<stdint.h>
#include<time.h>

#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libswscale/swscale.h>

#include <SDL2/SDL.h>
#include <SDL2/SDL_net.h>

#define INBUF_SIZE 1000000
#define FF_INPUT_BUFFER_PADDING_SIZE 32

#ifdef __MINGW32__
#undef main /* Prevents SDL from overriding main() */
#endif

#include <stdio.h>

// compatibility with newer API
#if LIBAVCODEC_VERSION_INT < AV_VERSION_INT(55,28,1)
#define av_frame_alloc avcodec_alloc_frame
#define av_frame_free avcodec_free_frame
#endif



void SRD_ensure(int nbytes );
int SRD_readUInt32();
uint8_t * SRD_read(int nbytes);
int SRDNet_get_frame_length();
int SRDNet_get_frame_number();


enum type {TYPE_KEY_DOWN=1, TYPE_KEY_UP=2, TYPE_MOUSE_MOTION=3, TYPE_MOUSE_DOWN=4, TYPE_MOUSE_UP=5 , TYPE_ENCODER_START=6, TYPE_ENCODER_STOP=7 };

struct Message
{
	int type;
	int x;
	int y;
	int button;
	int keycode;
	int width;
	int height;
	int codec_width;
	int codec_height;
	int bandwidth;
	int fps;
	int sdl;
};

uint8_t inbuf[INBUF_SIZE + FF_INPUT_BUFFER_PADDING_SIZE];
//memset(inbuf+INBUF_SIZE, 0, FF_INPUT_BUFFER_PADDING_SIZE);
IPaddress ip;
TCPsocket sd;
int inbuf_average = 0;

int main(int argc, char *argv[]) 
{


	fprintf(stdout, "init() \n");
	int screen_width = 1280;
	int screen_height = 720;
	int codec_width = 800;
	int codec_height = 600;
	int bandwidth = 10000000;
	int fps = 25;

	char* hostname = strdup(argv[1]);

	int port = atoi(argv[2]);

	char* video_definition = argv[3];


	if(argv[4] != NULL && atoi(argv[4]) > 0) // have custom bandwidth
	{
		bandwidth = atoi(argv[4]);
	}	

	if(argv[4] != NULL && atoi(argv[5]) > 0) //have custom fps
	{
		fps = atoi(argv[5]); 
	}



	printf("parameters hostname : %s, port : %d, video resolution : %s, bandwidth : %dKb, fps : %d \n", hostname, port, video_definition, bandwidth, fps);


	if(video_definition != NULL)
	{
		if(strcmp("720p", video_definition) == 0)
		{
			codec_width = 1280;
			codec_height = 720;
			printf("swicth video resolution to %dx%d \n", codec_width, codec_height);
		}  

		if(strcmp("1080p", video_definition) == 0)
		{
			screen_width = codec_width = 1920;
			screen_height = codec_height = 1080;
			printf("swicth video resolution to %dx%d \n", codec_width, codec_height);
		} 
	} 




	/** key press logger for exit event ctrl + alt+ q **/
	int ctrl_press = false;
	int alt_press = false;

	// SDL Event
	SDL_Event userEvent;
	bool quit = false;

	AVFormatContext *pFormatCtx = NULL;
	int             i, videoStream;
	AVCodecContext  *pCodecCtx = NULL;
	AVCodec         *pCodec = NULL;
	AVFrame         *pFrame = NULL;
	float           aspect_ratio;
	struct SwsContext *sws_ctx = NULL;
	AVCodecParserContext *parser = NULL;
	int pts, dts;
	SDL_Texture   *bmp;
	SDL_Window     *screen;
	SDL_Renderer *renderer;
	SDL_Rect        rect;
	SDL_Event       event;

	Uint8 *yPlane, *uPlane, *vPlane;
	size_t yPlaneSz, uvPlaneSz;
	int uvPitch;



	// Register all formats and codecs
	av_register_all();
	avformat_network_init();

	if(SDL_Init(SDL_INIT_VIDEO | SDL_INIT_AUDIO | SDL_INIT_TIMER)) {
		fprintf(stderr, "Could not initialize SDL - %s\n", SDL_GetError());
		exit(1);
	}



	// Find the decoder for the video stream
	pCodec=avcodec_find_decoder(AV_CODEC_ID_H264);
	if(pCodec==NULL) {
		fprintf(stderr, "Unsupported codec!\n");
		return -1; // Codec not found
	}

	// Copy context
	pCodecCtx = avcodec_alloc_context3(pCodec);
	pCodecCtx->width = codec_width; //TODO set value
	pCodecCtx->height = codec_height; // TODO set value
	pCodecCtx->pix_fmt = AV_PIX_FMT_YUV420P;


	pCodecCtx->flags|= CODEC_FLAG_TRUNCATED;


	// Open codec
	if(avcodec_open2(pCodecCtx, pCodec, NULL)<0)
		return -1; // Could not open codec

	// Allocate video frame
	pFrame=av_frame_alloc();


	// Make a screen to put our video
	printf("Make a screen to put our video \n");
	screen = SDL_CreateWindow(
			"StreamMyDesktop Client",
			SDL_WINDOWPOS_UNDEFINED,
			SDL_WINDOWPOS_UNDEFINED,
			screen_width,
			screen_height,
			0);


	renderer = SDL_CreateRenderer(screen, -1, 0);
	if (!renderer) {
		fprintf(stderr, "SDL: could not create renderer - exiting\n");
		exit(1);

	}

	// Allocate a place to put our YUV image on that screen

	printf("Allocate a place to put our YUV image on that screen\n ");
	bmp = SDL_CreateTexture(
			renderer,
			SDL_PIXELFORMAT_YV12,
			SDL_TEXTUREACCESS_STREAMING,
			screen_width,
			screen_height
			);

	// initialize SWS context for software scaling
	printf("initialize SWS context for software scaling\n ");
	sws_ctx = sws_getContext(pCodecCtx->width,
			pCodecCtx->height,
			pCodecCtx->pix_fmt,
			codec_width, 
			codec_height,
			AV_PIX_FMT_YUV420P,
			SWS_FAST_BILINEAR,
			NULL,
			NULL,
			NULL
			);


	// set up YV12 pixel array (12 bits per pixel)
	printf("set up YV12 pixel array (12 bits per pixel)\n ");
	yPlaneSz = screen_width * screen_height;
	uvPlaneSz = screen_width * screen_height / 4;
	yPlane = (Uint8*)malloc(yPlaneSz);
	uPlane = (Uint8*)malloc(uvPlaneSz);
	vPlane = (Uint8*)malloc(uvPlaneSz);
	if (!yPlane || !uPlane || !vPlane) {
		fprintf(stderr, "Could not allocate pixel buffers - exiting\n");
		exit(1);
	}

	uvPitch = screen_width/ 2;

	/**
	 *
	 *
	 * NETWORK
	 *
	 */

	fprintf(stdout, "video width : %i, height : %i, fps : %i\n ", pCodecCtx->width, pCodecCtx->height, fps);


	if(SDLNet_Init() < 0 ) {
		fprintf(stderr, "SDLNet_Init: %s\n", SDLNet_GetError());
		exit(EXIT_FAILURE);
	}

	if(SDLNet_ResolveHost(&ip, hostname, port) < 0) {
		fprintf(stderr, "unable to resolve address %s , port %s \n", argv[1], argv[2]);
		exit(1);
	} 

	if(!(sd = SDLNet_TCP_Open(&ip))) {
		fprintf(stderr, "SDLNet_TCP_Open: %s\n", SDLNet_GetError());
		exit(EXIT_FAILURE);	
	}
	// inital packet with information
	struct Message init;
	init.type = TYPE_ENCODER_START;
	init.width = screen_width;
	init.height = screen_height;
	init.fps = fps;
	init.codec_width = codec_width;
	init.codec_height = codec_height;
	init.bandwidth = bandwidth;
	init.sdl = 1;


	SDLNet_TCP_Send(sd, (void * )&init, sizeof(init));

	// received data loop
	AVPacket        packet;
	packet.data = NULL;
	packet.size = 0;

	parser = av_parser_init(pCodecCtx->codec_id);
	parser->flags |= PARSER_FLAG_ONCE;

	int screen_is_fullscreen = 0;

	printf("start event loop\n ");
	for(;;) {


		// sdl event

		while(SDL_PollEvent(&userEvent)) {

			struct Message send; 

			switch(userEvent.type) {
				case SDL_QUIT: 
					quit = true;
					break;

				case SDL_KEYDOWN: 
					//printf("pressed key %d\n", userEvent.key.keysym.sym);
					send.type = TYPE_KEY_DOWN;
					if(userEvent.key.keysym.sym == 1073742048){
						ctrl_press = true;
					} 
					if(userEvent.key.keysym.sym == 1073742050){
						alt_press = true;
					} 
					if(userEvent.key.keysym.sym == 113 && ctrl_press && alt_press){
						quit = true;
					} else 	if(userEvent.key.keysym.sym == 102 && ctrl_press && alt_press){
						if(screen_is_fullscreen) 
						{
							screen_is_fullscreen = false;
							SDL_SetWindowFullscreen(screen, SDL_WINDOW_FULLSCREEN_DESKTOP);						
						} 
						else
						{
							screen_is_fullscreen = true;
							SDL_SetWindowFullscreen(screen, SDL_WINDOW_FULLSCREEN);
						} 

					} else {

						send.keycode = userEvent.key.keysym.sym;
						SDLNet_TCP_Send(sd, (void * )&send, sizeof(send));
					} 


					break;

				case SDL_KEYUP: 
					//printf("released key %d\n", userEvent.key.keysym.sym);
					send.type = TYPE_KEY_UP;
					if(userEvent.key.keysym.sym == 1073742048){
						ctrl_press = false;
					} 
					if(userEvent.key.keysym.sym == 1073742050){
						alt_press = false;
					}

					send.keycode = userEvent.key.keysym.sym;
					SDLNet_TCP_Send(sd, (void * )&send, sizeof(send));
					break;				

				case SDL_MOUSEMOTION: 
					//printf("mouse position x: %d, y: %d \n", userEvent.motion.x, userEvent.motion.y);
					send.type = TYPE_MOUSE_MOTION;
					send.x = (int) userEvent.motion.x;
					send.y = (int) userEvent.motion.y;
					SDLNet_TCP_Send(sd, (void * )&send, sizeof(send));
					break;
				case SDL_MOUSEBUTTONDOWN: {
								  send.type = TYPE_MOUSE_DOWN;
								  switch(userEvent.button.button) {
									  case SDL_BUTTON_LEFT: {
													//printf("left click down\n");
													send.button = 1;
													SDLNet_TCP_Send(sd, (void * )&send, sizeof(send));
													break;
												}
									  case SDL_BUTTON_RIGHT: {
													 //printf("right click down\n");
													 send.button = 3;
													 SDLNet_TCP_Send(sd, (void * )&send, sizeof(send));
													 break;
												 }
									  case SDL_BUTTON_MIDDLE: {
													  //printf("middle click down\n");
													  send.button = 2;
													  SDLNet_TCP_Send(sd, (void * )&send, sizeof(send));
													  break;
												  }
								  }
								  SDLNet_TCP_Send(sd, (void * )&send, sizeof(send));
								  break;
							  }

				case SDL_MOUSEBUTTONUP:    {
								   send.type = TYPE_MOUSE_UP;
								   switch(userEvent.button.button) {
									   case SDL_BUTTON_LEFT: {
													 //printf("left click released\n");
													 send.button = 1;
													 SDLNet_TCP_Send(sd, (void * )&send, sizeof(send));
													 break;
												 }
									   case SDL_BUTTON_RIGHT: {
													  //printf("right click released\n");
													  send.button = 3;
													  SDLNet_TCP_Send(sd, (void * )&send, sizeof(send));
													  break;
												  }
									   case SDL_BUTTON_MIDDLE: {
													   //printf("middle click released\n");
													   send.button = 2;
													   SDLNet_TCP_Send(sd, (void * )&send, sizeof(send));
													   break;
												   }
								   }
								   SDLNet_TCP_Send(sd, (void * )&send, sizeof(send));
								   break;
							   }


			}



		}

		if(quit)
			break;


		//Video decode part


		av_init_packet(&packet);

		int frame_counter = SRDNet_get_frame_number();
		int frame_length =  SRDNet_get_frame_length();

		SRD_ensure(frame_length);
		uint8_t *frame = SRD_read(frame_length);

		SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "frame number : %d, frame size : %d", frame_counter, frame_length);





		packet.data = frame;
		packet.size = frame_length;	

		while(packet.size > 0) {

			int lenght;
			// Decode video frame
			int frameFinished;
			lenght = avcodec_decode_video2(pCodecCtx, pFrame, &frameFinished, &packet);


			if( lenght < 0 ) {
				fprintf(stderr, "Error while decoding frame\n");
			}
			// Did we get a video frame?
			if(frameFinished) {
				AVPicture pict;
				pict.data[0] = yPlane;
				pict.data[1] = uPlane;
				pict.data[2] = vPlane;
				pict.linesize[0] = screen_width;
				pict.linesize[1] = uvPitch;
				pict.linesize[2] = uvPitch;		     

				// Convert the image into YUV format that SDL uses
				sws_scale(sws_ctx, (uint8_t const * const *)pFrame->data,
						pFrame->linesize, 0, pCodecCtx->height,
						pict.data, pict.linesize);


				SDL_UpdateYUVTexture(
						bmp,
						NULL,
						yPlane,
						screen_width,
						uPlane,
						uvPitch,
						vPlane,
						uvPitch

						);

				SDL_RenderClear(renderer);
				SDL_RenderCopy(renderer, bmp, NULL, NULL);
				SDL_RenderPresent(renderer);	

			}
			if(packet.data)
			{
				packet.size -=lenght;
				packet.data +=lenght;
			}
			av_free_packet(&packet);
		}


	}


	// Free the YUV frame
	av_frame_free(&pFrame);

	// Close the codec
	avcodec_close(pCodecCtx);

	// Close the video file
	avformat_close_input(&pFormatCtx);

	return 0;

}

int SRDNet_get_frame_number()
{
	SRD_ensure(4);
	return SRD_readUInt32();

}

int SRDNet_get_frame_length()
{
	SRD_ensure(4);
	return SRD_readUInt32();
}

void SRD_ensure(int nbytes )
{

	char net_in[nbytes+1];
	do
	{
		int net_lenght = SDLNet_TCP_Recv(sd, net_in, nbytes+1); //FIXME : adjust max data
		memcpy(inbuf+inbuf_average, net_in, net_lenght);
		inbuf_average = net_lenght + inbuf_average;
	}

	while( inbuf_average < nbytes );

}

int SRD_readUInt32()
{
	uint8_t *data = SRD_read(4);

	uint32_t num = 
		(uint32_t)data[0] << 24 |
		(uint32_t) data[1] << 16 |
		(uint32_t) data[2] << 8  |
		(uint32_t) data[3];

	return num;
}

uint8_t * SRD_read(int nbytes)
{
	uint8_t *data = malloc(sizeof(uint8_t)* nbytes);
	memcpy(data, inbuf, nbytes);	
	memcpy(inbuf, inbuf+nbytes, inbuf_average - nbytes);
	inbuf_average -= nbytes;
	return data;
}


