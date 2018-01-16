#include <SDL2/SDL.h>

SDL_Window     *screen;
SDL_Texture   *bmp;
SDL_Renderer *renderer;
SDL_Event       event;



void init_video_surface(int screen_width, int screen_height);
void update_video_surface(); 

