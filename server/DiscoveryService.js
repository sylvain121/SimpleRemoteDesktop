var os = require("os");
var Netmask = require('netmask').Netmask;
var _ = require('lodash');
// var PORT = 8002;
var BROADCAST_ADDR = "58.65.67.255";
var dgram = require('dgram'); 
var server = dgram.createSocket("udp4");
var message = new Buffer(os.hostname()+";"+os.platform());
var interval = null;
const { log } = require('./logger.js');

var broadcastAddressArray = [];
var netInterface  = os.networkInterfaces();


module.exports.start = function(PORT) {
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
		interval = setInterval(broadcastNew, 3000);

	});

	function broadcastNew() {
		broadcastAddressArray.forEach(function(address){
			server.send(message, 0, message.length, PORT, address,  function() {
				log.debug("Discovery service : Sent '" + message + "'");

			});

		})

	}

}




module.exports.stop = function() {
	clearInterval(interval);
}
