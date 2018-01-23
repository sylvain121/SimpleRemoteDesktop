#include <stdbool.h>
#include<stdint.h>
#include<time.h>

#include <SDL2/SDL.h>
#include <SDL2/SDL_net.h>
#include <SDL2/SDL_thread.h>

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


typedef struct 
{
	int number;
	int length;
	uint8_t* data;
	
} Video_Frame;

typedef struct Video_Frame_Element Video_Frame_Element;
struct Video_Frame_Element
{
	Video_Frame *frame;
	Video_Frame_Element * next;

};

typedef struct
{
	Video_Frame_Element * first;
	int length;

} Video_Buffer;

IPaddress ip;
TCPsocket control_socket;
Video_Buffer *video_fifo;
SDL_Thread *netThread;

void SRDNet_Empty_input_buffer();
int init_network();
int SRDNet_get_frame_number();
int SRDNet_get_frame_length();
void SRD_ensure(int nbytes);
int SRD_readUInt32();
uint8_t * SRD_read(int nbytes);
int SRDNet_send_start_packet(); 
int SRDNet_send_stop_packet();
Video_Frame* pop_from_video_fifo();
void push_to_video_fifo(Video_Frame_Element * element);
int network_thread(void* configuration); 
void clean_video_fifo();
