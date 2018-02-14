
#include "config.h"
#include "input.h"
#include "network.h"
#include "video_surface.h"
#include "keysym_converter.h"

void get_input_event()
{
    if(SDL_WaitEvent(&userEvent)) {
		    SDL_Delay(10);    
                    struct Message send; 
        
                    switch(userEvent.type) {
			    case SDL_QUIT: 
				    quit = true;
				    break;

			    case SDL_KEYDOWN: 
				    //printf("pressed key %d\n", userEvent.key.keysym.sym);
				    send.type = TYPE_KEY_DOWN;
				    if(userEvent.key.keysym.sym == 1073742048){
					    ctrl_press = true;
				    } 
				    if(userEvent.key.keysym.sym == 1073742050){
					    alt_press = true;
				    } 
				    if(userEvent.key.keysym.sym == 113 && ctrl_press && alt_press){
					    quit = true;
				    } else 	if(userEvent.key.keysym.sym == 102 && ctrl_press && alt_press){
					    if(screen_is_fullscreen) 
					    {
						    screen_is_fullscreen = false;
						    SDL_SetWindowFullscreen(screen, 0);
						    SRD_UpdateScreenResolution();

					    } 
					    else
					    {
						    screen_is_fullscreen = true;
						    SDL_SetWindowFullscreen(screen, SDL_WINDOW_FULLSCREEN_DESKTOP);
						    SRD_UpdateScreenResolution();
					    } 

				    } else {

					    send.keycode = get_keysym(userEvent.key.keysym.sym);
					    SDLNet_TCP_Send(control_socket, (void * )&send, sizeof(send));
				    } 


				    break;

			    case SDL_KEYUP: 
				    //printf("released key %d\n", userEvent.key.keysym.sym);
				    send.type = TYPE_KEY_UP;
				    if(userEvent.key.keysym.sym == 1073742048){
					    ctrl_press = false;
				    } 
				    if(userEvent.key.keysym.sym == 1073742050){
					    alt_press = false;
				    }

				    send.keycode = get_keysym(userEvent.key.keysym.sym);
				    SDLNet_TCP_Send(control_socket, (void * )&send, sizeof(send));
				    break;				

			    case SDL_MOUSEMOTION: 
				    //printf("mouse position x: %d, y: %d \n", userEvent.motion.x, userEvent.motion.y);
				    send.type = TYPE_MOUSE_MOTION;
				    int w, h;
				    SDL_GetWindowSize(screen, &w, &h);

				    send.x =  ( (float) userEvent.motion.x / (float) w);
				    send.y = ( (float) userEvent.motion.y / (float) h);
				    SDLNet_TCP_Send(control_socket, (void * )&send, sizeof(send));
				    break;
			    case SDL_MOUSEBUTTONDOWN: {
							      send.type = TYPE_MOUSE_DOWN;
							      switch(userEvent.button.button) {
								      case SDL_BUTTON_LEFT: {
												    //printf("left click down\n");
												    send.button = 1;
												    SDLNet_TCP_Send(control_socket, (void * )&send, sizeof(send));
												    break;
											    }
								      case SDL_BUTTON_RIGHT: {
												     //printf("right click down\n");
												     send.button = 3;
												     SDLNet_TCP_Send(control_socket, (void * )&send, sizeof(send));
												     break;
											     }
								      case SDL_BUTTON_MIDDLE: {
												      //printf("middle click down\n");
												      send.button = 2;
												      SDLNet_TCP_Send(control_socket, (void * )&send, sizeof(send));
												      break;
											      }
							      }
							      SDLNet_TCP_Send(control_socket, (void * )&send, sizeof(send));
							      break;
						      }

			    case SDL_MOUSEBUTTONUP:    {
							       send.type = TYPE_MOUSE_UP;
							       switch(userEvent.button.button) {
								       case SDL_BUTTON_LEFT: {
												     //printf("left click released\n");
												     send.button = 1;
												     SDLNet_TCP_Send(control_socket, (void * )&send, sizeof(send));
												     break;
											     }
								       case SDL_BUTTON_RIGHT: {
												      //printf("right click released\n");
												      send.button = 3;
												      SDLNet_TCP_Send(control_socket, (void * )&send, sizeof(send));
												      break;
											      }
								       case SDL_BUTTON_MIDDLE: {
												       //printf("middle click released\n");
												       send.button = 2;
												       SDLNet_TCP_Send(control_socket, (void * )&send, sizeof(send));
												       break;
											       }
							       }
							       SDLNet_TCP_Send(control_socket, (void * )&send, sizeof(send));
							       break;
						       }
                    }
    }
} 
