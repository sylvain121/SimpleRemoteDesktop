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
	//
	configuration = malloc(sizeof(Configuration));
	configuration->screen = malloc(sizeof(Screen));
	configuration->codec = malloc(sizeof(Codec));
	configuration->server = malloc(sizeof(Server));
	configuration->screen->width = 1280;
	configuration->screen->height = 720;
	configuration->codec->width = 800;
	configuration->codec->height = 600;
	configuration->bandwidth = 1000000;
	configuration->fps = 25;

	ctrl_press = false;
	alt_press = false;
	quit = false;
	screen_is_fullscreen = 0;


	// parsing arguments

	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, stdout, "init() \n");

	configuration->server->hostname = strdup(argv[1]);
	configuration->server->port = atoi(argv[2]);
	char *video_definition = argv[3];


	if(argv[4] != NULL && atoi(argv[4]) > 0) // have custom bandwidth
	{
		configuration->bandwidth = atoi(argv[4]);
	}	

	if(argv[4] != NULL && atoi(argv[5]) > 0) //have custom fps
	{
		configuration->fps = atoi(argv[5]); 
	}

	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "parameters hostname : %s, port : %d, video resolution : %s, bandwidth : %dKb, fps : %d \n", 
			configuration->server->hostname, 
			configuration->server->port,
			video_definition, 
			configuration->bandwidth, 
			configuration->fps);

	if(video_definition != NULL)
	{
		if(strcmp("720p", video_definition) == 0)
		{
			configuration->codec->width = 1280;
			configuration->codec->height = 720;
			SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "swicth video resolution to %dx%d \n", configuration->codec->width, configuration->codec->height);
		}  

		if(strcmp("1080p", video_definition) == 0)
		{
			configuration->screen->width = configuration->codec->width = 1920;
			configuration->screen->height = configuration->codec->height = 1080;
			SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "swicth video resolution to %dx%d \n", configuration->codec->width, configuration->codec->height);
		} 
	} 


	// start SDL Application

	if(SDL_Init(SDL_INIT_VIDEO | SDL_INIT_AUDIO | SDL_INIT_TIMER)) {
		SDL_LogError(SDL_LOG_CATEGORY_APPLICATION, "Could not initialize SDL - %s\n", SDL_GetError());
		SRD_exit();
	}

	// init sdl surface
	init_video_surface(configuration->screen->width, configuration->screen->height); //FIXME return status code

	// init video decoder
	init_video_decoder(configuration->codec->width, configuration->codec->height);

	//init network and send start packet
	if(init_network()) 
	{
		// TODO init network error
		SRD_exit();
	}

	thread = SDL_CreateThread(video_thread, "video_thread", configuration);
	printf("start event loop\n "); //EVENT LOOP FOR CATCH INPUT EVENT //TODO REFACTOR
	for(;;) {

		// get event from loop // TODO check code
		get_input_event();


		if(quit)
			break;
	}
	return 0;

}

void SRD_exit()
{
	exit(1);
}

int video_thread(void* configuration) 
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
		decode_video_frame(frame, frame_length, configuration); 
		// update sdl texture with video_surface
		update_video_surface(); 

	}
}



