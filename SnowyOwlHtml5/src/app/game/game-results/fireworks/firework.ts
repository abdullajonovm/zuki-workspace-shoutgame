// Base firework acceleration.
// 1.0 causes fireworks to travel at a constant speed.
// Higher number increases rate firework accelerates over time.
import {Util} from 'app/util';
import {Particle} from './particle';

export interface FireworkVars {
    canvas: HTMLCanvasElement;
    context: CanvasRenderingContext2D;
    fireworks: Firework[];
    particles: Particle[];
    hue: number;
}

// Higher number increases rate firework accelerates over time.
const FIREWORK_ACCELERATION = 1.05;
// Minimum firework brightness.
const FIREWORK_BRIGHTNESS_MIN = 50;
// Maximum firework brightness.
const FIREWORK_BRIGHTNESS_MAX = 80;
// Base speed of fireworks.
const FIREWORK_SPEED = 4;
// Base length of firework trails.
const FIREWORK_TRAIL_LENGTH = 3;
// Determine if target position indicator is enabled.
const FIREWORK_TARGET_INDICATOR_ENABLED = false;
// Base particle count per firework.
const PARTICLE_COUNT = 100;


export class Firework {
    targetRadius: number;
    y: number;
    x: number;
    private startX: number;
    private startY: number;
    private endX: number;
    private endY: number;
    private distanceToEnd: number;
    private distanceTraveled: number;
    private trail: Array<any>;
    private trailLength: number;
    private angle: number;
    private speed: number;
    private acceleration: number;
    private brightness: any;

    constructor(startX: number, startY: number, endX: number, endY: number, private vars: FireworkVars) {
        // Set current coordinates.
        this.x = startX;
        this.y = startY;
        // Set starting coordinates.
        this.startX = startX;
        this.startY = startY;
        // Set end coordinates.
        this.endX = endX;
        this.endY = endY;
        // Get the distance to the end point.
        this.distanceToEnd = Util.calculateDistance(startX, startY, endX, endY);
        this.distanceTraveled = 0;
        // Create an array to track current trail particles.
        this.trail = [];
        // Trail length determines how many trailing particles are active at once.
        this.trailLength = FIREWORK_TRAIL_LENGTH;
        // While the trail length remains, add current point to trail list.
        while (this.trailLength--) {
            this.trail.push([this.x, this.y]);
        }
        // Calculate the angle to travel from start to end point.
        this.angle = Math.atan2(endY - startY, endX - startX);
        // Set the speed.
        this.speed = FIREWORK_SPEED;
        // Set the acceleration.
        this.acceleration = FIREWORK_ACCELERATION;
        // Set the brightness.
        this.brightness = Util.random(FIREWORK_BRIGHTNESS_MIN, FIREWORK_BRIGHTNESS_MAX);
        // Set the radius of click-target location.
        this.targetRadius = 2.5;
    }

    update(index: number): void {
        // Remove the oldest trail particle.
        this.trail.pop();
        // Add the current position to the start of trail.
        this.trail.unshift([this.x, this.y]);

        // Animate the target radius indicator.
        if (FIREWORK_TARGET_INDICATOR_ENABLED) {
            if (this.targetRadius < 8) {
                this.targetRadius += 0.3;
            } else {
                this.targetRadius = 1;
            }
        }

        // Increase speed based on acceleration rate.
        this.speed *= this.acceleration;

        // Calculate current velocity for both x and y axes.
        const xVelocity = Math.cos(this.angle) * this.speed;
        const yVelocity = Math.sin(this.angle) * this.speed;
        // Calculate the current distance travelled based on starting position, current position, and velocity.
        // This can be used to determine if firework has reached final position.
        this.distanceTraveled = Util.calculateDistance(this.startX, this.startY, this.x + xVelocity, this.y + yVelocity);

        // Check if final position has been reached (or exceeded).
        if (this.distanceTraveled >= this.distanceToEnd) {
            // Destroy firework by removing it from collection.
            this.vars.fireworks.splice(index, 1);
            // Create particle explosion at end point.  Important not to use this.x and this.y,
            // since that position is always one animation loop behind.
            createParticles(this.endX, this.endY, this.vars);
        } else {
            // End position hasn't been reached, so continue along current trajectory by updating current coordinates.
            this.x += xVelocity;
            this.y += yVelocity;
        }
    }

    draw(): void {
        const context = this.vars.context;
        // Begin a new path for firework trail.
        context.beginPath();
        // Get the coordinates for the oldest trail position.
        const trailEndX = this.trail[this.trail.length - 1][0];
        const trailEndY = this.trail[this.trail.length - 1][1];
        // Create a trail stroke from trail end position to current firework position.
        context.moveTo(trailEndX, trailEndY);
        context.lineTo(this.x, this.y);
        // Set stroke coloration and style.
        // Use hue, saturation, and light values instead of RGB.
        context.strokeStyle = `hsl(${this.vars.hue}, 100%, ${this.brightness}%)`;
        // Draw stroke.
        context.stroke();

        if (FIREWORK_TARGET_INDICATOR_ENABLED) {
            // Begin a new path for end position animation.
            context.beginPath();
            // Create an pulsing circle at the end point with targetRadius.
            context.arc(this.endX, this.endY, this.targetRadius, 0, Math.PI * 2);
            // Draw stroke.
            context.stroke();
        }
    }
}



// Create particle explosion at 'x' and 'y' coordinates.
function createParticles(x: number, y: number, vars: FireworkVars): void {
    // Set particle count.
    // Higher numbers may reduce performance.
    let particleCount = PARTICLE_COUNT;
    while (particleCount--) {
        // Create a new particle and add it to particles collection.
        vars.particles.push(new Particle(x, y, vars.hue, vars.particles, vars.context));
    }
}
