const fs = require("fs");
const htmlFile = 'src/index.html';
let html = fs.readFileSync(htmlFile, 'utf-8');
console.log("toggling production and development in the html");
html = html.replace(/<!--PRODUCTION-->(.*)/g, "<!--PRODUCTION:$1-->");
html = html.replace(/<!--DEVELOPMENT:(.*)-->/g, "<!--DEVELOPMENT-->$1");
fs.writeFileSync(htmlFile, html, 'utf-8');
console.log('Done!');
