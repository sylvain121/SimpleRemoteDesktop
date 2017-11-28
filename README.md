# SimpleRemoteDesktop

Simple remote desktop is an remote desktop client server application base on h264 protocol.
This application is experiental and developped on my personnal time.
Issue and push request are welcome.

## Server

Serveur side work actually with nodejs and Linux/X11 only.
See readme file in server folder to know requirements for building it.

```bash
 # in server folder : 
 nodejs main.js
```


## Client

Client side is based on SDL2.
it could be multiplaform, but I have only tested it on linux.
See readme file on client folder to know requirement for building it.

```bash
 # in client folder : 
 ./client hostname port 720p 10000 30
```



## Currently features

* only X11 server
* server encode h264 frame video via libx264
* low latency in 720p
* mouse and keyboard support
* no sound







## todo


## futures
