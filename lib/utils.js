var moment = require('moment');

/**
 * Parse message timestamps into a human readable format. Slack appends
 * numbers to the timestamp after a period to ensure uniqueness.
 */
function parseTimestamp(ts) {
  return moment.unix(ts.split('.')[0]).format("DD/MM/YYYY HH:MM:ss");
}

module.exports = {
  parseTimestamp: parseTimestamp
}