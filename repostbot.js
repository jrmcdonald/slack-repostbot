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
controller.on('ambient', function(bot, message) {
  var matches = message.text.match(pattern);

  if (matches) {
    var reposts = [];
    
    controller.storage.channels.get(message.channel, function(err, channel) {
      if (!channel) {
        channel = { 
          id: message.channel,
          urls: {}
        };
      }
        
      processMatches(channel, message, matches).then(function(reposts) {
        handleRepostResponses(bot, channel, message, reposts);
      }).catch(function(err) {
        console.log("ERROR: An error occurred whilst processing matches: " + err);
      });
    });
  }
});

/**
 * Respond to direct messages by checking whether the supplied
 * link is a repost or not.
 */
controller.on('direct_message', function(bot, message) {
  var matches = message.text.match(pattern);

  if (matches) {
    var validUrls = []

    for (var i = 0; i < matches.length; i++) {
      url = matches[i].split('|')[0].replace(/[<>]/g, '');
      if (linkify.test(url)) {
        validUrls.push(url);
      }
    }

    if (validUrls.length > 0) {
      controller.storage.channels.all(function(err, all_channels) {
        for (var d in all_channels) {
          var channel = all_channels[d];
          validUrls.forEach(function(url, index, array) {
            if (url in channel.urls) {
              var op = channel.urls[url];
              
              var date = parseTimestamp(op.timestamp);

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
 * Parse matches for links. Returns a Promise with either an array
 * of reposted URLs or an error.
 */
function processMatches(channel, message, matches) {
  return new Promise(function(resolve, reject) {
    var reposts = [];
    var promises = [];
    
    for (var i = 0; i < matches.length; i++) {
      // Remove the link label after the pipe character and replace the angle brackets
      url = matches[i].split('|')[0].replace(/[<>]/g, '');
      
      if (linkify.test(url)) {
        console.log("DEBUG: Processing url " + url);
        
        if (url in channel.urls) {
          console.log("DEBUG: Repost detected");
          
          reposts.push(url);
          
          channel.urls[url].count = (channel.urls[url].count || 0) + 1;
          
          promises.push(incrementUserPostCount(message.user));
        } else {
          console.log("DEBUG: Not a repost, saving URL in JSON storage.");
          
          channel.urls[url] = {
            user: message.user,
            channel: message.channel,
            timestamp: message.ts,
            count: 0
          }
        }
        
        controller.storage.channels.save(channel, function(err, res) {
          if (err) {
            reject(Error("An error occurred whilst saving the channel: " + err));
          }
        });
      }
    }
    
    // Wait for all users to be incremented
    Promise.all(promises).then(function() {
      resolve(reposts);
    }).catch(function(err) {
      reject(err);      
    });
  });    
}

/**
 * Increment the repost count for a specific user.
 */
function incrementUserPostCount(userid) {
  return new Promise(function(resolve, reject) {
    controller.storage.users.get(userid, function(err, user) {
      console.log("DEBUG: Incrementing count for user " + userid);
      
      if (!user) {
        user = {
          id: userid
        };
      }
      
      user.count = (user.count || 0) + 1;
      
      controller.storage.users.save(user, function(err, res) {
        if (err) {
          reject(Error("An error occurred whilst saving the user: " + err));
        } else {
          resolve();
        }
      });
    });
  });
}

/**
 * Send a reply for each repost.
 */
function handleRepostResponses(bot, channel, message, reposts) {
  if (reposts.length > 0) {
    reposts.forEach(function(url, index, array) {
      var op = channel.urls[url];
      var date = parseTimestamp(op.timestamp);
      
      fetchUser(op.user).then(function(user) {
        var response = '<' + url + '> was posted by ' + user.name + ' on ' + date + '. It has been reposted ' + op.count + ' time(s).';
        
        console.log("DEBUG: Replying for reposted URL " + url + "."); 
      
        bot.reply(message, {
          text: response,
          link_names: 0,
          unfurl_links: false,
          unfurl_media: false
        });
      }).catch(function(err) {
        console.log("ERROR: An error occurred whilst handling the response: " + err);
      });
    });
  }
}

/**
 * Parse message timestamps into a human readable format. Slack appends
 * numbers to the timestamp after a period to ensure uniqueness.
 */
function parseTimestamp(ts) {
  return moment.unix(ts.split('.')[0]).format("DD/MM/YYYY HH:MM:ss");
}

/**
 * Fetch a single user from the json storage. If the user doesn't exist
 * or doesn't have a name then update it using the Slack Web API.
 */
function fetchUser(id) {
  return new Promise(function(resolve, reject) {
    console.log("DEBUG: Attempting to fetch user " + id);
    controller.storage.users.get(id, function(err, user) {
      if (user && user.name) {
        resolve(user);
      } else {
        console.log("DEBUG: " + user.id + " is not in storage, calling Slack API.");
        worker.api.users.info({user: id}, function (err, res) {          
          var newUser = {
            id: res.user.id, 
            name: res.user.name,
            count: (user.count || 0)
          };
          
          controller.storage.users.save(newUser, function(err, res) {
            if (err) {
              reject(err);
            } else {
              resolve(newUser)
            }
          });
        })
      }
    })
  });
}

/**
 * Fetch an updated list of users from the Slack Web API and
 * store the user list.
 */
function fetchAllUsers() {
  worker.api.users.list({}, function(err, res) {
    var users = res.members;

    for (var u in users) {
      var user = users[u];

      controller.storage.users.save({id: user.id, name: user.name});
    }
  });
}

// Refresh the user list when starting up
// fetchAllUsers();