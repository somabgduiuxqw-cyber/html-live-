package com.example.data.repository

import com.example.model.Project
import com.example.model.ProjectFile
import com.example.model.ProjectType
import java.util.UUID

data class TemplateProject(
    val title: String,
    val description: String,
    val type: ProjectType,
    val iconName: String,
    val htmlContent: String,
    val cssContent: String,
    val jsContent: String
)

object TemplateRepository {

    val templates: List<TemplateProject> = listOf(
        // 1. 2D Canvas Platformer Game
        TemplateProject(
            title = "2D Canvas Platformer Game",
            description = "Complete running canvas platformer with physics, jump, collectible coins, and on-screen touch controls.",
            type = ProjectType.GAME,
            iconName = "sports_esports",
            htmlContent = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
  <title>Platformer Adventure</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div id="game-container">
    <div id="hud">
      <div id="score-box">Coins: <span id="coins-count">0</span></div>
      <div id="health-box">Lives: <span id="lives-count">3</span></div>
    </div>
    <canvas id="gameCanvas" width="400" height="500"></canvas>
    
    <!-- Mobile Touch Controls -->
    <div id="touch-controls">
      <div class="dpad">
        <button id="btn-left" class="touch-btn">◀</button>
        <button id="btn-right" class="touch-btn">▶</button>
      </div>
      <button id="btn-jump" class="touch-btn action-btn">JUMP</button>
    </div>
  </div>
  <script src="script.js"></script>
</body>
</html>""",
            cssContent = """* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  user-select: none;
  -webkit-user-select: none;
}
body {
  background: #0f172a;
  color: #fff;
  font-family: system-ui, -apple-system, sans-serif;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  overflow: hidden;
}
#game-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 420px;
  position: relative;
}
#hud {
  display: flex;
  justify-content: space-between;
  width: 100%;
  padding: 8px 16px;
  background: #1e293b;
  font-weight: bold;
  font-size: 16px;
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
  color: #fbbf24;
}
#lives-count {
  color: #ef4444;
}
canvas {
  background: linear-gradient(to bottom, #38bdf8, #bae6fd 70%, #86efac);
  border: 2px solid #334155;
  display: block;
}
#touch-controls {
  display: flex;
  justify-content: space-between;
  width: 100%;
  padding: 12px 16px;
  background: #1e293b;
  border-bottom-left-radius: 8px;
  border-bottom-right-radius: 8px;
}
.touch-btn {
  background: #3b82f6;
  color: white;
  border: none;
  font-size: 20px;
  font-weight: bold;
  padding: 16px 24px;
  border-radius: 12px;
  box-shadow: 0 4px #1d4ed8;
  cursor: pointer;
  touch-action: manipulation;
}
.touch-btn:active {
  transform: translateY(2px);
  box-shadow: 0 2px #1d4ed8;
}
.action-btn {
  background: #10b981;
  box-shadow: 0 4px #047857;
  padding: 16px 32px;
}
.action-btn:active {
  box-shadow: 0 2px #047857;
}""",
            jsContent = """// 2D Canvas Platformer Game Engine
const canvas = document.getElementById('gameCanvas');
const ctx = canvas.getContext('2d');

const coinsEl = document.getElementById('coins-count');
const livesEl = document.getElementById('lives-count');

let score = 0;
let lives = 3;

const player = {
  x: 50,
  y: 350,
  w: 24,
  h: 30,
  vx: 0,
  vy: 0,
  speed: 4,
  jumpStrength: -11,
  grounded: false,
  color: '#e11d48'
};

const gravity = 0.5;

const platforms = [
  { x: 0, y: 450, w: 400, h: 50, color: '#15803d' }, // Ground
  { x: 60, y: 380, w: 90, h: 14, color: '#64748b' },
  { x: 200, y: 320, w: 100, h: 14, color: '#64748b' },
  { x: 100, y: 250, w: 80, h: 14, color: '#64748b' },
  { x: 240, y: 190, w: 110, h: 14, color: '#64748b' },
  { x: 80, y: 130, w: 90, h: 14, color: '#64748b' }
];

let coins = [
  { x: 100, y: 350, r: 8, collected: false },
  { x: 250, y: 290, r: 8, collected: false },
  { x: 140, y: 220, r: 8, collected: false },
  { x: 290, y: 160, r: 8, collected: false },
  { x: 120, y: 100, r: 8, collected: false }
];

const keys = { left: false, right: false, jump: false };

// Touch event bindings
function bindTouch(id, keyName) {
  const btn = document.getElementById(id);
  btn.addEventListener('touchstart', (e) => { e.preventDefault(); keys[keyName] = true; });
  btn.addEventListener('touchend', (e) => { e.preventDefault(); keys[keyName] = false; });
  btn.addEventListener('mousedown', () => keys[keyName] = true);
  btn.addEventListener('mouseup', () => keys[keyName] = false);
}

bindTouch('btn-left', 'left');
bindTouch('btn-right', 'right');
bindTouch('btn-jump', 'jump');

// Keyboard support
window.addEventListener('keydown', (e) => {
  if (e.key === 'ArrowLeft' || e.key === 'a') keys.left = true;
  if (e.key === 'ArrowRight' || e.key === 'd') keys.right = true;
  if (e.key === 'ArrowUp' || e.key === ' ' || e.key === 'w') keys.jump = true;
});

window.addEventListener('keyup', (e) => {
  if (e.key === 'ArrowLeft' || e.key === 'a') keys.left = false;
  if (e.key === 'ArrowRight' || e.key === 'd') keys.right = false;
  if (e.key === 'ArrowUp' || e.key === ' ' || e.key === 'w') keys.jump = false;
});

function update() {
  if (keys.left) player.vx = -player.speed;
  else if (keys.right) player.vx = player.speed;
  else player.vx = 0;

  if (keys.jump && player.grounded) {
    player.vy = player.jumpStrength;
    player.grounded = false;
  }

  player.vy += gravity;
  player.x += player.vx;
  player.y += player.vy;

  // Screen bounds
  if (player.x < 0) player.x = 0;
  if (player.x + player.w > canvas.width) player.x = canvas.width - player.w;

  player.grounded = false;

  // Collision with platforms
  for (const plat of platforms) {
    if (
      player.x < plat.x + plat.w &&
      player.x + player.w > plat.x &&
      player.y + player.h >= plat.y &&
      player.y + player.h <= plat.y + plat.h + player.vy &&
      player.vy >= 0
    ) {
      player.grounded = true;
      player.vy = 0;
      player.y = plat.y - player.h;
    }
  }

  // Coin collection
  for (const coin of coins) {
    if (!coin.collected) {
      const dist = Math.hypot((player.x + player.w/2) - coin.x, (player.y + player.h/2) - coin.y);
      if (dist < coin.r + player.w/2) {
        coin.collected = true;
        score += 10;
        coinsEl.innerText = score;
        console.log("Coin collected! Score: " + score);
      }
    }
  }

  // Fall off bottom check
  if (player.y > canvas.height) {
    lives--;
    livesEl.innerText = lives;
    console.warn("Player fell! Lives remaining: " + lives);
    if (lives <= 0) {
      alert("Game Over! Score: " + score);
      score = 0;
      lives = 3;
      coins.forEach(c => c.collected = false);
      coinsEl.innerText = score;
      livesEl.innerText = lives;
    }
    player.x = 50;
    player.y = 350;
    player.vx = 0;
    player.vy = 0;
  }
}

function draw() {
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  // Draw Platforms
  for (const plat of platforms) {
    ctx.fillStyle = plat.color;
    ctx.fillRect(plat.x, plat.y, plat.w, plat.h);
    ctx.strokeStyle = '#0f172a';
    ctx.lineWidth = 1;
    ctx.strokeRect(plat.x, plat.y, plat.w, plat.h);
  }

  // Draw Coins
  for (const coin of coins) {
    if (!coin.collected) {
      ctx.beginPath();
      ctx.arc(coin.x, coin.y, coin.r, 0, Math.PI * 2);
      ctx.fillStyle = '#fbbf24';
      ctx.fill();
      ctx.strokeStyle = '#d97706';
      ctx.lineWidth = 2;
      ctx.stroke();
    }
  }

  // Draw Player
  ctx.fillStyle = player.color;
  ctx.fillRect(player.x, player.y, player.w, player.h);

  // Draw Player Eyes
  ctx.fillStyle = '#fff';
  const eyeOffset = player.vx < 0 ? 3 : 13;
  ctx.fillRect(player.x + eyeOffset, player.y + 6, 6, 6);
  ctx.fillStyle = '#000';
  ctx.fillRect(player.x + eyeOffset + 2, player.y + 8, 2, 2);
}

function loop() {
  update();
  draw();
  requestAnimationFrame(loop);
}

console.log("2D Platformer loaded and running!");
requestAnimationFrame(loop);"""
        ),

        // 2. Retro Snake Game
        TemplateProject(
            title = "Retro Snake Game",
            description = "Classic arcade snake with canvas rendering, high score, and swipe / touch D-pad.",
            type = ProjectType.GAME,
            iconName = "grid_view",
            htmlContent = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Retro Snake</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="snake-app">
    <header>
      <h1>SNAKE ARCADE</h1>
      <div class="scores">
        <div>Score: <span id="score">0</span></div>
        <div>Best: <span id="high-score">0</span></div>
      </div>
    </header>
    
    <canvas id="board" width="300" height="300"></canvas>
    
    <div class="dpad-container">
      <button class="dpad-btn up" onclick="setDir(0, -1)">▲</button>
      <div class="dpad-middle">
        <button class="dpad-btn left" onclick="setDir(-1, 0)">◀</button>
        <button class="dpad-btn down" onclick="setDir(0, 1)">▼</button>
        <button class="dpad-btn right" onclick="setDir(1, 0)">▶</button>
      </div>
    </div>
  </div>
  <script src="script.js"></script>
</body>
</html>""",
            cssContent = """body {
  margin: 0;
  background: #020617;
  color: #38bdf8;
  font-family: 'Courier New', monospace;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
}
.snake-app {
  text-align: center;
  max-width: 340px;
  width: 100%;
  padding: 10px;
}
header h1 {
  font-size: 22px;
  letter-spacing: 2px;
  margin-bottom: 6px;
  color: #a855f7;
}
.scores {
  display: flex;
  justify-content: space-around;
  font-size: 16px;
  margin-bottom: 12px;
  color: #4ade80;
}
canvas {
  background: #0f172a;
  border: 3px solid #3b82f6;
  border-radius: 8px;
  box-shadow: 0 0 15px rgba(59, 130, 246, 0.4);
}
.dpad-container {
  margin-top: 15px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}
.dpad-middle {
  display: flex;
  gap: 12px;
}
.dpad-btn {
  background: #1e293b;
  color: #38bdf8;
  border: 2px solid #334155;
  font-size: 20px;
  width: 60px;
  height: 50px;
  border-radius: 8px;
  cursor: pointer;
}
.dpad-btn:active {
  background: #38bdf8;
  color: #000;
}""",
            jsContent = """const canvas = document.getElementById('board');
const ctx = canvas.getContext('2d');
const scoreEl = document.getElementById('score');
const highScoreEl = document.getElementById('high-score');

const gridSize = 15;
const tileCount = canvas.width / gridSize;

let snake = [{x: 10, y: 10}];
let dx = 1;
let dy = 0;
let food = {x: 5, y: 5};
let score = 0;
let highScore = localStorage.getItem('snake_hi') || 0;
highScoreEl.innerText = highScore;

let gameInterval = null;

function setDir(newX, newY) {
  // Prevent reverse direction
  if (dx === -newX && newX !== 0) return;
  if (dy === -newY && newY !== 0) return;
  dx = newX;
  dy = newY;
}

function spawnFood() {
  food.x = Math.floor(Math.random() * tileCount);
  food.y = Math.floor(Math.random() * tileCount);
}

function gameTick() {
  const head = {x: snake[0].x + dx, y: snake[0].y + dy};

  // Wall collisions
  if (head.x < 0 || head.x >= tileCount || head.y < 0 || head.y >= tileCount) {
    return gameOver();
  }

  // Self collision
  for (let part of snake) {
    if (part.x === head.x && part.y === head.y) {
      return gameOver();
    }
  }

  snake.unshift(head);

  // Eat food
  if (head.x === food.x && head.y === food.y) {
    score += 10;
    scoreEl.innerText = score;
    spawnFood();
  } else {
    snake.pop();
  }

  render();
}

function gameOver() {
  clearInterval(gameInterval);
  if (score > highScore) {
    highScore = score;
    localStorage.setItem('snake_hi', highScore);
    highScoreEl.innerText = highScore;
  }
  alert("Game Over! Score: " + score);
  score = 0;
  scoreEl.innerText = score;
  snake = [{x: 10, y: 10}];
  dx = 1;
  dy = 0;
  spawnFood();
  startGame();
}

function render() {
  ctx.fillStyle = '#0f172a';
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  // Draw Food
  ctx.fillStyle = '#ef4444';
  ctx.beginPath();
  ctx.arc(food.x * gridSize + gridSize/2, food.y * gridSize + gridSize/2, gridSize/2 - 1, 0, Math.PI * 2);
  ctx.fill();

  // Draw Snake
  snake.forEach((part, index) => {
    ctx.fillStyle = index === 0 ? '#4ade80' : '#22c55e';
    ctx.fillRect(part.x * gridSize, part.y * gridSize, gridSize - 1, gridSize - 1);
  });
}

function startGame() {
  clearInterval(gameInterval);
  gameInterval = setInterval(gameTick, 140);
}

startGame();
console.log("Retro Snake initialized.");"""
        ),

        // 3. Modern Portfolio
        TemplateProject(
            title = "Modern Developer Portfolio",
            description = "Sleek responsive portfolio with dark theme, animated skill tags, interactive project showcases, and contact modal.",
            type = ProjectType.WEBSITE,
            iconName = "person",
            htmlContent = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Alex Rivera — Web & Game Engineer</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <nav class="navbar">
    <div class="logo">&lt;Alex.dev/&gt;</div>
    <ul class="nav-links">
      <li><a href="#about">About</a></li>
      <li><a href="#projects">Work</a></li>
      <li><a href="#contact">Contact</a></li>
    </ul>
  </nav>

  <header class="hero" id="about">
    <div class="avatar-ring">💻</div>
    <h1>Alex Rivera</h1>
    <p class="tagline">Creative Web Developer & Game Enthusiast</p>
    <div class="badges">
      <span class="badge">HTML5</span>
      <span class="badge">CSS3</span>
      <span class="badge">JavaScript</span>
      <span class="badge">Canvas 2D</span>
    </div>
  </header>

  <section class="section" id="projects">
    <h2>Featured Creations</h2>
    <div class="grid">
      <div class="card">
        <h3>🚀 Orbit Runner</h3>
        <p>A fast-paced 2D canvas endless runner with dynamic physics and custom procedural track generation.</p>
        <button class="btn" onclick="openDemo('Orbit Runner')">View Project</button>
      </div>
      <div class="card">
        <h3>🎨 Chroma Palette</h3>
        <p>Real-time color scheme generator and contrast accessibility validator built with pure modern JavaScript.</p>
        <button class="btn" onclick="openDemo('Chroma Palette')">View Project</button>
      </div>
      <div class="card">
        <h3>⚡ Astro Dashboard</h3>
        <p>Live responsive developer telemetry dashboard featuring modular SVG charts and local storage sync.</p>
        <button class="btn" onclick="openDemo('Astro Dashboard')">View Project</button>
      </div>
    </div>
  </section>

  <footer class="footer" id="contact">
    <p>Let's build something awesome together.</p>
    <p class="copy">© 2026 Alex Rivera. Built with HTML Live.</p>
  </footer>

  <script src="script.js"></script>
</body>
</html>""",
            cssContent = """* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}
body {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  background: #090d16;
  color: #e2e8f0;
  line-height: 1.6;
}
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.5rem;
  background: rgba(15, 23, 42, 0.85);
  backdrop-filter: blur(10px);
  position: sticky;
  top: 0;
  z-index: 100;
  border-bottom: 1px solid #1e293b;
}
.logo {
  font-weight: 800;
  color: #38bdf8;
  font-size: 1.1rem;
}
.nav-links {
  display: flex;
  list-style: none;
  gap: 1.2rem;
}
.nav-links a {
  color: #94a3b8;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}
.nav-links a:hover {
  color: #38bdf8;
}
.hero {
  text-align: center;
  padding: 3rem 1.5rem;
  background: radial-gradient(circle at 50% 20%, rgba(56, 189, 248, 0.12), transparent 70%);
}
.avatar-ring {
  font-size: 3rem;
  display: inline-block;
  padding: 1rem;
  background: #1e293b;
  border-radius: 50%;
  border: 2px solid #38bdf8;
  margin-bottom: 1rem;
  box-shadow: 0 0 25px rgba(56, 189, 248, 0.25);
}
.hero h1 {
  font-size: 2.2rem;
  color: #f8fafc;
  margin-bottom: 0.5rem;
}
.tagline {
  color: #94a3b8;
  font-size: 1.1rem;
  margin-bottom: 1.5rem;
}
.badges {
  display: flex;
  justify-content: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}
.badge {
  background: #1e293b;
  color: #38bdf8;
  padding: 0.35rem 0.8rem;
  border-radius: 9999px;
  font-size: 0.85rem;
  border: 1px solid #334155;
}
.section {
  padding: 2.5rem 1.5rem;
  max-width: 900px;
  margin: 0 auto;
}
.section h2 {
  font-size: 1.6rem;
  margin-bottom: 1.5rem;
  text-align: center;
  color: #f1f5f9;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1.2rem;
}
.card {
  background: #111827;
  padding: 1.5rem;
  border-radius: 12px;
  border: 1px solid #1f2937;
  transition: transform 0.2s, border-color 0.2s;
}
.card:hover {
  transform: translateY(-4px);
  border-color: #38bdf8;
}
.card h3 {
  color: #38bdf8;
  margin-bottom: 0.6rem;
}
.card p {
  color: #9ca3af;
  font-size: 0.9rem;
  margin-bottom: 1.2rem;
}
.btn {
  background: #0284c7;
  color: #fff;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}
.btn:hover {
  background: #0369a1;
}
.footer {
  text-align: center;
  padding: 2.5rem 1rem;
  border-top: 1px solid #1e293b;
  color: #64748b;
  font-size: 0.9rem;
}
.copy {
  margin-top: 0.5rem;
  font-size: 0.8rem;
}""",
            jsContent = """function openDemo(projectName) {
  console.log("Exploring project: " + projectName);
  alert("Opening showcase for: " + projectName + "\\n\\nThis project is built using native HTML, CSS, and JavaScript!");
}

console.log("Alex Rivera portfolio script loaded.");"""
        ),

        // 4. Interactive Calculator
        TemplateProject(
            title = "Interactive Glassmorphism Calculator",
            description = "Real functional web calculator with keypad, decimal arithmetic, history log, and responsive layout.",
            type = ProjectType.APP,
            iconName = "calculate",
            htmlContent = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Calculator</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="calc-wrapper">
    <div class="display">
      <div id="history"></div>
      <div id="current">0</div>
    </div>
    <div class="keypad">
      <button class="btn fn" onclick="clearAll()">AC</button>
      <button class="btn fn" onclick="deleteChar()">⌫</button>
      <button class="btn fn" onclick="appendOp('%')">%</button>
      <button class="btn op" onclick="appendOp('/')">÷</button>

      <button class="btn num" onclick="appendNum('7')">7</button>
      <button class="btn num" onclick="appendNum('8')">8</button>
      <button class="btn num" onclick="appendNum('9')">9</button>
      <button class="btn op" onclick="appendOp('*')">×</button>

      <button class="btn num" onclick="appendNum('4')">4</button>
      <button class="btn num" onclick="appendNum('5')">5</button>
      <button class="btn num" onclick="appendNum('6')">6</button>
      <button class="btn op" onclick="appendOp('-')">−</button>

      <button class="btn num" onclick="appendNum('1')">1</button>
      <button class="btn num" onclick="appendNum('2')">2</button>
      <button class="btn num" onclick="appendNum('3')">3</button>
      <button class="btn op" onclick="appendOp('+')">+</button>

      <button class="btn num zero" onclick="appendNum('0')">0</button>
      <button class="btn num" onclick="appendDot()">.</button>
      <button class="btn eq" onclick="calculate()">=</button>
    </div>
  </div>
  <script src="script.js"></script>
</body>
</html>""",
            cssContent = """body {
  margin: 0;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: radial-gradient(circle at center, #1e1b4b, #0f172a);
  font-family: -apple-system, sans-serif;
  color: #fff;
}
.calc-wrapper {
  background: rgba(30, 41, 59, 0.7);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 24px;
  padding: 20px;
  width: 320px;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.5);
}
.display {
  background: rgba(15, 23, 42, 0.8);
  border-radius: 16px;
  padding: 16px;
  text-align: right;
  margin-bottom: 20px;
  min-height: 80px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
#history {
  font-size: 14px;
  color: #94a3b8;
  height: 20px;
}
#current {
  font-size: 32px;
  font-weight: 600;
  color: #38bdf8;
  overflow: hidden;
}
.keypad {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}
.btn {
  border: none;
  font-size: 20px;
  font-weight: 500;
  height: 55px;
  border-radius: 14px;
  cursor: pointer;
  transition: transform 0.1s, opacity 0.2s;
}
.btn:active {
  transform: scale(0.95);
  opacity: 0.8;
}
.num {
  background: #334155;
  color: #f8fafc;
}
.fn {
  background: #475569;
  color: #cbd5e1;
}
.op {
  background: #3b82f6;
  color: white;
}
.eq {
  background: #10b981;
  color: white;
  grid-column: span 1;
}
.zero {
  grid-column: span 2;
}""",
            jsContent = """let currentVal = '0';
let historyVal = '';
let shouldReset = false;

const currentEl = document.getElementById('current');
const historyEl = document.getElementById('history');

function updateDisplay() {
  currentEl.innerText = currentVal;
  historyEl.innerText = historyVal;
}

function appendNum(num) {
  if (currentVal === '0' || shouldReset) {
    currentVal = num;
    shouldReset = false;
  } else {
    currentVal += num;
  }
  updateDisplay();
}

function appendDot() {
  if (!currentVal.includes('.')) {
    currentVal += '.';
    updateDisplay();
  }
}

function appendOp(op) {
  historyVal = currentVal + ' ' + op;
  shouldReset = true;
  updateDisplay();
}

function clearAll() {
  currentVal = '0';
  historyVal = '';
  updateDisplay();
}

function deleteChar() {
  if (currentVal.length > 1) {
    currentVal = currentVal.slice(0, -1);
  } else {
    currentVal = '0';
  }
  updateDisplay();
}

function calculate() {
  if (!historyVal) return;
  const parts = historyVal.split(' ');
  const prev = parseFloat(parts[0]);
  const op = parts[1];
  const cur = parseFloat(currentVal);

  let res = 0;
  switch(op) {
    case '+': res = prev + cur; break;
    case '-': res = prev - cur; break;
    case '*': res = prev * cur; break;
    case '/': res = cur !== 0 ? prev / cur : 'Error'; break;
    case '%': res = prev % cur; break;
  }
  historyVal = historyVal + ' ' + currentVal + ' =';
  currentVal = String(res);
  shouldReset = true;
  updateDisplay();
  console.log("Calculated result: " + res);
}

console.log("Calculator app initialized.");"""
        ),

        // 5. To-Do Application with LocalStorage
        TemplateProject(
            title = "TaskFlow To-Do List",
            description = "Persistent task management app with category badges, filter tabs, completion toggles, and localStorage persistence.",
            type = ProjectType.APP,
            iconName = "check_circle",
            htmlContent = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>TaskFlow</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="app-card">
    <header>
      <h1>TaskFlow</h1>
      <p>Stay organized effortlessly</p>
    </header>

    <div class="input-row">
      <input type="text" id="task-input" placeholder="What needs to be done?">
      <button id="add-btn" onclick="addTask()">Add</button>
    </div>

    <div class="filters">
      <button class="filter-tab active" onclick="setFilter('all')">All</button>
      <button class="filter-tab" onclick="setFilter('active')">Active</button>
      <button class="filter-tab" onclick="setFilter('completed')">Completed</button>
    </div>

    <ul id="task-list"></ul>

    <div class="footer-info">
      <span id="counter">0 tasks remaining</span>
      <button class="clear-btn" onclick="clearCompleted()">Clear Done</button>
    </div>
  </div>
  <script src="script.js"></script>
</body>
</html>""",
            cssContent = """body {
  background: #0f172a;
  color: #e2e8f0;
  font-family: system-ui, sans-serif;
  margin: 0;
  padding: 20px;
  display: flex;
  justify-content: center;
}
.app-card {
  background: #1e293b;
  width: 100%;
  max-width: 440px;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.4);
}
header h1 {
  margin: 0 0 4px;
  color: #38bdf8;
  font-size: 24px;
}
header p {
  margin: 0 0 16px;
  color: #94a3b8;
  font-size: 14px;
}
.input-row {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
input[type="text"] {
  flex: 1;
  padding: 12px 14px;
  border-radius: 8px;
  border: 1px solid #334155;
  background: #0f172a;
  color: #fff;
  font-size: 15px;
}
input[type="text"]:focus {
  outline: 2px solid #38bdf8;
}
#add-btn {
  background: #38bdf8;
  color: #0f172a;
  border: none;
  padding: 0 20px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
}
.filters {
  display: flex;
  gap: 8px;
  margin-bottom: 14px;
}
.filter-tab {
  background: transparent;
  border: 1px solid #334155;
  color: #94a3b8;
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}
.filter-tab.active {
  background: #38bdf8;
  color: #0f172a;
  border-color: #38bdf8;
  font-weight: bold;
}
#task-list {
  list-style: none;
  padding: 0;
  margin: 0 0 16px;
  max-height: 320px;
  overflow-y: auto;
}
.task-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: #0f172a;
  margin-bottom: 8px;
  border-radius: 8px;
  border: 1px solid #334155;
}
.task-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.task-text.done {
  text-decoration: line-through;
  color: #64748b;
}
.delete-btn {
  background: transparent;
  border: none;
  color: #ef4444;
  cursor: pointer;
  font-size: 16px;
}
.footer-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;
  color: #94a3b8;
}
.clear-btn {
  background: transparent;
  border: none;
  color: #cbd5e1;
  text-decoration: underline;
  cursor: pointer;
}""",
            jsContent = """let tasks = JSON.parse(localStorage.getItem('taskflow_items') || '[]');
let currentFilter = 'all';

function saveTasks() {
  localStorage.setItem('taskflow_items', JSON.stringify(tasks));
  renderTasks();
}

function addTask() {
  const input = document.getElementById('task-input');
  const text = input.value.trim();
  if (!text) return;
  tasks.push({ id: Date.now(), text, completed: false });
  input.value = '';
  saveTasks();
  console.log("Added task: " + text);
}

function toggleTask(id) {
  tasks = tasks.map(t => t.id === id ? { ...t, completed: !t.completed } : t);
  saveTasks();
}

function deleteTask(id) {
  tasks = tasks.filter(t => t.id !== id);
  saveTasks();
}

function setFilter(f) {
  currentFilter = f;
  document.querySelectorAll('.filter-tab').forEach(tab => {
    tab.classList.toggle('active', tab.innerText.toLowerCase() === f);
  });
  renderTasks();
}

function clearCompleted() {
  tasks = tasks.filter(t => !t.completed);
  saveTasks();
}

function renderTasks() {
  const list = document.getElementById('task-list');
  list.innerHTML = '';

  const filtered = tasks.filter(t => {
    if (currentFilter === 'active') return !t.completed;
    if (currentFilter === 'completed') return t.completed;
    return true;
  });

  filtered.forEach(t => {
    const li = document.createElement('li');
    li.className = 'task-item';
    li.innerHTML = '<div class="task-left">' +
      '<input type="checkbox" ' + (t.completed ? 'checked' : '') + ' onchange="toggleTask(' + t.id + ')">' +
      '<span class="task-text ' + (t.completed ? 'done' : '') + '">' + t.text + '</span>' +
      '</div>' +
      '<button class="delete-btn" onclick="deleteTask(' + t.id + ')">✕</button>';
    list.appendChild(li);
  });

  const remaining = tasks.filter(t => !t.completed).length;
  document.getElementById('counter').innerText = remaining + " tasks remaining";
}

// Initial render
renderTasks();
console.log("TaskFlow loaded with " + tasks.length + " tasks.");"""
        ),

        // 6. Interactive Canvas Particle Art
        TemplateProject(
            title = "Interactive Canvas Particle Art",
            description = "Touch-responsive generative particle fireworks simulation with physics, color transitions, and gravity.",
            type = ProjectType.CANVAS,
            iconName = "auto_awesome",
            htmlContent = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
  <title>Canvas Particle Art</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="overlay">
    <h2>Touch or Drag Screen</h2>
    <p>Particles: <span id="count">0</span></p>
  </div>
  <canvas id="canvas"></canvas>
  <script src="script.js"></script>
</body>
</html>""",
            cssContent = """body {
  margin: 0;
  overflow: hidden;
  background: #000;
  font-family: sans-serif;
}
.overlay {
  position: absolute;
  top: 16px;
  left: 16px;
  color: #fff;
  pointer-events: none;
  z-index: 10;
}
.overlay h2 {
  font-size: 18px;
  margin: 0;
  color: #38bdf8;
}
.overlay p {
  font-size: 13px;
  margin: 4px 0 0;
  color: #94a3b8;
}
canvas {
  display: block;
}""",
            jsContent = """const canvas = document.getElementById('canvas');
const ctx = canvas.getContext('2d');
const countEl = document.getElementById('count');

function resize() {
  canvas.width = window.innerWidth;
  canvas.height = window.innerHeight;
}
window.addEventListener('resize', resize);
resize();

const particles = [];
let hue = 0;

class Particle {
  constructor(x, y) {
    this.x = x;
    this.y = y;
    this.size = Math.random() * 8 + 3;
    this.speedX = Math.random() * 6 - 3;
    this.speedY = Math.random() * 6 - 3;
    this.color = 'hsl(' + hue + ', 100%, 55%)';
    this.decay = 0.96;
  }
  update() {
    this.x += this.speedX;
    this.y += this.speedY;
    this.size *= this.decay;
  }
  draw() {
    ctx.fillStyle = this.color;
    ctx.beginPath();
    ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
    ctx.fill();
  }
}

function addParticles(x, y, amount = 8) {
  for (let i = 0; i < amount; i++) {
    particles.push(new Particle(x, y));
  }
  hue += 4;
}

canvas.addEventListener('touchmove', (e) => {
  for (let touch of e.touches) {
    addParticles(touch.clientX, touch.clientY);
  }
});
canvas.addEventListener('mousemove', (e) => {
  addParticles(e.clientX, e.clientY, 3);
});

function animate() {
  ctx.fillStyle = 'rgba(0, 0, 0, 0.15)';
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  for (let i = particles.length - 1; i >= 0; i--) {
    particles[i].update();
    particles[i].draw();
    if (particles[i].size < 0.5) {
      particles.splice(i, 1);
    }
  }

  countEl.innerText = particles.length;
  requestAnimationFrame(animate);
}

// Seed initial particles
for (let i = 0; i < 30; i++) {
  addParticles(canvas.width / 2, canvas.height / 2, 1);
}

animate();
console.log("Canvas particle simulation active.");"""
        ),

        // 7. Blank Starter Template
        TemplateProject(
            title = "Clean HTML5 Starter",
            description = "Minimalist boilerplate with index.html, style.css, and script.js ready for your custom creation.",
            type = ProjectType.EMPTY,
            iconName = "code",
            htmlContent = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>New Project</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="welcome-box">
    <h1>Hello, World! 🚀</h1>
    <p>Your HTML Live project is running successfully.</p>
    <button id="action-btn">Click Me</button>
    <div id="output"></div>
  </div>
  <script src="script.js"></script>
</body>
</html>""",
            cssContent = """body {
  margin: 0;
  padding: 0;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #0f172a;
  color: #f8fafc;
  font-family: system-ui, -apple-system, sans-serif;
}

.welcome-box {
  text-align: center;
  padding: 2rem;
  background: #1e293b;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.4);
  max-width: 380px;
}

h1 {
  color: #38bdf8;
  margin-bottom: 0.5rem;
}

button {
  background: #0284c7;
  color: #fff;
  border: none;
  padding: 10px 20px;
  font-size: 16px;
  border-radius: 8px;
  cursor: pointer;
  margin-top: 1rem;
}

button:active {
  background: #0369a1;
}

#output {
  margin-top: 1rem;
  font-weight: bold;
  color: #4ade80;
}""",
            jsContent = """// HTML Live JavaScript
const btn = document.getElementById('action-btn');
const output = document.getElementById('output');
let count = 0;

btn.addEventListener('click', () => {
  count++;
  output.innerText = 'Button clicked ' + count + ' times!';
  console.log('Action triggered. Count is now: ' + count);
});

console.log('Clean Starter initialized. Ready to build!');"""
        )
    )
}
