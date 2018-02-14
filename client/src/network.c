#include "config.h"
#include "network.h"


void init_video_fifo()
{
	video_fifo = malloc(sizeof(Video_Buffer));
	video_fifo->first = NULL;
	video_fifo->length = 0;	
}

int init_network()
{

	init_video_fifo();
	inbuf_average = 0;
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "Init network interface");
	if(SDLNet_Init() < 0 ) {
		SDL_LogError(SDL_LOG_CATEGORY_ERROR, "SDLNet_Init: %s\n", SDLNet_GetError());
		return 0;
	}

	if(SDLNet_ResolveHost(&ip, configuration->server->hostname, configuration->server->port) < 0) {
		SDL_LogError(SDL_LOG_CATEGORY_ERROR, "unable to resolve address %s , port %d\n", 
				configuration->server->hostname,
			       	configuration->server->port);
		return 0;
	} 
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "connecting to server");
	if(!(control_socket = SDLNet_TCP_Open(&ip))) {
		 SDL_LogError(SDL_LOG_CATEGORY_ERROR,"SDLNet_TCP_Open: %s\n", SDLNet_GetError());
		return 0;
	}
	
	netThread = SDL_CreateThread(network_thread, "network_thread", configuration);
	return 0;
}

int SRDNet_send_start_packet() 
{
	// inital packet with information
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, " network : sending start packet");
	struct Message init;
	init.type = TYPE_ENCODER_START;
	init.x = 1;
	init.y = 1;
	init.button = 1;
	init.keycode = 1;
	init.fps = configuration->fps;
	init.codec_width = configuration->codec->width;
	init.codec_height = configuration->codec->height;
	init.bandwidth = configuration->bandwidth;
	init.sdl = 0;
	SDL_LogVerbose(SDL_LOG_CATEGORY_APPLICATION, "sending init frame : type: %d, fps: %d, codec width: %d, codec height: %d, bandwidth: %d", init.type, init.fps, init.codec_width, init.codec_height, init.bandwidth);
	SDLNet_TCP_Send(control_socket, (void * )&init, sizeof(init));
	return 0;

}

int SRDNet_send_stop_packet() 
{
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, " network : sending stop packet");
	struct Message stop;
	stop.type = TYPE_ENCODER_STOP;
	SDL_LogVerbose(SDL_LOG_CATEGORY_APPLICATION, "sending stop frame"); 
	SDLNet_TCP_Send(control_socket, (void * )&stop, sizeof(stop));
	return 0;

}

int SRDNet_get_frame_number()
{
	SRD_ensure(4);
	return SRD_readUInt32();

}

int SRDNet_get_frame_length()
{
	SRD_ensure(4);
	return SRD_readUInt32();
}


void SRDNet_Empty_input_buffer()
{
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "cleaning input tcp buffer");
	char net_in[2048];
	int i;

	do {
		i = SDLNet_TCP_Recv(control_socket, net_in, 1024);
		SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "%d bytes remains", i);
	}
	while( i > 0 );
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "cleaned");

}

void SRD_ensure(int nbytes )
{
	char net_in[nbytes+1];
	do
	{
		int net_lenght = SDLNet_TCP_Recv(control_socket, net_in, nbytes); //FIXME : adjust max data
		if(net_lenght <= 0) {
			// TCP Connection is broken. (because of error or closure)
			     SDLNet_TCP_Close(control_socket);
			     SDL_LogError(SDL_LOG_CATEGORY_ERROR, "Broken socket disconnected");    
			
		}
		else {
			memcpy(inbuf+inbuf_average, net_in, net_lenght);
			inbuf_average = net_lenght + inbuf_average;
			SDL_LogVerbose(SDL_LOG_CATEGORY_APPLICATION, "reading %d bytes from network. buffer average : %d\n", net_lenght, inbuf_average);
		}
	}

	while( inbuf_average < nbytes );

}

int SRD_readUInt32()
{
	uint8_t *data = SRD_read(4);
	SDL_LogVerbose(SDL_LOG_CATEGORY_APPLICATION, " %x %x %x %x \n", data[0], data[1], data[2], data[3]);
	uint32_t num = 
		(uint32_t)data[0] << 24 |
		(uint32_t) data[1] << 16 |
		(uint32_t) data[2] << 8  |
		(uint32_t) data[3];

	return num;
}

uint8_t * SRD_read(int nbytes)
{
	SDL_LogVerbose(SDL_LOG_CATEGORY_APPLICATION, "average byte : %d, read bytes : %d \n", inbuf_average, nbytes);
	uint8_t *data = malloc(sizeof(uint8_t)* nbytes);
	memcpy(data, inbuf, nbytes);	
	memcpy(inbuf, inbuf+nbytes, inbuf_average - nbytes);
	inbuf_average -= nbytes;
	return data;
}




int network_thread(void* configuration) 
{
	SDL_LogInfo(SDL_LOG_CATEGORY_APPLICATION, "starting network thread");
	while(true) 
	{
		Video_Frame *frame = malloc(sizeof(Video_Frame));
		
		// get frame from network
		frame->number = SRDNet_get_frame_number();
		frame->length =  SRDNet_get_frame_length();

		SRD_ensure(frame->length);
		frame->data = SRD_read(frame->length);

		Video_Frame_Element * element = malloc(sizeof(Video_Frame_Element));
		element->frame = NULL;
		element->next = NULL;
		element->frame = frame;
		push_to_video_fifo(element);


	}
}



void push_to_video_fifo(Video_Frame_Element * element)
{
	if(video_fifo->first != NULL) {
		Video_Frame_Element * current = video_fifo->first;
		while(current->next != NULL) 
		{
			current = current->next;
		}
		current->next = element;

	}
	else 
	{
		video_fifo->first = element;
	}
	video_fifo->length++;
}

Video_Frame* pop_from_video_fifo()
{
	if(video_fifo->first != NULL)
	{
		Video_Frame_Element* element = video_fifo->first;
		Video_Frame* frame = element->frame;
		video_fifo->first = element->next;
		video_fifo->length--;
		free(element);
		return frame;


	} 
	else
	{
		return NULL;
	}

}

void clean_video_fifo()
{
	video_fifo->first = NULL; //FIXME
}
