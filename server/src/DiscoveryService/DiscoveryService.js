var os = require("os");
var Netmask = require('netmask').Netmask;
var _ = require('lodash');
var PORT = 8002;
var BROADCAST_ADDR = "58.65.67.255";
var dgram = require('dgram'); 
var server = dgram.createSocket("udp4");
var message = new Buffer(os.hostname()+";"+os.platform());

var broadcastAddressArray = [];
var netInterface  = os.networkInterfaces();

_.forIn(netInterface, function(value, key){
	value.forEach(function(elem){
		if(elem.family === "IPv4" && elem.internal === false) {
			var range = new Netmask(elem.address+"/"+elem.netmask);
			broadcastAddressArray.push(range.broadcast);
		}

	})

})



server.bind(function() {
	server.setBroadcast(true);
	setInterval(broadcastNew, 3000);

});

function broadcastNew() {
	broadcastAddressArray.forEach(function(address){
		server.send(message, 0, message.length, PORT, address,  function() {
//			console.log("Sent '" + message + "'");

		});

	})

}
