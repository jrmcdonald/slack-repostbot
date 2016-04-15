if (!process.env.token) {
    console.log('Error: Specify token in environment');
    process.exit(1);
}

var botkit = require('botkit');
var linkify = require('linkifyjs');
var moment = require('moment');

// Regex match links in slack messages, links will be formatted:
// <http://www.google.com|label>
// <http://www.googele.com>
var pattern = /<(.+?)>/g;

// Set up the controller uing jfs
var controller = botkit.slackbot({
  debug: false,
  json_file_store: './storage'
});

// Grab the worker so we can call the Slack Web API directly
var worker = controller.spawn({
  token: process.env.token
}).startRTM()

/**
 * Listen to all messages and process for links. When a link is 
 * found, determine if it is a repost and respond accordingly.
 */
controller.on('ambient', function(bot,message) {
  var matches = message.text.match(pattern);

  if (matches) {
    var urls = []
    for (var i = 0; i < matches.length; i++) {
      url = matches[i].split('|')[0].replace(/[<>]/g, '');
      if (linkify.test(url)) {
        controller.storage.channels.get(message.channel, function(err, data) {
          if (!data) {
            data = {
              id: message.channel,
              urls: {}
            }
          }

          if (url in data.urls) {
            urls.push(url);
          } else {
            data.urls[url] = {
              user: message.user,
              channel: message.channel,
              timestamp: message.ts
            }
            controller.storage.channels.save(data);
          }
        });
      }
    }

    if (urls.length > 0) {
      controller.storage.channels.get(message.channel, function(err, data) {
        urls.forEach(function(url, index, array) {
          var op = data.urls[url];

          var unixts = op.timestamp.split('.')[0];
          var date = moment.unix(unixts).format("DD/MM/YYYY HH:MM:ss");

          refreshUsers();

          controller.storage.users.get(op.user, function(err, user) {
            var response = '<' + url + '> was posted';

            if (user) {
              response += ' by ' + user.name;
            }

            response += ' on ' + date + '.';

            bot.reply(message, {
              text: response,
              link_names: 0,
              unfurl_links: false,
              unfurl_media: false
            });
          });
        });
      });
    }
  }
});

/**
 * Respond to direct messages by checking whether the supplied
 * link is a repost or not.
 */
controller.on('direct_message', function(bot, message) {
  var matches = message.text.match(pattern);

  if (matches) {
    var urls = []

    for (var i = 0; i < matches.length; i++) {
      url = matches[i].split('|')[0].replace(/[<>]/g, '');
      if (linkify.test(url)) {
        urls.push(url);
      }
    }

    if (urls.length > 0) {
    controller.storage.channels.all(function(err, all_data) {
      for (var d in all_data) {
        var data = all_data[d];
        urls.forEach(function(url, index, array) {
          if (url in data.urls) {
            var op = data.urls[url];

            var unixts = op.timestamp.split('.')[0];
            var date = moment.unix(unixts).format("DD/MM/YYYY HH:MM:ss");

            var response = '<' + url + '> was posted in <#' + op.channel + '> on ' + date + '.';

            bot.reply(message, {
              text: response,
              link_names: 0,
              unfurl_links: false,
              unfurl_media: false
            });
          }
        });
      }
    });
  }
  }
});

/**
 * Fetch an updated list of users from the Slack Web API and
 * store the user list.
 */
function refreshUsers() {
  worker.api.users.list({}, function(err, res) {
    var users = res.members;

    for (var u in users) {
      var user = users[u];

      controller.storage.users.save({id: user.id, name: user.name});
    }
  });
}

// Refresh the user list when starting up
refreshUsers();