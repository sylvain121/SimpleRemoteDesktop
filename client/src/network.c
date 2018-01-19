#include "config.h"
#include "network.h"


int init_network()
{

	inbuf_average = 0;

	if(SDLNet_Init() < 0 ) {
		SDL_LogError(SDL_LOG_CATEGORY_ERROR, "SDLNet_Init: %s\n", SDLNet_GetError());
		return 0;
	}

	if(SDLNet_ResolveHost(&ip, configuration->server->hostname, configuration->server->port) < 0) {
		SDL_LogError(SDL_LOG_CATEGORY_ERROR, "unable to resolve address %s , port %s \n", 
				configuration->server->hostname,
			       	configuration->server->port);
		return 0;
	} 

	if(!(control_socket = SDLNet_TCP_Open(&ip))) {
		 SDL_LogError(SDL_LOG_CATEGORY_ERROR,"SDLNet_TCP_Open: %s\n", SDLNet_GetError());
		return 0;
	}
	return 0;
}

int SRDNet_send_start_packet() 
{
	// inital packet with information
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
	init.sdl = 1;
	SDL_LogVerbose(SDL_LOG_CATEGORY_APPLICATION, "sending init frame : type: %d, fps: %d, codec width: %d, codec height: %d, bandwidth: %d", init.type, init.fps, init.codec_width, init.codec_height, init.bandwidth);
	SDLNet_TCP_Send(control_socket, (void * )&init, sizeof(init));
	return 0;

}

int SRDNet_send_stop_packet() 
{
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
			     return 0;
			
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
