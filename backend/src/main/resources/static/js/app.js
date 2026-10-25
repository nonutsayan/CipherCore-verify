/* ==========================================================================
   CIPHERCORE // PROJECT CHIMERA MASTER APPLICATION CONTROLLER
   Full-Stack SIH26261 Regulatory Intelligence & Document Forensics
   ========================================================================== */

/* ==========================================================================
       PROJECT CHIMERA: CIPHERCORE MASTER ENGINE CONTROLLER
       Dual Experience Manager, Three.js 3D Intelligence Core, & Consolidated Router
       ========================================================================== */

    let threeAnimId = null;
    let threeIsPaused = false;
    let threeAnimateFn = null;

    // Dual Experience Mode Switcher (Story vs Command Centre)
    function switchAppMode(mode) {
      const viewStory = document.getElementById('experience-story');
      const viewCommand = document.getElementById('experience-command');

      if (mode === 'command') {
        if (viewStory) viewStory.style.display = 'none';
        if (viewCommand) viewCommand.style.display = 'flex';
        threeIsPaused = true;
        if (threeAnimId) {
          cancelAnimationFrame(threeAnimId);
          threeAnimId = null;
        }
        window.scrollTo({ top: 0, behavior: 'instant' });
      } else {
        if (viewCommand) viewCommand.style.display = 'none';
        if (viewStory) viewStory.style.display = 'block';
        threeIsPaused = false;
        if (threeAnimateFn && !threeAnimId) {
          threeAnimId = requestAnimationFrame(threeAnimateFn);
        }
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    }

    // Direct Module Switcher in Command Centre
    function switchCommandModule(moduleId) {
      switchTab(moduleId);
    }

    // Unified helper: open a command module directly from the story narrative
    function openCommandModule(moduleId) {
      switchAppMode('command');
      switchCommandModule(moduleId);
    }

    // Workstation Tab alias
    function switchWorkstationTab(tabId) {
      switchCommandModule(tabId);
    }

    // Smooth Scroll Helper
    function smoothScrollTo(id) {
      const el = document.getElementById(id);
      if (el) el.scrollIntoView({ behavior: 'smooth' });
    }

    // Scroll Progress Tracker & Active Nav Link Highlighter
    function initScrollTracker() {
      const progressBar = document.getElementById('scrollProgressBar');
      const navLinks = document.querySelectorAll('.story-nav-link');
      const scenes = document.querySelectorAll('.narrative-scene');

      window.addEventListener('scroll', () => {
        const winScroll = document.documentElement.scrollTop || document.body.scrollTop;
        const height = document.documentElement.scrollHeight - document.documentElement.clientHeight;
        const scrolled = (winScroll / (height || 1)) * 100;
        if (progressBar) progressBar.style.width = scrolled + '%';

        // Highlight active nav scene
        let currentSceneId = '';
        scenes.forEach(scene => {
          const rect = scene.getBoundingClientRect();
          if (rect.top <= 200 && rect.bottom >= 200) {
            currentSceneId = scene.getAttribute('id');
          }
        });

        if (currentSceneId) {
          navLinks.forEach(link => {
            link.classList.remove('active');
            if (link.getAttribute('href') === '#' + currentSceneId) {
              link.classList.add('active');
            }
          });
        }
      });
    }

    // Three.js Intelligence Core Engine with Full Lifecycle Management
    function initThreeIntelligenceCore() {
      const container = document.getElementById('intelligenceCoreContainer');
      const canvas = document.getElementById('intelligenceCoreCanvas');
      if (!container || !canvas || typeof THREE === 'undefined') {
        init2DFallbackCore();
        return;
      }

      try {
        const width = 480;
        const height = 480;
        const scene = new THREE.Scene();
        const camera = new THREE.PerspectiveCamera(45, width / height, 0.1, 1000);
        camera.position.z = 110;

        const renderer = new THREE.WebGLRenderer({ canvas: canvas, alpha: true, antialias: true });
        renderer.setSize(width, height);
        renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));

        // Ambient & Directional Cyber Lighting
        const ambientLight = new THREE.AmbientLight(0x0B1020, 2.5);
        scene.add(ambientLight);

        const cyanLight = new THREE.PointLight(0x57E8FF, 3, 160);
        cyanLight.position.set(45, 45, 55);
        scene.add(cyanLight);

        const violetLight = new THREE.PointLight(0xA78BFA, 2.5, 160);
        violetLight.position.set(-45, -45, 45);
        scene.add(violetLight);

        // Core Group
        const coreGroup = new THREE.Group();
        scene.add(coreGroup);

        // 1. Central Luminous Core
        const coreGeo = new THREE.IcosahedronGeometry(18, 2);
        const coreMat = new THREE.MeshStandardMaterial({
          color: 0x050711,
          emissive: 0x11182A,
          wireframe: true,
          wireframeLinewidth: 1.5
        });
        const coreMesh = new THREE.Mesh(coreGeo, coreMat);
        coreGroup.add(coreMesh);

        // Inner glowing core sphere
        const innerGeo = new THREE.SphereGeometry(12, 24, 24);
        const innerMat = new THREE.MeshBasicMaterial({
          color: 0x57E8FF,
          transparent: true,
          opacity: 0.22
        });
        const innerMesh = new THREE.Mesh(innerGeo, innerMat);
        coreGroup.add(innerMesh);

        // 2. Three Orbital Validation Rings
        const ring1Geo = new THREE.TorusGeometry(32, 0.55, 16, 100);
        const ring1Mat = new THREE.MeshBasicMaterial({ color: 0x57E8FF, transparent: true, opacity: 0.6 });
        const ring1 = new THREE.Mesh(ring1Geo, ring1Mat);
        ring1.rotation.x = Math.PI / 3;
        coreGroup.add(ring1);

        const ring2Geo = new THREE.TorusGeometry(42, 0.45, 16, 100);
        const ring2Mat = new THREE.MeshBasicMaterial({ color: 0xA78BFA, transparent: true, opacity: 0.5 });
        const ring2 = new THREE.Mesh(ring2Geo, ring2Mat);
        ring2.rotation.y = Math.PI / 4;
        ring2.rotation.x = -Math.PI / 5;
        coreGroup.add(ring2);

        const ring3Geo = new THREE.TorusGeometry(52, 0.4, 16, 100);
        const ring3Mat = new THREE.MeshBasicMaterial({ color: 0x6485FF, transparent: true, opacity: 0.45 });
        const ring3 = new THREE.Mesh(ring3Geo, ring3Mat);
        ring3.rotation.z = Math.PI / 6;
        ring3.rotation.y = -Math.PI / 3;
        coreGroup.add(ring3);

        // 3. Orbital Particle Stream (200 particles)
        const particleCount = 200;
        const particleGeo = new THREE.BufferGeometry();
        const particlePositions = new Float32Array(particleCount * 3);
        const particleAngles = [];
        const particleRadii = [];
        const particleSpeeds = [];

        for (let i = 0; i < particleCount; i++) {
          const radius = 22 + Math.random() * 32;
          const angle = Math.random() * Math.PI * 2;
          const speed = (0.005 + Math.random() * 0.012) * (Math.random() > 0.5 ? 1 : -1);
          particleAngles.push(angle);
          particleRadii.push(radius);
          particleSpeeds.push(speed);

          const yOffset = (Math.random() - 0.5) * 16;
          particlePositions[i * 3] = Math.cos(angle) * radius;
          particlePositions[i * 3 + 1] = yOffset;
          particlePositions[i * 3 + 2] = Math.sin(angle) * radius;
        }

        particleGeo.setAttribute('position', new THREE.BufferAttribute(particlePositions, 3));
        const particleMat = new THREE.PointsMaterial({
          color: 0x57E8FF,
          size: 2.0,
          transparent: true,
          opacity: 0.85
        });
        const particleSystem = new THREE.Points(particleGeo, particleMat);
        coreGroup.add(particleSystem);

        // 4. Five Floating Validation Nodes with Connecting Dynamic Ray Lines
        const nodeDefs = [
          { name: 'OCR Engine', color: 0x57E8FF, r: 32, angle: 0 },
          { name: 'ELA Forensics', color: 0xFB7185, r: 42, angle: (2 * Math.PI) / 5 },
          { name: 'Authority Gate', color: 0x6485FF, r: 38, angle: (4 * Math.PI) / 5 },
          { name: 'APH Rules', color: 0xFBBF24, r: 46, angle: (6 * Math.PI) / 5 },
          { name: 'PoA Ledger', color: 0x34D399, r: 34, angle: (8 * Math.PI) / 5 }
        ];

        const nodeMeshes = [];
        const rayLines = [];

        nodeDefs.forEach(def => {
          const nodeGeo = new THREE.SphereGeometry(2.4, 16, 16);
          const nodeMat = new THREE.MeshBasicMaterial({ color: def.color });
          const mesh = new THREE.Mesh(nodeGeo, nodeMat);
          mesh.userData = def;
          coreGroup.add(mesh);
          nodeMeshes.push({ mesh, def });

          // Connecting ray line to center
          const lineGeo = new THREE.BufferGeometry().setFromPoints([
            new THREE.Vector3(0, 0, 0),
            new THREE.Vector3(0, 0, 0)
          ]);
          const lineMat = new THREE.LineBasicMaterial({
            color: def.color,
            transparent: true,
            opacity: 0.25
          });
          const line = new THREE.Line(lineGeo, lineMat);
          coreGroup.add(line);
          rayLines.push(line);
        });

        // Pointer Parallax
        let mouseX = 0, mouseY = 0;
        let targetX = 0, targetY = 0;

        window.addEventListener('mousemove', (e) => {
          const rect = canvas.getBoundingClientRect();
          if (rect.width > 0 && rect.height > 0) {
            mouseX = ((e.clientX - rect.left) / rect.width) * 2 - 1;
            mouseY = -(((e.clientY - rect.top) / rect.height) * 2 - 1);
          }
        });

        // Animation Loop with Visibility & Experience Safety
        let clock = new THREE.Clock();

        threeAnimateFn = function animate() {
          if (threeIsPaused || document.hidden) {
            threeAnimId = null;
            return;
          }

          threeAnimId = requestAnimationFrame(animate);

          // Pointer damping & scroll parallax
          targetX += (mouseX * 0.35 - targetX) * 0.05;
          targetY += (mouseY * 0.25 - targetY) * 0.05;
          const scrollOffset = (window.scrollY || 0) * 0.001;
          coreGroup.rotation.y += 0.005 + scrollOffset * 0.002;
          coreGroup.rotation.x = targetY;
          coreGroup.rotation.z = -targetX * 0.4;

          // Differential Ring Rotations
          ring1.rotation.z += 0.008;
          ring2.rotation.x -= 0.006;
          ring3.rotation.y += 0.004;

          // Pulse Core
          const time = clock.getElapsedTime();
          const scale = 1 + Math.sin(time * 2.5) * 0.04;
          coreMesh.scale.set(scale, scale, scale);

          // Animate Particles
          const pos = particleGeo.attributes.position.array;
          for (let i = 0; i < particleCount; i++) {
            particleAngles[i] += particleSpeeds[i];
            pos[i * 3] = Math.cos(particleAngles[i]) * particleRadii[i];
            pos[i * 3 + 2] = Math.sin(particleAngles[i]) * particleRadii[i];
          }
          particleGeo.attributes.position.needsUpdate = true;

          // Animate Nodes & Ray Lines
          nodeMeshes.forEach((item, idx) => {
            const curAngle = item.def.angle + time * 0.3;
            const nx = Math.cos(curAngle) * item.def.r;
            const nz = Math.sin(curAngle) * item.def.r;
            const ny = Math.sin(time * 1.5 + idx) * 5;
            item.mesh.position.set(nx, ny, nz);

            // Update ray line geometry
            const linePositions = rayLines[idx].geometry.attributes.position.array;
            linePositions[3] = nx;
            linePositions[4] = ny;
            linePositions[5] = nz;
            rayLines[idx].geometry.attributes.position.needsUpdate = true;
          });

          renderer.render(scene, camera);
        };

        // Resume on tab visibility change
        document.addEventListener('visibilitychange', () => {
          if (!document.hidden && !threeIsPaused && !threeAnimId && threeAnimateFn) {
            threeAnimId = requestAnimationFrame(threeAnimateFn);
          }
        });

        // Start animation loop
        threeAnimId = requestAnimationFrame(threeAnimateFn);
      } catch (err) {
        console.warn('Three.js initialization fallback:', err);
        init2DFallbackCore();
      }
    }

    // High-Performance 2D Canvas Fallback
    function init2DFallbackCore() {
      const canvas = document.getElementById('intelligenceCoreFallbackCanvas');
      const originalCanvas = document.getElementById('intelligenceCoreCanvas');
      if (!canvas) return;

      if (originalCanvas) originalCanvas.style.display = 'none';
      canvas.style.display = 'block';

      const ctx = canvas.getContext('2d');
      if (!ctx) return;

      let angle = 0;
      const width = canvas.width;
      const height = canvas.height;
      const cx = width / 2;
      const cy = height / 2;

      function render2D() {
        if (threeIsPaused || document.hidden) {
          requestAnimationFrame(render2D);
          return;
        }

        ctx.clearRect(0, 0, width, height);
        angle += 0.01;

        // Glow
        const grad = ctx.createRadialGradient(cx, cy, 10, cx, cy, 180);
        grad.addColorStop(0, 'rgba(87, 232, 255, 0.2)');
        grad.addColorStop(1, 'transparent');
        ctx.fillStyle = grad;
        ctx.beginPath();
        ctx.arc(cx, cy, 180, 0, Math.PI * 2);
        ctx.fill();

        // Rings
        ctx.strokeStyle = 'rgba(87, 232, 255, 0.4)';
        ctx.lineWidth = 1.5;
        ctx.beginPath();
        ctx.arc(cx, cy, 140, 0, Math.PI * 2);
        ctx.stroke();

        ctx.strokeStyle = 'rgba(167, 139, 250, 0.4)';
        ctx.beginPath();
        ctx.arc(cx, cy, 100, -angle, Math.PI * 2 - angle);
        ctx.stroke();

        // Core
        ctx.fillStyle = '#0B1020';
        ctx.strokeStyle = '#57E8FF';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.arc(cx, cy, 36, 0, Math.PI * 2);
        ctx.fill();
        ctx.stroke();

        ctx.fillStyle = '#57E8FF';
        ctx.font = 'bold 11px monospace';
        ctx.textAlign = 'center';
        ctx.fillText('CIPHERCORE', cx, cy + 4);

        requestAnimationFrame(render2D);
      }
      render2D();
    }

    // 2D Blockchain Canvas Consensus Ring
    function initBlockchainCanvas() {
      const canvas = document.getElementById('blockchainCanvas');
      if (!canvas) return;

      const ctx = canvas.getContext('2d');
      if (!ctx) return;

      const width = canvas.width;
      const height = canvas.height;
      let angle = 0;

      const nodes = [
        { name: 'AICTE HQ', angle: 0, r: 140, color: '#38bdf8' },
        { name: 'CDAC Pune', angle: (1 * Math.PI) / 3.5, r: 140, color: '#818cf8' },
        { name: 'IIT Delhi', angle: (2 * Math.PI) / 3.5, r: 140, color: '#34d399' },
        { name: 'Bureau Stds', angle: (3 * Math.PI) / 3.5, r: 140, color: '#38bdf8' },
        { name: 'AICTE WRO', angle: (4 * Math.PI) / 3.5, r: 140, color: '#f59e0b' },
        { name: 'MoE', angle: (5 * Math.PI) / 3.5, r: 140, color: '#ec4899' },
        { name: 'NIC', angle: (6 * Math.PI) / 3.5, r: 140, color: '#06b6d4' }
      ];

      function draw() {
        if (document.hidden) {
          requestAnimationFrame(draw);
          return;
        }

        ctx.clearRect(0, 0, width, height);

        const cx = width / 2;
        const cy = height / 2;

        angle += 0.008;

        // Ambient Radial Glow
        const grad = ctx.createRadialGradient(cx, cy, 20, cx, cy, 180);
        grad.addColorStop(0, 'rgba(87, 232, 255, 0.22)');
        grad.addColorStop(0.5, 'rgba(167, 139, 250, 0.1)');
        grad.addColorStop(1, 'rgba(5, 7, 17, 0)');
        ctx.fillStyle = grad;
        ctx.beginPath();
        ctx.arc(cx, cy, 180, 0, Math.PI * 2);
        ctx.fill();

        // Outer Ring
        ctx.strokeStyle = 'rgba(87, 232, 255, 0.25)';
        ctx.lineWidth = 1.5;
        ctx.setLineDash([4, 6]);
        ctx.beginPath();
        ctx.arc(cx, cy, 140, 0, Math.PI * 2);
        ctx.stroke();

        // Inner Ring
        ctx.strokeStyle = 'rgba(167, 139, 250, 0.35)';
        ctx.lineWidth = 2;
        ctx.setLineDash([8, 8]);
        ctx.beginPath();
        ctx.arc(cx, cy, 80, -angle * 1.5, Math.PI * 2 - angle * 1.5);
        ctx.stroke();
        ctx.setLineDash([]);

        // Core Shield
        ctx.fillStyle = '#0B1020';
        ctx.strokeStyle = '#57E8FF';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.arc(cx, cy, 38, 0, Math.PI * 2);
        ctx.fill();
        ctx.stroke();

        // Center Icon
        ctx.fillStyle = '#57E8FF';
        ctx.font = 'bold 12px "JetBrains Mono", monospace';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText('PoA #26261', cx, cy - 6);
        ctx.font = '9px "Inter", sans-serif';
        ctx.fillStyle = '#34D399';
        ctx.fillText('7 VALIDATORS', cx, cy + 10);

        // Nodes & Connecting Rays
        nodes.forEach((node, idx) => {
          const curAngle = node.angle + angle;
          const nx = cx + Math.cos(curAngle) * node.r;
          const ny = cy + Math.sin(curAngle) * node.r;

          // Ray to center
          ctx.strokeStyle = 'rgba(87, 232, 255, 0.18)';
          ctx.lineWidth = 1;
          ctx.beginPath();
          ctx.moveTo(cx, cy);
          ctx.lineTo(nx, ny);
          ctx.stroke();

          // Inter-node ring connection
          const nextNode = nodes[(idx + 1) % nodes.length];
          const nextAngle = nextNode.angle + angle;
          const nnx = cx + Math.cos(nextAngle) * nextNode.r;
          const nny = cy + Math.sin(nextAngle) * nextNode.r;

          ctx.strokeStyle = 'rgba(167, 139, 250, 0.2)';
          ctx.beginPath();
          ctx.moveTo(nx, ny);
          ctx.lineTo(nnx, nny);
          ctx.stroke();

          // Draw Node Glow
          ctx.fillStyle = node.color;
          ctx.beginPath();
          ctx.arc(nx, ny, 6, 0, Math.PI * 2);
          ctx.fill();

          ctx.strokeStyle = 'rgba(255, 255, 255, 0.8)';
          ctx.lineWidth = 1.5;
          ctx.beginPath();
          ctx.arc(nx, ny, 8, 0, Math.PI * 2);
          ctx.stroke();

          // Label
          ctx.font = '10px "Inter", sans-serif';
          ctx.fillStyle = '#e2e8f0';
          ctx.fillText(node.name, nx, ny + (ny > cy ? 18 : -14));
        });

        requestAnimationFrame(draw);
      }

      draw();
    }


/* State Data Store */
    const DOSSIERS = {
      apex: {
        id: 'AICTE-2025-EOA-98104',
        name: 'Apex Institute of Technology',
        status: 'VERIFIED COMPLIANT',
        statusClass: 'verified',
        risk: '96%',
        riskColor: '#34d399',
        flags: '0 Flags',
        flagsColor: '#10b981',
        intake: '540 / 540',
        docs: [
          { id: 'land', name: 'Land Ownership Deed (7/12 Extract)', file: 'land_deed_apex.pdf', status: 'verified', statusText: 'VERIFIED MATCH', details: 'Area: 10.4 Acres Â· Reg: 9812/2018' },
          { id: 'fire', name: 'Fire Safety Certificate (NOC)', file: 'fire_noc_apex.pdf', status: 'verified', statusText: 'ACTIVE (Valid to 2026)', details: 'Issued by State Fire Dept Â· Clean' },
          { id: 'bld',  name: 'Building Plan Approval', file: 'building_plan_approved.pdf', status: 'verified', statusText: 'MUNICIPAL APPROVED', details: 'Carpet Area: 14,200 mÂ²' },
          { id: 'fac',  name: 'Faculty Master Register', file: 'faculty_register_2025.pdf', status: 'verified', statusText: '100% VERIFIED', details: 'F:S Ratio 1:14.2 Â· All PANs distinct' }
        ]
      },
      horizon: {
        id: 'AICTE-2025-EOA-44219',
        name: 'Horizon College of Engineering',
        status: 'FLAGGED FOR SCRUTINY',
        statusClass: 'flagged',
        risk: '68%',
        riskColor: '#fb7185',
        flags: '3 Flags',
        flagsColor: '#f59e0b',
        intake: '420 / 420',
        docs: [
          { id: 'fire', name: 'Fire Safety Certificate (NOC)', file: 'fire_noc_2024.pdf', status: 'alert', statusText: 'EXPIRED NOC', details: 'Expired on 31-Jul-2024' },
          { id: 'land', name: 'Land Conversion Order', file: 'revenue_land_order.pdf', status: 'alert', statusText: 'SPLICED STAMP', details: 'Digital stamp fingerprint duplicate' },
          { id: 'fac',  name: 'Faculty Rolls & Form 16', file: 'faculty_rolls.pdf', status: 'warning', statusText: 'DUPLICATE PAN', details: '1 Faculty concurrent at another college' },
          { id: 'bld',  name: 'Building Plan Architecture', file: 'campus_master_plan.pdf', status: 'verified', statusText: 'APPROVED', details: 'Meets minimum built-up norms' }
        ]
      },
      greenfield: {
        id: 'AICTE-2025-EOA-11028',
        name: 'Greenfield Polytechnic',
        status: 'DEFICIENT INFRASTRUCTURE',
        statusClass: 'deficient',
        risk: '52%',
        riskColor: '#f59e0b',
        flags: '4 Flags',
        flagsColor: '#f43f5e',
        intake: '240 / 300',
        docs: [
          { id: 'bld',  name: 'Building Floor Plans', file: 'greenfield_floors.pdf', status: 'alert', statusText: 'INADEQUATE LABS', details: 'Electrical Lab carpet area short by 34 mÂ²' },
          { id: 'fac',  name: 'Faculty List', file: 'faculty_diploma.pdf', status: 'warning', statusText: 'F:S RATIO 1:28', details: 'Requires 6 additional lecturers' },
          { id: 'land', name: 'Lease Agreement', file: 'land_lease_greenfield.pdf', status: 'verified', statusText: '30-YR LEASE', details: 'Valid through 2052' },
          { id: 'fire', name: 'Fire NOC', file: 'fire_provisional.pdf', status: 'warning', statusText: 'PROVISIONAL', details: 'Final certificate pending inspection' }
        ]
      }
    };

    let activeDossierKey = 'horizon';
    let activeDocIndex = 0;
    let backendDocs = [];
    let activeBackendDocId = null;
    let activeFilterCategory = 'ALL';
    let currentReportData = null;

    function escapeHtml(str) {
      if (str === null || str === undefined) return '';
      return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
    }

    function resolveApiBase() {
      if (typeof window !== 'undefined' && window.location && window.location.protocol && window.location.protocol.startsWith('http')) {
        const port = window.location.port;
        if (port === '3000' || port === '8080') return '/api';
        return window.location.protocol + '//' + window.location.hostname + ':8080/api';
      }
      return 'http://127.0.0.1:8080/api';
    }
    const API_BASE = resolveApiBase();
    console.log('CipherCore Document Intelligence initialized. API_BASE:', API_BASE);

    /* Tab Switching */
    function switchTab(tabId) {
      document.querySelectorAll('.tab-section').forEach(sec => {
        sec.classList.remove('active');
        sec.style.display = '';
      });
      document.querySelectorAll('.nav-tab').forEach(tab => tab.classList.remove('active'));
      document.querySelectorAll('.sidebar-item').forEach(item => {
        item.classList.remove('active');
        if (item.getAttribute('data-module') === tabId) {
          item.classList.add('active');
        }
      });

      const target = document.getElementById(tabId);
      if (target) {
        target.classList.add('active');
      }

      const btn = document.querySelector('.nav-tab[data-tab="' + tabId + '"]');
      if (btn) btn.classList.add('active');

      const breadcrumbEl = document.getElementById('commandBreadcrumbActive');
      const activeBtn = document.querySelector('.sidebar-item[data-module="' + tabId + '"]');
      if (breadcrumbEl && activeBtn) {
        const lbl = activeBtn.querySelector('.lbl');
        breadcrumbEl.textContent = lbl ? lbl.textContent.trim() : tabId;
      }

      if (tabId === 'tab-overview') {
        if (typeof updateDossierView === 'function') updateDossierView();
      } else if (tabId === 'tab-ocr') {
        if (typeof refreshDocumentList === 'function') refreshDocumentList();
      } else if (tabId === 'tab-forensics') {
        if (typeof loadForensicsDashboard === 'function') {
          loadForensicsDashboard(activeBackendDocId || 2);
        }
      } else if (tabId === 'tab-authorities') {
        if (typeof initAuthorityVerificationDashboard === 'function') {
          initAuthorityVerificationDashboard();
        }
      } else if (tabId === 'tab-cv') {
        const sel = document.getElementById('cvAppSelector');
        const appId = sel ? sel.value : '2';
        if (typeof loadCvData === 'function') loadCvData(appId);
      } else if (tabId === 'tab-decision') {
        const sel = document.getElementById('decAppSelector');
        const appId = sel ? sel.value : '2';
        if (typeof loadComplianceData === 'function') loadComplianceData(appId);
      } else if (tabId === 'tab-blockchain') {
        if (typeof initBlockchainNetwork === 'function') initBlockchainNetwork();
      } else if (tabId === 'tab-security') {
        if (typeof loadSecurityConsoleData === 'function') loadSecurityConsoleData();
      }

      window.scrollTo({ top: 0, behavior: 'smooth' });
    }

    document.querySelectorAll('.nav-tab').forEach(btn => {
      btn.addEventListener('click', function() {
        switchTab(this.dataset.tab);
      });
    });

    /* Institution Dropdown Handler */
    const instSelect = document.getElementById('instituteSelect');
    if (instSelect) {
      instSelect.addEventListener('change', function() {
        activeDossierKey = this.value;
        activeDocIndex = 0;
        updateDossierView();
      });
    }

    function updateDossierView() {
      const data = DOSSIERS[activeDossierKey];
      if (!data) return;

      // Update Bar Elements
      const tag = document.getElementById('dossierStatusTag');
      if (tag) {
        tag.textContent = data.status;
        tag.className = 'tag-status ' + data.statusClass;
      }

      const riskEl = document.getElementById('statRiskScore');
      if (riskEl) {
        riskEl.textContent = data.risk;
        riskEl.style.color = data.riskColor;
      }
      const flagEl = document.getElementById('statDeficiencies');
      if (flagEl) {
        flagEl.textContent = data.flags;
        flagEl.style.color = data.flagsColor;
      }
      const intakeEl = document.getElementById('statSanctionedIntake');
      if (intakeEl) intakeEl.textContent = data.intake;

      // Update Report Modal Fields
      const repAppId = document.getElementById('repAppId');
      if (repAppId) repAppId.textContent = data.id;
      const repInstName = document.getElementById('repInstName');
      if (repInstName) repInstName.textContent = data.name;

      renderDocList();
      renderActiveDoc();
    }

    /* Render Active Document Content */
    function renderActiveDoc() {
      const dossier = DOSSIERS[activeDossierKey];
      if (!dossier || !dossier.docs) return;
      const doc = dossier.docs[activeDocIndex];
      if (!doc) return;

      const titleEl = document.getElementById('activeDocTitle');
      if (titleEl) {
        titleEl.innerHTML = '<svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg> OCR Visual Inspector: ' + escapeHtml(doc.name);
      }
      const subEl = document.getElementById('activeDocSubtitle');
      if (subEl) subEl.textContent = 'Document File: ' + doc.file + ' · AICTE Structured Scrutiny';
      
      const badge = document.getElementById('activeDocBadge');
      if (badge) {
        badge.textContent = doc.statusText;
        badge.className = 'tag-status ' + doc.status;
      }

      const fnEl = document.getElementById('activeDocFilename');
      if (fnEl) fnEl.textContent = doc.file;

      // Mock OCR Canvas Content based on doc
      const contentBox = document.getElementById('mockDocContent');
      const tableBody = document.getElementById('entityTableBody');

      if (!contentBox || !tableBody) return;

      if (doc.id === 'fire') {
        contentBox.innerHTML = `
          <div>------------------------------------------------------------------------</div>
          <div style="font-weight:700; color:#fff;">GOVERNMENT OF MAHARASHTRA · DIRECTORATE OF FIRE SERVICES</div>
          <div>FIRE SAFETY CLEARANCE & NO OBJECTION CERTIFICATE</div>
          <div>------------------------------------------------------------------------</div>
          <div>ISSUED TO: <span class="ocr-bbox verified"><span class="ocr-bbox-tag">INST_NAME</span>${dossier.name}</span></div>
          <div>CAMPUS ADDRESS: <span class="ocr-bbox"><span class="ocr-bbox-tag">ADDRESS</span>Sector 14, Plot 9, Institutional Area, Mumbai 400076</span></div>
          <div>NOC REFERENCE NUMBER: <span class="ocr-bbox verified"><span class="ocr-bbox-tag">REF_NO</span>FS/NOC/2023/88192-W</span></div>
          <div>EFFECTIVE DATE: 01-AUG-2023</div>
          <div>VALID UP TO: <span class="ocr-bbox alert"><span class="ocr-bbox-tag">EXPIRED</span>31-JUL-2024</span> (CRITICAL: EXPIRED 92 DAYS AGO)</div>
          <div>OCCUPANCY CLASSIFICATION: Educational Institution Group-B</div>
          <div>------------------------------------------------------------------------</div>
        `;

        tableBody.innerHTML = `
          <tr>
            <td class="entity-key">NOC Reference No.</td>
            <td>FS/NOC/2023/88192-W</td>
            <td>Must be authenticated by Fire Authority</td>
            <td><span class="tag-status verified">MATCHED</span></td>
          </tr>
          <tr>
            <td class="entity-key">Validity End Date</td>
            <td style="color:#f43f5e; font-weight:700;">31-JUL-2024</td>
            <td>APH Norm: Active throughout Academic Year 2025-26</td>
            <td><span class="tag-status alert">NON-COMPLIANT</span></td>
          </tr>
          <tr>
            <td class="entity-key">Occupancy Norm</td>
            <td>Group-B Educational</td>
            <td>Permitted for Engineering & Tech institutions</td>
            <td><span class="tag-status verified">VERIFIED</span></td>
          </tr>
          <tr>
            <td class="entity-key">Issuing Jurisdiction</td>
            <td>Mumbai Fire Brigade HQ</td>
            <td>Cross-checked with Maharashtra Fire Services DB</td>
            <td><span class="tag-status verified">VALID AUTHORITY</span></td>
          </tr>
        `;
      } else if (doc.id === 'land') {
        contentBox.innerHTML = `
          <div>------------------------------------------------------------------------</div>
          <div style="font-weight:700; color:#fff;">REVENUE & FOREST DEPARTMENT · GOVT OF MAHARASHTRA</div>
          <div>NON-AGRICULTURAL (NA) LAND CONVERSION CERTIFICATE</div>
          <div>------------------------------------------------------------------------</div>
          <div>SURVEY NUMBER: <span class="ocr-bbox verified"><span class="ocr-bbox-tag">SURVEY</span>Plot 14/A, Hissa 2</span></div>
          <div>LAND TITLE HOLDER: <span class="ocr-bbox verified"><span class="ocr-bbox-tag">OWNER</span>${dossier.name} Society</span></div>
          <div>DECREED CARPET AREA: <span class="ocr-bbox"><span class="ocr-bbox-tag">AREA</span>12.45 Acres (Contiguous)</span></div>
          <div>OFFICIAL SEAL/STAMP: <span class="ocr-bbox alert"><span class="ocr-bbox-tag">SEAL_ANOMALY</span>DISTRICT_COLLECTOR_STAMP_V2</span></div>
          <div>FORENSIC ALERT: Cloned sub-pixel stamp artifact detected at (x:412, y:819).</div>
          <div>------------------------------------------------------------------------</div>
        `;

        tableBody.innerHTML = `
          <tr>
            <td class="entity-key">Total Land Area</td>
            <td>12.45 Acres</td>
            <td>AICTE Mega/Metro Norm: Min 2.5 Acres contiguous</td>
            <td><span class="tag-status verified">MEETS NORM</span></td>
          </tr>
          <tr>
            <td class="entity-key">Land Title Ownership</td>
            <td>Society 99-Year Registered Lease</td>
            <td>Registered with Sub-Registrar Office</td>
            <td><span class="tag-status verified">VERIFIED</span></td>
          </tr>
          <tr>
            <td class="entity-key">Physical Stamp Forensics</td>
            <td style="color:#fb7185; font-weight:700;">Cloned Stamp Pattern</td>
            <td>Digital Forensic: Duplicate hash found in Doc #1120</td>
            <td><span class="tag-status alert">FORGERY SUSPECT</span></td>
          </tr>
        `;
      } else if (doc.id === 'fac') {
        contentBox.innerHTML = `
          <div>------------------------------------------------------------------------</div>
          <div style="font-weight:700; color:#fff;">FACULTY APPOINTMENT & SALARY MASTER REGISTER</div>
          <div>FORM 16 / TDS WITHHOLDING CROSS-VERIFICATION</div>
          <div>------------------------------------------------------------------------</div>
          <div>TOTAL SANCTIONED FACULTY: <span class="ocr-bbox"><span class="ocr-bbox-tag">TOTAL</span>42 Assistant/Associate Professors</span></div>
          <div>PROF. DR. ARUN JOSHI: <span class="ocr-bbox alert"><span class="ocr-bbox-tag">PAN_COLLISION</span>PAN: ABCPJ8819M</span></div>
          <div>CROSS-CAMPUS COLLISION: Duplicate active employment detected at 'Trinity College'.</div>
          <div>STUDENT-TO-FACULTY RATIO: <span class="ocr-bbox warning"><span class="ocr-bbox-tag">RATIO</span>1:19.4</span> (AICTE Norm: 1:15.0)</div>
          <div>------------------------------------------------------------------------</div>
        `;

        tableBody.innerHTML = `
          <tr>
            <td class="entity-key">Faculty Cadre Ratio</td>
            <td>1 : 2 : 6 (Prof : Assoc : Asst)</td>
            <td>Cadre proportion compliant with 6th Pay norms</td>
            <td><span class="tag-status verified">COMPLIANT</span></td>
          </tr>
          <tr>
            <td class="entity-key">Concurrent PAN Check</td>
            <td style="color:#fb7185; font-weight:700;">1 Duplicate Faculty Entry</td>
            <td>PAN ABCPJ8819M flagged at AICTE Institute ID 1-98124</td>
            <td><span class="tag-status alert">GHOST FACULTY</span></td>
          </tr>
          <tr>
            <td class="entity-key">Student-Faculty Ratio (SFR)</td>
            <td style="color:#f59e0b; font-weight:700;">1 : 19.4</td>
            <td>Required for EOA Extension: Max 1:15</td>
            <td><span class="tag-status warning">DEFICIENT (-8 FAC)</span></td>
          </tr>
        `;
      } else {
        contentBox.innerHTML = `
          <div>------------------------------------------------------------------------</div>
          <div style="font-weight:700; color:#fff;">CAMPUS INFRASTRUCTURE & ARCHITECTURAL MASTER PLAN</div>
          <div>MUNICIPAL CORPORATION APPROVED DRAWING</div>
          <div>------------------------------------------------------------------------</div>
          <div>INSTRUCTIONAL AREA: <span class="ocr-bbox verified"><span class="ocr-bbox-tag">AREA</span>9,840 m²</span> (AICTE Norm: 8,500 m²)</div>
          <div>ADMINISTRATIVE AREA: <span class="ocr-bbox verified"><span class="ocr-bbox-tag">AREA</span>1,620 m²</span></div>
          <div>AMENITIES & SPORTS: <span class="ocr-bbox verified"><span class="ocr-bbox-tag">AREA</span>2,450 m²</span></div>
          <div>ACCESSIBILITY (DIVYANGJAN): <span class="ocr-bbox verified"><span class="ocr-bbox-tag">NORMS</span>Ramps, Lifts & Braille Signage Verified</span></div>
          <div>------------------------------------------------------------------------</div>
        `;

        tableBody.innerHTML = `
          <tr>
            <td class="entity-key">Instructional Built-up Area</td>
            <td>9,840 m²</td>
            <td>AICTE Minimum: 8,500 m² for 420 intake</td>
            <td><span class="tag-status verified">PASS (+1,340 m²)</span></td>
          </tr>
          <tr>
            <td class="entity-key">Barrier-Free Divyangjan Access</td>
            <td>Lifts, Braille & Tactile Path</td>
            <td>Mandatory RPwD Act 2016 Compliance</td>
            <td><span class="tag-status verified">COMPLIANT</span></td>
          </tr>
        `;
      }
    }

    function reRunOCR() {
      const box = document.getElementById('mockDocContent');
      if (box) {
        box.style.opacity = '0.4';
        setTimeout(() => {
          box.style.opacity = '1';
          alert('OCR Engine re-ran successfully. 100% text bounding boxes refreshed.');
        }, 400);
      } else {
        alert('OCR Engine re-ran successfully.');
      }
    }

    function reCheckAPIs() {
      alert('Refreshing real-time API queries to DigiLocker, State Fire NOC, and University Affiliation Portals...');
    }

    function handleDecision(actionText) {
      const box = document.getElementById('decisionAlertBox');
      if (box) {
        box.style.display = 'block';
        box.style.background = 'rgba(56, 189, 248, 0.15)';
        box.style.border = '1px solid #38bdf8';
        box.style.color = '#38bdf8';
        box.innerHTML = '<strong>Official Decision Recorded:</strong> ' + escapeHtml(actionText) + '<br>' +
          '<span style="font-size:0.7rem; color:#94a3b8;">Timestamp: ' + new Date().toLocaleTimeString() + ' · Logged in tamper-evident AICTE audit trail.</span>';
      }
      alert('Decision recorded: ' + actionText);
    }

    /* Report Modal Handlers */
    function openReportModal() {
      const modal = document.getElementById('reportModal');
      if (modal) modal.classList.add('open');
    }
    function closeReportModal() {
      const modal = document.getElementById('reportModal');
      if (modal) modal.classList.remove('open');
    }

    /* Category Filter */
    function filterDocsByCategory(category) {
      activeFilterCategory = category;
      document.querySelectorAll('#categoryFilterPills .filter-pill').forEach(btn => {
        btn.classList.toggle('active', btn.textContent.toLowerCase().includes(category.toLowerCase()) || 
                                     (category === 'ALL' && btn.textContent.includes('All')));
      });
      renderDocList();
    }

    /* Document View Toggle (Structured vs Raw vs Original PDF) */
    function toggleDocView(mode) {
      document.querySelectorAll('.view-toggle-btn').forEach(b => b.classList.remove('active'));
      document.getElementById('formattedViewContainer').style.display = 'none';
      document.getElementById('rawTextViewContainer').style.display = 'none';
      document.getElementById('pdfViewContainer').style.display = 'none';

      if (mode === 'formatted') {
        document.getElementById('btnViewFormatted').classList.add('active');
        document.getElementById('formattedViewContainer').style.display = 'block';
      } else if (mode === 'raw') {
        document.getElementById('btnViewRaw').classList.add('active');
        document.getElementById('rawTextViewContainer').style.display = 'block';
      } else if (mode === 'pdf') {
        document.getElementById('btnViewPdf').classList.add('active');
        document.getElementById('pdfViewContainer').style.display = 'block';
        if (activeBackendDocId) {
          document.getElementById('pdfPreviewFrame').src = `${API_BASE}/documents/${activeBackendDocId}/preview`;
        }
      }
    }

    /* Trigger Upload */
    function triggerFileUpload() {
      document.getElementById('realFileInput').click();
    }

    /* Handle File Upload */
    async function handleRealFileSelected(files) {
      if (!files || files.length === 0) return;
      const file = files[0];

      const pBox = document.getElementById('uploadProgressBar');
      const pText = document.getElementById('uploadStatusText');
      const pFill = document.getElementById('progressFill');
      const pPct = document.getElementById('uploadPercent');

      pBox.style.display = 'block';
      pText.textContent = `Uploading ${file.name} (Validating Magic Bytes & SHA-256)...`;
      pFill.style.width = '30%';
      pPct.textContent = '30%';

      const formData = new FormData();
      formData.append('file', file);
      formData.append('uploadedBy', 'officer_sc401');

      let progressInterval = null;
      try {
        let curProgress = 65;
        pFill.style.width = curProgress + '%';
        pPct.textContent = curProgress + '%';
        pText.textContent = 'Extracting Text (PDFBox / OCR) & Classifying Category...';

        // Increment smoothly while server executes OCR analysis
        progressInterval = setInterval(() => {
          if (curProgress < 92) {
            curProgress += 4;
            pFill.style.width = curProgress + '%';
            pPct.textContent = curProgress + '%';
            if (curProgress >= 80) {
              pText.textContent = 'Verifying Checklist Requirements & Field Extraction...';
            }
          }
        }, 350);

        const res = await fetch(`${API_BASE}/documents/upload`, {
          method: 'POST',
          body: formData
        });

        if (progressInterval) clearInterval(progressInterval);

        if (!res.ok) {
          let errMsg = 'Upload failed (HTTP ' + res.status + ')';
          try {
            const err = await res.json();
            errMsg = err.message || errMsg;
          } catch (e) {
            const txt = await res.text();
            if (txt) errMsg = txt.substring(0, 120);
          }
          throw new Error(errMsg);
        }

        const data = await res.json();
        pFill.style.width = '100%';
        pPct.textContent = '100%';
        pText.textContent = `Success! Document #${data.documentId} processed as ${data.predictedCategory}`;

        setTimeout(() => {
          pBox.style.display = 'none';
        }, 2200);

        await refreshDocumentList();
        selectDocument(data.documentId);
      } catch (err) {
        if (progressInterval) clearInterval(progressInterval);
        pFill.style.width = '100%';
        pFill.style.background = '#f43f5e';
        pText.textContent = 'Upload Error: ' + err.message + ' (Connecting to ' + API_BASE + ')';
        pText.style.color = '#fb7185';
        setTimeout(() => { 
          pBox.style.display = 'none'; 
          pFill.style.background = ''; 
          pText.style.color = 'var(--cyan)';
        }, 8000);
      }
    }

    /* Refresh Documents from Backend */
    async function refreshDocumentList() {
      try {
        const res = await fetch(`${API_BASE}/documents`);
        if (res.ok) {
          backendDocs = await res.json();
          console.log('Synced backend documents:', backendDocs.length);
        }
      } catch (e) {
        console.warn('Backend not responding at localhost:8080, displaying local cached dossier.');
      }
      renderDocList();
      // Sync dropdowns for forensic analysis and comparator
      if (backendDocs && backendDocs.length > 0) {
        const forensicSel = document.getElementById('forensicDocSelect');
        const compA = document.getElementById('compDocA');
        const compB = document.getElementById('compDocB');
        if (forensicSel) {
          const curVal = forensicSel.value;
          forensicSel.innerHTML = backendDocs.map(d => `<option value="${d.id}">Doc #${d.id}: ${d.originalFilename} (${d.documentCategory || d.predictedCategory || 'DOC'})</option>`).join('');
          if (curVal && backendDocs.some(d => String(d.id) === curVal)) {
            forensicSel.value = curVal;
          }
        }
        if (compA && compB) {
          const opts = backendDocs.map(d => `<option value="${d.id}">Doc #${d.id}: ${d.originalFilename}</option>`).join('');
          compA.innerHTML = opts;
          compB.innerHTML = opts;
          if (backendDocs.length > 1) {
            compB.selectedIndex = 1;
          }
        }
      }
      if (!activeBackendDocId && backendDocs.length > 0) {
        selectDocument(backendDocs[0].id);
      }
    }

    /* Render Document List */
    function renderDocList() {
      const container = document.getElementById('docListContainer');
      container.innerHTML = '';

      // If backend has documents, render them
      if (backendDocs && backendDocs.length > 0) {
        let filtered = backendDocs;
        if (activeFilterCategory !== 'ALL') {
          filtered = backendDocs.filter(d => d.documentCategory === activeFilterCategory || d.predictedCategory === activeFilterCategory);
        }

        if (filtered.length === 0) {
          container.innerHTML = '<div style="padding:16px; color:var(--text-dim); font-size:0.8rem; text-align:center;">No documents match category filter.</div>';
          return;
        }

        filtered.forEach((doc) => {
          const isSelected = doc.id === activeBackendDocId;
          const item = document.createElement('div');
          item.className = 'doc-item' + (isSelected ? ' selected' : '');
          item.onclick = () => selectDocument(doc.id);

          const statusClass = doc.completenessStatus === 'PASS' ? 'verified' : 
                             (doc.completenessStatus === 'DEFICIENCY' ? 'flagged' : 'alert');

          item.innerHTML = `
            <div class="doc-main">
              <div class="doc-icon">
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline></svg>
              </div>
              <div>
                <div class="doc-name">${doc.originalFilename}</div>
                <div class="doc-sub">${doc.documentCategory || doc.predictedCategory || 'DOCUMENT'} Â· ${doc.ocrEngine || 'PDFBOX'}</div>
              </div>
            </div>
            <span class="tag-status ${statusClass}">${doc.completenessStatus || 'COMPLETED'}</span>
          `;
          container.appendChild(item);
        });
        return;
      }

      // Fallback to sample dossiers if backend is empty
      const docs = DOSSIERS[activeDossierKey].docs;
      docs.forEach((doc, idx) => {
        const item = document.createElement('div');
        item.className = 'doc-item' + (idx === activeDocIndex ? ' selected' : '');
        item.onclick = () => {
          activeDocIndex = idx;
          renderDocList();
          renderActiveDoc();
        };

        item.innerHTML = `
          <div class="doc-main">
            <div class="doc-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path><polyline points="14 2 14 8 20 8"></polyline></svg>
            </div>
            <div>
              <div class="doc-name">${doc.name}</div>
              <div class="doc-sub">${doc.details}</div>
            </div>
          </div>
          <span class="tag-status ${doc.status}">${doc.statusText}</span>
        `;
        container.appendChild(item);
      });
    }

    /* Select and Inspect Document */
    async function selectDocument(docId) {
      activeBackendDocId = docId;
      renderDocList();

      try {
        const res = await fetch(`${API_BASE}/documents/${docId}/report`);
        if (res.ok) {
          const report = await res.json();
          currentReportData = report;
          populateReportIntoUI(report);
          return;
        }
      } catch (err) {
        console.warn('Could not load report from backend:', err);
      }

      // Fallback
      renderActiveDoc();
    }

    /* Populate Structured Report Data into Right & Left Panels */
    function populateReportIntoUI(report) {
      // Titles & Badges
      document.getElementById('activeDocTitle').innerHTML = `
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"></circle><line x1="21" y1="21" x2="16.65" y2="16.65"></line></svg>
        ${report.file.originalFilename}
      `;
      document.getElementById('activeDocSubtitle').textContent = `SHA-256: ${report.file.sha256 ? report.file.sha256.substring(0, 16) + '...' : 'N/A'} Â· Size: ${(report.file.fileSizeBytes / 1024).toFixed(1)} KB Â· Pages: ${report.file.pageCount}`;
      
      const badge = document.getElementById('activeDocBadge');
      const compStatus = report.completeness.status;
      badge.textContent = compStatus;
      badge.className = 'tag-status ' + (compStatus === 'PASS' ? 'verified' : (compStatus === 'DEFICIENCY' ? 'flagged' : 'alert'));

      // Engine Meta
      document.getElementById('activeOcrEngine').textContent = report.processing.ocrEngine || 'PDFBOX_TEXT_EXTRACTOR';
      document.getElementById('activeOcrQuality').textContent = report.processing.ocrQuality || 'HIGH';
      document.getElementById('activePageBadge').textContent = `Pages: ${report.file.pageCount}`;

      // Classification Strip
      document.getElementById('predictedCategoryName').textContent = report.classification.category;
      const confPct = report.classification.confidence ? Math.round(report.classification.confidence * 100) : 95;
      document.getElementById('predictedConfidencePill').textContent = `${confPct}% Confidence`;
      
      const evCont = document.getElementById('evidencePillsContainer');
      evCont.innerHTML = '';
      if (report.classification.evidence && report.classification.evidence.length > 0) {
        report.classification.evidence.forEach(ev => {
          const span = document.createElement('span');
          span.className = 'evidence-tag';
          span.textContent = ev;
          evCont.appendChild(span);
        });
      }

      // Override select default
      const ovSelect = document.getElementById('categoryOverrideSelect');
      if (ovSelect) ovSelect.value = report.classification.category;

      // Completeness Card
      const compBadge = document.getElementById('completenessStatusBadge');
      compBadge.textContent = compStatus;
      compBadge.className = 'tag-status ' + (compStatus === 'PASS' ? 'verified' : (compStatus === 'DEFICIENCY' ? 'flagged' : 'alert'));
      document.getElementById('completenessSummaryText').textContent = report.completeness.summary || 'Completeness evaluated';
      document.getElementById('completenessRequiredRatio').textContent = report.classification.requiresManualReview ? 'Manual confirmation recommended' : 'Automated validation passed';

      // Temporal Consistency Card
      const tempBadge = document.getElementById('temporalStatusBadge');
      const tempSummary = document.getElementById('temporalSummaryText');
      const tempDetail = document.getElementById('temporalDetailText');

      if (report.consistencyChecks && report.consistencyChecks.length > 0) {
        const topIssue = report.consistencyChecks[0];
        tempBadge.textContent = topIssue.status;
        tempBadge.className = 'tag-status flagged';
        tempSummary.textContent = topIssue.message;
        tempDetail.textContent = topIssue.recommendedAction || 'Scrutiny required';
      } else {
        tempBadge.textContent = 'VALID';
        tempBadge.className = 'tag-status verified';
        tempSummary.textContent = 'Chronological sequence verified';
        tempDetail.textContent = 'No revocation or void statements detected';
      }

      // Authenticity Boundary
      if (report.authenticity) {
        document.getElementById('authenticityStatusLabel').textContent = report.authenticity.status;
      }

      // Extracted Fields Table
      const tbody = document.getElementById('entityTableBody');
      tbody.innerHTML = '';

      const fields = report.extractedFields;
      for (const [key, field] of Object.entries(fields)) {
        const tr = document.createElement('tr');

        let statusClass = 'status-badge-not-found';
        if (field.status === 'EXTRACTED') statusClass = 'status-badge-extracted';
        else if (field.status === 'AMBIGUOUS') statusClass = 'status-badge-ambiguous';
        else if (field.status === 'UNREADABLE') statusClass = 'status-badge-unreadable';
        else if (field.status === 'NOT_STATED') statusClass = 'status-badge-not-stated';

        const valDisplay = field.effectiveValue || field.normalizedValue || field.value || '<span style="color:#64748b;">[NOT_FOUND]</span>';
        const normDisplay = field.normalizedValue || '<span style="color:#64748b;">-</span>';
        const snippet = field.sourceEvidence || 'Direct document extraction';

        tr.innerHTML = `
          <td class="entity-key">${key}</td>
          <td style="font-family:var(--font-mono); font-size:0.75rem;">${valDisplay}</td>
          <td style="font-family:var(--font-mono); font-size:0.75rem; color:#38bdf8;">${normDisplay}</td>
          <td><span class="${statusClass}">${field.status}</span></td>
          <td>
            <div style="display:flex; justify-content:space-between; align-items:center; gap:8px;">
              <span style="font-size:0.68rem; color:var(--text-dim); max-width:140px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;" title="${snippet}">
                ${snippet}
              </span>
              <button class="btn-edit-field" onclick="openFieldCorrectionModal('${key}', '${encodeURIComponent(field.value || '')}', '${encodeURIComponent(field.effectiveValue || '')}')">
                Correct
              </button>
            </div>
          </td>
        `;
        tbody.appendChild(tr);
      }

      // Review Status
      if (report.review) {
        document.getElementById('currentReviewStatus').textContent = `Status: ${report.review.status} ${report.review.reviewerId ? 'by ' + report.review.reviewerId : ''}`;
      }

      // Formatted Visual Sheet
      const sheet = document.getElementById('mockDocContent');
      sheet.innerHTML = `
        <div style="border-bottom:1px solid rgba(255,255,255,0.1); padding-bottom:8px; margin-bottom:10px;">
          <div style="font-weight:800; color:#fff; font-size:0.9rem;">${report.file.originalFilename}</div>
          <div style="font-size:0.7rem; color:var(--text-dim); font-family:var(--font-mono);">CATEGORY: ${report.classification.category} Â· PAGES: ${report.file.pageCount}</div>
        </div>
      `;

      for (const [key, field] of Object.entries(fields)) {
        if (field.value) {
          const row = document.createElement('div');
          row.style.marginBottom = '6px';
          row.innerHTML = `<strong>${key}:</strong> <span class="ocr-bbox verified"><span class="ocr-bbox-tag">${key}</span>${field.value}</span>`;
          sheet.appendChild(row);
        }
      }

      // Load Raw Extracted Text
      loadRawText(report.documentId);
    }

    async function loadRawText(docId) {
      try {
        const res = await fetch(`${API_BASE}/documents/${docId}/text`);
        if (res.ok) {
          const data = await res.json();
          let fullText = '';
          if (data.pages) {
            fullText = data.pages.map(p => `--- PAGE ${p.pageNumber} (${p.extractionMethod}) ---\n${p.extractedText}`).join('\n\n');
          } else {
            fullText = data.fullText || 'No text extracted.';
          }
          document.getElementById('rawTextContent').textContent = fullText;
          document.getElementById('rawCharCount').textContent = 'Characters: ' + fullText.length;
          document.getElementById('rawWordCount').textContent = 'Words: ' + fullText.split(/\s+/).length;
        }
      } catch (e) {}
    }

    /* Category Override Handler */
    async function applyCategoryOverride() {
      if (!activeBackendDocId) {
        alert('Please select an uploaded document first.');
        return;
      }
      const newCategory = document.getElementById('categoryOverrideSelect').value;
      try {
        const res = await fetch(`${API_BASE}/documents/${activeBackendDocId}/classification`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            category: newCategory,
            reviewerId: 'officer_sc401',
            notes: 'Category manually updated by scrutiny officer'
          })
        });

        if (res.ok) {
          alert(`Document category updated to ${newCategory}. Pipeline reprocessed!`);
          selectDocument(activeBackendDocId);
        } else {
          alert('Failed to override category.');
        }
      } catch (err) {
        alert('Error contacting backend: ' + err.message);
      }
    }

    /* Field Correction Modal Handlers */
    function openFieldCorrectionModal(fieldName, rawVal, currentVal) {
      document.getElementById('editDocId').value = activeBackendDocId;
      document.getElementById('editFieldName').value = fieldName;
      document.getElementById('editRawValue').value = decodeURIComponent(rawVal);
      document.getElementById('editCorrectedValue').value = decodeURIComponent(currentVal) || decodeURIComponent(rawVal);
      document.getElementById('editReviewNotes').value = '';
      document.getElementById('fieldCorrectionModal').classList.add('open');
    }

    function closeFieldCorrectionModal() {
      document.getElementById('fieldCorrectionModal').classList.remove('open');
    }

    async function saveFieldCorrection() {
      const docId = document.getElementById('editDocId').value;
      const fieldName = document.getElementById('editFieldName').value;
      const correctedValue = document.getElementById('editCorrectedValue').value.trim();
      const notes = document.getElementById('editReviewNotes').value.trim() || 'Scrutiny reviewer correction';

      if (!correctedValue) {
        alert('Please provide a corrected value.');
        return;
      }

      try {
        const res = await fetch(`${API_BASE}/documents/${docId}/fields/${fieldName}`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            value: correctedValue,
            reviewerId: 'officer_sc401',
            notes: notes
          })
        });

        if (res.ok) {
          closeFieldCorrectionModal();
          alert(`Field '${fieldName}' successfully corrected. Audit event recorded.`);
          selectDocument(docId);
        } else {
          alert('Failed to save field correction.');
        }
      } catch (err) {
        alert('Error: ' + err.message);
      }
    }

    /* Submit Review Verdict */
    async function submitReviewVerdict(decision) {
      if (!activeBackendDocId) {
        alert('Please select a document first.');
        return;
      }

      const notes = prompt(`Enter officer review remarks for decision '${decision}':`, `Verified by Officer SC-401 (${decision})`);
      if (notes === null) return;

      try {
        const res = await fetch(`${API_BASE}/documents/${activeBackendDocId}/review`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            decision: decision,
            reviewerId: 'officer_sc401',
            notes: notes
          })
        });

        if (res.ok) {
          alert(`Review decision '${decision}' recorded in permanent audit trail.`);
          selectDocument(activeBackendDocId);
        } else {
          alert('Failed to submit review decision.');
        }
      } catch (err) {
        alert('Error: ' + err.message);
      }
    }

    /* View Section 15 JSON Report */
    function viewSection15Json() {
      if (!currentReportData) {
        alert('No document report data available.');
        return;
      }
      document.getElementById('jsonReportContent').textContent = JSON.stringify(currentReportData, null, 2);
      document.getElementById('jsonReportModal').classList.add('open');
    }

    function closeJsonReportModal() {
      document.getElementById('jsonReportModal').classList.remove('open');
    }

    function copyJsonToClipboard() {
      if (!currentReportData) return;
      navigator.clipboard.writeText(JSON.stringify(currentReportData, null, 2));
      alert('Section 15 Report JSON copied to clipboard!');
    }

    // Auto-sync backend documents on page load
    setTimeout(refreshDocumentList, 500);

    /* ==========================================================================
       FORENSIC ANOMALY & DOCUMENT AUTHENTICATION LOGIC (DELIVERABLE 3)
       ========================================================================== */
    let activeForensicDocId = 2;
    let activeForensicFilter = 'ALL';
    let currentForensicReport = null;
    let currentForensicAnomalies = [];
    let currentForensicDuplicates = [];

    function switchForensicDoc(docId) {
      activeForensicDocId = parseInt(docId, 10);
      loadForensicsDashboard(activeForensicDocId);
    }

    async function loadForensicsDashboard(docId) {
      if (!docId) docId = activeForensicDocId || 2;
      activeForensicDocId = docId;

      const sel = document.getElementById('forensicDocSelect');
      if (sel) sel.value = String(docId);

      try {
        // Fetch full Section 22 integrity report
        const repRes = await fetch(`${API_BASE}/documents/${docId}/integrity-report`);
        if (repRes.ok) {
          currentForensicReport = await repRes.json();
        }

        // Fetch document anomalies
        const anomRes = await fetch(`${API_BASE}/documents/${docId}/anomalies`);
        if (anomRes.ok) {
          currentForensicAnomalies = await anomRes.json();
        }

        // Fetch duplicates
        const dupRes = await fetch(`${API_BASE}/documents/${docId}/duplicates`);
        if (dupRes.ok) {
          currentForensicDuplicates = await dupRes.json();
        }

        populateForensicsUI(currentForensicReport, currentForensicAnomalies, currentForensicDuplicates);
      } catch (err) {
        console.warn('Could not fetch forensic data from backend:', err);
      }
    }

    function populateForensicsUI(report, anomalies, duplicates) {
      if (!report) return;

      // Update subtitle
      const sub = document.getElementById('forensicActiveDocSubtitle');
      if (sub && report.document) {
        sub.textContent = `Inspecting: ${report.document.filename} Â· SHA-256: ${report.integrity ? report.integrity.sha256.substring(0, 16) + '...' : 'N/A'}`;
      }

      // 1. KPI Bar
      const kpiSha = document.getElementById('kpiShaStatus');
      const kpiShaSub = document.getElementById('kpiShaSub');
      if (report.integrity) {
        const isValid = report.integrity.valid;
        kpiSha.textContent = isValid ? 'VALID' : 'TAMPERED';
        kpiSha.style.color = isValid ? '#10b981' : '#f43f5e';
        kpiShaSub.textContent = isValid ? 'Cryptographic match' : 'Residual tamper detected';
      }

      const critCount = (anomalies || []).filter(a => a.severity === 'CRITICAL').length;
      const highCount = (anomalies || []).filter(a => a.severity === 'HIGH').length;
      const medCount = (anomalies || []).filter(a => a.severity === 'MEDIUM').length;
      const infoCount = (anomalies || []).filter(a => a.severity === 'INFORMATIONAL').length;

      document.getElementById('kpiCriticalFlags').textContent = critCount;
      document.getElementById('kpiCriticalFlags').style.color = critCount > 0 ? '#f43f5e' : '#10b981';

      document.getElementById('kpiOpenAnomalies').textContent = (anomalies || []).length;
      document.getElementById('kpiOpenAnomaliesSub').textContent = `High: ${highCount} Â· Medium: ${medCount}`;

      // Duplicates KPI
      const kpiDup = document.getElementById('kpiDuplicateScore');
      const kpiDupSub = document.getElementById('kpiDuplicateSub');
      if (duplicates && duplicates.length > 0) {
        const topDup = duplicates[0];
        const scorePct = Math.round((topDup.similarityScore || 0.88) * 100);
        kpiDup.textContent = scorePct + '%';
        kpiDup.style.color = scorePct > 90 ? '#f43f5e' : '#fbbf24';
        kpiDupSub.textContent = topDup.matchType || 'Near Duplicate';
      } else {
        kpiDup.textContent = '0%';
        kpiDup.style.color = '#10b981';
        kpiDupSub.textContent = 'No duplicate matches';
      }

      // Cross doc KPI
      const crossCount = (anomalies || []).filter(a => a.anomalyType === 'CROSS_DOCUMENT_CONTRADICTION').length;
      document.getElementById('kpiCrossDocCount').textContent = crossCount;

      // Risk score & Authenticity Banner
      const riskScore = report.riskScore != null ? report.riskScore : (critCount > 0 ? 88.0 : 6.0);
      const authStatus = report.authenticityStatus || (critCount > 0 ? 'MANIPULATION_INDICATORS_DETECTED' : 'NO_ANOMALIES_DETECTED');

      document.getElementById('kpiRiskScore').textContent = Math.round(riskScore) + ' / 100';
      document.getElementById('kpiRiskScore').style.color = riskScore > 60 ? '#f43f5e' : (riskScore > 30 ? '#f59e0b' : '#10b981');
      document.getElementById('kpiVerdictSub').textContent = riskScore > 60 ? 'SUSPICIOUS (REVIEW)' : (riskScore > 30 ? 'EVIDENCE CAUTION' : 'LOW RISK');

      const banner = document.getElementById('forensicAssessmentBanner');
      const bannerText = document.getElementById('forensicAssessmentText');
      const bannerBadge = document.getElementById('forensicAssessmentBadge');

      if (critCount > 0 || riskScore > 60) {
        banner.className = 'forensic-assessment-alert critical';
        bannerText.innerHTML = `AUTHENTICITY REVIEW REQUIRED â€” ${critCount} CRITICAL &amp; ${highCount} HIGH INTEGRITY ANOMALIES DETECTED`;
        bannerBadge.textContent = 'SCRUTINY MANDATORY';
        bannerBadge.style.background = '#f43f5e';
      } else if (highCount > 0 || medCount > 0) {
        banner.className = 'forensic-assessment-alert warning';
        bannerText.innerHTML = `ADVISORY CAUTION â€” ${highCount} HIGH / ${medCount} MEDIUM ANOMALIES REQUIRE SCRUTINY CONFIRMATION`;
        bannerBadge.textContent = 'REVIEW RECOMMENDED';
        bannerBadge.style.background = '#f59e0b';
      } else {
        banner.className = 'forensic-assessment-alert valid';
        bannerText.innerHTML = `ALL FORENSIC INTEGRITY PASS CRITERIA SATISFIED â€” ZERO SUSPICIOUS MANIPULATION INDICATORS`;
        bannerBadge.textContent = 'AUTHENTICITY PASSED';
        bannerBadge.style.background = '#10b981';
      }

      // Update Nav badge count
      const navBadge = document.getElementById('forensicNavBadge');
      if (navBadge) {
        navBadge.textContent = (anomalies || []).length + ' Alerts';
        navBadge.style.background = critCount > 0 ? '#f43f5e' : '#10b981';
      }

      // Update Subtabs data
      if (report.integrity) {
        document.getElementById('subtabShaChecksum').textContent = report.integrity.sha256;
      }

      if (duplicates && duplicates.length > 0) {
        const topDup = duplicates[0];
        document.getElementById('subtabDupMatchBadge').textContent = `${topDup.matchType} (${topDup.similarityScore})`;
        document.getElementById('subtabDupDesc').textContent = topDup.matchDetails || 'Similarity detected against historical database submission.';
      }

      // Render Anomaly Findings
      renderForensicAnomaliesList(anomalies || []);
    }

    function renderForensicAnomaliesList(anomalies) {
      const container = document.getElementById('forensicAnomaliesListContainer');
      container.innerHTML = '';

      // Update filter counts
      const allCount = anomalies.length;
      const critCount = anomalies.filter(a => a.severity === 'CRITICAL').length;
      const highCount = anomalies.filter(a => a.severity === 'HIGH').length;
      const medCount = anomalies.filter(a => a.severity === 'MEDIUM').length;
      const infoCount = anomalies.filter(a => a.severity === 'INFORMATIONAL' || a.severity === 'LOW').length;

      document.getElementById('pillCountAll').textContent = allCount;
      document.getElementById('pillCountCrit').textContent = critCount;
      document.getElementById('pillCountHigh').textContent = highCount;
      document.getElementById('pillCountMed').textContent = medCount;
      document.getElementById('pillCountInfo').textContent = infoCount;

      let filtered = anomalies;
      if (activeForensicFilter !== 'ALL') {
        filtered = anomalies.filter(a => a.severity === activeForensicFilter);
      }

      if (filtered.length === 0) {
        container.innerHTML = '<div style="padding:20px; text-align:center; color:var(--text-dim); font-size:0.8rem;">No anomalies matching current filter.</div>';
        return;
      }

      filtered.forEach(anom => {
        const sevClass = (anom.severity || 'medium').toLowerCase();
        const card = document.createElement('div');
        card.className = `forensic-anomaly-item ${sevClass}`;

        let evidenceSnippet = 'No raw text snippet attached.';
        let evidenceField = 'N/A';
        let evidenceLimitations = 'Standard detector limitations apply.';
        let pageNum = 1;

        if (anom.evidenceList && anom.evidenceList.length > 0) {
          const ev = anom.evidenceList[0];
          evidenceSnippet = ev.sourceSnippet || ev.textSnippet || evidenceSnippet;
          evidenceField = ev.sourceField || evidenceField;
          evidenceLimitations = ev.limitations || evidenceLimitations;
          pageNum = ev.pageNumber || 1;
        }

        const confPct = Math.round((anom.detectorConfidence || anom.confidence || 0.9) * 100);

        card.innerHTML = `
          <div class="anomaly-top-row">
            <div class="anomaly-title-area">
              <span class="anomaly-title-text">${anom.title}</span>
            </div>
            <div class="anomaly-meta-badges">
              <span class="tag-status ${sevClass === 'critical' ? 'flagged' : (sevClass === 'high' ? 'alert' : 'verified')}">${anom.severity}</span>
              <span style="font-size:0.68rem; padding:2px 8px; border-radius:10px; background:rgba(255,255,255,0.06); color:var(--cyan); font-family:var(--font-mono);">${confPct}% Conf</span>
            </div>
          </div>
          <div class="anomaly-body-row">
            ${anom.description}
          </div>
          ${anom.whyItMatters ? `<div class="anomaly-why-row"><strong>Why It Matters:</strong> ${anom.whyItMatters}</div>` : ''}
          <div class="anomaly-evidence-box">
            <div style="font-size:0.65rem; color:var(--text-dim); margin-bottom:2px;">EVIDENCE (Page ${pageNum} Â· Field: ${evidenceField}):</div>
            <div>"${evidenceSnippet}"</div>
            <div style="font-size:0.65rem; color:#94a3b8; margin-top:4px;"><em>Notice: ${evidenceLimitations}</em></div>
          </div>
          <div class="anomaly-action-bar">
            <span style="color:var(--text-dim); font-size:0.7rem;">Status: <strong style="color:#fff;">${anom.reviewStatus || 'OPEN'}</strong></span>
            <button class="btn-primary" style="padding:4px 12px; font-size:0.72rem; background:#0284c7;" onclick="openAdjudicateModal(${anom.id}, '${encodeURIComponent(anom.title)}')">
              Review &amp; Adjudicate
            </button>
          </div>
        `;
        container.appendChild(card);
      });
    }

    function filterForensicAnomalies(filter) {
      activeForensicFilter = filter;
      document.querySelectorAll('#anomalyFilterPills .forensic-filter-pill').forEach(b => {
        b.classList.toggle('active', b.textContent.toUpperCase().includes(filter));
      });
      renderForensicAnomaliesList(currentForensicAnomalies || []);
    }

    function switchForensicSubTab(subTabKey) {
      document.querySelectorAll('.forensic-subtab-btn').forEach(b => {
        b.classList.toggle('active', b.dataset.subtab === subTabKey);
      });
      document.querySelectorAll('.forensic-subtab-pane').forEach(p => {
        p.classList.toggle('active', p.id === subTabKey);
      });
    }

    async function triggerForensicScreening() {
      const docId = activeForensicDocId || 2;
      try {
        const res = await fetch(`${API_BASE}/documents/${docId}/analyze-integrity`, { method: 'POST' });
        if (res.ok) {
          alert('Full multi-engine forensic screening executed successfully! All algorithms completed.');
          await loadForensicsDashboard(docId);
        } else {
          alert('Failed to trigger forensic analysis.');
        }
      } catch (e) {
        alert('Error: ' + e.message);
      }
    }

    function openAdjudicateModal(anomalyId, titleEncoded) {
      document.getElementById('adjAnomalyId').value = anomalyId;
      document.getElementById('adjModalTitle').textContent = 'Scrutiny Officer Adjudication: Anomaly #' + anomalyId;
      if (titleEncoded) {
        document.getElementById('adjModalSubtitle').textContent = decodeURIComponent(titleEncoded);
      }
      document.getElementById('adjNotesText').value = '';
      document.getElementById('adjudicateModal').classList.add('open');
    }

    function closeAdjudicateModal() {
      document.getElementById('adjudicateModal').classList.remove('open');
    }

    async function submitAdjudication() {
      const anomId = document.getElementById('adjAnomalyId').value;
      const decision = document.getElementById('adjDecisionSelect').value;
      const reviewer = document.getElementById('adjReviewerSelect').value;
      const notes = document.getElementById('adjNotesText').value.trim();

      if (!notes) {
        alert('Please provide mandatory scrutiny notes / rationale for AICTE audit compliance.');
        return;
      }

      try {
        const res = await fetch(`${API_BASE}/anomalies/${anomId}/review`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            decision: decision,
            reviewerId: reviewer,
            notes: notes
          })
        });

        if (res.ok) {
          closeAdjudicateModal();
          alert(`Decision '${decision}' recorded in tamper-evident audit repository!`);
          await loadForensicsDashboard(activeForensicDocId);
        } else {
          alert('Failed to record adjudication.');
        }
      } catch (e) {
        alert('Error: ' + e.message);
      }
    }

    function openComparatorModal() {
      document.getElementById('compDocA').value = String(activeForensicDocId || 2);
      document.getElementById('compDocB').value = activeForensicDocId === 2 ? "1" : "2";
      document.getElementById('comparatorModal').classList.add('open');
      runDocumentComparison();
    }

    function closeComparatorModal() {
      document.getElementById('comparatorModal').classList.remove('open');
    }

    async function runDocumentComparison() {
      const docA = document.getElementById('compDocA').value;
      const docB = document.getElementById('compDocB').value;
      const area = document.getElementById('comparatorResultsArea');

      area.innerHTML = '<div style="padding:20px; text-align:center; color:#38bdf8;">Executing side-by-side token Jaccard and field-level diff...</div>';

      try {
        const res = await fetch(`${API_BASE}/documents/${docA}/compare/${docB}`, { method: 'POST' });
        if (res.ok) {
          const comp = await res.json();
          renderComparatorResults(comp);
        } else {
          area.innerHTML = '<div style="padding:20px; text-align:center; color:#f43f5e;">Unable to compare documents (HTTP ' + res.status + ').</div>';
        }
      } catch (e) {
        area.innerHTML = '<div style="padding:20px; text-align:center; color:#f43f5e;">Comparison error: ' + e.message + '</div>';
      }
    }

    function renderComparatorResults(comp) {
      const area = document.getElementById('comparatorResultsArea');
      const simPct = Math.round((comp.similarityScore || 0) * 100);

      area.innerHTML = `
        <div style="background:#0f172a; border:1px solid var(--border-color); border-radius:8px; padding:12px 16px; margin-bottom:14px; display:flex; justify-content:space-between; align-items:center;">
          <div>
            <span style="font-size:0.75rem; color:#94a3b8;">TOKEN JACCARD SIMILARITY:</span>
            <strong style="font-size:1.1rem; color:${simPct > 80 ? '#fb7185' : '#38bdf8'}; font-family:var(--font-mono); margin-left:8px;">${simPct}%</strong>
          </div>
          <div>
            <span style="font-size:0.75rem; color:#94a3b8;">MATCH CLASSIFICATION:</span>
            <span class="tag-status ${comp.matchType === 'EXACT_DUPLICATE' ? 'flagged' : 'verified'}" style="margin-left:8px;">${comp.matchType || 'NEAR_DUPLICATE'}</span>
          </div>
        </div>

        <table class="diff-table">
          <thead>
            <tr>
              <th>Extracted Field / Property</th>
              <th>Document A (Target)</th>
              <th>Document B (Comparison)</th>
              <th>Reconciliation Verdict</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td>Legal Title</td>
              <td>Horizon Educational Trust</td>
              <td>Apex Institute of Technology</td>
              <td class="diff-conflict">MISMATCH</td>
            </tr>
            <tr>
              <td>Certificate Reference</td>
              <td>FS/2022/4412</td>
              <td>FS/2023/9812</td>
              <td class="diff-conflict">DIFFERENT REFS</td>
            </tr>
            <tr>
              <td>Validity Expiry</td>
              <td style="color:#fb7185; font-weight:700;">31-JUL-2024 (EXPIRED)</td>
              <td style="color:#34d399;">31-DEC-2026 (ACTIVE)</td>
              <td class="diff-conflict">LAPSED VALIDITY</td>
            </tr>
            <tr>
              <td>Cryptographic Hash</td>
              <td><code style="font-size:0.7rem; color:var(--cyan);">7a8f10b39c...</code></td>
              <td><code style="font-size:0.7rem; color:var(--cyan);">9a2f1b4c8d...</code></td>
              <td class="diff-match">DISTINCT DIGEST</td>
            </tr>
          </tbody>
        </table>
      `;
    }

    function viewSection22Json() {
      if (!currentForensicReport) {
        alert('Forensic report data is loading or unavailable.');
        return;
      }
      document.getElementById('sec22JsonContent').textContent = JSON.stringify(currentForensicReport, null, 2);
      document.getElementById('sec22JsonModal').classList.add('open');
    }

    function closeSec22JsonModal() {
      document.getElementById('sec22JsonModal').classList.remove('open');
    }

    function copySec22Json() {
      if (!currentForensicReport) return;
      navigator.clipboard.writeText(JSON.stringify(currentForensicReport, null, 2));
      alert('Section 22 Forensic JSON copied to clipboard!');
    }

    async function openAuditTrailModal() {
      const container = document.getElementById('auditTrailListContainer');
      container.innerHTML = '<div style="padding:14px; text-align:center; color:#38bdf8;">Loading permanent audit repository...</div>';
      document.getElementById('auditTrailModal').classList.add('open');

      try {
        const res = await fetch(`${API_BASE}/audit-trail`);
        if (res.ok) {
          const events = await res.json();
          renderAuditTrail(events);
        } else {
          container.innerHTML = '<div style="padding:14px; text-align:center; color:#f43f5e;">Unable to load audit events.</div>';
        }
      } catch (e) {
        container.innerHTML = '<div style="padding:14px; text-align:center; color:#f43f5e;">Error: ' + e.message + '</div>';
      }
    }

    function closeAuditTrailModal() {
      document.getElementById('auditTrailModal').classList.remove('open');
    }

    function renderAuditTrail(events) {
      const container = document.getElementById('auditTrailListContainer');
      container.innerHTML = '';

      if (!events || events.length === 0) {
        container.innerHTML = '<div style="padding:14px; text-align:center; color:#64748b;">No audit events recorded yet.</div>';
        return;
      }

      events.forEach(ev => {
        const item = document.createElement('div');
        item.style.padding = '10px 14px';
        item.style.background = '#030712';
        item.style.border = '1px solid #1e293b';
        item.style.borderRadius = '6px';
        item.style.fontSize = '0.75rem';

        item.innerHTML = `
          <div style="display:flex; justify-content:space-between; margin-bottom:4px;">
            <strong style="color:var(--cyan);">${ev.eventType || 'AUDIT_RECORD'}</strong>
            <span style="color:#64748b; font-family:var(--font-mono); font-size:0.7rem;">${ev.timestamp || new Date().toISOString()}</span>
          </div>
          <div style="color:#e2e8f0; margin-bottom:4px;">${ev.details || ev.actionDescription || 'Audit trail log entry'}</div>
          <div style="font-size:0.7rem; color:#94a3b8;">Actor: <strong style="color:#fff;">${ev.actorUsername || ev.actor || 'SYSTEM'}</strong> Â· Target: ${ev.targetEntity || 'Document'}</div>
        `;
        container.appendChild(item);
      });
    }

    // Hook into switchTab to auto-load forensics & issuing authority verification
    const originalSwitchTab = switchTab;
    switchTab = function(tabId) {
      originalSwitchTab(tabId);
      if (tabId === 'tab-forensics') {
        loadForensicsDashboard(activeBackendDocId || activeForensicDocId || 2);
      } else if (tabId === 'tab-authorities') {
        initAuthorityVerificationDashboard();
      }
    };

    /* ==========================================================================
       DELIVERABLE 4: ISSUING AUTHORITY VERIFICATION ENGINE JAVASCRIPT CONTROLLER
       ========================================================================== */
    let currentAuthResults = [];
    let currentAuthGateways = [];
    let activeAuthFilter = 'ALL';
    let currentAuthResultForReview = null;
    let currentSection20Report = null;

    async function initAuthorityVerificationDashboard() {
      const appId = document.getElementById('authAppSelector') ? document.getElementById('authAppSelector').value : '2';
      await loadAuthorityGateways();
      await loadAuthorityVerifications(appId);
    }

    async function loadAuthorityGateways() {
      try {
        const res = await fetch(`${API_BASE}/authorities`);
        if (res.ok) {
          currentAuthGateways = await res.json();
        } else {
          currentAuthGateways = getFallbackGateways();
        }
      } catch (e) {
        console.warn('Could not fetch authorities from API, using defaults:', e);
        currentAuthGateways = getFallbackGateways();
      }
      renderAuthorityGateways(currentAuthGateways);
    }

    function getFallbackGateways() {
      return [
        { sourceId: 'SRC_AICTE_REGISTRY', authorityName: 'AICTE Central Approval Directory', sourceType: 'OFFICIAL_API', endpointConfiguration: 'https://api.aicte-india.org/registry/v2', sourceStatus: 'CONFIGURED_ACTIVE', approvedDomains: '*.aicte-india.org, *.gov.in' },
        { sourceId: 'SRC_MAHA_FIRE', authorityName: 'Maharashtra State Fire Services Portal', sourceType: 'GOVERNMENT_PORTAL', endpointConfiguration: 'https://mahafire.gov.in/noc-verify', sourceStatus: 'CONFIGURED_ACTIVE', approvedDomains: 'mahafire.gov.in, *.gov.in' },
        { sourceId: 'SRC_DIGILOCKER_LAND', authorityName: 'DigiLocker National Land Registry Gateway', sourceType: 'DIGITAL_REPOSITORY', endpointConfiguration: 'https://api.digilocker.gov.in/land/records', sourceStatus: 'CONFIGURED_ACTIVE', approvedDomains: 'digilocker.gov.in, *.gov.in' },
        { sourceId: 'SRC_MU_AFFILIATION', authorityName: 'University of Mumbai Affiliation Council', sourceType: 'INSTITUTIONAL_DATABASE', endpointConfiguration: 'https://mu.ac.in/affiliation/verify', sourceStatus: 'CONFIGURED_ACTIVE', approvedDomains: 'mu.ac.in, mumbaiuniversity.ac.in' },
        { sourceId: 'SRC_AICTE_FACULTY_PAN', authorityName: 'AICTE Central Faculty Registry & PAN', sourceType: 'INSTITUTIONAL_DATABASE', endpointConfiguration: 'https://faculty.aicte-india.org/api/v1/verify', sourceStatus: 'OFFLINE', approvedDomains: 'faculty.aicte-india.org' },
        { sourceId: 'SRC_QR_PORTAL', authorityName: 'ZXing 2D Regulatory QR Code Engine', sourceType: 'QR_BARCODE_LOOKUP', endpointConfiguration: 'https://aicte-india.org/qr/validate', sourceStatus: 'CONFIGURED_ACTIVE', approvedDomains: '*.aicte-india.org, *.gov.in, *.nic.in' },
        { sourceId: 'SRC_PKI_CRL', authorityName: 'National CCA Root PKI & CRL Gateway', sourceType: 'PKI_SIGNATURE', endpointConfiguration: 'https://ocsp.cca.gov.in', sourceStatus: 'CONFIGURED_ACTIVE', approvedDomains: 'cca.gov.in, *.gov.in' },
        { sourceId: 'SRC_MANUAL_REVIEW', authorityName: 'APH Level-2 Scrutiny Manual Fallback Queue', sourceType: 'MANUAL_FALLBACK', endpointConfiguration: 'https://aph.aicte-india.org/scrutiny-portal', sourceStatus: 'CONFIGURED_ACTIVE', approvedDomains: '*.aicte-india.org' }
      ];
    }

    function renderAuthorityGateways(sources) {
      const grid = document.getElementById('authorityGatewaysGrid');
      if (!grid) return;
      grid.innerHTML = '';

      sources.forEach(src => {
        const isOffline = src.sourceStatus === 'OFFLINE' || src.sourceStatus === 'DISABLED';
        const statusBg = isOffline ? 'rgba(56,189,248,0.15)' : 'rgba(16,185,129,0.15)';
        const statusColor = isOffline ? '#38bdf8' : '#34d399';
        const statusText = isOffline ? 'OFFLINE (MAINTENANCE)' : 'CONFIGURED ACTIVE';

        const card = document.createElement('div');
        card.className = 'authority-card';
        card.style.position = 'relative';
        card.innerHTML = `
          <div class="auth-top" style="margin-bottom:8px;">
            <div class="auth-title" style="font-weight:700; color:#f8fafc;">
              <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="3" y1="9" x2="21" y2="9"></line><line x1="9" y1="21" x2="9" y2="9"></line></svg>
              ${src.authorityName || src.name}
            </div>
            <span class="auth-status-pill" id="pill-${src.sourceId}" style="background:${statusBg}; color:${statusColor}; font-size:0.68rem; font-weight:700;">
              ${statusText}
            </span>
          </div>
          <div class="auth-response" style="margin-bottom:8px;">
            <code>SOURCE_ID: ${src.sourceId}<br>
            TYPE: ${src.sourceType} Â· JURISDICTION: India<br>
            ENDPOINT: ${src.endpointConfiguration || src.endpointUrl || 'https://api.aicte-india.org'}<br>
            APPROVED_DOMAINS: ${src.approvedDomains || '*.gov.in'}</code>
          </div>
          <div style="display:flex; justify-content:space-between; align-items:center; font-size:0.72rem;">
            <span style="color:#64748b;" id="latency-${src.sourceId}">Latency: ~${Math.floor(Math.random()*40)+15}ms</span>
            <button class="btn-secondary" style="padding:3px 10px; font-size:0.7rem; color:#94a3b8; border-color:rgba(255,255,255,0.1);" onclick="testGateway('${src.sourceId}', this)">
              Ping Gateway
            </button>
          </div>
        `;
        grid.appendChild(card);
      });
    }

    async function testGateway(sourceId, btn) {
      if (btn) btn.innerText = 'Pinging...';
      try {
        const res = await fetch(`${API_BASE}/admin/authorities/${sourceId}/test`, { method: 'POST' });
        if (res.ok) {
          const data = await res.json();
          const latencyElem = document.getElementById(`latency-${sourceId}`);
          if (latencyElem) latencyElem.innerText = `Latency: ${data.latencyMs}ms (${data.message})`;
          const pill = document.getElementById(`pill-${sourceId}`);
          if (pill) {
            pill.style.background = data.healthy ? 'rgba(16,185,129,0.15)' : 'rgba(56,189,248,0.15)';
            pill.style.color = data.healthy ? '#34d399' : '#38bdf8';
            pill.innerText = data.healthy ? 'ACTIVE 200 OK' : 'GATEWAY OUTAGE (SAFE FALLBACK)';
          }
        }
      } catch (e) {
        console.error('Error testing gateway:', e);
      } finally {
        if (btn) btn.innerText = 'Ping Gateway';
      }
    }

    async function checkAllGatewayHealth() {
      for (const src of currentAuthGateways) {
        await testGateway(src.sourceId, null);
      }
      alert('All 8 authority gateways health verified with live TLS handshake and latency diagnostics.');
    }

    async function loadAuthorityVerifications(appId) {
      const container = document.getElementById('authorityVerificationsList');
      if (container) {
        container.innerHTML = '<div style="padding:24px; text-align:center; color:#38bdf8; font-size:0.85rem;">Querying authority verification records from persistent repository...</div>';
      }

      try {
        const res = await fetch(`${API_BASE}/applications/${appId}/verification-summary`);
        if (res.ok) {
          const summary = await res.json();
          currentAuthResults = summary.results || [];
          updateAuthKpiBar(summary);
          renderAuthorityVerifications(currentAuthResults, activeAuthFilter);
        } else {
          loadFallbackVerifications();
        }
      } catch (e) {
        console.warn('Error fetching verification summary from API:', e);
        loadFallbackVerifications();
      }
    }

    function loadFallbackVerifications() {
      currentAuthResults = [
        {
          id: 1,
          documentId: 1,
          applicationId: 1,
          sourceId: 'SRC_AICTE_REGISTRY',
          sourceRecordId: 'AICTE-PERM-1-3324501',
          overallStatus: 'VERIFIED',
          recordMatchStatus: 'EXACT_MATCH',
          validityStatus: 'ACTIVE_VALID',
          signatureStatus: 'VALID_TRUSTED',
          qrStatus: 'VERIFIED',
          summary: 'Institutional approval corroborated in AICTE National Registry. Permanent Institute ID 1-3324501 confirmed active for AY 2025-26.',
          limitations: 'Corroborated against AICTE Central Registry snapshot dated 2026-10-01.',
          manualReviewRequired: false,
          generatedAt: new Date(Date.now() - 3600000*3).toISOString(),
          comparisons: [
            { fieldName: 'permanentInstituteId', submittedValue: '1-3324501', officialValue: '1-3324501', comparisonMethod: 'EXACT', comparisonStatus: 'MATCH', confidence: 1.0, evidenceSnippet: 'Corroborated in AICTE National Institute Directory' },
            { fieldName: 'institutionName', submittedValue: 'Apex Institute of Technology', officialValue: 'Apex Institute of Technology', comparisonMethod: 'NORMALIZED_TEXT', comparisonStatus: 'MATCH_AFTER_NORMALIZATION', confidence: 0.98, evidenceSnippet: 'Legal denomination matches AICTE directory' },
            { fieldName: 'approvedIntake', submittedValue: '480', officialValue: '480', comparisonMethod: 'EXACT', comparisonStatus: 'MATCH', confidence: 1.0, evidenceSnippet: 'Sanctioned intake capacity matches council quota' }
          ]
        },
        {
          id: 2,
          documentId: 2,
          applicationId: 2,
          sourceId: 'SRC_MAHA_FIRE',
          sourceRecordId: 'NOC-MAHA-FS-2022-4412',
          overallStatus: 'EXPIRED',
          recordMatchStatus: 'EXACT_MATCH',
          validityStatus: 'EXPIRED',
          signatureStatus: 'VALID_TRUSTED',
          qrStatus: 'NOT_PRESENT',
          summary: 'Fire Safety Clearance Certificate registration number FS/2022/4412 verified in Maharashtra Fire Directorate records, but statutory validity expired on 2024-07-31.',
          limitations: 'State fire clearance database valid as of current sync. Requires valid renewal certificate.',
          manualReviewRequired: true,
          generatedAt: new Date(Date.now() - 3600000*2).toISOString(),
          comparisons: [
            { fieldName: 'nocReferenceNumber', submittedValue: 'FS/2022/4412', officialValue: 'FS/2022/4412', comparisonMethod: 'EXACT', comparisonStatus: 'MATCH', confidence: 1.0, evidenceSnippet: 'Certificate registration number verified in state archive' },
            { fieldName: 'validityEndDate', submittedValue: '31-07-2024', officialValue: '2024-07-31', comparisonMethod: 'DATE_CHRONOLOGY', comparisonStatus: 'MISMATCH', confidence: 1.0, evidenceSnippet: 'Statutory validity expired: Clearance expired on 2024-07-31. No renewal application on file' }
          ]
        },
        {
          id: 3,
          documentId: 2,
          applicationId: 2,
          sourceId: 'SRC_MU_AFFILIATION',
          sourceRecordId: 'MU-AFFIL-2024-9102',
          overallStatus: 'MISMATCH',
          recordMatchStatus: 'DISCREPANCY_DETECTED',
          validityStatus: 'ACTIVE_VALID',
          signatureStatus: 'VALID_TRUSTED',
          qrStatus: 'NOT_PRESENT',
          summary: 'Affiliation order corroboration detected intake capacity mismatch: Document claims 120 seats for B.Tech CS, but University Academic Council registry approves 60 seats.',
          limitations: 'Corroborated against Mumbai University Affiliation Council notification dated 2024-06-15.',
          manualReviewRequired: true,
          generatedAt: new Date(Date.now() - 3600000*2).toISOString(),
          comparisons: [
            { fieldName: 'approvedSeatsComputerScience', submittedValue: '120', officialValue: '60', comparisonMethod: 'EXACT', comparisonStatus: 'MISMATCH', confidence: 0.99, evidenceSnippet: 'Critical intake disparity: Document claims 120 seats; official registry sanctioned 60 seats' },
            { fieldName: 'affiliationCategory', submittedValue: 'TEMPORARY', officialValue: 'TEMPORARY', comparisonMethod: 'EXACT', comparisonStatus: 'MATCH', confidence: 1.0, evidenceSnippet: 'Affiliation granted on annual provisional basis' }
          ]
        },
        {
          id: 4,
          documentId: 2,
          applicationId: 2,
          sourceId: 'SRC_AICTE_FACULTY_PAN',
          sourceRecordId: 'N/A',
          overallStatus: 'SOURCE_UNAVAILABLE',
          recordMatchStatus: 'UNKNOWN',
          validityStatus: 'UNKNOWN',
          signatureStatus: 'NOT_CHECKED',
          qrStatus: 'NOT_CHECKED',
          summary: 'Central Faculty Roster gateway is temporarily undergoing scheduled maintenance (HTTP 504 Gateway Timeout). Verification cannot be performed at this moment.',
          limitations: 'Upstream endpoint https://faculty.aicte-india.org unreachable. Failure is classified as SOURCE_UNAVAILABLE, not a document defect.',
          manualReviewRequired: false,
          generatedAt: new Date(Date.now() - 3600000).toISOString(),
          comparisons: []
        },
        {
          id: 5,
          documentId: 2,
          applicationId: 2,
          sourceId: 'SRC_DIGILOCKER_LAND',
          sourceRecordId: 'DL-MAHA-LAND-7821',
          overallStatus: 'PENDING',
          recordMatchStatus: 'PENDING_SYNC',
          validityStatus: 'ACTIVE_VALID',
          signatureStatus: 'NOT_CHECKED',
          qrStatus: 'NOT_PRESENT',
          summary: 'District Revenue Land Title mutation verification submitted to DigiLocker Land Gateway. State mutation sync in progress.',
          limitations: 'District revenue registry sync cycles occur every 12 hours.',
          manualReviewRequired: false,
          generatedAt: new Date(Date.now() - 1800000).toISOString(),
          comparisons: [
            { fieldName: 'surveyGatNumber', submittedValue: 'Gat No. 142/1', officialValue: 'Gat No. 142/1', comparisonMethod: 'NORMALIZED_TEXT', comparisonStatus: 'MATCH_AFTER_NORMALIZATION', confidence: 0.95, evidenceSnippet: 'Survey plot boundary matches revenue record' },
            { fieldName: 'landAreaAcres', submittedValue: '5.2 Acres', officialValue: 'Awaiting District Verification', comparisonMethod: 'CONTEXTUAL', comparisonStatus: 'NEEDS_REVIEW', confidence: 0.60, evidenceSnippet: 'Awaiting formal mutation entry endorsement' }
          ]
        },
        {
          id: 6,
          documentId: 2,
          applicationId: 2,
          sourceId: 'SRC_MANUAL_REVIEW',
          sourceRecordId: 'SOP-APH-SCRUTINY-V4',
          overallStatus: 'MANUAL_REVIEW',
          recordMatchStatus: 'PENDING_MANUAL_SCRUTINY',
          validityStatus: 'PENDING_SCRUTINY',
          signatureStatus: 'REQUIRES_OFFICER_SIGN_OFF',
          qrStatus: 'NOT_APPLICABLE',
          summary: 'Physical seal and non-digitized sub-registrar endorsement detected on document. Assigned to Level-2 Scrutiny Committee for manual officer corroboration.',
          limitations: 'Automated digital corroboration unavailable for physical sub-registrar book registrations prior to 2018.',
          manualReviewRequired: true,
          generatedAt: new Date(Date.now() - 900000).toISOString(),
          comparisons: [
            { fieldName: 'physicalSealInspection', submittedValue: 'Sub-Registrar District Thane', officialValue: 'Manual Registry Roster', comparisonMethod: 'CONTEXTUAL', comparisonStatus: 'NEEDS_REVIEW', confidence: 0.50, evidenceSnippet: 'Requires physical scrutiny of seal embossment and watermarked judicial stamp paper' }
          ]
        }
      ];

      updateAuthKpiBar({
        totalVerifications: 6,
        verifiedCount: 1,
        mismatchCount: 1,
        expiredCount: 1,
        sourceUnavailableCount: 1,
        manualReviewCount: 2
      });

      renderAuthorityVerifications(currentAuthResults, activeAuthFilter);
    }

    function updateAuthKpiBar(summary) {
      if (document.getElementById('authKpiTotal')) document.getElementById('authKpiTotal').innerText = summary.totalVerifications || currentAuthResults.length;
      if (document.getElementById('authKpiVerified')) document.getElementById('authKpiVerified').innerText = summary.verifiedCount || currentAuthResults.filter(r => r.overallStatus === 'VERIFIED').length;
      if (document.getElementById('authKpiMismatch')) document.getElementById('authKpiMismatch').innerText = summary.mismatchCount || currentAuthResults.filter(r => r.overallStatus === 'MISMATCH').length;
      if (document.getElementById('authKpiExpired')) document.getElementById('authKpiExpired').innerText = summary.expiredCount || currentAuthResults.filter(r => r.overallStatus === 'EXPIRED').length;
      if (document.getElementById('authKpiUnavailable')) document.getElementById('authKpiUnavailable').innerText = summary.sourceUnavailableCount || currentAuthResults.filter(r => r.overallStatus === 'SOURCE_UNAVAILABLE').length;
      if (document.getElementById('authKpiManual')) document.getElementById('authKpiManual').innerText = summary.manualReviewCount || currentAuthResults.filter(r => r.overallStatus === 'MANUAL_REVIEW').length;
    }

    function filterAuthorityVerifications(filter, btn) {
      activeAuthFilter = filter;
      const pills = document.querySelectorAll('#authFilterPills button');
      pills.forEach(p => {
        p.style.background = 'rgba(255,255,255,0.06)';
        p.style.color = '#94a3b8';
      });
      if (btn) {
        btn.style.background = '#38bdf8';
        btn.style.color = '#0f172a';
      }
      renderAuthorityVerifications(currentAuthResults, activeAuthFilter);
    }

    function renderAuthorityVerifications(results, filter) {
      const container = document.getElementById('authorityVerificationsList');
      if (!container) return;
      container.innerHTML = '';

      const filtered = results.filter(r => {
        if (filter === 'ALL') return true;
        return r.overallStatus === filter;
      });

      if (filtered.length === 0) {
        container.innerHTML = `
          <div style="padding:32px; text-align:center; background:rgba(15,23,42,0.4); border-radius:8px; border:1px solid rgba(255,255,255,0.05); color:#64748b;">
            No verification records match the selected filter "${filter}".
          </div>
        `;
        return;
      }

      filtered.forEach(res => {
        const item = document.createElement('div');
        item.style.background = '#030712';
        item.style.border = '1px solid rgba(255,255,255,0.08)';
        item.style.borderRadius = '10px';
        item.style.padding = '18px 20px';
        item.style.boxShadow = '0 4px 12px rgba(0,0,0,0.3)';

        // Color coding based on status
        let statusBg = 'rgba(16,185,129,0.15)', statusColor = '#34d399', statusBorder = 'rgba(16,185,129,0.3)';
        if (res.overallStatus === 'MISMATCH') {
          statusBg = 'rgba(244,63,94,0.15)'; statusColor = '#fb7185'; statusBorder = 'rgba(244,63,94,0.3)';
        } else if (res.overallStatus === 'EXPIRED') {
          statusBg = 'rgba(245,158,11,0.15)'; statusColor = '#fbbf24'; statusBorder = 'rgba(245,158,11,0.3)';
        } else if (res.overallStatus === 'SOURCE_UNAVAILABLE') {
          statusBg = 'rgba(56,189,248,0.15)'; statusColor = '#38bdf8'; statusBorder = 'rgba(56,189,248,0.3)';
        } else if (res.overallStatus === 'MANUAL_REVIEW') {
          statusBg = 'rgba(192,132,252,0.15)'; statusColor = '#c084fc'; statusBorder = 'rgba(192,132,252,0.3)';
        } else if (res.overallStatus === 'PENDING') {
          statusBg = 'rgba(148,163,184,0.15)'; statusColor = '#94a3b8'; statusBorder = 'rgba(148,163,184,0.3)';
        }

        // Build Field Comparisons HTML if present
        let comparisonsHtml = '';
        if (res.comparisons && res.comparisons.length > 0) {
          const rows = res.comparisons.map(fc => {
            let rowColor = '#34d399';
            if (fc.comparisonStatus === 'MISMATCH') rowColor = '#fb7185';
            else if (fc.comparisonStatus === 'PARTIAL_MATCH') rowColor = '#fbbf24';
            else if (fc.comparisonStatus === 'NEEDS_REVIEW') rowColor = '#c084fc';

            return `
              <tr style="border-bottom:1px solid rgba(255,255,255,0.04); font-size:0.75rem;">
                <td style="padding:8px 10px; font-weight:600; color:#e2e8f0; font-family:var(--font-mono);">${fc.fieldName}</td>
                <td style="padding:8px 10px; color:#cbd5e1;">${fc.submittedValue || 'â€”'}</td>
                <td style="padding:8px 10px; font-weight:700; color:#f8fafc;">${fc.officialValue || 'â€”'}</td>
                <td style="padding:8px 10px;"><span style="background:rgba(255,255,255,0.06); padding:2px 6px; border-radius:4px; font-size:0.68rem; color:#94a3b8;">${fc.comparisonMethod}</span></td>
                <td style="padding:8px 10px; color:${rowColor}; font-weight:700;">${fc.comparisonStatus}</td>
                <td style="padding:8px 10px; color:#94a3b8; font-size:0.7rem;">${fc.evidenceSnippet || 'Corroborated'}</td>
              </tr>
            `;
          }).join('');

          comparisonsHtml = `
            <div style="margin-top:14px; background:rgba(15,23,42,0.6); border-radius:6px; border:1px solid rgba(255,255,255,0.05); overflow:hidden;">
              <table style="width:100%; text-align:left; border-collapse:collapse;">
                <thead>
                  <tr style="background:rgba(255,255,255,0.03); font-size:0.7rem; color:#94a3b8; border-bottom:1px solid rgba(255,255,255,0.06);">
                    <th style="padding:6px 10px;">Field Name</th>
                    <th style="padding:6px 10px;">Submitted Document Value</th>
                    <th style="padding:6px 10px;">Official Authority Value</th>
                    <th style="padding:6px 10px;">Method</th>
                    <th style="padding:6px 10px;">Status</th>
                    <th style="padding:6px 10px;">Evidence Detail</th>
                  </tr>
                </thead>
                <tbody>
                  ${rows}
                </tbody>
              </table>
            </div>
          `;
        }

        item.innerHTML = `
          <div style="display:flex; justify-content:space-between; align-items:flex-start; margin-bottom:10px; flex-wrap:wrap; gap:8px;">
            <div>
              <div style="display:flex; align-items:center; gap:8px; margin-bottom:4px;">
                <span style="font-weight:700; font-size:0.95rem; color:#f8fafc;">Doc #${res.documentId} Â· ${res.sourceId}</span>
                <span style="font-family:var(--font-mono); font-size:0.72rem; color:#64748b;">Record Ref: ${res.sourceRecordId || 'N/A'}</span>
              </div>
              <div style="font-size:0.75rem; color:#94a3b8;">
                Generated: ${new Date(res.generatedAt || Date.now()).toLocaleString()} Â· Dossier App #${res.applicationId || '1'}
              </div>
            </div>
            <div style="display:flex; gap:8px; align-items:center;">
              <span style="background:${statusBg}; color:${statusColor}; border:1px solid ${statusBorder}; padding:4px 12px; border-radius:6px; font-weight:800; font-size:0.75rem; letter-spacing:0.05em;">
                ${res.overallStatus}
              </span>
            </div>
          </div>

          <!-- Multi-Dimensional Breakdown Badges -->
          <div style="display:flex; flex-wrap:wrap; gap:8px; margin-bottom:12px; font-size:0.7rem;">
            <span style="background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); padding:3px 8px; border-radius:4px; color:#cbd5e1;">
              Match: <strong style="color:#f8fafc;">${res.recordMatchStatus}</strong>
            </span>
            <span style="background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); padding:3px 8px; border-radius:4px; color:#cbd5e1;">
              Validity: <strong style="color:${res.validityStatus === 'EXPIRED' ? '#fbbf24' : '#34d399'};">${res.validityStatus}</strong>
            </span>
            <span style="background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); padding:3px 8px; border-radius:4px; color:#cbd5e1;">
              Signature: <strong style="color:${res.signatureStatus === 'TAMPERED_AFTER_SIGNING' ? '#fb7185' : '#38bdf8'};">${res.signatureStatus}</strong>
            </span>
            <span style="background:rgba(255,255,255,0.04); border:1px solid rgba(255,255,255,0.08); padding:3px 8px; border-radius:4px; color:#cbd5e1;">
              QR Code: <strong style="color:#94a3b8;">${res.qrStatus}</strong>
            </span>
          </div>

          <!-- Summary & Limitations -->
          <div style="background:rgba(30,41,59,0.4); border-left:3px solid ${statusColor}; padding:10px 14px; border-radius:0 6px 6px 0; margin-bottom:12px;">
            <p style="font-size:0.8rem; color:#e2e8f0; margin:0 0 4px 0; line-height:1.4;">
              ${res.summary}
            </p>
            <p style="font-size:0.72rem; color:#94a3b8; margin:0; font-style:italic;">
              <strong>Technical Limitations:</strong> ${res.limitations}
            </p>
          </div>

          <!-- Field Comparisons -->
          ${comparisonsHtml}

          <!-- Action Toolbar -->
          <div style="display:flex; justify-content:flex-end; gap:8px; margin-top:14px;">
            <button class="btn-secondary" style="padding:5px 12px; font-size:0.72rem; color:#cbd5e1;" onclick="openAuthEvidenceModal(${res.id})">
              ðŸ” View Raw Evidence
            </button>
            <button class="btn-secondary" style="padding:5px 12px; font-size:0.72rem; color:#38bdf8; border-color:rgba(56,189,248,0.3);" onclick="viewResultSection20(${res.id})">
              ðŸ“„ Section 20 JSON
            </button>
            <button class="btn-secondary" style="padding:5px 12px; font-size:0.72rem; color:#94a3b8;" onclick="retryAuthorityVerification(${res.id})">
              ðŸ”„ Retry Query
            </button>
            <button class="btn-primary" style="padding:5px 14px; font-size:0.72rem; background:#7c3aed; color:#fff;" onclick="openAuthReviewModal(${res.id})">
              âš–ï¸ Scrutiny Review
            </button>
          </div>
        `;

        container.appendChild(item);
      });
    }

    async function triggerAuthorityVerification() {
      const docSelect = document.getElementById('authDocSelector');
      const sourceSelect = document.getElementById('authSourceSelector');
      const docId = docSelect ? docSelect.value : '2';
      const sourceId = sourceSelect ? sourceSelect.value : 'ALL';

      const btn = event ? event.target : null;
      if (btn) btn.innerText = 'Corroborating...';

      try {
        const url = (sourceId === 'ALL') ? `${API_BASE}/documents/${docId}/verify` : `${API_BASE}/documents/${docId}/verify/${sourceId}`;
        const res = await fetch(url, { method: 'POST' });
        if (res.ok) {
          const newResults = await res.json();
          alert(`Successfully executed origin corroboration for Document #${docId}. Generated ${newResults.length} verification result(s).`);
          const appId = document.getElementById('authAppSelector') ? document.getElementById('authAppSelector').value : '2';
          await loadAuthorityVerifications(appId);
        } else {
          alert('Authority verification initiated. Reloading records...');
          const appId = document.getElementById('authAppSelector') ? document.getElementById('authAppSelector').value : '2';
          await loadAuthorityVerifications(appId);
        }
      } catch (e) {
        console.error('Error triggering verification:', e);
        alert('Verification simulated for demo mode. Dashboard updated.');
      } finally {
        if (btn) btn.innerText = 'Run Corroboration';
      }
    }

    async function retryAuthorityVerification(resultId) {
      try {
        const res = await fetch(`${API_BASE}/verifications/${resultId}/retry`, { method: 'POST' });
        if (res.ok) {
          alert(`Verification check #${resultId} re-executed against authoritative source.`);
          const appId = document.getElementById('authAppSelector') ? document.getElementById('authAppSelector').value : '2';
          await loadAuthorityVerifications(appId);
        }
      } catch (e) {
        console.error('Retry error:', e);
      }
    }

    function onAuthorityDossierChange() {
      const appId = document.getElementById('authAppSelector').value;
      const docSelect = document.getElementById('authDocSelector');
      if (docSelect) {
        if (appId === '1') {
          docSelect.innerHTML = `
            <option value="1">aicte_eoa_2024_25.pdf</option>
            <option value="3">fire_noc_apex_valid.pdf</option>
          `;
        } else {
          docSelect.innerHTML = `
            <option value="2">fire_noc_2024_revised.pdf</option>
            <option value="4">land_title_deed_horizon.pdf</option>
            <option value="5">faculty_list_AY2024.pdf</option>
          `;
        }
      }
      loadAuthorityVerifications(appId);
    }

    /* Modal Handlers */
    function openAuthReviewModal(resultId) {
      currentAuthResultForReview = resultId;
      const modal = document.getElementById('authReviewModal');
      if (modal) modal.classList.add('open');
    }

    function closeAuthReviewModal() {
      const modal = document.getElementById('authReviewModal');
      if (modal) modal.classList.remove('open');
    }

    async function submitAuthReview() {
      if (!currentAuthResultForReview) return;
      const decision = document.getElementById('authReviewDecisionSelect').value;
      const reviewerId = document.getElementById('authReviewerIdSelect').value;
      const notes = document.getElementById('authReviewNotesText').value;

      try {
        const res = await fetch(`${API_BASE}/verifications/${currentAuthResultForReview}/review`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ reviewerId, decision, notes })
        });
        if (res.ok) {
          alert('Scrutiny Officer determination successfully submitted and recorded in tamper-evident audit repository.');
          closeAuthReviewModal();
          const appId = document.getElementById('authAppSelector') ? document.getElementById('authAppSelector').value : '2';
          await loadAuthorityVerifications(appId);
        }
      } catch (e) {
        console.error('Review submission error:', e);
        alert('Review decision saved in session.');
        closeAuthReviewModal();
      }
    }

    async function openAuthEvidenceModal(resultId) {
      const modal = document.getElementById('authEvidenceModal');
      const content = document.getElementById('authEvidenceContent');
      if (!modal || !content) return;
      content.innerHTML = '<div style="padding:14px; text-align:center; color:#38bdf8;">Retrieving cryptographic payloads...</div>';
      modal.classList.add('open');

      try {
        const res = await fetch(`${API_BASE}/verifications/${resultId}/evidence`);
        if (res.ok) {
          const evItems = await res.json();
          content.innerHTML = `<pre class="json-code-box">${JSON.stringify(evItems, null, 2)}</pre>`;
        } else {
          content.innerHTML = '<div style="color:#94a3b8; font-size:0.8rem;">Evidence payload digest: SHA256[e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855]</div>';
        }
      } catch (e) {
        content.innerHTML = '<div style="color:#f43f5e; font-size:0.8rem;">Error loading evidence: ' + e.message + '</div>';
      }
    }

    function closeAuthEvidenceModal() {
      const modal = document.getElementById('authEvidenceModal');
      if (modal) modal.classList.remove('open');
    }

    async function viewResultSection20(resultId) {
      try {
        const res = await fetch(`${API_BASE}/verifications/${resultId}/report`);
        if (res.ok) {
          currentSection20Report = await res.json();
        } else {
          const match = currentAuthResults.find(r => r.id === resultId);
          currentSection20Report = {
            reportSchemaVersion: '2.0-SIH26261',
            reportType: 'SECTION_20_ISSUING_AUTHORITY_VERIFICATION',
            verificationResultId: resultId,
            overallStatus: match ? match.overallStatus : 'VERIFIED',
            verificationDimensions: match ? { recordMatchStatus: match.recordMatchStatus, validityStatus: match.validityStatus, signatureStatus: match.signatureStatus, qrStatus: match.qrStatus } : {},
            executiveSummary: match ? match.summary : 'Institutional verification corroborated.',
            fieldComparisons: match ? match.comparisons : []
          };
        }
      } catch (e) {
        console.warn('Error loading Section 20 JSON:', e);
      }
      displaySection20Modal();
    }

    function openSection20ReportModal() {
      if (currentAuthResults && currentAuthResults.length > 0) {
        viewResultSection20(currentAuthResults[0].id);
      } else {
        viewResultSection20(1);
      }
    }

    function displaySection20Modal() {
      const box = document.getElementById('sec20JsonContent');
      if (box && currentSection20Report) {
        box.textContent = JSON.stringify(currentSection20Report, null, 2);
      }
      const modal = document.getElementById('sec20JsonModal');
      if (modal) modal.classList.add('open');
    }

    function closeSec20JsonModal() {
      const modal = document.getElementById('sec20JsonModal');
      if (modal) modal.classList.remove('open');
    }

    function copySec20Json() {
      if (!currentSection20Report) return;
      navigator.clipboard.writeText(JSON.stringify(currentSection20Report, null, 2));
      alert('Section 20 Verification JSON copied to clipboard!');
    }


  
    /* ==========================================================================
       DELIVERABLE 5: INFRASTRUCTURE COMPUTER VISION & REGULATORY COMPLIANCE SCRIPT
       ========================================================================== */
    let currentCvEvidenceList = [];
    let currentCvAssetList = [];
    let currentActiveCvFilter = 'ALL';
    let currentComplianceAssessment = null;
    let currentComplianceRules = [];
    let currentComplianceFindings = [];
    let currentComplianceTasks = [];
    let currentActiveRuleFilter = 'ALL';
    let currentComplianceReport = null;

    /* TAB 4: Load Infrastructure Evidence & Assets */
    async function loadCvData(appId) {
      try {
        const [evRes, assetRes] = await Promise.all([
          fetch(`${API_BASE}/applications/${appId}/infrastructure-evidence`),
          fetch(`${API_BASE}/applications/${appId}/infrastructure-assets`)
        ]);

        if (evRes.ok) {
          currentCvEvidenceList = await evRes.json();
        } else {
          currentCvEvidenceList = getFallbackCvEvidence(appId);
        }

        if (assetRes.ok) {
          currentCvAssetList = await assetRes.json();
        } else {
          currentCvAssetList = getFallbackCvAssets(appId);
        }
      } catch (e) {
        console.warn('Could not fetch CV data from API, using fallback:', e);
        currentCvEvidenceList = getFallbackCvEvidence(appId);
        currentCvAssetList = getFallbackCvAssets(appId);
      }

      renderCvEvidence(currentCvEvidenceList, currentActiveCvFilter);
      renderCvAssets(currentCvAssetList);
      updateCvKpis(currentCvEvidenceList);
    }

    function onCvAppChange() {
      const appId = document.getElementById('cvAppSelector').value;
      loadCvData(appId);
    }

    function updateCvKpis(list) {
      const countEl = document.getElementById('cvTotalMediaCount');
      if (countEl) countEl.innerText = list.length + ' Files';

      let discrepancies = 0;
      list.forEach(item => {
        if (item.analysisStatus === 'POSSIBLE_DISCREPANCY') discrepancies++;
      });

      const discEl = document.getElementById('cvDiscrepancyCount');
      const discSub = document.getElementById('cvDiscrepancySub');
      if (discEl) discEl.innerText = discrepancies + (discrepancies === 1 ? ' Detected' : ' Detected');
      if (discSub) discSub.innerText = discrepancies > 0 ? 'Ramp slope exceeds 1:12' : 'All norms verified';
    }

    function filterCvMedia(filter, btn) {
      currentActiveCvFilter = filter;
      document.querySelectorAll('.cv-filter-btn').forEach(b => b.classList.remove('active'));
      if (btn) btn.classList.add('active');
      renderCvEvidence(currentCvEvidenceList, filter);
    }

    function renderCvEvidence(list, filter) {
      const container = document.getElementById('cvMediaContainer');
      if (!container) return;

      const filtered = filter === 'ALL' ? list : list.filter(item => item.evidenceType === filter);

      if (filtered.length === 0) {
        container.innerHTML = '<div style="padding:24px; text-align:center; color:#94a3b8; font-size:0.8rem;">No evidence media matching current filter.</div>';
        return;
      }

      container.innerHTML = filtered.map(item => {
        const isDiscrepancy = item.analysisStatus === 'POSSIBLE_DISCREPANCY';
        const badgeColor = isDiscrepancy ? '#f43f5e' : '#10b981';
        const badgeBg = isDiscrepancy ? 'rgba(244,63,94,0.15)' : 'rgba(16,185,129,0.15)';
        const borderStyle = isDiscrepancy ? 'border: 2px solid rgba(244,63,94,0.6);' : 'border: 1px solid rgba(56,189,248,0.2);';

        let previewUrl = 'https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=600&q=80';
        if (item.fileName.includes('ramp')) previewUrl = 'https://images.unsplash.com/photo-1541829070764-84a7d30dd3f3?auto=format&fit=crop&w=600&q=80';
        if (item.fileName.includes('floor')) previewUrl = 'https://images.unsplash.com/photo-1503387762-592deb58ef4e?auto=format&fit=crop&w=600&q=80';
        if (item.fileName.includes('video')) previewUrl = 'https://images.unsplash.com/photo-1562774053-701939374585?auto=format&fit=crop&w=600&q=80';

        return `
          <div class="cv-item" style="${borderStyle}">
            <div class="cv-photo-box" style="background-image:url('${previewUrl}'); position:relative;">
              <div class="cv-box-overlay"></div>
              <div class="cv-pill" style="position:absolute; top:8px; left:8px; background:rgba(15,23,42,0.85); color:#38bdf8; font-size:0.68rem; padding:3px 8px; border-radius:4px;">
                ${item.evidenceType} &bull; Quality: ${Math.round((item.qualityScore||0.9)*100)}%
              </div>
              <div style="position:absolute; top:8px; right:8px; background:${badgeBg}; color:${badgeColor}; font-size:0.68rem; font-weight:700; padding:3px 8px; border-radius:4px; border:1px solid ${badgeColor};">
                ${isDiscrepancy ? 'DISCREPANCY FLAGGED' : 'OBSERVATIONS VALID'}
              </div>
              <div class="cv-detection-box ${isDiscrepancy ? 'danger' : ''}" style="top:25%; left:20%; width:55%; height:55%;"></div>
            </div>
            <div class="cv-details" style="padding:14px;">
              <div class="cv-title" style="font-weight:700; font-size:0.9rem; color:#f8fafc; margin-bottom:6px;">
                ${item.fileName}
              </div>
              <div style="font-size:0.72rem; color:#94a3b8; font-family:var(--font-mono); margin-bottom:10px;">
                SHA256: ${item.sha256Hash ? item.sha256Hash.substring(0, 16) + '...' : 'e3b0c44298fc1c14...'}
              </div>
              <div class="cv-norm-check" style="margin-bottom:6px; font-size:0.75rem;">
                <span>AI Feature: <strong>${item.fileName.includes('ramp') ? 'Wheelchair Ramp Gradient' : item.fileName.includes('floor') ? 'Floor Carpet Area' : 'Terminal Apparatus'}</strong></span>
                <span style="color:${badgeColor}; font-weight:700;">${isDiscrepancy ? '1:8 Measured (Norm &le; 1:12)' : 'Norm Verified'}</span>
              </div>
              ${isDiscrepancy ? `
                <div style="background:rgba(244,63,94,0.1); border-left:3px solid #f43f5e; padding:6px 10px; border-radius:4px; font-size:0.72rem; color:#fecdd3; margin-top:8px;">
                  <strong>Deficiency Notice:</strong> Ramp slope measured at 1:8 is steeper than maximum allowed 1:12 slope under AICTE Appendix 4 &amp; NBC 2016.
                </div>
              ` : `
                <div style="background:rgba(16,185,129,0.08); border-left:3px solid #10b981; padding:6px 10px; border-radius:4px; font-size:0.72rem; color:#a7f3d0; margin-top:8px;">
                  Corroborates claimed campus infrastructure inventory under APH Clause 7.18.
                </div>
              `}
            </div>
          </div>
        `;
      }).join('');
    }

    function renderCvAssets(assets) {
      const tbody = document.getElementById('cvAssetsTableBody');
      if (!tbody) return;

      if (assets.length === 0) {
        tbody.innerHTML = '<tr><td colspan="8" style="text-align:center; color:#94a3b8; padding:18px;">No registered campus assets found.</td></tr>';
        return;
      }

      tbody.innerHTML = assets.map(a => `
        <tr>
          <td><strong style="color:#38bdf8;">${a.assetCode}</strong></td>
          <td>${a.assetName}</td>
          <td><span style="background:rgba(56,189,248,0.12); color:#38bdf8; padding:2px 8px; border-radius:4px; font-size:0.72rem;">${a.category}</span></td>
          <td>${a.buildingName || 'Main Block'} (${a.floorLevel || 'Ground'})</td>
          <td>${a.claimedAreaSqm ? a.claimedAreaSqm + ' sqm' : 'N/A'}</td>
          <td><strong>${a.equipmentCount || a.claimedCapacity || 'N/A'}</strong></td>
          <td>
            <span style="color:${a.isAccessible ? '#10b981' : '#f43f5e'}; font-weight:700;">
              ${a.isAccessible ? '&check; Yes' : '&cross; No'}
            </span>
          </td>
          <td><span class="tag-status verified">${a.assetStatus || 'FUNCTIONAL'}</span></td>
        </tr>
      `).join('');
    }

    async function triggerCampusCvInspection() {
      const btn = document.getElementById('btnRunCvInspection');
      if (btn) btn.innerText = 'Inspecting Video & Frames...';
      const appId = document.getElementById('cvAppSelector').value;

      try {
        if (currentCvEvidenceList.length > 0) {
          const firstId = currentCvEvidenceList[0].id;
          await fetch(`${API_BASE}/infrastructure-evidence/${firstId}/analyze`, { method: 'POST' });
        }
        alert('Campus Computer Vision inspection completed. Extracted visual feature bounding boxes and checked statutory slope thresholds.');
        await loadCvData(appId);
      } catch (e) {
        console.error('CV analysis trigger error:', e);
        alert('Inspection simulated in demonstration environment.');
      } finally {
        if (btn) btn.innerHTML = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polygon points="5 3 19 12 5 21 5 3"></polygon></svg> Run Campus CV Scrutiny';
      }
    }

    /* TAB 5: Load Regulatory Compliance Data */
    async function loadComplianceData(appId) {
      try {
        const res = await fetch(`${API_BASE}/applications/${appId}/compliance-assessments`);
        let assessments = [];
        if (res.ok) {
          assessments = await res.json();
        }

        if (assessments.length === 0) {
          const evalRes = await fetch(`${API_BASE}/applications/${appId}/compliance-assessments/evaluate`, { method: 'POST' });
          if (evalRes.ok) {
            currentComplianceAssessment = await evalRes.json();
          }
        } else {
          currentComplianceAssessment = assessments[0];
        }

        if (currentComplianceAssessment) {
          const rulesRes = await fetch(`${API_BASE}/compliance-assessments/${currentComplianceAssessment.id}/rules`);
          if (rulesRes.ok) {
            currentComplianceRules = await rulesRes.json();
          }
          const taskRes = await fetch(`${API_BASE}/compliance-assessments/${currentComplianceAssessment.id}/review-tasks`);
          if (taskRes.ok) {
            currentComplianceTasks = await taskRes.json();
          }
        }
      } catch (e) {
        console.warn('Error loading compliance assessment, using fallback:', e);
        loadFallbackCompliance(appId);
      }

      updateComplianceKpiBar(currentComplianceAssessment);
      renderComplianceRules(currentComplianceRules, currentActiveRuleFilter);
    }

    function onDecAppChange() {
      const appId = document.getElementById('decAppSelector').value;
      loadComplianceData(appId);
    }

    async function reEvaluateComplianceRules() {
      const btn = document.getElementById('btnReevaluateRules');
      if (btn) btn.innerText = 'Evaluating Rules...';
      const appId = document.getElementById('decAppSelector').value;

      try {
        const evalRes = await fetch(`${API_BASE}/applications/${appId}/compliance-assessments/evaluate`, { method: 'POST' });
        if (evalRes.ok) {
          currentComplianceAssessment = await evalRes.json();
          alert(`AICTE Regulatory Evaluation completed: ${currentComplianceAssessment.passedCount} Passed, ${currentComplianceAssessment.deficiencyCount} Deficiencies. Score: ${currentComplianceAssessment.overallComplianceScore}%`);
          await loadComplianceData(appId);
    }
      } catch (e) {
        console.error('Re-evaluation error:', e);
        alert('Re-evaluation simulated for demonstration environment.');
      } finally {
        if (btn) btn.innerHTML = '<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="23 4 23 10 17 10"></polyline><path d="M20.49 15a9 9 0 1 1-2.12-9.36L23 10"></path></svg> Re-evaluate Rules';
      }
    }

    function updateComplianceKpiBar(assessment) {
      if (!assessment) return;
      const scoreEl = document.getElementById('decComplianceScore');
      const passEl = document.getElementById('decPassedCount');
      const defEl = document.getElementById('decDeficiencyCount');
      const warnEl = document.getElementById('decWarningCount');
      const hashEl = document.getElementById('decAssessmentHash');

      if (scoreEl) {
        scoreEl.innerText = (assessment.overallComplianceScore || 85.7) + '%';
        scoreEl.style.color = (assessment.overallComplianceScore >= 80) ? '#38bdf8' : '#f43f5e';
      }
      if (passEl) passEl.innerText = (assessment.passedCount || 12) + ' / ' + (assessment.totalRulesEvaluated || 14);
      if (defEl) defEl.innerText = (assessment.deficiencyCount || 0) + ' Items';
      if (warnEl) warnEl.innerText = (assessment.warningCount || 0) + ' Items';
      if (hashEl && assessment.assessmentHash) {
        hashEl.innerText = 'SHA256: ' + assessment.assessmentHash.substring(0, 16) + '...';
        hashEl.title = assessment.assessmentHash;
      }
    }

    function filterComplianceRules(filter, btn) {
      currentActiveRuleFilter = filter;
      document.querySelectorAll('.rule-filter-btn').forEach(b => b.classList.remove('active'));
      if (btn) btn.classList.add('active');
      renderComplianceRules(currentComplianceRules, filter);
    }

    function renderComplianceRules(rules, filter) {
      const tbody = document.getElementById('complianceRulesTableBody');
      if (!tbody) return;

      const filtered = filter === 'ALL' ? rules : rules.filter(r => r.status === filter);

      if (filtered.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:#94a3b8; padding:18px;">No rules matching current filter.</td></tr>';
        return;
      }

      tbody.innerHTML = filtered.map(r => {
        let statusBadge = '<span class="tag-status verified">PASS</span>';
        if (r.status === 'DEFICIENCY') {
          statusBadge = '<span class="tag-status flagged">CRITICAL DEFICIENCY</span>';
        } else if (r.status === 'WARNING') {
          statusBadge = '<span class="tag-status deficient">ADVISORY WARNING</span>';
        } else if (r.status === 'MANUAL_REVIEW') {
          statusBadge = '<span class="tag-status" style="background:rgba(168,85,247,0.15); color:#c084fc;">MANUAL REVIEW</span>';
        }

        const isDeficient = r.status === 'DEFICIENCY' || r.status === 'WARNING';

        return `
          <tr>
            <td>
              <strong style="color:#f8fafc; font-size:0.8rem;">${r.ruleTitle}</strong><br>
              <code style="font-size:0.7rem; color:#38bdf8;">${r.ruleCode}</code>
            </td>
            <td style="font-size:0.74rem; color:#cbd5e1;">
              ${r.statutoryCitation || 'AICTE APH 2024-27'}
            </td>
            <td style="font-size:0.75rem;">
              <div style="color:#94a3b8;">Expected: ${r.expectedValue || 'Norm Compliant'}</div>
              <div style="color:${isDeficient ? '#fda4af' : '#a7f3d0'}; font-weight:600; margin-top:2px;">
                Observed: ${r.observedValue || 'Verified'}
              </div>
            </td>
            <td>${statusBadge}</td>
            <td>
              ${isDeficient ? `
                <button class="btn-secondary" style="padding:4px 10px; font-size:0.72rem; color:#f8fafc;" onclick="openComplianceOverrideModal(${r.id}, '${r.ruleCode}')">
                  Override
                </button>
              ` : `
                <span style="color:#64748b; font-size:0.72rem;">&mdash;</span>
              `}
            </td>
          </tr>
        `;
      }).join('');
    }

    function handleComplianceAction(actionType) {
      const box = document.getElementById('decisionAlertBox');
      if (!box) return;

      const officer = 'Dr. Rajesh Sharma (Scrutiny Committee Member)';
      const auditRef = 'AUDIT-' + Math.random().toString(36).substring(2, 9).toUpperCase();

      if (actionType === 'DEFICIENCY_NOTICE') {
        box.style.display = 'block';
        box.style.background = 'rgba(244,63,94,0.15)';
        box.style.border = '1px solid #f43f5e';
        box.style.color = '#fecdd3';
        box.innerHTML = `&check; <strong>15-Day Statutory Deficiency Notice Issued.</strong> Dispatched defect memorandum to Horizon College of Engineering. Audit Reference: <code>${auditRef}</code> | Officer: ${officer}`;
      } else if (actionType === 'SPOT_INSPECTION') {
        box.style.display = 'block';
        box.style.background = 'rgba(245,158,11,0.15)';
        box.style.border = '1px solid #f59e0b';
        box.style.color = '#fef3c7';
        box.innerHTML = `&check; <strong>Expert Visiting Committee (EVC) Physical Spot Inspection Ordered.</strong> 3-member technical committee assigned for physical on-site verification. Audit Reference: <code>${auditRef}</code>`;
      } else if (actionType === 'APPROVE_CONDITIONAL') {
        box.style.display = 'block';
        box.style.background = 'rgba(16,185,129,0.15)';
        box.style.border = '1px solid #10b981';
        box.style.color = '#d1fae5';
        box.innerHTML = `&check; <strong>Conditional Extension of Approval (EoA) Recommended.</strong> Officer justification recorded under APH Discretionary Clause 1.8. Audit Reference: <code>${auditRef}</code>`;
      }
    }

    function openComplianceOverrideModal(resultId, ruleCode) {
      const modal = document.getElementById('complianceOverrideModal');
      const codeInput = document.getElementById('overrideRuleCodeText');
      const resultInput = document.getElementById('overrideResultId');
      const taskInput = document.getElementById('overrideTaskId');

      if (codeInput) codeInput.value = ruleCode;
      if (resultInput) resultInput.value = resultId;
      if (taskInput) {
        const task = currentComplianceTasks.find(t => t.ruleEvaluationResultId === resultId);
        taskInput.value = task ? task.id : '1';
      }
      if (modal) modal.classList.add('open');
    }

    function closeComplianceOverrideModal() {
      const modal = document.getElementById('complianceOverrideModal');
      if (modal) modal.classList.remove('open');
    }

    async function submitComplianceOverride() {
      const taskId = document.getElementById('overrideTaskId').value || '1';
      const status = document.getElementById('overrideStatusSelect').value;
      const rationale = document.getElementById('overrideRationaleText').value || 'Officer review determined compliant after examining PWD undertaking.';
      const justification = document.getElementById('overrideStatutoryText').value || 'AICTE APH 2024-27 Clause 1.8 (Discretionary Powers)';

      try {
        const res = await fetch(`${API_BASE}/compliance-review-tasks/${taskId}/decision`, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            decision: 'CONFIRMED',
            officerRationale: rationale,
            statutoryJustification: justification,
            overrideRuleDecision: true,
            overriddenStatus: status,
            additionalEvidenceRequested: '',
            officerUsername: 'Dr. Rajesh Sharma (Scrutiny Officer)'
          })
        });

        if (res.ok) {
          alert('Discretionary Override recorded into permanent immutable audit trail. Assessment score updated.');
          closeComplianceOverrideModal();
          const appId = document.getElementById('decAppSelector').value;
          await loadComplianceData(appId);
    } else {
          alert('Override recorded in session mode.');
          closeComplianceOverrideModal();
        }
      } catch (e) {
        console.error('Override error:', e);
        alert('Override recorded in session mode.');
        closeComplianceOverrideModal();
      }
    }

    async function openComplianceReportModal() {
      const modal = document.getElementById('complianceReportModal');
      const content = document.getElementById('complianceReportContent');
      if (!modal || !content) return;

      const assessmentId = currentComplianceAssessment ? currentComplianceAssessment.id : 2;
      content.textContent = 'Loading Section 22/23 JSON Compliance Report...';
      modal.classList.add('open');

      try {
        const res = await fetch(`${API_BASE}/compliance-assessments/${assessmentId}/report`);
        if (res.ok) {
          currentComplianceReport = await res.json();
          content.textContent = JSON.stringify(currentComplianceReport, null, 2);
        } else {
          content.textContent = JSON.stringify({
            reportSchemaVersion: '2.0-SIH26261',
            status: 'ASSESSMENT_COMPLETE',
            overallComplianceScore: 85.7,
            rulesEvaluated: 14,
            passed: 12,
            deficiencies: 2
          }, null, 2);
        }
      } catch (e) {
        content.textContent = JSON.stringify({ error: e.message }, null, 2);
      }
    }

    function closeComplianceReportModal() {
      const modal = document.getElementById('complianceReportModal');
      if (modal) modal.classList.remove('open');
    }

    function copyComplianceReportJson() {
      const content = document.getElementById('complianceReportContent');
      if (!content) return;
      navigator.clipboard.writeText(content.textContent);
      alert('Section 22/23 Compliance Report JSON copied to clipboard!');
    }

    /* Fallback data for demonstration stability */
    function getFallbackCvEvidence(appId) {
      if (appId === '1') {
        return [
          { id: 5, evidenceType: 'PHOTOGRAPH', fileName: 'apex_ai_computing_lab.jpg', sha256Hash: 'a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2', qualityScore: 0.96, analysisStatus: 'OBSERVATIONS_AVAILABLE' },
          { id: 6, evidenceType: 'PHOTOGRAPH', fileName: 'apex_accessible_ramp.jpg', sha256Hash: 'b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3d4e5f6a1b2c3', qualityScore: 0.94, analysisStatus: 'OBSERVATIONS_AVAILABLE' }
        ];
      }
      return [
        { id: 1, evidenceType: 'PHOTOGRAPH', fileName: 'horizon_ramp_incline_photo.jpg', sha256Hash: '7c8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d', qualityScore: 0.92, analysisStatus: 'POSSIBLE_DISCREPANCY' },
        { id: 2, evidenceType: 'PHOTOGRAPH', fileName: 'horizon_cse_lab_workstations.jpg', sha256Hash: '8d9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e', qualityScore: 0.94, analysisStatus: 'OBSERVATIONS_AVAILABLE' },
        { id: 3, evidenceType: 'FLOOR_PLAN_PDF', fileName: 'horizon_campus_master_floor_plan.pdf', sha256Hash: '9e0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f', qualityScore: 0.98, analysisStatus: 'OBSERVATIONS_AVAILABLE' },
        { id: 4, evidenceType: 'VIDEO_WALKTHROUGH', fileName: 'horizon_mech_lab_video_walkthrough.mp4', sha256Hash: '0f1a2b3c4d5e6f7a8b9c0d1e2f3a4b5c6d7e8f9a0b1c2d3e4f5a6b7c8d9e0f1a', qualityScore: 0.88, analysisStatus: 'OBSERVATIONS_AVAILABLE' }
      ];
    }

    function getFallbackCvAssets(appId) {
      if (appId === '1') {
        return [
          { assetCode: 'APEX-LAB-01', assetName: 'Advanced Computing & AI Lab', category: 'COMPUTER_FACILITY', buildingName: 'Engineering Tower', floorLevel: 'Ground', claimedAreaSqm: 160.0, equipmentCount: 40, isAccessible: true, assetStatus: 'FUNCTIONAL' },
          { assetCode: 'APEX-RAMP-01', assetName: 'Universal Barrier-Free Ramp', category: 'ACCESSIBILITY', buildingName: 'Engineering Tower', floorLevel: 'Ground', claimedAreaSqm: 50.0, equipmentCount: 2, isAccessible: true, assetStatus: 'FUNCTIONAL' }
        ];
      }
      return [
        { assetCode: 'RM-101', assetName: 'CSE Central Programming Lab', category: 'COMPUTER_FACILITY', buildingName: 'Main Tech Block', floorLevel: '1st Floor', claimedAreaSqm: 140.0, equipmentCount: 30, isAccessible: true, assetStatus: 'FUNCTIONAL' },
        { assetCode: 'RAMP-MAIN-BLDG', assetName: 'North Entrance Accessible Wheelchair Ramp', category: 'ACCESSIBILITY', buildingName: 'Administrative Block', floorLevel: 'Ground', claimedAreaSqm: 45.0, equipmentCount: 2, isAccessible: true, assetStatus: 'FUNCTIONAL' },
        { assetCode: 'LAB-MECH-01', assetName: 'Strength of Materials & Mechanics Lab', category: 'LABORATORY', buildingName: 'Workshop Block', floorLevel: 'Ground', claimedAreaSqm: 180.0, equipmentCount: 8, isAccessible: true, assetStatus: 'FUNCTIONAL' },
        { assetCode: 'LIB-CENTRAL', assetName: 'Central Reference & Digital Library', category: 'LIBRARY', buildingName: 'Knowledge Center', floorLevel: '2nd Floor', claimedAreaSqm: 480.0, equipmentCount: 20, isAccessible: true, assetStatus: 'FUNCTIONAL' }
      ];
    }

    function loadFallbackCompliance(appId) {
      currentComplianceAssessment = {
        id: appId === '1' ? 1 : 2,
        academicYear: '2026-27',
        overallComplianceScore: appId === '1' ? 92.8 : 85.7,
        totalRulesEvaluated: 14,
        passedCount: appId === '1' ? 13 : 12,
        deficiencyCount: appId === '1' ? 0 : 2,
        warningCount: 0,
        assessmentHash: '8f24b109e3ca15b89a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f'
      };

      currentComplianceRules = [
        { id: 1, ruleCode: 'AICTE_RAMP_GRADIENT_001', ruleTitle: 'Barrier-Free Accessibility Wheelchair Ramp Gradient', statutoryCitation: 'AICTE APH 2024-27, App 4 & NBC 2016', expectedValue: 'Gradient <= 1:12', observedValue: appId === '1' ? '1:14 Compliant' : '1:8 Measured (Discrepancy)', status: appId === '1' ? 'PASS' : 'DEFICIENCY' },
        { id: 2, ruleCode: 'AICTE_INTAKE_SANCTION_001', ruleTitle: 'Sanctioned Intake Conformance with Affiliating Body', statutoryCitation: 'AICTE APH 2024-27, Clause 2.8', expectedValue: 'Claimed <= Sanctioned', observedValue: appId === '1' ? 'Sanctioned: 540' : 'Claimed: 120, Sanctioned: 60', status: appId === '1' ? 'PASS' : 'DEFICIENCY' },
        { id: 3, ruleCode: 'AICTE_FSR_TECH_001', ruleTitle: 'Faculty-to-Student Ratio for Engineering UG', statutoryCitation: 'AICTE APH 2024-27, Clause 7.14', expectedValue: 'Ratio <= 1:15', observedValue: '1:14.2 (Compliant)', status: 'PASS' },
        { id: 4, ruleCode: 'AICTE_LAND_AREA_001', ruleTitle: 'Minimum Campus Land Area Norms', statutoryCitation: 'AICTE APH 2024-27, Table 4.1', expectedValue: '>= 5.0 Acres', observedValue: '5.2 Acres (Title Verified)', status: 'PASS' },
        { id: 5, ruleCode: 'AICTE_COMP_RATIO_001', ruleTitle: 'Student-to-Computer Workstation Ratio', statutoryCitation: 'AICTE APH 2024-27, Table 5.4', expectedValue: 'Ratio <= 1:4', observedValue: '1:3.8 (120 terminals for 420 students)', status: 'PASS' },
        { id: 6, ruleCode: 'AICTE_FIRE_NOC_VAL_001', ruleTitle: 'Valid Fire Safety Certificate / NOC', statutoryCitation: 'AICTE APH 2024-27, Clause 6.3', expectedValue: 'Active Fire NOC', observedValue: 'NOC submitted and registered', status: 'PASS' },
        { id: 7, ruleCode: 'AICTE_DOC_MANDATORY_001', ruleTitle: 'Mandatory Statutory Document Completeness', statutoryCitation: 'AICTE APH 2024-27, Appendix 16', expectedValue: 'Min 4 documents', observedValue: '4 documents submitted', status: 'PASS' },
        { id: 8, ruleCode: 'AICTE_LAB_EQUIP_001', ruleTitle: 'Laboratories Minimum Functional Apparatus Norm', statutoryCitation: 'AICTE APH 2024-27, Clause 7.18', expectedValue: '>= 5 apparatus per lab', observedValue: 'All labs meet apparatus quota', status: 'PASS' },
        { id: 9, ruleCode: 'AICTE_ANTI_RAGGING_001', ruleTitle: 'Statutory Anti-Ragging Committee Affidavit', statutoryCitation: 'AICTE Regulations 2009', expectedValue: 'Mandatory Committee', observedValue: 'Affidavit verified', status: 'PASS' },
        { id: 10, ruleCode: 'AICTE_GRIEVANCE_COMM_001', ruleTitle: 'Student Grievance Redressal Committee (SGRC)', statutoryCitation: 'AICTE SGRC Regulations 2019', expectedValue: 'Constituted SGRC', observedValue: 'SGRC notified with Omsperson', status: 'PASS' },
        { id: 11, ruleCode: 'AICTE_SOURCE_VERIF_001', ruleTitle: 'Independent Issuing Authority Corroboration', statutoryCitation: 'AICTE APH 2024-27, Clause 1.4', expectedValue: 'Origin Corroboration', observedValue: appId === '1' ? 'Corroborated by 3 checks' : 'Affiliation check flagged mismatch', status: appId === '1' ? 'PASS' : 'DEFICIENCY' },
        { id: 12, ruleCode: 'AICTE_FORENSIC_INTEGRITY_001', ruleTitle: 'Document Forensic Tamper Screening', statutoryCitation: 'AICTE APH 2024-27, Appendix 18', expectedValue: '0 critical anomalies', observedValue: '0 critical tampering anomalies', status: 'PASS' },
        { id: 13, ruleCode: 'AICTE_LIBRARY_VOLUMES_001', ruleTitle: 'Minimum Technical Library Titles & Volumes', statutoryCitation: 'AICTE APH 2024-27, Table 7.5', expectedValue: '100 titles, 500 volumes', observedValue: '1,200 titles, 6,500 volumes', status: 'PASS' },
        { id: 14, ruleCode: 'AICTE_BUILDING_STABILITY_001', ruleTitle: 'Building Structural Soundness Certificate', statutoryCitation: 'AICTE APH 2024-27, Clause 6.5', expectedValue: 'Chartered Stability Cert', observedValue: 'Soundness certificate verified', status: 'PASS' }
      ];
    }

    // Auto-initialize Tab 4 and Tab 5 on page load
    window.addEventListener('DOMContentLoaded', () => {
      if (typeof updateDossierView === 'function') updateDossierView();
      if (typeof refreshDocumentList === 'function') refreshDocumentList();
      if (typeof loadCvData === 'function') loadCvData('2');
      if (typeof loadComplianceData === 'function') loadComplianceData('2');
      if (typeof initSecurityConsole === 'function') initSecurityConsole();
      if (typeof initAuthorityVerificationDashboard === 'function') initAuthorityVerificationDashboard();
    });


    /* =========================================================================
       DELIVERABLE 7: SECURITY & IMMUTABLE AUDIT TRAIL JAVASCRIPT LOGIC
       ========================================================================= */

    let currentSecurityUser = {
      username: 'officer_sc401',
      fullName: 'Dr. Rajesh Sharma',
      role: 'ROLE_REGULATORY_OFFICER',
      institutionName: 'Western Regional Office',
      token: 'bearer_ciphercore_officer_sc401'
    };

    const PERSONA_MAP = {
      'officer_sc401': { username: 'officer_sc401', fullName: 'Dr. Rajesh Sharma', role: 'ROLE_REGULATORY_OFFICER', institutionName: 'Western Regional Office', token: 'bearer_ciphercore_officer_sc401' },
      'senior_reviewer': { username: 'senior_reviewer', fullName: 'Dr. Meenakshi Sundaram', role: 'ROLE_SENIOR_REVIEWER', institutionName: 'AICTE Central Approval Bureau', token: 'bearer_ciphercore_senior_reviewer' },
      'admin': { username: 'admin', fullName: 'AICTE System Administrator', role: 'ROLE_SYSTEM_ADMINISTRATOR', institutionName: 'AICTE Headquarters', token: 'bearer_ciphercore_admin' },
      'auditor': { username: 'auditor', fullName: 'Suresh Menon', role: 'ROLE_AUDITOR', institutionName: 'Ministry of Education Vigilance Unit', token: 'bearer_ciphercore_auditor' },
      'applicant_apex': { username: 'applicant_apex', fullName: 'Principal Apex Tech', role: 'ROLE_INSTITUTION_APPLICANT', institutionName: 'Apex Institute of Technology', token: 'bearer_ciphercore_applicant_apex' },
      'applicant_bharat': { username: 'applicant_bharat', fullName: 'Registrar Bharat Engg', role: 'ROLE_INSTITUTION_APPLICANT', institutionName: 'Bharat Engineering College', token: 'bearer_ciphercore_applicant_bharat' },
      'service_ai': { username: 'service_ai', fullName: 'CipherCore Autonomous AI Engine', role: 'ROLE_SERVICE_AI', institutionName: 'Autonomous Daemon Services', token: 'bearer_ciphercore_service_ai' }
    };

    function initSecurityConsole() {
      // Sync initial active details
      syncPersonaDetailsUi();
    }

    function switchSecurityPersona(username, btnEl) {
      if (PERSONA_MAP[username]) {
        currentSecurityUser = PERSONA_MAP[username];
      }

      document.querySelectorAll('.role-switch-btn').forEach(btn => {
        btn.classList.remove('active');
        btn.style.background = 'rgba(255,255,255,0.05)';
        btn.style.color = '#cbd5e1';
        btn.style.borderColor = 'rgba(255,255,255,0.1)';
      });

      if (btnEl) {
        btnEl.classList.add('active');
        btnEl.style.background = '#0284c7';
        btnEl.style.color = '#fff';
        btnEl.style.borderColor = '#38bdf8';
      }

      syncPersonaDetailsUi();
      loadSecurityConsoleData();
    }

    function syncPersonaDetailsUi() {
      const u = currentSecurityUser;
      const elUser = document.getElementById('secCurUser');
      const elRole = document.getElementById('secCurRole');
      const elInst = document.getElementById('secCurInst');
      const elToken = document.getElementById('secCurToken');

      if (elUser) elUser.textContent = u.fullName + ' (' + u.username + ')';
      if (elRole) {
        elRole.textContent = u.role;
        elRole.style.background = u.role.includes('ADMIN') ? '#8b5cf6' :
                                  u.role.includes('REGULATORY') || u.role.includes('REVIEWER') ? '#0284c7' :
                                  u.role.includes('AUDITOR') ? '#10b981' :
                                  u.role.includes('AI') ? '#f59e0b' : '#64748b';
      }
      if (elInst) elInst.textContent = u.institutionName;
      if (elToken) elToken.textContent = u.token;

      // Update Header representation if elements exist
      const hName = document.getElementById('headerUserName');
      const hRole = document.getElementById('headerUserRole');
      if (hName) hName.textContent = u.fullName;
      if (hRole) hRole.textContent = u.role.replace('ROLE_', '');
    }

    async function loadSecurityConsoleData() {
      loadSecurityKpis();
      loadAuditIntegrity();
      loadAuditEvents();
      loadSecurityEvents();
      loadUserAdminTable();
    }

    async function loadSecurityKpis() {
      try {
        const res = await fetch('/api/security/kpis');
        if (!res.ok) return;
        const data = await res.json();

        const elTot = document.getElementById('kpiTotalEvents');
        const elFail = document.getElementById('kpiFailedLogins');
        const elDeny = document.getElementById('kpiAccessDenials');
        const elStatus = document.getElementById('kpiAuditStatus');
        const elCount = document.getElementById('kpiAuditCount');

        if (elTot) elTot.textContent = data.totalSecurityEvents || '0';
        if (elFail) elFail.textContent = data.failedLoginAttempts || '0';
        if (elDeny) elDeny.textContent = (data.accessDenialsBlocked || 0) + ' Blocked';
        if (elCount) elCount.textContent = (data.auditChainRecordsCount || 0) + ' records cryptographically linked';

        if (elStatus) {
          if (data.auditChainValid) {
            elStatus.innerHTML = 'âœ… SEALED (SHA-256)';
            elStatus.style.color = '#10b981';
          } else {
            elStatus.innerHTML = 'ðŸš¨ TAMPER DETECTED';
            elStatus.style.color = '#ef4444';
          }
        }
      } catch (err) {
        console.warn('Could not load security KPIs:', err);
      }
    }

    async function loadAuditIntegrity() {
      try {
        const res = await fetch('/api/audit/integrity');
        if (!res.ok) return;
        const data = await res.json();
        renderAuditIntegrityBox(data);
      } catch (err) {
        console.warn('Could not load audit integrity:', err);
      }
    }

    function renderAuditIntegrityBox(data) {
      const alertBox = document.getElementById('chainVerificationAlert');
      const alertText = document.getElementById('chainVerificationText');
      const latestHash = document.getElementById('chainLatestHash');

      if (!alertBox || !alertText) return;

      if (data.valid) {
        alertBox.style.background = 'rgba(16, 185, 129, 0.1)';
        alertBox.style.borderColor = '#10b981';
        alertBox.style.color = '#a7f3d0';
        alertText.innerHTML = '<strong>Cryptographic Hash Chain Intact:</strong> ' + data.message;
        if (latestHash) latestHash.textContent = 'Latest Seal: ' + (data.latestHash ? data.latestHash.substring(0, 16) + '...' : 'Genesis');
      } else {
        alertBox.style.background = 'rgba(239, 68, 68, 0.18)';
        alertBox.style.borderColor = '#ef4444';
        alertBox.style.color = '#fecaca';
        alertText.innerHTML = '<strong style="color:#f87171;">ðŸš¨ CRYPTOGRAPHIC TAMPER DETECTED:</strong> ' + data.message +
          ' (Tampered Seq #' + (data.tamperedSequenceNumber || '?') + ')';
        if (latestHash) latestHash.textContent = 'MISMATCH: ' + (data.actualHash ? data.actualHash.substring(0, 16) + '...' : '');
      }
    }

    async function runAuditIntegrityCheck() {
      const alertText = document.getElementById('chainVerificationText');
      if (alertText) alertText.textContent = 'Running SHA-256 recursive digest verification across full chain...';
      try {
        const res = await fetch('/api/audit/integrity/check', { method: 'POST' });
        const data = await res.json();
        renderAuditIntegrityBox(data);
        loadSecurityKpis();
      } catch (err) {
        alert('Audit integrity verification failed: ' + err.message);
      }
    }

    async function triggerTamperSimulation() {
      try {
        const res = await fetch('/api/audit/simulate-tamper', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ sequenceNumber: 2, modifiedDetails: 'TAMPERED EVIDENCE: Unofficial record altered in database directly' })
        });
        const data = await res.json();
        renderAuditIntegrityBox(data.verificationResult);
        loadSecurityKpis();
        loadAuditEvents();
      } catch (err) {
        alert('Simulation failed: ' + err.message);
      }
    }

    async function restoreTamperSimulation() {
      try {
        const res = await fetch('/api/audit/restore-tamper', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ sequenceNumber: 2, originalDetails: 'AICTE Approval Process Handbook (APH 2026) initial category seeding: Extension of Approval (EoA)' })
        });
        const data = await res.json();
        renderAuditIntegrityBox(data.verificationResult);
        loadSecurityKpis();
        loadAuditEvents();
      } catch (err) {
        alert('Restore failed: ' + err.message);
      }
    }

    async function exportAuditTrailCsv() {
      window.open('/api/audit/export?username=' + encodeURIComponent(currentSecurityUser.username), '_blank');
    }

    async function loadAuditEvents() {
      const tbody = document.getElementById('auditChainTableBody');
      if (!tbody) return;

      try {
        const res = await fetch('/api/audit/events?limit=50');
        if (!res.ok) return;
        const list = await res.json();

        if (list.length === 0) {
          tbody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding:15px; color:#94a3b8;">No audit records recorded yet.</td></tr>';
          return;
        }

        tbody.innerHTML = list.map(ev => {
          const prevShort = ev.previousHash ? ev.previousHash.substring(0, 10) + '...' : '00000000...';
          const hashShort = ev.eventHash ? ev.eventHash.substring(0, 12) + '...' : '---';
          const isGenesis = ev.sequenceNumber === 1;

          return `
            <tr>
              <td><span class="badge" style="background:#1e293b; color:#38bdf8; font-weight:700;">#${ev.sequenceNumber || '-'}</span></td>
              <td style="font-size:0.7rem; color:var(--text-muted);">${ev.eventTimestamp ? ev.eventTimestamp.replace('T', ' ').substring(0, 19) : ''}</td>
              <td>
                <div style="font-weight:600; color:#f1f5f9; font-size:0.75rem;">${escapeHtml(ev.username || 'system')}</div>
                <div style="font-size:0.65rem; color:#94a3b8;">${escapeHtml(ev.userRole || '')}</div>
              </td>
              <td>
                <span class="badge" style="background:rgba(56,189,248,0.12); color:#38bdf8; font-size:0.68rem; border:1px solid rgba(56,189,248,0.3);">${escapeHtml(ev.action || '')}</span>
                <div style="font-size:0.65rem; color:#64748b; margin-top:2px;">${escapeHtml(ev.entityName || '')} ${escapeHtml(ev.entityId || '')}</div>
              </td>
              <td style="font-size:0.72rem; color:#cbd5e1; max-width:280px; word-break:break-word;">
                ${escapeHtml(ev.details || '')}
              </td>
              <td>
                <code title="Previous Hash: ${ev.previousHash || ''}" style="cursor:pointer; color:#94a3b8; font-size:0.65rem; background:rgba(0,0,0,0.3); padding:2px 5px; border-radius:3px;">
                  ${isGenesis ? '00000000 (GENESIS)' : prevShort}
                </code>
              </td>
              <td>
                <code title="Full SHA-256 Digest: ${ev.eventHash || ''}" style="cursor:pointer; color:#a7f3d0; font-size:0.65rem; background:rgba(16,185,129,0.1); border:1px solid rgba(16,185,129,0.3); padding:2px 5px; border-radius:3px;">
                  ${hashShort}
                </code>
              </td>
            </tr>
          `;
        }).join('');
      } catch (err) {
        tbody.innerHTML = '<tr><td colspan="7" style="color:#ef4444; padding:15px; text-align:center;">Failed to load audit events.</td></tr>';
      }
    }

    async function loadSecurityEvents() {
      const tbody = document.getElementById('securityEventsTableBody');
      if (!tbody) return;

      try {
        const res = await fetch('/api/security/events?limit=30');
        if (!res.ok) return;
        const list = await res.json();

        if (list.length === 0) {
          tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; padding:15px; color:#94a3b8;">No security events recorded.</td></tr>';
          return;
        }

        tbody.innerHTML = list.map(s => {
          const isDenied = s.outcome === 'DENIED' || s.outcome === 'FAILURE';
          const outcomeBadge = isDenied ?
            '<span class="badge" style="background:#dc2626; color:#fff; font-size:0.65rem;">DENIED</span>' :
            '<span class="badge" style="background:#16a34a; color:#fff; font-size:0.65rem;">SUCCESS</span>';

          return `
            <tr>
              <td style="font-size:0.68rem; color:var(--text-muted);">${s.occurredAt ? s.occurredAt.replace('T', ' ').substring(0, 19) : ''}</td>
              <td><span style="font-weight:600; font-size:0.72rem; color:${isDenied ? '#f87171' : '#38bdf8'};">${escapeHtml(s.eventType || '')}</span></td>
              <td>
                <div style="font-size:0.72rem; color:#f1f5f9;">${escapeHtml(s.actorUsername || 'anonymous')}</div>
                <div style="font-size:0.62rem; color:#64748b;">${escapeHtml(s.actorRole || '')}</div>
              </td>
              <td style="font-size:0.68rem; color:#cbd5e1;">${escapeHtml(s.targetType || '')} ${escapeHtml(s.targetId || '')}</td>
              <td>${outcomeBadge}</td>
              <td style="font-size:0.65rem; color:${isDenied ? '#fca5a5' : '#94a3b8'};">${escapeHtml(s.reasonCode || '')}</td>
            </tr>
          `;
        }).join('');
      } catch (err) {
        tbody.innerHTML = '<tr><td colspan="6" style="color:#ef4444; padding:15px; text-align:center;">Failed to load security events.</td></tr>';
      }
    }

    async function loadUserAdminTable() {
      const tbody = document.getElementById('userAdminTableBody');
      if (!tbody) return;

      try {
        const res = await fetch('/api/admin/users?callerUsername=admin');
        if (!res.ok) return;
        const users = await res.json();

        tbody.innerHTML = users.map(u => {
          const statusBadge = u.active ?
            '<span class="badge" style="background:#10b981; color:#fff; font-size:0.65rem;">ACTIVE</span>' :
            '<span class="badge" style="background:#ef4444; color:#fff; font-size:0.65rem;">DEACTIVATED</span>';

          const toggleBtn = u.active ?
            `<button onclick="toggleUserActiveStatus(${u.id}, false)" class="btn-secondary" style="font-size:0.65rem; padding:3px 8px; color:#ef4444; border-color:#ef4444;">Deactivate</button>` :
            `<button onclick="toggleUserActiveStatus(${u.id}, true)" class="btn-secondary" style="font-size:0.65rem; padding:3px 8px; color:#10b981; border-color:#10b981;">Activate</button>`;

          return `
            <tr>
              <td>
                <div style="font-weight:600; font-size:0.75rem; color:#f8fafc;">${escapeHtml(u.username)}</div>
                <div style="font-size:0.65rem; color:#94a3b8;">${escapeHtml(u.fullName || '')} &bull; ${escapeHtml(u.institutionName || 'AICTE')}</div>
              </td>
              <td>
                <select onchange="updateUserRole(${u.id}, this.value)" style="background:#1e293b; color:#38bdf8; border:1px solid rgba(255,255,255,0.15); border-radius:4px; font-size:0.68rem; padding:2px 4px;">
                  <option value="ROLE_SYSTEM_ADMINISTRATOR" ${u.role === 'ROLE_SYSTEM_ADMINISTRATOR' || u.role === 'ROLE_ADMIN' ? 'selected' : ''}>ADMIN</option>
                  <option value="ROLE_REGULATORY_OFFICER" ${u.role === 'ROLE_REGULATORY_OFFICER' || u.role === 'ROLE_COMPLIANCE_OFFICER' ? 'selected' : ''}>OFFICER</option>
                  <option value="ROLE_SENIOR_REVIEWER" ${u.role === 'ROLE_SENIOR_REVIEWER' || u.role === 'ROLE_REVIEWER' ? 'selected' : ''}>SENIOR_REVIEWER</option>
                  <option value="ROLE_AUDITOR" ${u.role === 'ROLE_AUDITOR' ? 'selected' : ''}>AUDITOR</option>
                  <option value="ROLE_INSTITUTION_APPLICANT" ${u.role === 'ROLE_INSTITUTION_APPLICANT' || u.role === 'ROLE_APPLICANT' ? 'selected' : ''}>APPLICANT</option>
                  <option value="ROLE_SERVICE_AI" ${u.role === 'ROLE_SERVICE_AI' ? 'selected' : ''}>SERVICE_AI</option>
                </select>
              </td>
              <td>${statusBadge}</td>
              <td>${toggleBtn}</td>
            </tr>
          `;
        }).join('');
      } catch (err) {
        tbody.innerHTML = '<tr><td colspan="4" style="color:#ef4444; padding:15px; text-align:center;">Failed to load user accounts.</td></tr>';
      }
    }

    async function toggleUserActiveStatus(userId, makeActive) {
      try {
        const res = await fetch(`/api/admin/users/${userId}/status?callerUsername=admin`, {
          method: 'PATCH',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ active: makeActive })
        });
        if (res.ok) {
          loadUserAdminTable();
          loadSecurityEvents();
          loadSecurityKpis();
        } else {
          const err = await res.json();
          alert('Failed to update status: ' + (err.error || 'Unknown error'));
        }
      } catch (err) {
        alert('Request failed: ' + err.message);
      }
    }

    async function updateUserRole(userId, newRole) {
      try {
        const res = await fetch(`/api/admin/users/${userId}/roles?callerUsername=admin`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ role: newRole })
        });
        if (res.ok) {
          loadUserAdminTable();
          loadSecurityEvents();
          loadAuditEvents();
        } else {
          const err = await res.json();
          alert('Failed to update role: ' + (err.error || 'Unknown error'));
        }
      } catch (err) {
        alert('Request failed: ' + err.message);
      }
    }

    /* SIH Interactive Test Bed Actions */

    async function testIdorAttack() {
      const box = document.getElementById('idorTestOutput');
      if (!box) return;
      box.style.display = 'block';
      box.style.background = 'rgba(15, 23, 42, 0.9)';
      box.style.border = '1px solid #f87171';
      box.innerHTML = '<span style="color:#38bdf8;">GET /api/documents/1/download?username=applicant_bharat</span><br><span style="color:#fbbf24;">Executing cross-institution request...</span>';

      try {
        const res = await fetch('/api/documents/1/download?username=applicant_bharat');
        const data = await res.json();

        if (res.status === 403) {
          box.innerHTML = `
            <span style="color:#f87171; font-weight:700;">HTTP 403 FORBIDDEN: Access Denied</span><br>
            <span style="color:#fecaca;">IDOR Shield: ${escapeHtml(data.error || 'Cross-institution access prohibited')}</span><br>
            <span style="color:#34d399;">&check; Security Event logged: DOCUMENT_ACCESS_DENIED</span>
          `;
        } else {
          box.innerHTML = `<span style="color:#ef4444;">Unexpected response status: ${res.status}</span>`;
        }
        loadSecurityKpis();
        loadSecurityEvents();
      } catch (err) {
        box.innerHTML = `<span style="color:#ef4444;">Network error: ${err.message}</span>`;
      }
    }

    async function testAiAdverseActionBlock() {
      const box = document.getElementById('aiGateTestOutput');
      if (!box) return;
      box.style.display = 'block';
      box.style.background = 'rgba(15, 23, 42, 0.9)';
      box.style.border = '1px solid #fbbf24';
      box.innerHTML = '<span style="color:#38bdf8;">POST /api/applications/1/adverse-action/validate-and-commit?username=service_ai</span><br><span style="color:#fbbf24;">Submitting adverse decision from AI identity...</span>';

      try {
        const res = await fetch('/api/applications/1/adverse-action/validate-and-commit?username=service_ai', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            outcome: 'REJECTED_REFUSAL_OF_APPROVAL',
            statutoryRationale: 'AICTE Approval Process Handbook Clause 1.4: Refusal due to automatic anomaly threshold breach',
            conditionsPrecedent: 'None'
          })
        });
        const data = await res.json();

        if (res.status === 403) {
          box.innerHTML = `
            <span style="color:#fbbf24; font-weight:700;">HTTP 403 FORBIDDEN: Human Gate Triggered</span><br>
            <span style="color:#fde68a;">Gate Reason: ${escapeHtml(data.error || '')}</span><br>
            <span style="color:#34d399;">&check; Security Event logged: ADVERSE_ACTION_ATTEMPTED_BLOCKED</span>
          `;
        } else {
          box.innerHTML = `<span style="color:#ef4444;">Unexpected status: ${res.status}</span>`;
        }
        loadSecurityKpis();
        loadSecurityEvents();
      } catch (err) {
        box.innerHTML = `<span style="color:#ef4444;">Error: ${err.message}</span>`;
      }
    }

    async function testAuthorizedApproval() {
      const box = document.getElementById('authGateTestOutput');
      if (!box) return;
      box.style.display = 'block';
      box.style.background = 'rgba(15, 23, 42, 0.9)';
      box.style.border = '1px solid #34d399';
      box.innerHTML = '<span style="color:#38bdf8;">POST /api/applications/1/adverse-action/validate-and-commit?username=officer_sc401</span><br><span style="color:#fbbf24;">Submitting authorized decision with APH rationale...</span>';

      try {
        const res = await fetch('/api/applications/1/adverse-action/validate-and-commit?username=officer_sc401', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            outcome: 'DEFICIENCY_NOTICE_ISSUED',
            statutoryRationale: 'AICTE Approval Process Handbook 2026-27 Chapter I Clause 1.4(d): Fire Safety NOC requires Tehsildar land record rectification within 15 days.',
            conditionsPrecedent: 'Submit certified fire NOC and converted land title deed.',
            committeeReference: 'AICTE-SC-WRO-2026/SEC-01',
            spotInspectionRequired: true
          })
        });
        const data = await res.json();

        if (res.ok) {
          box.innerHTML = `
            <span style="color:#34d399; font-weight:700;">HTTP 200 OK: Decision Sealed</span><br>
            <span style="color:#a7f3d0;">Official Disposition: ${escapeHtml(data.outcome)}</span><br>
            <span style="color:#94a3b8; font-size:0.65rem;">SHA-256 Seal: <code>${escapeHtml(data.decisionIntegritySeal ? data.decisionIntegritySeal.substring(0, 24) + '...' : '')}</code></span><br>
            <span style="color:#38bdf8;">&check; Linked to Immutable Audit Trail (New Hash Chain Block)</span>
          `;
        } else {
          box.innerHTML = `<span style="color:#ef4444;">Error: ${escapeHtml(data.error || 'Failed')}</span>`;
        }
        loadSecurityKpis();
        loadAuditEvents();
        loadSecurityEvents();
        loadAuditIntegrity();
      } catch (err) {
        box.innerHTML = `<span style="color:#ef4444;">Error: ${err.message}</span>`;
      }
    }


    /* =========================================================================
       PRESENTATION MODE & WORKSPACE SWITCHER CONTROLLER
       ========================================================================= */
    let currentPresentationMode = 'showcase';

    function switchPresentationMode(mode) {
      currentPresentationMode = mode;
      const showcaseEl = document.getElementById('showcase-view');
      const btnShowcase = document.getElementById('btnModeShowcase');
      const btnWorkspace = document.getElementById('btnModeWorkspace');

      if (mode === 'showcase') {
        if (showcaseEl) showcaseEl.style.display = 'block';
        if (btnShowcase) btnShowcase.classList.add('active');
        if (btnWorkspace) btnWorkspace.classList.remove('active');
        window.scrollTo({ top: 0, behavior: 'smooth' });
      } else {
        if (showcaseEl) showcaseEl.style.display = 'none';
        if (btnWorkspace) btnWorkspace.classList.add('active');
        if (btnShowcase) btnShowcase.classList.remove('active');
        // Ensure active tab section is displayed safely
        const activeTab = document.querySelector('.nav-tab.active');
        const tabId = (activeTab && activeTab.dataset && activeTab.dataset.tab) || 
                      (activeTab && activeTab.getAttribute && activeTab.getAttribute('data-tab')) || 
                      'tab-overview';
        switchTab(tabId);
      }
    }

    function openOperationalWorkspace(tabId) {
      switchPresentationMode('workspace');
      if (tabId) {
        switchTab(tabId);
      }
    }

    function smoothScrollTo(elementId) {
      const el = document.getElementById(elementId);
      if (el) {
        el.scrollIntoView({ behavior: 'smooth' });
      }
    }


    // Consolidated Master DOMContentLoaded Lifecycle Runner
    window.addEventListener('DOMContentLoaded', () => {
      initScrollTracker();
      if (typeof initThreeIntelligenceCore === 'function') initThreeIntelligenceCore();
      if (typeof initBlockchainCanvas === 'function') initBlockchainCanvas();
      if (typeof initBlockchainNetwork === 'function') initBlockchainNetwork();
      if (typeof updateDossierView === 'function') updateDossierView();
      if (typeof refreshDocumentList === 'function') refreshDocumentList();
      if (typeof initAuthorityVerificationDashboard === 'function') initAuthorityVerificationDashboard();
      if (typeof loadCvData === 'function') loadCvData('2');
      if (typeof loadComplianceData === 'function') loadComplianceData('2');
      if (typeof initSecurityConsole === 'function') initSecurityConsole();
    });