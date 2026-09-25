package com.example.data.repository

import com.example.model.CourseCategory
import com.example.model.Lesson
import com.example.model.TutorialModule

object LearningRepository {

    val modules: List<TutorialModule> = listOf(
        TutorialModule(
            id = "mod_html_foundation",
            category = CourseCategory.HTML,
            title = "HTML5 Semantic Foundation",
            description = "Master doctype, document hierarchy, paragraphs, headings, and links from the ground up.",
            level = "Beginner",
            estimatedMinutes = 20,
            sizeKb = 110,
            isDownloaded = true,
            downloadDate = "Pre-installed Offline",
            lessonIds = listOf("html_1", "html_2", "html_3"),
            tags = listOf("HTML", "Semantics", "Typography", "Links")
        ),
        TutorialModule(
            id = "mod_html_interactive",
            category = CourseCategory.HTML,
            title = "Responsive Media & Forms",
            description = "Build accessible image containers, input controls, form validation, and interactive buttons.",
            level = "Intermediate",
            estimatedMinutes = 18,
            sizeKb = 95,
            isDownloaded = true,
            downloadDate = "Pre-installed Offline",
            lessonIds = listOf("html_4", "html_5"),
            tags = listOf("HTML", "Forms", "Images", "Accessibility")
        ),
        TutorialModule(
            id = "mod_css_box_model",
            category = CourseCategory.CSS,
            title = "CSS Box Model & Colors",
            description = "Master content, padding, borders, margins, box-sizing, and modern color palettes.",
            level = "Beginner",
            estimatedMinutes = 16,
            sizeKb = 105,
            isDownloaded = true,
            downloadDate = "Pre-installed Offline",
            lessonIds = listOf("css_1", "css_box_decor"),
            tags = listOf("CSS", "BoxModel", "Styling", "Borders")
        ),
        TutorialModule(
            id = "mod_css_flex_grid",
            category = CourseCategory.CSS,
            title = "Flexbox & Responsive Grid",
            description = "Create flexible multi-column layouts, mobile-friendly headers, and clean responsive grids.",
            level = "Intermediate",
            estimatedMinutes = 24,
            sizeKb = 130,
            isDownloaded = true,
            downloadDate = "Pre-installed Offline",
            lessonIds = listOf("css_2", "css_grid"),
            tags = listOf("CSS", "Flexbox", "Grid", "Responsive")
        ),
        TutorialModule(
            id = "mod_js_basics",
            category = CourseCategory.JAVASCRIPT,
            title = "JavaScript Logic & State",
            description = "Variables, data types, arithmetic, conditional branches, and arrow functions.",
            level = "Beginner",
            estimatedMinutes = 22,
            sizeKb = 120,
            isDownloaded = true,
            downloadDate = "Pre-installed Offline",
            lessonIds = listOf("js_1", "js_functions"),
            tags = listOf("JavaScript", "ES6", "Variables", "Functions")
        ),
        TutorialModule(
            id = "mod_js_dom",
            category = CourseCategory.JAVASCRIPT,
            title = "DOM Events & Interactivity",
            description = "Query selectors, click events, dynamic element creation, and real-time UI updates.",
            level = "Intermediate",
            estimatedMinutes = 20,
            sizeKb = 125,
            isDownloaded = true,
            downloadDate = "Pre-installed Offline",
            lessonIds = listOf("js_dom_events", "js_async"),
            tags = listOf("JavaScript", "DOM", "Events", "Async")
        ),
        TutorialModule(
            id = "mod_game_arcade",
            category = CourseCategory.GAME_DEV,
            title = "2D Canvas Arcade Studio",
            description = "Frame animations, physics vectors, player controls, and bouncing arcade mechanics.",
            level = "Advanced",
            estimatedMinutes = 30,
            sizeKb = 145,
            isDownloaded = true,
            downloadDate = "Pre-installed Offline",
            lessonIds = listOf("game_1", "game_paddle"),
            tags = listOf("Canvas", "GameDev", "Physics", "Arcade")
        ),
        TutorialModule(
            id = "mod_web_projects",
            category = CourseCategory.WEB_PROJECTS,
            title = "Full Web App Projects",
            description = "Assemble end-to-end interactive applications like calculators and personal dashboards.",
            level = "Intermediate",
            estimatedMinutes = 25,
            sizeKb = 150,
            isDownloaded = true,
            downloadDate = "Pre-installed Offline",
            lessonIds = listOf("project_calc", "project_todo"),
            tags = listOf("Projects", "MiniApps", "Portfolio")
        )
    )

    val lessons: List<Lesson> = listOf(
        // HTML Level 1 - Absolute Beginner
        Lesson(
            id = "html_1",
            category = CourseCategory.HTML,
            order = 1,
            title = "1. Document Structure & Doctype",
            level = "Beginner",
            durationMinutes = 6,
            summary = "Understand the fundamental anatomy of every webpage, the DOCTYPE declaration, and <html>, <head>, and <body> tags.",
            theoryMarkdown = """### What is HTML?
HTML stands for **HyperText Markup Language**. It provides the semantic skeleton and structure for every website on the Internet.

Every valid HTML document starts with:
- `<!DOCTYPE html>`: Tells the browser this is modern HTML5.
- `<html lang="en">`: The root container of the page.
- `<head>`: Holds metadata, document title, and links to stylesheets.
- `<body>`: Contains all visible elements (headings, paragraphs, buttons, canvases).""",
            defaultHtml = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>My First Webpage</title>
</head>
<body>
  <h1>Welcome to HTML Live!</h1>
  <p>This is the start of your web development journey.</p>
</body>
</html>""",
            defaultCss = "body { font-family: sans-serif; padding: 20px; background: #0f172a; color: #fff; }",
            defaultJs = "console.log('Document loaded successfully!');",
            commonMistakes = listOf(
                "Forgetting to close tags (e.g., writing <p> without </p>).",
                "Putting visible user content inside the <head> tag instead of the <body>."
            ),
            challengeQuestion = "Add an <h2> subtitle underneath the <h1> tag that says 'Coding directly on Android'.",
            challengeHint = "Use the <h2>...</h2> tag right below the <h1> heading.",
            moduleId = "mod_html_foundation"
        ),

        Lesson(
            id = "html_2",
            category = CourseCategory.HTML,
            order = 2,
            title = "2. Typography & Text Hierarchy",
            level = "Beginner",
            durationMinutes = 8,
            summary = "Master semantic text formatting with h1-h6 headings, paragraphs, bold, emphasis, and inline spans.",
            theoryMarkdown = """### Hierarchy & Readability
Headings establish an outline for screen readers, search engines, and visitors.
- `<h1>` to `<h6>`: Headings ranging from most important (`h1`) to least (`h6`).
- `<p>`: Standard paragraph block.
- `<strong>`: Bold text indicating strong importance.
- `<em>`: Emphasized italicized text.
- `<span>`: An inline container for targeted CSS styling.""",
            defaultHtml = """<h1>The Ultimate Guide to Coding</h1>
<h2>Chapter 1: Getting Started</h2>
<p>Learning web development is <strong>empowering</strong> and <em>fun</em>.</p>
<p>With <span style="color: #38bdf8; font-weight: bold;">HTML Live</span>, you can build anywhere!</p>""",
            defaultCss = "body { font-family: sans-serif; padding: 20px; line-height: 1.6; background: #1e293b; color: #e2e8f0; }",
            defaultJs = "console.log('Typography lesson initialized');",
            commonMistakes = listOf(
                "Using multiple <h1> tags on a single page (best practice is only one per page).",
                "Using headings just to make text larger instead of styling with CSS."
            ),
            challengeQuestion = "Create an <h3> heading called 'Section 1.1' followed by a paragraph with <strong>bold</strong> text.",
            challengeHint = "Wrap your heading in <h3></h3> and text in <p><strong>...</strong></p>.",
            moduleId = "mod_html_foundation"
        ),

        Lesson(
            id = "html_3",
            category = CourseCategory.HTML,
            order = 3,
            title = "3. Hyperlinks & Navigation",
            level = "Beginner",
            durationMinutes = 7,
            summary = "Connect web documents using anchor <a> tags, href attributes, and target specifications.",
            theoryMarkdown = """### The Power of Hyperlinks
Hyperlinks connect the world wide web.
The `<a>` (anchor) element defines a link.
Key attributes:
- `href`: Specifies the target destination URL or anchor `#id`.
- `target="_blank"`: Opens the link in a new tab/window.
- `rel="noopener noreferrer"`: Security best practice for external links.""",
            defaultHtml = """<h2>Helpful Web Resources</h2>
<p>Check out these vital developer destinations:</p>
<ul>
  <li><a href="https://developer.mozilla.org" target="_blank">MDN Web Docs</a></li>
  <li><a href="#footer">Jump to Footer</a></li>
</ul>
<div style="height: 300px;"></div>
<p id="footer">You have reached the bottom anchor!</p>""",
            defaultCss = "body { font-family: sans-serif; padding: 20px; background: #0f172a; color: #fff; } a { color: #38bdf8; }",
            defaultJs = "console.log('Navigation ready');",
            commonMistakes = listOf(
                "Leaving the href attribute empty or omitting it entirely.",
                "Typing herf instead of href."
            ),
            challengeQuestion = "Add a link pointing to 'https://google.com' with the text 'Search the Web'.",
            challengeHint = "<a href='https://google.com'>Search the Web</a>",
            moduleId = "mod_html_foundation"
        ),

        Lesson(
            id = "html_4",
            category = CourseCategory.HTML,
            order = 4,
            title = "4. Images & Responsive Media",
            level = "Beginner",
            durationMinutes = 8,
            summary = "Embed images properly with src, alt attributes for accessibility, width, height, and responsive wrappers.",
            theoryMarkdown = """### Images on the Web
The `<img>` tag is self-closing (void element).
Attributes:
- `src`: The image file path or URL.
- `alt`: Alternate descriptive text for screen readers and when images fail to load.
- `width` & `height`: Intrinsic aspect ratio hints to eliminate Cumulative Layout Shift (CLS).""",
            defaultHtml = """<h2>Dynamic Web Images</h2>
<img src="https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=500&q=80" 
     alt="Laptop showing lines of code" 
     class="responsive-img">
<p class="caption">A professional web developer workstation.</p>""",
            defaultCss = "body { font-family: sans-serif; padding: 20px; background: #0f172a; color: #fff; } .responsive-img { max-width: 100%; height: auto; border-radius: 12px; } .caption { font-size: 13px; color: #94a3b8; }",
            defaultJs = "console.log('Image element rendered');",
            commonMistakes = listOf(
                "Leaving alt empty or writing useless descriptions like 'image'.",
                "Forgetting responsive CSS (like max-width: 100%) causing images to overflow on mobile."
            ),
            challengeQuestion = "Add a second image with an alt attribute describing what it displays.",
            challengeHint = "Use <img src='...' alt='Description'>.",
            moduleId = "mod_html_interactive"
        ),

        Lesson(
            id = "html_5",
            category = CourseCategory.HTML,
            order = 5,
            title = "5. Forms, Inputs & User Interactivity",
            level = "Intermediate",
            durationMinutes = 10,
            summary = "Gather input using <form>, <input>, <textarea>, <select>, and <button> elements.",
            theoryMarkdown = """### Interactive Forms
Forms are how users submit data, search, log in, and customize settings.
Essential Elements:
- `<form>`: The wrapper with submit handling.
- `<input type="text | email | password | checkbox | radio | number">`: Diverse input controls.
- `<label for="id">`: Associates text with an input for accessible tapping.
- `<button type="submit">`: Triggers form validation and submission.""",
            defaultHtml = """<form id="signup-form">
  <h2>Create Account</h2>
  <label for="username">Username:</label><br>
  <input type="text" id="username" required placeholder="coder123"><br><br>
  
  <label for="email">Email Address:</label><br>
  <input type="email" id="email" required placeholder="you@domain.com"><br><br>
  
  <button type="submit">Register Now</button>
</form>
<p id="msg"></p>""",
            defaultCss = """body { font-family: sans-serif; padding: 20px; background: #0f172a; color: #fff; }
input { padding: 8px 12px; width: 100%; max-width: 280px; border-radius: 6px; border: 1px solid #334155; background: #1e293b; color: #fff; }
button { background: #38bdf8; color: #000; border: none; padding: 10px 20px; border-radius: 6px; font-weight: bold; cursor: pointer; }""",
            defaultJs = """document.getElementById('signup-form').addEventListener('submit', (e) => {
  e.preventDefault();
  const u = document.getElementById('username').value;
  document.getElementById('msg').innerText = 'Welcome aboard, ' + u + '!';
  console.log('Form submitted for user: ' + u);
});""",
            commonMistakes = listOf(
                "Omitting <label> elements, making mobile touch targets difficult to activate.",
                "Not preventing default form submission in JavaScript (e.preventDefault()), causing unwanted reloads."
            ),
            challengeQuestion = "Add a password input field with type='password' and required attribute.",
            challengeHint = "<input type='password' id='pwd' required>",
            moduleId = "mod_html_interactive"
        ),

        // CSS Course
        Lesson(
            id = "css_1",
            category = CourseCategory.CSS,
            order = 1,
            title = "1. CSS Box Model Mastery",
            level = "Beginner",
            durationMinutes = 8,
            summary = "Understand Content, Padding, Border, Margin, and why box-sizing: border-box is essential.",
            theoryMarkdown = """### The Box Model
Every element in CSS is a rectangular box made of four distinct layers:
1. **Content**: The text, image, or child element.
2. **Padding**: Transparent inner space surrounding the content.
3. **Border**: The outline surrounding padding.
4. **Margin**: Transparent outer space pushing adjacent elements away.

**Pro-tip**: Always set `box-sizing: border-box;` so padding doesn't inflate your width!""",
            defaultHtml = """<div class="box box-content">Content Box</div>
<div class="box box-border">Border-Box (Modern Standard)</div>""",
            defaultCss = """body { font-family: sans-serif; padding: 20px; background: #0f172a; color: #fff; }
.box {
  width: 200px;
  height: 80px;
  padding: 20px;
  border: 4px solid #38bdf8;
  margin-bottom: 20px;
  background: #1e293b;
}
.box-content {
  box-sizing: content-box; /* Width becomes 200 + 40 + 8 = 248px */
}
.box-border {
  box-sizing: border-box; /* Total width stays exactly 200px! */
}""",
            defaultJs = "console.log('Box model demonstrated');",
            commonMistakes = listOf(
                "Confusing margin (outside space) with padding (inside space).",
                "Allowing borders and padding to break grid layouts due to missing box-sizing: border-box."
            ),
            challengeQuestion = "Change the margin of .box to 30px and set border-radius to 12px.",
            challengeHint = "Add 'margin: 30px;' and 'border-radius: 12px;' inside the .box selector.",
            moduleId = "mod_css_box_model"
        ),

        Lesson(
            id = "css_box_decor",
            category = CourseCategory.CSS,
            order = 2,
            title = "2. Shadows, Gradients & Borders",
            level = "Beginner",
            durationMinutes = 8,
            summary = "Elevate flat designs with box-shadows, linear gradients, and rounded border radii.",
            theoryMarkdown = """### Modern CSS Aesthetics
Polished UIs rely on subtle depth cues:
- `box-shadow`: Adds elevation (x-offset, y-offset, blur-radius, spread, color).
- `linear-gradient()`: Smooth color transitions.
- `backdrop-filter: blur(10px)`: Modern frosted glass effects.""",
            defaultHtml = """<div class="glow-card">
  <h3>Glassmorphism Card</h3>
  <p>Modern gradient aesthetics rendered live.</p>
</div>""",
            defaultCss = """body { font-family: sans-serif; padding: 24px; background: #020617; color: #fff; }
.glow-card {
  padding: 24px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(56, 189, 248, 0.2), rgba(99, 102, 241, 0.2));
  border: 1px solid rgba(255, 255, 255, 0.15);
  box-shadow: 0 10px 25px -5px rgba(56, 189, 248, 0.3);
}""",
            defaultJs = "console.log('Decorations ready');",
            commonMistakes = listOf(
                "Overusing intense, pitch-black shadows that look dated.",
                "Using hard edges where smooth border-radius creates better ergonomics."
            ),
            challengeQuestion = "Add a 4px blur-radius and change background gradient to emerald green.",
            challengeHint = "Use linear-gradient(135deg, #10b981, #047857)",
            moduleId = "mod_css_box_model"
        ),

        Lesson(
            id = "css_2",
            category = CourseCategory.CSS,
            order = 3,
            title = "3. Flexbox: Modern 1D Layouts",
            level = "Intermediate",
            durationMinutes = 12,
            summary = "Build flexible, responsive layouts effortlessly with display: flex, justify-content, align-items, and gap.",
            theoryMarkdown = """### Flexbox Fundamentals
Flexbox arranges items along a main axis (row or column).
Key Container Properties:
- `display: flex;`: Activates flexbox.
- `justify-content`: Positions items along main axis (`center`, `space-between`, `flex-start`).
- `align-items`: Aligns items along cross axis (`center`, `stretch`).
- `gap`: Space between children without messy margins!""",
            defaultHtml = """<div class="flex-container">
  <div class="card">Item 1</div>
  <div class="card">Item 2</div>
  <div class="card">Item 3</div>
</div>""",
            defaultCss = """.flex-container {
  display: flex;
  justify-content: space-around;
  align-items: center;
  gap: 12px;
  background: #1e293b;
  padding: 20px;
  border-radius: 8px;
}
.card {
  background: #3b82f6;
  color: white;
  padding: 16px 24px;
  border-radius: 6px;
  font-weight: bold;
}""",
            defaultJs = "console.log('Flexbox active');",
            commonMistakes = listOf(
                "Applying justify-content to children instead of the parent flex container.",
                "Using floats or inline-block for layouts instead of Flexbox."
            ),
            challengeQuestion = "Change justify-content to space-between and add a 4th card item.",
            challengeHint = "Set justify-content: space-between on .flex-container.",
            moduleId = "mod_css_flex_grid"
        ),

        Lesson(
            id = "css_grid",
            category = CourseCategory.CSS,
            order = 4,
            title = "4. CSS Grid: 2D Dashboard Systems",
            level = "Intermediate",
            durationMinutes = 12,
            summary = "Build complex columns and rows simultaneously using display: grid and grid-template-columns: repeat(auto-fit, minmax()).",
            theoryMarkdown = """### The Power of CSS Grid
While Flexbox is 1-dimensional (row OR column), CSS Grid is 2-dimensional (rows AND columns).
Key syntax:
- `grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));`: Automatically adjusts column count based on available screen width without media queries!""",
            defaultHtml = """<div class="dashboard-grid">
  <div class="tile">Analytics</div>
  <div class="tile">Users</div>
  <div class="tile">Revenue</div>
  <div class="tile">Settings</div>
</div>""",
            defaultCss = """.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(100px, 1fr));
  gap: 12px;
  padding: 16px;
  background: #0f172a;
}
.tile {
  background: #1e293b;
  color: #38bdf8;
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  font-weight: bold;
}""",
            defaultJs = "console.log('Grid initialized');",
            commonMistakes = listOf(
                "Using fixed pixel widths on grid columns that overflow small mobile screens.",
                "Forgetting the gap property and adding manual margins on child tiles."
            ),
            challengeQuestion = "Change the minmax minimum to 80px and add 2 more dashboard tiles.",
            challengeHint = "Update minmax(80px, 1fr) in the grid-template-columns.",
            moduleId = "mod_css_flex_grid"
        ),

        // JavaScript Course
        Lesson(
            id = "js_1",
            category = CourseCategory.JAVASCRIPT,
            order = 1,
            title = "1. Variables, Scope & Data Types",
            level = "Beginner",
            durationMinutes = 10,
            summary = "Master modern JavaScript declarations (const, let), primitive types, and block scoping.",
            theoryMarkdown = """### Modern Declarations
Always use `const` and `let` (never `var`):
- `const`: For values that should not be reassigned.
- `let`: For variables whose value changes over time (like counters).

Primitive Data Types:
- String: `"Hello, World!"`
- Number: `42`, `3.14`
- Boolean: `true`, `false`
- Null & Undefined: Absence of value.""",
            defaultHtml = """<h2>JavaScript Variables in Action</h2>
<p id="output">Calculating...</p>
<button id="inc-btn">Increment Counter</button>""",
            defaultCss = """body { font-family: sans-serif; padding: 20px; background: #0f172a; color: #fff; }
button { background: #10b981; color: white; border: none; padding: 10px 16px; border-radius: 6px; cursor: pointer; }""",
            defaultJs = """const appName = 'HTML Live';
let counter = 0;

function updateUI() {
  const output = document.getElementById('output');
  output.innerText = appName + ' count: ' + counter;
  console.log('Counter updated: ' + counter);
}

document.getElementById('inc-btn').addEventListener('click', () => {
  counter += 1;
  updateUI();
});

updateUI();""",
            commonMistakes = listOf(
                "Trying to reassign a const variable (causes TypeError: Assignment to constant variable).",
                "Using == instead of === (always use strict equality ===)."
            ),
            challengeQuestion = "Add a 'Reset' button that sets counter back to 0 when clicked.",
            challengeHint = "Add <button id='reset-btn'>Reset</button> and an event listener resetting counter = 0.",
            moduleId = "mod_js_basics"
        ),

        Lesson(
            id = "js_functions",
            category = CourseCategory.JAVASCRIPT,
            order = 2,
            title = "2. Functions & Arrow Syntax",
            level = "Beginner",
            durationMinutes = 12,
            summary = "Write clean, reusable logic with function declarations, parameters, return values, and modern arrow syntax.",
            theoryMarkdown = """### Modern Functions in JS
Functions package logic for reusability.
- Arrow function syntax: `const add = (a, b) => a + b;`
- Concise body returns implicitly.
- Default parameters: `const greet = (name = 'Friend') => ...`""",
            defaultHtml = """<h2>Dynamic Dice Roller</h2>
<p id="dice-result">Roll: -</p>
<button id="roll-btn">Roll 6-Sided Die</button>""",
            defaultCss = """body { font-family: sans-serif; padding: 20px; background: #0f172a; color: #fff; }
button { background: #38bdf8; color: #000; font-weight: bold; border: none; padding: 10px 18px; border-radius: 8px; cursor: pointer; }""",
            defaultJs = """const rollDie = (sides = 6) => Math.floor(Math.random() * sides) + 1;

document.getElementById('roll-btn').addEventListener('click', () => {
  const result = rollDie(6);
  document.getElementById('dice-result').innerText = 'Rolled: ' + result;
  console.log('Dice result: ' + result);
});""",
            commonMistakes = listOf(
                "Forgetting Math.floor() when generating random integers.",
                "Confusing function parameters with arguments."
            ),
            challengeQuestion = "Modify the rollDie call to support a 20-sided die (D20).",
            challengeHint = "Pass 20 into rollDie(20).",
            moduleId = "mod_js_basics"
        ),

        Lesson(
            id = "js_dom_events",
            category = CourseCategory.JAVASCRIPT,
            order = 3,
            title = "3. Dynamic DOM Manipulation & Events",
            level = "Intermediate",
            durationMinutes = 10,
            summary = "Select elements with document.querySelector, listen to input events, and dynamically toggle classes.",
            theoryMarkdown = """### DOM Manipulation
The Document Object Model (DOM) is the object tree representation of the webpage.
Core Methods:
- `document.querySelector('.class')`: Finds the first matching element.
- `element.addEventListener('click', callback)`: Attaches event listeners.
- `element.classList.toggle('active')`: Elegantly toggles CSS classes without rewriting inline styles.""",
            defaultHtml = """<h2>Live Text Mirror</h2>
<input type="text" id="live-input" placeholder="Type something cool...">
<p>Mirror: <span id="mirror-target" style="color: #38bdf8; font-weight: bold;"></span></p>""",
            defaultCss = """body { font-family: sans-serif; padding: 20px; background: #0f172a; color: #fff; }
input { padding: 10px 14px; width: 100%; max-width: 320px; background: #1e293b; color: #fff; border: 1px solid #334155; border-radius: 8px; }""",
            defaultJs = """const input = document.getElementById('live-input');
const mirror = document.getElementById('mirror-target');

input.addEventListener('input', (e) => {
  mirror.innerText = e.target.value || '(Empty)';
});""",
            commonMistakes = listOf(
                "Using 'change' event instead of 'input' event when real-time typing feedback is needed.",
                "Attaching multiple duplicate event listeners inside loops."
            ),
            challengeQuestion = "Make the mirror text turn red if the length of the string is greater than 10 characters.",
            challengeHint = "Check e.target.value.length > 10 and update mirror.style.color.",
            moduleId = "mod_js_dom"
        ),

        Lesson(
            id = "js_async",
            category = CourseCategory.JAVASCRIPT,
            order = 4,
            title = "4. Asynchronous Logic & Timers",
            level = "Intermediate",
            durationMinutes = 10,
            summary = "Master non-blocking asynchronous execution with setTimeout, setInterval, and Promise concepts.",
            theoryMarkdown = """### Non-Blocking Asynchronous Code
JavaScript is single-threaded, using an **Event Loop** to run async tasks:
- `setTimeout(fn, delayMs)`: Executes once after delay.
- `setInterval(fn, delayMs)`: Repeats execution continuously.
- `clearInterval(timerId)`: Halts recurring intervals.""",
            defaultHtml = """<h2>Digital Stopwatch</h2>
<h1 id="timer-display" style="font-size: 40px; color: #38bdf8; font-family: monospace;">00:00</h1>
<button id="start-btn">Start</button>
<button id="stop-btn">Stop</button>""",
            defaultCss = """body { font-family: sans-serif; padding: 20px; background: #0f172a; color: #fff; }
button { background: #334155; color: #fff; padding: 8px 16px; border: none; border-radius: 6px; margin-right: 8px; cursor: pointer; }""",
            defaultJs = """let seconds = 0;
let timer = null;

const display = document.getElementById('timer-display');

document.getElementById('start-btn').addEventListener('click', () => {
  if (timer) return;
  timer = setInterval(() => {
    seconds++;
    display.innerText = '00:' + (seconds < 10 ? '0' : '') + seconds;
  }, 1000);
});

document.getElementById('stop-btn').addEventListener('click', () => {
  clearInterval(timer);
  timer = null;
});""",
            commonMistakes = listOf(
                "Starting a new interval without clearing the existing one, causing runaway acceleration.",
                "Expecting setTimeout(fn, 0) to execute synchronously (it is queued on the event loop)."
            ),
            challengeQuestion = "Add a Reset button that resets seconds to 0 and updates the display.",
            challengeHint = "Add <button id='reset'>Reset</button> and set seconds = 0.",
            moduleId = "mod_js_dom"
        ),

        // Game Dev Course
        Lesson(
            id = "game_1",
            category = CourseCategory.GAME_DEV,
            order = 1,
            title = "1. Canvas Coordinates & The Game Loop",
            level = "Intermediate",
            durationMinutes = 12,
            summary = "Understand the HTML5 Canvas 2D context, coordinate system (0,0 is top-left), and requestAnimationFrame loop.",
            theoryMarkdown = """### The Heart of Every Game
All computer games run on a **Game Loop**:
1. **Process Input**: Check touch, keyboard, mouse.
2. **Update**: Move entities, apply gravity, detect collisions.
3. **Render**: Clear previous frame and draw current frame.

In JavaScript, `requestAnimationFrame(loop)` synchronizes with the display refresh rate (60Hz or 120Hz).""",
            defaultHtml = """<canvas id="game" width="320" height="240"></canvas>
<p>Bouncing Ball Simulation</p>""",
            defaultCss = """body { font-family: sans-serif; padding: 10px; background: #020617; color: #38bdf8; text-align: center; }
canvas { background: #0f172a; border: 2px solid #38bdf8; border-radius: 8px; }""",
            defaultJs = """const canvas = document.getElementById('game');
const ctx = canvas.getContext('2d');

let x = 100, y = 50;
let vx = 3, vy = 2;
const radius = 12;

function loop() {
  // 1. Update Physics
  x += vx;
  y += vy;

  if (x - radius < 0 || x + radius > canvas.width) vx = -vx;
  if (y - radius < 0 || y + radius > canvas.height) vy = -vy;

  // 2. Clear Screen
  ctx.clearRect(0, 0, canvas.width, canvas.height);

  // 3. Draw Ball
  ctx.beginPath();
  ctx.arc(x, y, radius, 0, Math.PI * 2);
  ctx.fillStyle = '#38bdf8';
  ctx.fill();
  ctx.strokeStyle = '#fff';
  ctx.stroke();

  requestAnimationFrame(loop);
}

requestAnimationFrame(loop);
console.log('Game loop running!');""",
            commonMistakes = listOf(
                "Forgetting ctx.clearRect(), causing trails/ghosting.",
                "Using setInterval() for animations instead of requestAnimationFrame()."
            ),
            challengeQuestion = "Make the ball bounce faster by changing vx = 5 and vy = 4.",
            challengeHint = "Modify let vx = 5, vy = 4;",
            moduleId = "mod_game_arcade"
        ),

        Lesson(
            id = "game_paddle",
            category = CourseCategory.GAME_DEV,
            order = 2,
            title = "2. Touch & Mouse Paddle Control",
            level = "Advanced",
            durationMinutes = 18,
            summary = "Build an arcade breakout/pong paddle controlled by touch or mouse position.",
            theoryMarkdown = """### Player Input on Touchscreens
On mobile, touch events (`touchmove`, `touchstart`) and pointer events (`pointermove`) give relative or client coordinates.
Map `touch.clientX` relative to `canvas.getBoundingClientRect()` to accurately position the player's paddle!""",
            defaultHtml = """<canvas id="pong-canvas" width="300" height="300"></canvas>
<p>Drag your finger or mouse across the canvas!</p>""",
            defaultCss = """body { font-family: sans-serif; text-align: center; background: #020617; color: #fff; padding: 10px; }
canvas { background: #0f172a; border: 2px solid #10b981; border-radius: 8px; touch-action: none; }""",
            defaultJs = """const canvas = document.getElementById('pong-canvas');
const ctx = canvas.getContext('2d');

let paddleX = 110;
const paddleWidth = 80;
const paddleHeight = 12;

canvas.addEventListener('pointermove', (e) => {
  const rect = canvas.getBoundingClientRect();
  paddleX = e.clientX - rect.left - (paddleWidth / 2);
  // Keep inside canvas bounds
  if (paddleX < 0) paddleX = 0;
  if (paddleX + paddleWidth > canvas.width) paddleX = canvas.width - paddleWidth;
});

function draw() {
  ctx.clearRect(0, 0, canvas.width, canvas.height);
  ctx.fillStyle = '#10b981';
  ctx.fillRect(paddleX, canvas.height - 24, paddleWidth, paddleHeight);
  requestAnimationFrame(draw);
}
draw();""",
            commonMistakes = listOf(
                "Forgetting `touch-action: none` on the canvas, causing the entire webpage to scroll when dragging.",
                "Using screenX instead of clientX with bounding client rect offset."
            ),
            challengeQuestion = "Widen the paddle from 80 to 120 pixels.",
            challengeHint = "Change const paddleWidth = 120;",
            moduleId = "mod_game_arcade"
        ),

        // Web Projects
        Lesson(
            id = "project_calc",
            category = CourseCategory.WEB_PROJECTS,
            order = 1,
            title = "1. Minimalist Pocket Calculator",
            level = "Intermediate",
            durationMinutes = 15,
            summary = "Create a responsive keypad calculator with clean arithmetic evaluation and glassmorphism.",
            theoryMarkdown = """### Mini-Project: Pocket Calculator
Combines CSS Grid for the button pad with JavaScript state management to build a fully functional pocket tool.""",
            defaultHtml = """<div class="calc">
  <div id="screen">0</div>
  <div class="grid">
    <button onclick="press('7')">7</button><button onclick="press('8')">8</button><button onclick="press('9')">9</button><button onclick="press('/')">/</button>
    <button onclick="press('4')">4</button><button onclick="press('5')">5</button><button onclick="press('6')">6</button><button onclick="press('*')">*</button>
    <button onclick="press('1')">1</button><button onclick="press('2')">2</button><button onclick="press('3')">3</button><button onclick="press('-')">-</button>
    <button onclick="clearCalc()">C</button><button onclick="press('0')">0</button><button onclick="calcResult()">=</button><button onclick="press('+')">+</button>
  </div>
</div>""",
            defaultCss = """.calc { width: 220px; margin: 20px auto; background: #1e293b; padding: 16px; border-radius: 12px; }
#screen { background: #0f172a; color: #38bdf8; font-size: 24px; padding: 12px; text-align: right; border-radius: 6px; margin-bottom: 12px; }
.grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 6px; }
button { padding: 12px; background: #334155; color: #fff; font-size: 16px; border: none; border-radius: 6px; font-weight: bold; }""",
            defaultJs = """let expr = '';
const screen = document.getElementById('screen');
function press(val) {
  expr += val;
  screen.innerText = expr;
}
function clearCalc() {
  expr = '';
  screen.innerText = '0';
}
function calcResult() {
  try {
    expr = String(Function('"use strict";return (' + expr + ')')());
    screen.innerText = expr;
  } catch(e) {
    screen.innerText = 'Error';
    expr = '';
  }
}""",
            commonMistakes = listOf(
                "Using raw eval() without safety restrictions.",
                "Not handling Division by Zero or invalid syntax errors."
            ),
            challengeQuestion = "Add a decimal point button '.' to the keypad.",
            challengeHint = "<button onclick=\"press('.')\">.</button>",
            moduleId = "mod_web_projects"
        ),

        Lesson(
            id = "project_todo",
            category = CourseCategory.WEB_PROJECTS,
            order = 2,
            title = "2. Offline Task & Habit Tracker",
            level = "Intermediate",
            durationMinutes = 15,
            summary = "Build a local task manager with add, toggle complete, and delete actions.",
            theoryMarkdown = """### Mini-Project: Local Task Tracker
Learn DOM item appending, deletion, and local state management for offline productivity.""",
            defaultHtml = """<h2>My Coding Habits</h2>
<input type="text" id="task-input" placeholder="New habit (e.g. 15m CSS)...">
<button id="add-btn">Add</button>
<ul id="task-list"></ul>""",
            defaultCss = """body { font-family: sans-serif; padding: 16px; background: #0f172a; color: #fff; }
input { padding: 8px; background: #1e293b; border: 1px solid #334155; color: #fff; border-radius: 6px; }
button { padding: 8px 14px; background: #10b981; color: #000; border: none; border-radius: 6px; font-weight: bold; }
ul { list-style: none; padding: 0; margin-top: 16px; }
li { padding: 8px 12px; background: #1e293b; border-radius: 6px; margin-bottom: 6px; display: flex; justify-content: space-between; align-items: center; }""",
            defaultJs = """const input = document.getElementById('task-input');
const list = document.getElementById('task-list');

document.getElementById('add-btn').addEventListener('click', () => {
  const text = input.value.trim();
  if (!text) return;
  const li = document.createElement('li');
  li.innerHTML = '<span>' + text + '</span><button style="background:#ef4444;color:#fff;border:none;border-radius:4px;padding:2px 8px;">✕</button>';
  li.querySelector('button').onclick = () => li.remove();
  list.appendChild(li);
  input.value = '';
});""",
            commonMistakes = listOf(
                "Not clearing the text input value after adding a task.",
                "Not preventing empty strings from creating blank task cards."
            ),
            challengeQuestion = "Add an alert when clicking an added task span.",
            challengeHint = "Add li.querySelector('span').onclick = () => alert(...);",
            moduleId = "mod_web_projects"
        )
    )

    fun getModuleById(id: String): TutorialModule? {
        return modules.find { it.id == id }
    }

    fun getLessonsForCategory(category: CourseCategory): List<Lesson> {
        return lessons.filter { it.category == category }.sortedBy { it.order }
    }

    fun getLessonsForModule(moduleId: String): List<Lesson> {
        return lessons.filter { it.moduleId == moduleId }.sortedBy { it.order }
    }

    fun getLessonById(id: String): Lesson? {
        return lessons.find { it.id == id }
    }

    fun getNextLesson(currentLessonId: String): Lesson? {
        val current = lessons.find { it.id == currentLessonId } ?: return null
        val moduleLessons = getLessonsForModule(current.moduleId)
        val currentIndex = moduleLessons.indexOfFirst { it.id == currentLessonId }
        return if (currentIndex in 0 until moduleLessons.lastIndex) {
            moduleLessons[currentIndex + 1]
        } else {
            null
        }
    }

    fun getPreviousLesson(currentLessonId: String): Lesson? {
        val current = lessons.find { it.id == currentLessonId } ?: return null
        val moduleLessons = getLessonsForModule(current.moduleId)
        val currentIndex = moduleLessons.indexOfFirst { it.id == currentLessonId }
        return if (currentIndex > 0) {
            moduleLessons[currentIndex - 1]
        } else {
            null
        }
    }
}
