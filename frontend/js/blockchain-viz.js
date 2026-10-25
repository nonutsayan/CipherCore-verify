/* ==========================================================================
   BHARATVIDYA POA BLOCKCHAIN CONSENSUS VISUALIZER (2D RING)
   7 Sovereign Validator Nodes with Ray Connections
   ========================================================================== */

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