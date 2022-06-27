// Script to automatically increase the app version number when 'npm run build' is called.
// Also toggles out the production and sandbox scripts in the index.html file

const fs = require("fs");

// updateVersion();
toggleProdHTML();

function updateVersion() {
    const versionFile = 'src/environments/version.ts';
    const parsedVersion = fs.readFileSync(versionFile, 'utf-8');

    const version = +parsedVersion.match(/\d+/)[0];

    console.log('Old version was: ', version);
    let newValue = parsedVersion.replace(version, version + 1);

    console.log('Writing ' + versionFile + '...');
    console.log(newValue);

    fs.writeFileSync(versionFile, newValue, 'utf-8');
    console.log('Done!');
};

function toggleProdHTML() {
    const htmlFile = 'src/index.html';
    let html = fs.readFileSync(htmlFile, 'utf-8');
    console.log("toggling production and development in the html");
    html = html.replace(/<!--DEVELOPMENT-->(.*)/g, "<!--DEVELOPMENT:$1-->");
    html = html.replace(/<!--PRODUCTION:(.*)-->/g, "<!--PRODUCTION-->$1");
    fs.writeFileSync(htmlFile, html, 'utf-8');
    console.log('Done!');
}
