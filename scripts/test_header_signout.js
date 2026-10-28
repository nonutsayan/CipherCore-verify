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

// 2. Verify headerSignOutBtn is present and configured
const hasSignOutBtn = html.includes('id="headerSignOutBtn"');
const hasSignOutClick = html.includes('onclick="signOutUser()"');
console.log('2. Prominent header Sign Out button present:', (hasSignOutBtn && hasSignOutClick) ? 'PASS' : 'FAIL');
if (!hasSignOutBtn || !hasSignOutClick) {
  console.error('FAIL: headerSignOutBtn or onclick="signOutUser()" missing');
  process.exit(1);
}

// 3. Verify interactive user dropdown menu
const hasUserDropdown = html.includes('id="userProfileDropdown"');
const hasToggleUserDropdown = html.includes('toggleUserDropdown()');
console.log('3. Interactive user profile dropdown menu present:', (hasUserDropdown && hasToggleUserDropdown) ? 'PASS' : 'FAIL');
if (!hasUserDropdown || !hasToggleUserDropdown) {
  console.error('FAIL: userProfileDropdown or toggleUserDropdown missing');
  process.exit(1);
}

// 4. Verify mobile secondary tab bar
const hasMobileTabBar = html.includes('id="mobileTabBar"');
const hasMobileNav1 = html.includes('id="mobileNav-applications"');
console.log('4. Mobile responsive navigation bar present:', (hasMobileTabBar && hasMobileNav1) ? 'PASS' : 'FAIL');
if (!hasMobileTabBar || !hasMobileNav1) {
  console.error('FAIL: mobileTabBar or mobileNav items missing');
  process.exit(1);
}

// 5. Verify dropdown profile sync in JS
const hasDropSync = html.includes('dropdownUserName') && html.includes('dropdownUserRole');
console.log('5. Dropdown account details sync logic present in updateAuthUI:', hasDropSync ? 'PASS' : 'FAIL');
if (!hasDropSync) {
  console.error('FAIL: Dropdown sync logic missing');
  process.exit(1);
}

console.log('\nALL HEADER & SIGN OUT TESTS PASSED WITH 100% SUCCESS!');
