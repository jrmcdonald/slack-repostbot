var bunyan = require('bunyan');
var linkify = require('linkifyjs');

var constants = require('./constants.js');

function Reconciliation(controller, worker) {
  this.controller = controller;
  this.worker = worker;
  this.logger = bunyan.createLogger({name: 'reconciliation'});
  
  this.reconcileKnownChannels();
}

/**
 * Determine the channels that the bot is present in and then
 * perform message reconciliation on those channels.
 */
Reconciliation.prototype.reconcileKnownChannels = function () {
  var self = this;
  
  self.getBotUserId().then(function(bot_user) {
    self.getChannelList().then(function(channels) {
      var promises = [];
      
      for (var c in channels) {
        var channel = channels[c];
        
        if (channel.members.indexOf(bot_user) > -1) {
          self.logger.info("Bot is a member of channel " + channel.id + ", need to perform reconciliation.");
          promises.push(self.reconcileChannel(channel.id));
        }
      }
      
      Promise.all(promises).then(function() {
        self.logger.info("Finished reconciling known channels.")
      }).catch(function(err) {
        self.logger.error("An error occurred reconciling known channels: " + err);      
      }); 
    });
  });
}

/**
 * Query the Slack Web API to find the bot user id.
 */
Reconciliation.prototype.getBotUserId = function() {
  var self = this;
  
  return new Promise(function(resolve, reject) {
    self.worker.api.auth.test({}, function(err, res) {
      if (err) {
        throw(err);
      } else {
        resolve(res.user_id);
      }
    });
  });
}

/**
 * Query the Slack Web API to get a list of channels + members.
 */
Reconciliation.prototype.getChannelList = function() {
  var self = this;
  
  return new Promise(function(resolve, reject) {
    self.worker.api.channels.list({}, function(err, res) {
      if (err) {
        reject(err);
      } else {
        resolve(res.channels);
      }
    });
  });
}

/**
 * Reconcile missed URLs for a specific channel. Fetches the
 * messages in a channel's history and then processes them
 * oldest to newest to find missing URLs.
 */
Reconciliation.prototype.reconcileChannel = function (id) {
  var self = this;
  
  return new Promise(function(resolve, reject) {
    var messages = [];
    var params = {
      channel: id,
      count: 1000,
    };
    
    self.findLastReconciliation(id).then(function(oldest) {
      if (oldest) {
        self.logger.info("Reconciliation for " + id + " last occured at " + oldest);
        params.oldest = oldest;
      } 
      
      self.fetchHistory(messages, params).then(function() {
        self.logger.info("Finished fetching channel history for " + id);
        
        if (messages.length > 0) {
          self.reconcileMessages(messages.reverse(), id).then(function() {
            self.logger.info("Finished reconciling messages for channel " + id);
            var latest = messages[messages.length - 1].ts;
            
            self.setLastReconciliation(id, latest).then(function() {
              self.logger.info("Updated last reconcilation date for for channel " + id);
              resolve();
            }).catch(function(err) {
              reject("An error occured whilst setting the last reconciliation date for channel " + id + ": " + err);
            });
          }).catch(function(err) {
            reject("An error occured whilst reconciling messages: " + err);
          });
        } else {
          self.logger.info("No new messages since last reconciliation for " + id);
          resolve();
        }
      }).catch(function(err) {
        reject("An error occured whilst fetching history: " + err);
      });
    }).catch(function(err) {
      reject("An error occurred whilst fetching the last reconciliation date for channel " + id + ": " + err);
    });
  });
}

/**
 * Get the last reconciliation date for a channel from the local storage.
 */
Reconciliation.prototype.findLastReconciliation = function(channel) {
  var self = this;
  
  return new Promise(function(resolve, reject) {
      self.controller.storage.channels.get(channel, function(err, res) {
        if (res) {
          resolve(res.last_reconciliation);
        } else {
          resolve(null);
        }
      })
  });
}

/**
 * Set the last reconciliation date for a channel in the local storage.
 */
Reconciliation.prototype.setLastReconciliation = function(channel, timestamp) {
  var self = this;
  
  return new Promise(function(resolve, reject) {
    self.controller.storage.channels.get(channel, function(err, res) {
      if (err) {
        reject(err);
      } else {
        res.last_reconciliation = timestamp;
        
        self.controller.storage.channels.save(res, function(err, res) {
          if (err) {
            reject(err);
          } else {
            resolve();
          }
        });
      }
    });
  });
}

/**
 * Recursive function to fetch message history for a specified channel.
 */
Reconciliation.prototype.fetchHistory = function (messages, params) {
  var self = this;
  
  return new Promise(function(resolve, reject) {
    self.worker.api.channels.history(params, function(err, res) {
      if (err) {
        reject(err);  
      }
       
      Array.prototype.push.apply(messages, res.messages);
      if (res.has_more) {
        params.latest = res.messages[res.messages.length - 1].ts;
        
        self.fetchHistory(messages, params).then(function() {
          resolve();
        }).catch(function(err) {
          reject(err);
        });
      } else {
        resolve();
      }
    }); 
  });
}

/**
 * Iterate over each the messages in the array and check
 * for missing URLs.
 */
Reconciliation.prototype.reconcileMessages = function (messages, id) {
  var self = this;
  
  return messages.reduce(function(promise, message) {
    return promise.then(function() {
      return self.reconcileMessage(message, id);
    });
  }, Promise.resolve());
}

/**
 * Process an individual message for matches and store it.
 */
Reconciliation.prototype.reconcileMessage = function (message, id) {
  var self = this;
  
  return new Promise(function(resolve, reject) {
    var promises = [];
    var matches = message.text.match(constants.URL_PATTERN);

    if (matches) {
      for (var i = 0; i < matches.length; i++) {
        // Remove the link label after the pipe character and replace the angle brackets
        url = matches[i].split('|')[0].replace(/[<>]/g, '');
        
        if (linkify.test(url)) {
          promises.push(self.storeUrl(url, message, id));
        }
      }
    }
    
    Promise.all(promises).then(function() {
      resolve();
    }).catch(function(err) {
      reject(err);      
    });
  });
}

/**
 * Store the URL if it doesn't already exist in storage.
 */
Reconciliation.prototype.storeUrl = function (url, message, id) {
  var self = this;
  
  return new Promise(function(resolve, reject) {
    self.controller.storage.channels.get(id, function(err, channel) {  
      if (!channel) {
        channel = { 
          id: id,
          urls: {}
        };
      }
      
      if (!(url in channel.urls)){
        self.logger.info("Reconciling a previously unknown URL <" + url + ">.");
        
        channel.urls[url] = {
          user: message.user,
          channel: message.channel,
          timestamp: message.ts,
          count: 0
        }
        
        self.controller.storage.channels.save(channel, function(err, res) {
          if (err) {
            reject(err);
          } else {
            resolve();
          }
        });
      } else {
        resolve();
      }
    });    
  });
}

module.exports = Reconciliation;