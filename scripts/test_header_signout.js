const fs = require('fs');

const html = fs.readFileSync('portal.html', 'utf8');

console.log('=== HEADER & SIGN OUT VERIFICATION TEST ===\n');

// 1. Verify runtimeModeBadge is removed from header
const hasRuntimeBadge = html.includes('id="runtimeModeBadge"');
console.log('1. GitHub Pages Demo runtimeModeBadge removed:', !hasRuntimeBadge ? 'PASS' : 'FAIL');
if (hasRuntimeBadge) {
  console.error('FAIL: id="runtimeModeBadge" was found in portal.html');
  process.exit(1);
}

// 2. Verify headerSignOutBtn is present and configured with handleSignOut / signOutUser
const hasSignOutBtn = html.includes('id="headerSignOutBtn"');
const hasSignOutClick = html.includes('handleSignOut()') || html.includes('signOutUser()');
console.log('2. Prominent header Sign Out button present:', (hasSignOutBtn && hasSignOutClick) ? 'PASS' : 'FAIL');
if (!hasSignOutBtn || !hasSignOutClick) {
  console.error('FAIL: headerSignOutBtn or handleSignOut / signOutUser missing');
  process.exit(1);
}

// 3. Verify compact officer profile badge with role hiding under 1280px
const hasUserBadge = html.includes('class="user-badge"');
const hasUserAvatar = html.includes('class="user-badge-avatar"');
const hasResponsiveRole = html.includes('@media (max-width: 1279px)');
console.log('3. Compact officer badge & responsive role hiding present:', (hasUserBadge && hasUserAvatar && hasResponsiveRole) ? 'PASS' : 'FAIL');
if (!hasUserBadge || !hasUserAvatar || !hasResponsiveRole) {
  console.error('FAIL: Compact officer badge or responsive role rule missing');
  process.exit(1);
}

// 4. Verify flex-shrink: 0 enforcement on Sign Out button
const hasFlexShrink = html.includes('flex-shrink: 0 !important;') && html.includes('.btn-signout');
console.log('4. flex-shrink: 0 !important enforced on .btn-signout:', hasFlexShrink ? 'PASS' : 'FAIL');
if (!hasFlexShrink) {
  console.error('FAIL: flex-shrink: 0 !important missing from .btn-signout');
  process.exit(1);
}

// 5. Verify index.html also has hardened nav and handleSignOut
const indexHtml = fs.readFileSync('index.html', 'utf8');
const indexHasHardenedNav = indexHtml.includes('overflow:visible !important;white-space:nowrap') || indexHtml.includes('overflow: visible !important; white-space: nowrap;');
const indexHasSignOut = indexHtml.includes('handleSignOut()');
console.log('5. Hardened flexbox and Sign Out synchronization on index.html:', (indexHasHardenedNav && indexHasSignOut) ? 'PASS' : 'FAIL');
if (!indexHasHardenedNav || !indexHasSignOut) {
  console.error('FAIL: index.html missing hardened nav or handleSignOut');
  process.exit(1);
}

console.log('\nALL ARCHITECT HEADER & SIGN OUT TESTS PASSED WITH 100% SUCCESS!');
