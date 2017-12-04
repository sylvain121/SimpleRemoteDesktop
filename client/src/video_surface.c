#include "config.h"
#include "video_surface.h"
#include "video_decoder.h"

void init_video_surface(int screen_width, int screen_height)
{
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



}

void update_video_surface() 
{
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
