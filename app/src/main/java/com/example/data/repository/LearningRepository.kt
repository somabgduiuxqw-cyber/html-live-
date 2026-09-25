package com.example.data.repository

import com.example.model.CourseCategory
import com.example.model.Lesson

object LearningRepository {

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
            challengeHint = "Use the <h2>...</h2> tag right below the <h1> heading."
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
            challengeHint = "Wrap your heading in <h3></h3> and text in <p><strong>...</strong></p>."
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
            challengeHint = "<a href='https://google.com'>Search the Web</a>"
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
            challengeHint = "Use <img src='...' alt='Description'>."
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
            challengeHint = "<input type='password' id='pwd' required>"
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
            challengeHint = "Add 'margin: 30px;' and 'border-radius: 12px;' inside the .box selector."
        ),

        Lesson(
            id = "css_2",
            category = CourseCategory.CSS,
            order = 2,
            title = "2. Flexbox: Modern 1D Layouts",
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
            challengeHint = "Set justify-content: space-between on .flex-container."
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
            challengeHint = "Add <button id='reset-btn'>Reset</button> and an event listener resetting counter = 0."
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
            challengeHint = "Modify let vx = 5, vy = 4;"
        )
    )

    fun getLessonsForCategory(category: CourseCategory): List<Lesson> {
        return lessons.filter { it.category == category }.sortedBy { it.order }
    }
}
