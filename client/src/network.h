#include <stdbool.h>
#include<stdint.h>
#include<time.h>

#include <SDL2/SDL.h>
#include <SDL2/SDL_net.h>

#ifdef __MINGW32__
#undef main /* Prevents SDL from overriding main() */
#endif

#include <stdio.h>


#define INBUF_SIZE 1000000
#define FF_INPUT_BUFFER_PADDING_SIZE 32

uint8_t inbuf[INBUF_SIZE + FF_INPUT_BUFFER_PADDING_SIZE];
int inbuf_average;



enum type {TYPE_KEY_DOWN=1, TYPE_KEY_UP=2, TYPE_MOUSE_MOTION=3, TYPE_MOUSE_DOWN=4, TYPE_MOUSE_UP=5 , TYPE_ENCODER_START=6, TYPE_ENCODER_STOP=7 };

struct Message
{
	int type;
	float x;
	float y;
	int button;
	int keycode;
	int codec_width;
	int codec_height;
	int bandwidth;
	int fps;
	int sdl;
};


IPaddress ip;
TCPsocket control_socket;



int init_network();
int SRDNet_get_frame_number();
int SRDNet_get_frame_length();
void SRD_ensure(int nbytes);
int SRD_readUInt32();
uint8_t * SRD_read(int nbytes);
