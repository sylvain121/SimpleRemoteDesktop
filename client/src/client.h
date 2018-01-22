#include <SDL2/SDL.h>
#include <SDL2/SDL_net.h>
#include <SDL2/SDL_thread.h>
#include <stdbool.h>
#include<stdint.h>
#include<time.h>



SDL_Thread *thread;
int video_thread(void* data); 
void SRD_init_renderer();
void SRD_close();
void SRD_start_video();
bool close_video_thread;
void SRD_exit();

