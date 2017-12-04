// remote desktop sdl client
#include <stdbool.h>
#include<stdint.h>
#include<time.h>

#include <SDL2/SDL.h>
#include <SDL2/SDL_net.h>
#include <SDL2/SDL_thread.h>

#ifdef __MINGW32__
#undef main /* Prevents SDL from overriding main() */
#endif

#include <stdio.h>

#include "config.h"
#include "network.h"
#include "input.h"
#include "video_decoder.h"
#include "video_surface.h"


//memset(inbuf+INBUF_SIZE, 0, FF_INPUT_BUFFER_PADDING_SIZE);

SDL_Thread *thread = NULL;
int video_thread(void* data); 

int main(int argc, char *argv[]) 
{
	// set log level

	SDL_LogSetAllPriority(SDL_LOG_PRIORITY_WARN);

	// default value workaround

	screen_width = 1280;
	screen_height = 720;
	codec_width = 800;
	codec_height = 600;
	bandwidth = 10000000;
	fps = 25;

	ctrl_press = false;
	alt_press = false;
	quit = false;
	screen_is_fullscreen = 0;


	// parsing arguments

	fprintf(stdout, "init() \n");

	hostname = strdup(argv[1]);
	port = atoi(argv[2]);
	video_definition = argv[3];


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


	// start SDL Application

	if(SDL_Init(SDL_INIT_VIDEO | SDL_INIT_AUDIO | SDL_INIT_TIMER)) {
		fprintf(stderr, "Could not initialize SDL - %s\n", SDL_GetError());
		exit(1);
	}

	// init sdl surface
	init_video_surface(screen_width, screen_height); //FIXME return status code

	// init video decoder
	init_video_decoder(codec_width, codec_height);

	//init network and send start packet
	if(init_network()) 
	{
		// init network error
		exit(1);
	}

	thread = SDL_CreateThread(video_thread, "video_thread", NULL);
	printf("start event loop\n "); //EVENT LOOP FOR CATCH INPUT EVENT //TODO REFACTOR
	for(;;) {

		// get event from loop // TODO check code
		get_input_event();


		if(quit)
			break;
	}
	return 0;

}


int video_thread(void* data) 
{
	while(quit == false)
	{
		av_init_packet(&packet);

		// get frame from network
		int frame_counter = SRDNet_get_frame_number();
		int frame_length =  SRDNet_get_frame_length();

		SRD_ensure(frame_length);
		uint8_t *frame = SRD_read(frame_length);

		SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "frame number : %d, frame size : %d", frame_counter, frame_length);


		// decode frame from video_decoder
		decode_video_frame(frame, frame_length); 
		// update sdl texture with video_surface
		update_video_surface(); 

	}
}



