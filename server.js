const http = require('http');
const fs = require('fs');
const path = require('path');
const { execFile } = require('child_process');

const PORT = 3000;
const DIR = __dirname;
const FRONTEND_DIR = path.join(DIR, 'frontend');
const BACKEND_PORT = 8080;
const PYTHON_SCRIPT = path.join(DIR, 'backend', 'report_generator.py');

const MIME = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.pdf': 'application/pdf',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.mp4': 'video/mp4',
  '.svg': 'image/svg+xml',
  '.zip': 'application/zip'
};

function handleReportGeneration(req, res, urlObj) {
  const appId = urlObj.searchParams.get('appId') || 'APP-2026-101';
  const format = urlObj.searchParams.get('format') || 'pdf';
  const ext = format === 'json' ? 'json' : 'pdf';
  const contentType = format === 'json' ? 'application/json; charset=utf-8' : 'application/pdf';
  const filename = `AICTE_Scrutiny_Report_${appId}.${ext}`;
  const outPath = path.join(DIR, 'backend', 'uploads', filename);

  const sendFile = () => {
    if (fs.existsSync(outPath)) {
      res.writeHead(200, {
        'Content-Type': contentType,
        'Content-Disposition': `attachment; filename="${filename}"`,
        'Access-Control-Allow-Origin': '*',
        'Cache-Control': 'no-cache'
      });
      fs.createReadStream(outPath).pipe(res);
    } else {
      res.writeHead(500, { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' });
      res.end(JSON.stringify({ error: 'Report generation output not found' }));
    }
  };

  // If already pre-generated and no forced regenerate, send immediately
  if (fs.existsSync(outPath) && !urlObj.searchParams.has('force')) {
    return sendFile();
  }

  // Execute Python backend report_generator.py
  execFile('python', [PYTHON_SCRIPT, '--app-id', appId, '--format', format, '--output', outPath], (err, stdout, stderr) => {
    if (err) {
      console.error(`[Report Generator Error] ${err.message}`, stderr);
      // Fallback: check assets/reports directory
      const fallbackPath = path.join(DIR, 'assets', 'reports', filename);
      if (fs.existsSync(fallbackPath)) {
        res.writeHead(200, {
          'Content-Type': contentType,
          'Content-Disposition': `attachment; filename="${filename}"`,
          'Access-Control-Allow-Origin': '*'
        });
        return fs.createReadStream(fallbackPath).pipe(res);
      }
      res.writeHead(500, { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' });
      return res.end(JSON.stringify({ error: 'Failed to run report generator', details: stderr || err.message }));
    }
    sendFile();
  });
}

const server = http.createServer((req, res) => {
  const urlObj = new URL(req.url, `http://${req.headers.host || 'localhost:3000'}`);
  const reqPath = decodeURI(urlObj.pathname);

  // Dedicated route for Python Report Generator
  if (reqPath === '/api/reports/generate' || reqPath === '/api/reports/download') {
    return handleReportGeneration(req, res, urlObj);
  }

  // Handle report fallback for /api/validation-runs/:runId/report.pdf or .json
  const runReportMatch = reqPath.match(/^\/api\/validation-runs\/(\d+)\/report\.(pdf|json)$/);
  if (runReportMatch) {
    const runId = runReportMatch[1];
    const format = runReportMatch[2];
    const appId = `APP-2026-10${runId}`;
    urlObj.searchParams.set('appId', appId);
    urlObj.searchParams.set('format', format);
    return handleReportGeneration(req, res, urlObj);
  }

  // Reverse proxy backend endpoints (/api, swagger, h2-console) to Spring Boot
  const isBackendRoute = reqPath.startsWith('/api') || 
                         reqPath.startsWith('/swagger-ui') || 
                         reqPath.startsWith('/v3/api-docs') || 
                         reqPath.startsWith('/h2-console');

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
      res.writeHead(502, { 'Content-Type': 'application/json', 'Access-Control-Allow-Origin': '*' });
      res.end(JSON.stringify({ error: 'Backend unavailable on port 8080', details: err.message }));
    });

    req.pipe(proxyReq);
    return;
  }

  let localPath = reqPath;
  if (localPath === '/' || localPath === '') localPath = '/index.html';
  if (localPath === '/portal' || localPath === '/portal/') localPath = '/portal.html';
  if (localPath === '/landing' || localPath === '/landing/') localPath = '/landing.html';

  // Check frontend folder first, then fallback to root DIR
  let filePath = path.join(FRONTEND_DIR, localPath);
  if (!fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
    filePath = path.join(DIR, localPath);
  }

  if (!fs.existsSync(filePath) || fs.statSync(filePath).isDirectory()) {
    res.writeHead(404, { 'Content-Type': 'text/plain' });
    res.end('404 Not Found: ' + localPath);
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
  console.log(`[CipherCore] Integrated Python Report Generator at /api/reports/generate`);
  console.log(`[CipherCore] Proxying other /api requests to Spring Boot on port ${BACKEND_PORT}`);
});
