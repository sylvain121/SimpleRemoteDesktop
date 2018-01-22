#include "config.h"
#include "client.h"
#include "video_surface.h"
#include "video_decoder.h"
#include "network.h"

void init_video(int screen_width, int screen_height)
{
	// Make a screen to put our video
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, " Creating SDL windows size %dx%d", screen_width, screen_height);
	screen = SDL_CreateWindow(
			"StreamMyDesktop Client",
			SDL_WINDOWPOS_UNDEFINED,
			SDL_WINDOWPOS_UNDEFINED,
			screen_width,
			screen_height,
			0);


	renderer = SDL_CreateRenderer(screen, -1, 0);
	if (!renderer) {
		SDL_LogError(SDL_LOG_CATEGORY_APPLICATION, "SDL: could not create renderer - exiting\n");
		exit(1);

	}


}

void SRD_init_renderer_texture(int screen_width, int screen_height)
{
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "Creating Windows Texture %dx%d", screen_width, screen_height);
	bmp = SDL_CreateTexture(
			renderer,
			SDL_PIXELFORMAT_YV12,
			SDL_TEXTUREACCESS_STREAMING,
			screen_width,
			screen_height
			);

	// set up YV12 pixel array (12 bits per pixel)
	yPlaneSz = screen_width * screen_height;
	uvPlaneSz = screen_width * screen_height / 4;
	yPlane = (Uint8*)malloc(yPlaneSz);
	uPlane = (Uint8*)malloc(uvPlaneSz);
	vPlane = (Uint8*)malloc(uvPlaneSz);
	if (!yPlane || !uPlane || !vPlane) {
		SDL_LogError(SDL_LOG_CATEGORY_APPLICATION, "Could not allocate pixel buffers - exiting\n");
		exit(1);
	}

	uvPitch = screen_width/ 2;



}

void destroy_texture()
{
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "Deleting SDL texture");
	SDL_DestroyTexture(bmp);
}

void update_video_surface() 
{
	SDL_UpdateYUVTexture(
			bmp,
			NULL,
			yPlane,
			configuration->screen->width,
			uPlane,
			uvPitch,
			vPlane,
			uvPitch

			);

	SDL_RenderClear(renderer);
	SDL_RenderCopy(renderer, bmp, NULL, NULL);
	SDL_RenderPresent(renderer);	


}
void SRD_UpdateScreenResolution() 
{
	int w, h;
	SDL_GetRendererOutputSize(renderer,&w, &h);
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, " switching resolution to %dx%d", w, h);
	
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "halt video loop");
	close_video_thread = true;

	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "request stop Streaming");
	SRDNet_send_stop_packet();
	
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "waiting to thread stop");
	SDL_WaitThread(thread, NULL);

	
	clean_video_fifo(); //FIXME

	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "trying to set screen size to width : %d, height : %d", w, h);
	configuration->screen->width = w;
	configuration->screen->height = h;
	
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "trying to start new video thread");
	SRD_start_video();

}
