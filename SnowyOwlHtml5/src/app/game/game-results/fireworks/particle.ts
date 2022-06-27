import {Util} from '../../../util';

// Minimum particle brightness.
const PARTICLE_BRIGHTNESS_MIN = 50;
// Maximum particle brightness.
const PARTICLE_BRIGHTNESS_MAX = 80;
// Minimum particle decay rate.
const PARTICLE_DECAY_MIN = 0.015;
// Maximum particle decay rate.
const PARTICLE_DECAY_MAX = 0.03;
// Base particle friction.
// Slows the speed of particles over time.
const PARTICLE_FRICTION = 0.95;
// Base particle gravity.
// How quickly particles move toward a downward trajectory.
const PARTICLE_GRAVITY = 0.7;
// Variance in particle coloration.
const PARTICLE_HUE_VARIANCE = 40;
// Base particle transparency.
const PARTICLE_TRANSPARENCY = 1;
// Minimum particle speed.
const PARTICLE_SPEED_MIN = 1;
// Maximum particle speed.
const PARTICLE_SPEED_MAX = 8;
// Base length of explosion particle trails.
const PARTICLE_TRAIL_LENGTH = 5;


export class Particle {
    transparency: number;
    trail: [number, number][];
    speed: number;
    brightness: number;
    friction: number;
    private x: number;
    private y: number;
    private angle: number;
    private gravity: number;
    private hue: number;
    private decay: number;
    private trailLength: number;

    constructor(x: number, y: number, hue: number, private particles: Particle[], private context: CanvasRenderingContext2D) {
        // Set current position.
        this.x = x;
        this.y = y;
        // To better simulate a firework, set the angle of travel to random value in any direction.
        this.angle = Util.random(0, Math.PI * 2);
        // Set friction.
        this.friction = PARTICLE_FRICTION;
        // Set gravity.
        this.gravity = PARTICLE_GRAVITY;
        // Set the hue to somewhat randomized number.
        // This gives the particles within a firework explosion an appealing variance.
        this.hue = Util.random(hue - PARTICLE_HUE_VARIANCE, hue + PARTICLE_HUE_VARIANCE);
        // Set brightness.
        this.brightness = Util.random(PARTICLE_BRIGHTNESS_MIN, PARTICLE_BRIGHTNESS_MAX);
        // Set decay.
        this.decay = Util.random(PARTICLE_DECAY_MIN, PARTICLE_DECAY_MAX);
        // Set speed.
        this.speed = Util.random(PARTICLE_SPEED_MIN, PARTICLE_SPEED_MAX);
        // Create an array to track current trail particles.
        this.trail = [];
        // Trail length determines how many trailing particles are active at once.
        this.trailLength = PARTICLE_TRAIL_LENGTH;
        // While the trail length remains, add current point to trail list.
        while (this.trailLength--) {
            this.trail.push([this.x, this.y]);
        }
        // Set transparency.
        this.transparency = PARTICLE_TRANSPARENCY;
    }

    update(index: number): void {
        // Remove the oldest trail particle.
        this.trail.pop();
        // Add the current position to the start of trail.
        this.trail.unshift([this.x, this.y]);

        // Decrease speed based on friction rate.
        this.speed *= this.friction;
        // Calculate current position based on angle, speed, and gravity (for y-axis only).
        this.x += Math.cos(this.angle) * this.speed;
        this.y += Math.sin(this.angle) * this.speed + this.gravity;

        // Apply transparency based on decay.
        this.transparency -= this.decay;
        // Use decay rate to determine if particle should be destroyed.
        if (this.transparency <= this.decay) {
            // Destroy particle once transparency level is below decay.
            this.particles.splice(index, 1);
        }
    }

    draw(): void {
        const context = this.context;
        // Begin a new path for particle trail.
        context.beginPath();
        // Get the coordinates for the oldest trail position.
        const trailEndX = this.trail[this.trail.length - 1][0];
        const trailEndY = this.trail[this.trail.length - 1][1];
        // Create a trail stroke from trail end position to current particle position.
        context.moveTo(trailEndX, trailEndY);
        context.lineTo(this.x, this.y);
        // Set stroke coloration and style.
        // Use hue, brightness, and transparency instead of RGBA.
        context.strokeStyle = `hsla(${this.hue}, 100%, ${this.brightness}%, ${this.transparency})`;
        context.stroke();
    }
}

