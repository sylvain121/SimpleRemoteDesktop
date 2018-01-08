const bunyan = require('bunyan');
const log = bunyan.createLogger({name: 'SimpleDesktopStreamer', level: 'error'});

module.exports.log = log;
