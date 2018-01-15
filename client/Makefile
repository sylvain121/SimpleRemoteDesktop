CURRENT_DIR = $(shell pwd)
LIBDIR = $(CURRENT_DIR)/windows/lib
CFLAGS = -I$(CURRENT_DIR)/windows/include
BINDIR = $(CURRENT_DIR)/windows/bin
LDFLAGS = -L$(BINDIR)

all : clean SDL2 SDL2_net ffmpeg client dist

SDL2:
	wget https://www.libsdl.org/release/SDL2-2.0.7.tar.gz
	tar -zxvf SDL2-2.0.7.tar.gz
	cd SDL2-2.0.7/;	./configure --prefix=$(shell pwd)/windows --host=i686-w64-mingw32
	make -C SDL2-2.0.7/ -j24
	make -C SDL2-2.0.7/ install
	rm -Rf SDL2-2.0.7*

SDL2_net:
	wget https://www.libsdl.org/projects/SDL_net/release/SDL2_net-2.0.1.tar.gz
	tar -zxvf SDL2_net-2.0.1.tar.gz
	cd SDL2_net-2.0.1/; ./configure --prefix=$(shell pwd)/windows --host=i686-w64-mingw32 --with-sdl-prefix=$(shell pwd)/windows/
	make -C SDL2_net-2.0.1/ -j24
	make -C SDL2_net-2.0.1/ install
	rm -Rf SDL2_net-2.0.1*

ffmpeg:
	wget http://ffmpeg.org/releases/ffmpeg-3.4.tar.bz2
	tar -jxvf ffmpeg-3.4.tar.bz2
	cd ffmpeg-3.4; ./configure --arch=x86 --target-os=mingw32 --cross-prefix=i686-w64-mingw32- --enable-shared --prefix=$(shell pwd)/windows
	make -C ffmpeg-3.4/ -j24
	make -C ffmpeg-3.4/ install
	rm -Rf ffmpeg-3.4*

client:
	i686-w64-mingw32-gcc $(CFLAGS) $(LDFLAGS) -static-libgcc -static-libstdc++ -o $(shell pwd)/windows/bin/client.exe src/*.c -lavformat -lavcodec -lavutil -lswscale -lSDL2 -lSDL2_net

clean:
	rm -Rf windows

dist:
	rm -Rf out/
	mkdir out
	$(eval OUT = $(shell pwd)/out/)
	cp $(BINDIR)/client.exe $(OUT)
	cp $(BINDIR)/*.dll $(OUT)
	cp dll/*.dll $(OUT)
	cd out; zip ../client_windows.zip *
	rm -Rf out/
