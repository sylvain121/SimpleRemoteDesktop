const _ = require('lodash');
var key = [];

module.exports = {

	logKeyDown: function( keysym) {
		key.push({key: keysym});

	}

	reset: function( resetFunction ) {
		_.each(key, k => {
			return resetFunction(k.key, false);
		});
		key = [];
	}

}
