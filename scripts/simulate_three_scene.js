const fs = require('fs');

const html = fs.readFileSync('index.html', 'utf8');

const scriptMatches = [...html.matchAll(/<script\b[^>]*>([\s\S]*?)<\/script>/gi)];
const threeScript = scriptMatches.find(m => m[1].includes('new T.Scene()'))[1];

// Create mock window/document/THREE
global.window = {
  innerWidth: 1200,
  innerHeight: 800,
  devicePixelRatio: 1,
  addEventListener: () => {},
  matchMedia: () => ({ matches: false, addEventListener: () => {} }),
  THREE: {
    Scene: function() { this.add = () => {}; },
    PerspectiveCamera: function() { this.position = { z: 0 }; this.updateProjectionMatrix = () => {}; },
    Group: function() { this.add = () => {}; this.position = { z: 0 }; this.scale = { set: () => {}, setScalar: () => {} }; this.rotation = { x: 0, y: 0, z: 0 }; },
    WebGLRenderer: function() { this.setPixelRatio = () => {}; this.setClearColor = () => {}; this.setSize = () => {}; this.render = () => {}; },
    Color: function() { this.set = () => {}; this.lerp = () => {}; this.copy = () => {}; },
    BufferGeometry: function() { this.setAttribute = () => {}; },
    BufferAttribute: function() {},
    PlaneGeometry: function() {},
    CanvasTexture: function() {},
    MeshBasicMaterial: function() { this.color = { lerp: () => {} }; },
    LineBasicMaterial: function() { this.color = { lerp: () => {} }; },
    PointsMaterial: function() { this.color = { copy: () => {} }; },
    Mesh: function() { this.add = () => {}; this.position = { set: () => {} }; this.rotation = { set: () => {} }; this.scale = { set: () => {} }; },
    LineLoop: function() {},
    Line: function() {},
    Points: function() { this.rotation = { y: 0 }; },
    AdditiveBlending: 1,
    NormalBlending: 0,
    DoubleSide: 2
  }
};
global.document = {
  documentElement: {
    getAttribute: () => 'light',
    setAttribute: () => {},
    classList: { add: () => {}, remove: () => {} }
  },
  body: { style: {} },
  getElementById: (id) => {
    return {
      children: [],
      appendChild: () => {},
      addEventListener: () => {},
      getContext: () => ({
        fillRect() {}, strokeRect() {}, fillText() {}, beginPath() {},
        arc() {}, stroke() {}, fill() {}, moveTo() {}, lineTo() {},
        createLinearGradient: () => ({ addColorStop() {} }),
        measureText: () => ({ width: 50 })
      }),
      style: {}
    };
  },
  createElement: (tag) => ({
    width: 0,
    height: 0,
    getContext: () => ({
      fillRect() {}, strokeRect() {}, fillText() {}, beginPath() {},
      arc() {}, stroke() {}, fill() {}, moveTo() {}, lineTo() {},
      createLinearGradient: () => ({ addColorStop() {} }),
      measureText: () => ({ width: 50 })
    })
  }),
  querySelectorAll: () => [{
    addEventListener: () => {},
    classList: { add: () => {}, remove: () => {}, toggle: () => {} },
    dataset: { layout: 'fan', x: 1, s: 1, o: 1 },
    hasAttribute: () => true,
    getBoundingClientRect: () => ({ top: 0, bottom: 500 }),
    querySelector: () => null,
    querySelectorAll: () => []
  }],
  fonts: { ready: Promise.resolve() }
};
global.getComputedStyle = () => ({ getPropertyValue: () => '' });
global.matchMedia = () => ({ matches: false, addEventListener: () => {} });
global.MutationObserver = function() { this.observe = () => {}; };
global.performance = { now: () => 0 };
global.requestAnimationFrame = () => {};
global.localStorage = { getItem: () => null, setItem: () => {} };

try {
  eval(threeScript);
  console.log('Script ran successfully!');
} catch (e) {
  console.error('RUNTIME ERROR IN THREE.JS SCENE:', e);
}
