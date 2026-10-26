const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = 3000;
const DIR = __dirname;
const FRONTEND_DIR = path.join(DIR, 'frontend');
const BACKEND_PORT = 8080;

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.mp4': 'video/mp4',
  '.svg': 'image/svg+xml',
  '.zip': 'application/zip'
};

const server = http.createServer((req, res) => {
  // Reverse proxy backend endpoints (/api, swagger, h2-console) directly to Spring Boot
  const isBackendRoute = req.url.startsWith('/api') || 
                         req.url.startsWith('/swagger-ui') || 
                         req.url.startsWith('/v3/api-docs') || 
                         req.url.startsWith('/h2-console');

  if (isBackendRoute) {
    const proxyReq = http.request({
      hostname: '127.0.0.1',
      port: BACKEND_PORT,
      path: req.url,
      method: req.method,
      headers: { ...req.headers, host: `127.0.0.1:${BACKEND_PORT}` }
    }, (proxyRes) => {
      res.writeHead(proxyRes.statusCode, proxyRes.headers);
      proxyRes.pipe(res);
    });

    proxyReq.on('error', (err) => {
      res.writeHead(502, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: 'Backend unavailable on port 8080', details: err.message }));
    });

    req.pipe(proxyReq);
    return;
  }

  let reqPath = decodeURI(req.url.split('?')[0]);
  if (reqPath === '/' || reqPath === '') reqPath = '/index.html';
  if (reqPath === '/portal' || reqPath === '/portal/') reqPath = '/portal.html';
  if (reqPath === '/landing' || reqPath === '/landing/') reqPath = '/landing.html';

  // Check frontend folder first, then fallback to root DIR
  let filePath = path.join(FRONTEND_DIR, reqPath);
  if (!fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
    filePath = path.join(DIR, reqPath);
  }

  if (!fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
    res.writeHead(404, { 'Content-Type': 'text/plain' });
    res.end('404 Not Found: ' + reqPath);
    return;
  }

  const ext = path.extname(filePath).toLowerCase();
  const contentType = MIME[ext] || 'application/octet-stream';

  res.writeHead(200, {
    'Content-Type': contentType,
    'Access-Control-Allow-Origin': '*',
    'Cache-Control': 'no-cache, no-store, must-revalidate',
    'Pragma': 'no-cache',
    'Expires': '0'
  });
  fs.createReadStream(filePath).pipe(res);
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`[CipherCore] Proxy & Frontend Server listening on http://localhost:${PORT}`);
  console.log(`[CipherCore] Serving frontend directory from: ${FRONTEND_DIR}`);
  console.log(`[CipherCore] Proxying /api requests to Spring Boot on port ${BACKEND_PORT}`);
});
