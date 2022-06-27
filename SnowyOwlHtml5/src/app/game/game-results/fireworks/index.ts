// Minimum number of ticks per manual firework launch.
import {Firework} from './firework';
import {Particle} from './particle';

// Alpha level that canvas cleanup iteration removes existing trails.
// Lower value increases trail duration.
const CANVAS_CLEANUP_ALPHA = 0.15;
// Hue change per loop, used to rotate through different firework colors.
const HUE_STEP_INCREASE = 0.5;

const TICKS_PER_FIREWORK_MIN = 5;
// Minimum number of ticks between each automatic firework launch.
const TICKS_PER_FIREWORK_AUTOMATED_MIN = 20;
// Maximum number of ticks between each automatic firework launch.
const TICKS_PER_FIREWORK_AUTOMATED_MAX = 80;

// === END CONFIGURATION ===

// === LOCAL VARS ===

const canvas = document.getElementById('canvas')! as HTMLCanvasElement;
// Set canvas dimensions.
canvas.width = window.innerWidth;
canvas.height = window.innerHeight;
// Set the context, 2d in this case.
const context = canvas.getContext('2d')!;
// Firework and particles collections.
const fireworks: Firework[] = [];
const particles: Particle[] = [];

// Mouse coordinates.
let mouseX: number;
let mouseY: number;

// Variable to check if mouse is down.
let isMouseDown = false;
// Initial hue.
let hue = 120;
// Track number of ticks since automated firework.
let ticksSinceFireworkAutomated = 0;
// Track number of ticks since manual firework.
let ticksSinceFirework = 0;


// Launch fireworks automatically.
function launchAutomatedFirework(): void {
    // Determine if ticks since last automated launch is greater than random min/max values.
    if (ticksSinceFireworkAutomated >= random(TICKS_PER_FIREWORK_AUTOMATED_MIN, TICKS_PER_FIREWORK_AUTOMATED_MAX)) {
        // Check if mouse is not currently clicked.
        if (!isMouseDown) {
            // Set start position to bottom center.
            const startX = canvas.width / 2;
            const startY = canvas.height;
            // Set end position to random position, somewhere in the top half of screen.
            const endX = random(0, canvas.width);
            const endY = random(0, canvas.height / 2);
            // Create new firework and add to collection.
            fireworks.push(new Firework(startX, startY, endX, endY, {context, particles, hue, fireworks, canvas}));
            // Reset tick counter.
            ticksSinceFireworkAutomated = 0;
        }
    } else {
        // Increment counter.
        ticksSinceFireworkAutomated++;
    }
}

// Launch fireworks manually, if mouse is pressed.
function launchManualFirework(): void {
    // Check if ticks since last firework launch is less than minimum value.
    if (ticksSinceFirework >= TICKS_PER_FIREWORK_MIN) {
        // Check if mouse is down.
        if (isMouseDown) {
            // Set start position to bottom center.
            const startX = canvas.width / 2;
            const startY = canvas.height;
            // Create new firework and add to collection.
            fireworks.push(new Firework(startX, startY, mouseX, mouseY, {context, particles, hue, fireworks, canvas}));
            // Reset tick counter.
            ticksSinceFirework = 0;
        }
    } else {
        // Increment counter.
        ticksSinceFirework++;
    }
}

// Update all active fireworks.
function updateFireworks(): void {
    // Loop backwards through all fireworks, drawing and updating each.
    for (let i = fireworks.length - 1; i >= 0; --i) {
        fireworks[i].draw();
        fireworks[i].update(i);
    }
}

// Update all active particles.
function updateParticles(): void {
    // Loop backwards through all particles, drawing and updating each.
    for (let i = particles.length - 1; i >= 0; --i) {
        particles[i].draw();
        particles[i].update(i);
    }
}

window.requestAnimFrame = (() => {
    return window.requestAnimationFrame ||
        window.webkitRequestAnimationFrame ||
        window.mozRequestAnimationFrame ||
        function(callback: any): void {
            window.setTimeout(callback, 1000 / 60);
        };
})();

// === END APP HELPERS ===

// Primary loop.
function loop(): void {
    // Smoothly request animation frame for each loop iteration.
    window.requestAnimFrame(loop);

    // Adjusts coloration of fireworks over time.
    hue += HUE_STEP_INCREASE;

    // Clean the canvas.
    cleanCanvas();

    // Update fireworks.
    updateFireworks();

    // Update particles.
    updateParticles();

    // Launch automated fireworks.
    launchAutomatedFirework();

    // Launch manual fireworks.
    launchManualFirework();
}

function cleanCanvas(): void {
    // Set 'destination-out' composite mode, so additional fill doesn't remove non-overlapping content.
    context.globalCompositeOperation = 'destination-out';
    // Set alpha level of content to remove.
    // Lower value means trails remain on screen longer.
    context.fillStyle = `rgba(0, 0, 0, ${CANVAS_CLEANUP_ALPHA})`;
    // Fill entire canvas.
    context.fillRect(0, 0, canvas.width, canvas.height);
    // Reset composite mode to 'lighter', so overlapping particles brighten each other.
    context.globalCompositeOperation = 'lighter';
}

function random(min: number, max: number): number {
    return Math.random() * (max - min) + min;
}

// === EVENT LISTENERS ===

// Track current mouse position within canvas.
canvas.addEventListener('mousemove', (e) => {
    mouseX = e.pageX - canvas.offsetLeft;
    mouseY = e.pageY - canvas.offsetTop
});

// Track when mouse is pressed.
canvas.addEventListener('mousedown', (e) => {
    e.preventDefault();
    isMouseDown = true;
});

// Track when mouse is released.
canvas.addEventListener('mouseup', (e) => {
    e.preventDefault();
    isMouseDown = false;
});

// === END EVENT LISTENERS ===

// Initiate loop after window loads.
window.onload = loop;
