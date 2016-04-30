A slack bot to listen in on slack channels and notify the channel when a user posts a link that has already been posted.

Based on [Botkit](https://github.com/howdyai/botkit).

# Features

* Parses messages for links and checks them for reposts, responds in the channel when a link is reposted with a count of how many times it has been reposted.
* Keeps track of links on a per channel basis.
* When DM'ed with a link, will check all channels to see if it has been posted before.
* On startup, parses the history for channels it is a member of and looks for links it may have missed during any downtime.

# Usage

Clone the repo and run: 

```
npm install

token=<SLACK_BOT_TOKEN> node index.js
```

Where SLACK_BOT_TOKEN is your private bot access token. You can obtain a token by creating a new bot integration at [https://my.slack.com/services/new/bot](https://my.slack.com/services/new/bot).

Invite the bot to the channel you want it to monitor and get posting.

# Logging

Structured logging is provided by [Bunyan](https://github.com/trentm/node-bunyan). Use the bunyan cli tool to parse and display the logs in a more readable format:

```
token=<SLACK_BOT_TOKEN> node index.js | ./node_modules/bunyan/bin/bunyan
```

# License 

Unless otherwise stated, code in this repository is available under the MIT License.