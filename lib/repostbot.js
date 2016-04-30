var linkify = require('linkifyjs');
var constants = require('./constants.js');
var utils = require('./utils.js');

function Repostbot(controller, worker) {
  this.controller = controller;
  this.worker = worker;
  
  var self = this;
  
  /**
   * Listen to all messages and process for links. When a link is 
   * found, determine if it is a repost and respond accordingly.
   */
  controller.on('ambient', function(bot, message) {
    var matches = message.text.match(constants.URL_PATTERN);

    if (matches) {
      var reposts = [];
      
      controller.storage.channels.get(message.channel, function(err, channel) {
        if (!channel) {
          channel = { 
            id: message.channel,
            urls: {}
          };
        }
          
        self.processMatches(channel, message, matches).then(function(reposts) {
          self.handleRepostResponses(bot, channel, message, reposts);
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
    var matches = message.text.match(constants.URL_PATTERN);

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
                
                var date = utils.parseTimestamp(op.timestamp);

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
}

/**
 * Parse matches for links. Returns a Promise with either an array
 * of reposted URLs or an error.
 */
Repostbot.prototype.processMatches = function(channel, message, matches) {
  var self = this;
  
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
          
          promises.push(self.incrementUserPostCount(message.user));
        } else {
          console.log("DEBUG: Not a repost, saving URL in JSON storage.");
          
          channel.urls[url] = {
            user: message.user,
            channel: message.channel,
            timestamp: message.ts,
            count: 0
          }
        }
        
        self.controller.storage.channels.save(channel, function(err, res) {
          if (err) {
            reject("An error occurred whilst saving the channel: " + err);
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
Repostbot.prototype.incrementUserPostCount = function(userid) {
  var self = this;
  
  return new Promise(function(resolve, reject) {
    self.controller.storage.users.get(userid, function(err, user) {
      console.log("DEBUG: Incrementing count for user " + userid);
      
      if (!user) {
        user = {
          id: userid
        };
      }
      
      user.count = (user.count || 0) + 1;
      
      self.controller.storage.users.save(user, function(err, res) {
        if (err) {
          reject("An error occurred whilst saving the user: " + err);
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
Repostbot.prototype.handleRepostResponses = function(bot, channel, message, reposts) {
  var self = this;
  
  if (reposts.length > 0) {
    reposts.forEach(function(url, index, array) {
      var op = channel.urls[url];
      var date = utils.parseTimestamp(op.timestamp);
      
      self.fetchUser(op.user).then(function(user) {
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
 * Fetch a single user from the json storage. If the user doesn't exist
 * or doesn't have a name then update it using the Slack Web API.
 */
Repostbot.prototype.fetchUser = function(id) {
  var self = this;
  
  return new Promise(function(resolve, reject) {
    console.log("DEBUG: Attempting to fetch user " + id);
    self.controller.storage.users.get(id, function(err, user) {
      if (user && user.name) {
        resolve(user);
      } else {
        console.log("DEBUG: " + user.id + " is not in storage, calling Slack API.");
        self.worker.api.users.info({user: id}, function (err, res) {          
          var newUser = {
            id: res.user.id, 
            name: res.user.name,
            count: (user.count || 0)
          };
          
          self.controller.storage.users.save(newUser, function(err, res) {
            if (err) {
              reject(err);
            } else {
              resolve(newUser);
            }
          });
        })
      }
    });
  });
}

module.exports = Repostbot;