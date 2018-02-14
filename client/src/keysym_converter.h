
#include <SDL2/SDL.h>
#include <X11/keysym.h>

#define	SDLK_KP0	SDLK_KP_0
#define	SDLK_KP1	SDLK_KP_1
#define	SDLK_KP2	SDLK_KP_2
#define	SDLK_KP3	SDLK_KP_3
#define	SDLK_KP4	SDLK_KP_4
#define	SDLK_KP5	SDLK_KP_5
#define	SDLK_KP6	SDLK_KP_6
#define	SDLK_KP7	SDLK_KP_7
#define	SDLK_KP8	SDLK_KP_8
#define	SDLK_KP9	SDLK_KP_9
#define SDLK_NUMLOCK	SDLK_NUMLOCKCLEAR
#define SDLK_SCROLLOCK	SDLK_SCROLLLOCK
#define SDLK_RMETA	SDLK_RGUI
#define SDLK_LMETA	SDLK_LGUI
//#define SDLK_LSUPER
//#define SDLK_RSUPER
//#define SDLK_COMPOSE
//#define SDLK_PRINT
#define SDLK_BREAK	SDLK_PRINTSCREEN

typedef struct KeySym KeySym;

struct KeySym 
{
	int sdl;
	int keysym;
};


typedef struct KeySymElement KeySymElement;

struct KeySymElement
{
	KeySym* keysym;
	KeySymElement* next;	
};

KeySymElement* keysym_first;
KeySymElement* keysym_last;

void keysym_init();
void keysym_add(int sdl, int keysym);
int get_keysym(int sdlkey);
