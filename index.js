if (!process.env.token) {
    console.log('Error: Specify token in environment');
    process.exit(1);
}

var botkit = require('botkit');

var reconciliation = require('./lib/reconciliation.js');
var repostbot = require('./lib/repostbot.js');

// Set up the controller uing jfs
var controller = botkit.slackbot({
  debug: false,
  json_file_store: './storage'
});

// Grab the worker so we can call the Slack Web API directly
var worker = controller.spawn({
  token: process.env.token
}).startRTM()

var r = new reconciliation(controller, worker);
var b = new repostbot(controller, worker);