// Script to automatically increase the app version number when 'npm run build' is called.
// Yep, Aidan is that forgetful.

const fs = require("fs");

const versionFile = 'src/environments/version.ts';
const file = fs.readFileSync(versionFile, 'utf-8');

const version = +file.match(/\d+/)[0];

console.log('Old version was: ', version);
var newValue = file.replace(version, version + 1);

console.log('Writing ' + versionFile + '...');
console.log(newValue);

fs.writeFileSync(versionFile, newValue, 'utf-8');
console.log('Done!');
