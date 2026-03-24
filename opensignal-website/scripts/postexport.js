const fs = require('fs')
const path = require('path')

const outDir = path.join(__dirname, '..', 'out')

if (!fs.existsSync(outDir)) {
  console.log('No out/ directory found. Run next build first.')
  process.exit(0)
}

const entries = fs.readdirSync(outDir, { withFileTypes: true })

for (const entry of entries) {
  if (!entry.isFile() || !entry.name.endsWith('.html')) continue

  const baseName = entry.name.replace(/\.html$/, '')
  if (baseName === 'index' || baseName === '404') continue

  const targetDir = path.join(outDir, baseName)
  fs.mkdirSync(targetDir, { recursive: true })

  const sourcePath = path.join(outDir, entry.name)
  const targetPath = path.join(targetDir, 'index.html')
  fs.copyFileSync(sourcePath, targetPath)
}

console.log('Post-export routing files generated.')
