#!/usr/bin/env node

import { program } from 'commander';

import { SimpleRemoteDesktop, ScreenOffsetOption } from "./core/SimpleRemoteDesktop";
//import './DiscoveryService/DiscoveryService';

program.version("0.0.1")
    .option("-p, --port <port>", "listening port", "8001")
    .option("-w, --enable-webrtc", "enable web rtc connection mode", "false")
    .option('-w, --password <password>', 'set password for webrtc access', undefined)
    .option("-d, --enable-discovery", "enable discovery service ", "false")
    .option("-r, --readonly", "disable input", "false")
    .option("-c, --clip <clip>", "garb part of screen x+y:W+H", undefined)
    .parse(process.argv);



const app = new SimpleRemoteDesktop(program.port, program.enableWebrtc, program.password, program.readonly, getCLipping(program.clip));



function getCLipping(data: string): ScreenOffsetOption {
    const [offset, dimension] = data.split(":");
    const [xoffset, yoffset] = offset.split("+");
    const [width, height] = dimension.split("+");

    const obj = new ScreenOffsetOption(
        parseInt(xoffset, 10),
        parseInt(yoffset, 10),
        parseInt(width, 10),
        parseInt(height, 10));

        console.log(obj);
        return obj
}