// remote desktop sdl client
#ifdef __MINGW32__
#undef main /* Prevents SDL from overriding main() */
#endif

#include <stdio.h>
#include "client.h"
#include "config.h"
#include "network.h"
#include "input.h"
#include "video_decoder.h"
#include "video_surface.h"


//memset(inbuf+INBUF_SIZE, 0, FF_INPUT_BUFFER_PADDING_SIZE);


int main(int argc, char *argv[]) 
{

	// Declare display mode structure to be filled in.
	SDL_DisplayMode current;





	// set log level

	SDL_LogSetAllPriority(SDL_LOG_PRIORITY_INFO);

	// default value workaround
	//
	configuration = malloc(sizeof(Configuration));
	configuration->screen = malloc(sizeof(Screen));
	configuration->codec = malloc(sizeof(Codec));
	configuration->server = malloc(sizeof(Server));
	configuration->maxScreenSize = malloc(sizeof(Screen));
	configuration->screen->width = 800;
	configuration->maxScreenSize->height = 600;
	configuration->maxScreenSize->width = 800;
	configuration->screen->height = 600;
	configuration->codec->width = 800;
	configuration->codec->height = 600;
	configuration->bandwidth = 1000000;
	configuration->fps = 25;

	ctrl_press = false;
	alt_press = false;
	quit = false;
	screen_is_fullscreen = 0;
	close_video_thread = false;
	// parsing arguments

	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION,"init() \n");

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

	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "parameters hostname : %s, port : %d, video resolution : %s, bandwidth : %d, fps : %d \n", 
			configuration->server->hostname, 
			configuration->server->port,
			video_definition, 
			configuration->bandwidth, 
			configuration->fps);

	if(video_definition != NULL)
	{
		if(strcmp("720p", video_definition) == 0)
		{
			configuration->screen->width = configuration->codec->width = 1280;
			configuration->screen->height = configuration->codec->height = 720;
			SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "swicth video resolution to %dx%d \n", configuration->codec->width, configuration->codec->height);
		}
		if(strcmp("800p", video_definition) == 0)
		{
			configuration->screen->width = configuration->codec->width = 1280;
			configuration->screen->height = configuration->codec->height = 800;
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

	int should_be_zero = SDL_GetCurrentDisplayMode(0, &current);

	if(should_be_zero != 0 ){
		SDL_LogError(SDL_LOG_CATEGORY_APPLICATION, "Could not get Current display resolution %s", SDL_GetError());
	}else {
		configuration->maxScreenSize->width = current.w;
		configuration->maxScreenSize->height = current.h;
		SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "max screen resolution, width : %d, height: %d",configuration->maxScreenSize->width, configuration->maxScreenSize->height);

	}
	//init network and send start packet
	if(init_network()) 
	{
		// TODO init network error
		SRD_exit();
	}
	init_video(configuration->screen->width, configuration->screen->height); //FIXME return status code
	SRD_start_video();
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "start input event loop\n "); //EVENT LOOP FOR CATCH INPUT EVENT //TODO REFACTOR
	for(;;) {

		// get event from loop // TODO check code
		get_input_event();


		if(quit)
			break;
	}
	return 0;

}

void SRD_start_video()
{
	thread = SDL_CreateThread(video_thread, "video_thread", configuration);
	SRDNet_send_start_packet();
}

void SRD_init_renderer(Configuration* configuration)
{
	SRD_init_renderer_texture(configuration->screen->width, configuration->screen->height);
	init_video_decoder(configuration->codec->width, configuration->codec->height, configuration->screen->width, configuration->screen->height);


}

void SRD_close_renderer(Configuration* configuration)
{
	destroy_decoder();
	destroy_texture();
}

void SRD_exit()
{
	exit(1);
}

int video_thread(void* configuration) 
{
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "Starting video thread");	
	SRD_init_renderer(configuration);

	close_video_thread = false;
	while(close_video_thread == false)
	{
		av_init_packet(&packet);


		Video_Frame* frame = pop_from_video_fifo();
		if(frame != NULL)
		{
		
			SDL_LogVerbose(SDL_LOG_CATEGORY_APPLICATION, "frame number : %d, frame size : %d", frame->number, frame->length);
			// decode frame from video_decoder
			decode_video_frame(frame->data, frame->length, configuration); 
			// update sdl texture with video_surface
			update_video_surface(); 
			//free(frame); //FIXME

		}

	}
//	SRDNet_Empty_input_buffer();
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "finish and cleaning video thread");
	SRD_close_renderer(configuration);

	return 0;
}



