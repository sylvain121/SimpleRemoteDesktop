# SimpleRemoteDesktop client

client application based on sdl2

## requirements

ubuntu package : 
```bash
sudo apt-get install build-essential libavutil-dev libavcodec-dev libswscale-dev libx264-dev libsdl2-dev libsdl2-net-dev libavformat-dev
```
windows : 
```bash
sudo apt-get install mingw-w64
```

## build for windows
64bit system use Makefile_64bit_window_platform file. 
32bit system use Makefile

Type make cmd will generate the client.exe

## run the client
copy the zip file into windows machine and run client.exe

```bash
./client hostname port 720p 10000000 30
```

### arguments list
1. hostname : distant server hostname or ip.(required);
2. port : distant server tcp port. (required);
3. video resolution : 720p or 1080p ( 1080p is currently buggy ).( optional, default:800x600 )
4. bandwidth : bandwidth of the video stream in bit( optional, default : 10Mbit )
5. framerate : video refreash rate in frame by second( optional, default : 25 );

