#include <SDL2/SDL.h>
#include <SDL2/SDL_net.h>
#include <SDL2/SDL_thread.h>

SDL_Window     *screen;
SDL_Texture   *bmp;
SDL_Renderer *renderer;
SDL_Event       event;



void init_video(int screen_width, int screen_height);
void update_video_surface(); 
void destroy_texture();
void SRD_init_renderer_texture(int w, int h);
void SRD_UpdateScreenResolution();
