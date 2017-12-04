#include <SDL2/SDL.h>
#include <stdbool.h>
#include<stdint.h>
#include<time.h>
#include <stdio.h>


bool ctrl_press;
bool alt_press;
SDL_Event userEvent;
bool quit;


bool screen_is_fullscreen;


void get_input_event();
