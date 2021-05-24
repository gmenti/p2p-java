const fs = require('fs');
const path = require('path');
const { v4: uuidv4 } = require('uuid');

const assetsPath = path.join(__dirname, '../', process.argv[3]);

fs.rmdirSync(assetsPath, { recursive: true, force: true });
fs.mkdirSync(assetsPath);

let count = process.argv[2]
  ? parseInt(process.argv[2])
  : 20;

while (count--) {
  const filename = count.toString() + ".txt";
  const content = uuidv4();
  fs.writeFileSync(path.join(assetsPath, filename), content);
  console.log('Created asset file', { filename, content });
}
