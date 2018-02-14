#include "keysym_converter.h"



void keysym_init() 
{

	keysym_first = NULL;	
	keysym_last = NULL;


	keysym_add(SDLK_BACKSPACE, XK_BackSpace);
	keysym_add(SDLK_TAB, XK_Tab);
	keysym_add(SDLK_CLEAR, XK_Clear);
	keysym_add(SDLK_RETURN, XK_Return);
	keysym_add(SDLK_PAUSE, XK_Pause);
	keysym_add(SDLK_ESCAPE, XK_Escape);

	int i;
	for(i = 0x20; i <= 0x7a; i++) 
	{
		keysym_add(i, i);
	}
	keysym_add(SDLK_DELETE, XK_Delete);
	keysym_add(SDLK_KP0, XK_KP_0);
	keysym_add(SDLK_KP1, XK_KP_1);
	keysym_add(SDLK_KP2, XK_KP_2);
	keysym_add(SDLK_KP3, XK_KP_3);
	keysym_add(SDLK_KP4, XK_KP_4);
	keysym_add(SDLK_KP5, XK_KP_5);
	keysym_add(SDLK_KP6, XK_KP_6);
	keysym_add(SDLK_KP7, XK_KP_7);
	keysym_add(SDLK_KP8, XK_KP_8);
	keysym_add(SDLK_KP9, XK_KP_9);
	keysym_add(SDLK_KP_PERIOD, XK_KP_Delete);
	keysym_add(SDLK_KP_DIVIDE, XK_KP_Divide);
	keysym_add(SDLK_KP_MULTIPLY, XK_KP_Multiply);
	keysym_add(SDLK_KP_MINUS, XK_KP_Subtract);
	keysym_add(SDLK_KP_PLUS, XK_KP_Add);
	keysym_add(SDLK_KP_ENTER, XK_KP_Enter);
	keysym_add(SDLK_KP_EQUALS, XK_KP_Equal);
	keysym_add(SDLK_UP, XK_Up);
	keysym_add(SDLK_DOWN, XK_Down);
	keysym_add(SDLK_RIGHT, XK_Right);
	keysym_add(SDLK_LEFT, XK_Left);
	keysym_add(SDLK_INSERT, XK_Insert);
	keysym_add(SDLK_HOME, XK_Home);
	keysym_add(SDLK_END, XK_End);
	keysym_add(SDLK_PAGEUP, XK_Page_Up);
	keysym_add(SDLK_PAGEDOWN, XK_Page_Down);
	keysym_add(SDLK_F1, XK_F1);
	keysym_add(SDLK_F2, XK_F2);
	keysym_add(SDLK_F3, XK_F3);
	keysym_add(SDLK_F4, XK_F4);
	keysym_add(SDLK_F5, XK_F5);
	keysym_add(SDLK_F6, XK_F6);
	keysym_add(SDLK_F7, XK_F7);
	keysym_add(SDLK_F8, XK_F8);
	keysym_add(SDLK_F9, XK_F9);
	keysym_add(SDLK_F10, XK_F10);
	keysym_add(SDLK_F11, XK_F11);
	keysym_add(SDLK_F12, XK_F12);
	keysym_add(SDLK_F13, XK_F13);
	keysym_add(SDLK_F14, XK_F14);
	keysym_add(SDLK_F15, XK_F15);
	keysym_add(SDLK_NUMLOCK, XK_Num_Lock);
	keysym_add(SDLK_CAPSLOCK, XK_Caps_Lock);
	keysym_add(SDLK_SCROLLOCK, XK_Scroll_Lock);
	keysym_add(SDLK_RSHIFT, XK_Shift_R);
	keysym_add(SDLK_LSHIFT, XK_Shift_L);
	keysym_add(SDLK_RCTRL, XK_Control_R);
	keysym_add(SDLK_LCTRL, XK_Control_L);
	keysym_add(SDLK_RALT, XK_Alt_R);
	keysym_add(SDLK_LALT, XK_Alt_L);
	keysym_add(SDLK_RMETA, XK_Meta_R);
	keysym_add(SDLK_LMETA, XK_Meta_L);
	keysym_add(SDLK_HELP, XK_Help);
	keysym_add(SDLK_SYSREQ, XK_Sys_Req);
	keysym_add(SDLK_BREAK, XK_Break);
	keysym_add(SDLK_MENU,  XK_Menu);
}

void keysym_add(int sdl, int keysym)
{
	KeySym* key = malloc(sizeof(KeySym));
	key->sdl = sdl;
	key->keysym = keysym;

	KeySymElement* elem = malloc(sizeof(KeySymElement));
	elem->keysym = key;
	elem->next = NULL;

	if(keysym_first == NULL)
	{
		keysym_first = elem;
		keysym_last = elem;
	} else {
		keysym_last->next = elem;
		keysym_last = elem;
	}


}


int get_keysym(int sdl)
{
	KeySymElement* current = keysym_first;
	while(current->keysym->sdl != sdl) {
		if(current->next == NULL)
		{
			SDL_LogInfo(SDL_LOG_CATEGORY_ERROR, "end of keysym list reached");
			return -1;
		}
		current = current->next;
	}
	return current->keysym->keysym;
}
