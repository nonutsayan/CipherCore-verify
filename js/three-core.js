/* ==========================================================================
   CIPHERCORE THREE.JS 3D INTELLIGENCE CORE
   Wireframe Icosahedron Core, Differential Torus Rings, & Orbital Validator Nodes
   ========================================================================== */

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