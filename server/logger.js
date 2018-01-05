const bunyan = require('bunyan');
const log = bunyan.createLogger({name: 'SimpleDesktopStreamer', level: 'debug'});

module.exports.log = log;
