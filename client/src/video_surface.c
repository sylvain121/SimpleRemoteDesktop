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


}

void SRD_init_renderer_texture(int w, int h)
{
	renderer = SDL_CreateRenderer(screen, -1, 0);
	if (!renderer) {
		SDL_LogError(SDL_LOG_CATEGORY_APPLICATION, "SDL: could not create renderer - exiting\n");
		exit(1);

	}


	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "Creating Windows Texture %dx%d", w, h);
	bmp = SDL_CreateTexture(
			renderer,
			SDL_PIXELFORMAT_YV12,
			SDL_TEXTUREACCESS_STREAMING,
			w,
			h
			);

	// set up YV12 pixel array (12 bits per pixel)
	yPlaneSz = w * h;
	uvPlaneSz = w * h / 4;
	yPlane = (Uint8*)malloc(yPlaneSz);
	uPlane = (Uint8*)malloc(uvPlaneSz);
	vPlane = (Uint8*)malloc(uvPlaneSz);
	if (!yPlane || !uPlane || !vPlane) {
		SDL_LogError(SDL_LOG_CATEGORY_APPLICATION, "Could not allocate pixel buffers - exiting\n");
		exit(1);
	}

	uvPitch = w / 2;



}

void destroy_texture()
{
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "Deleting SDL texture");
	SDL_DestroyTexture(bmp);
	SDL_DestroyRenderer(renderer);
}

void update_video_surface() 
{
	if(bmp != NULL) 
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


}
void SRD_UpdateScreenResolution() 
{
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "======================= SWITCHING DISPLAYM MODE ===========================");
	int w, h;
	SDL_GetWindowSize(screen, &w, &h);
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "codec resolution %dx%d , screen size : %dx%d",
			configuration->codec->width,
			configuration->codec->height,
			w, h);
	float x_ratio = (float)w / (float)configuration->codec->width;
	float y_ratio = (float)h / (float)configuration->codec->height;
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, " new ratio x : %f, y %f", x_ratio, y_ratio);
	destroy_texture();
	SRD_init_renderer_texture(
			configuration->codec->width,
			configuration->codec->height
			);
	SDL_RenderSetScale(renderer, x_ratio, y_ratio);
}
