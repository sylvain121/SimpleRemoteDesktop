sudo apt-get install libsdl2-dev libsdl2-net-dev libavformat-dev
gcc -o client src/client.c src/input.c src/video_decoder.c src/video_surface.c src/network.c src/keysym_converter.c -Wall -lavutil -lavformat -lavcodec -lz -lavutil -lm -lswscale -g -lSDL2 -lSDL2_net

