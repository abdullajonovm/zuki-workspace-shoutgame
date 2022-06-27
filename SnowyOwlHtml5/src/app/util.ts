import CryptoJS from 'crypto-js/core';
import 'crypto-js/sha256';
import 'crypto-js/enc-latin1';
import 'crypto-js/aes';
import scrypt from 'scrypt-async';
import {Game} from './model/game';
import {Round} from './model/round';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {Subject} from 'rxjs/Subject';
import {CategoryFilter} from './home/filter/filter.component';

export class Util {
    static scryptString(str: string): Promise<string> {
        const salt = create64BitSalt();
        const logN = 12;
        const r = 8;
        const dkLen = 32;
        return new Promise((resolve) => {
            scrypt(str, salt, logN, r, dkLen, (key: any) => {
                const params = ((logN << 16) | (r << 8) | 1).toString(16);
                resolve(`$s0$${params}$${btoa(salt)}$${key}`);
            }, 'base64');
        });
    }

    static isString(a: any): a is string {
        return typeof a === 'string';
    }

    static decryptAES(decryptKey: string, text: string): string {
        const all = CryptoJS.enc.Latin1.parse(decryptKey);
        const key = CryptoJS.lib.WordArray.create(all.words.slice(0, 4)) as any;
        const ivWords = CryptoJS.lib.WordArray.create(all.words.slice(4, 8)) as any;
        const secretWords = CryptoJS.enc.Latin1.parse(text);
        const decrpyted = CryptoJS.AES.decrypt(secretWords.toString(CryptoJS.enc.Latin1), key, {iv: ivWords});
        return CryptoJS.enc.Latin1.stringify(decrpyted);
    }

    static sha256(str: string): string {
        return CryptoJS.SHA256(str).toString();
    }

    static trackByGame(game: Game): string {
        return game.id;
    }

    static randomInt(min: number, max: number): number {
        return Math.floor(Math.random() * (max - min + 1)) + min;
    }

    static random(min: number, max: number): number {
        return Math.random() * (max - min) + min;
    }

    static includes<T>(arr: T[], item: T): boolean {
        return !!arr.length && arr.indexOf(item) !== -1;
    }

    static remove<T>(arr: T[], item: T): void {
        const idx = arr.indexOf(item);
        if (idx !== -1) {
            arr.splice(idx, 1);
        }
    }

    /**
     * let x = { level1: { level2: { level3: 'hello' } } }
     * Util.get(x, 'level1.level2.level3') => 'hello'
     * @param obj The object to search
     * @param path
     * @param defaultVal
     */
    static get<T>(obj: any, path: string): T | undefined;
    static get<T>(obj: any, path: string, defaultVal: T): T
    static get<T>(obj: any, path: string, defaultVal?: T): T | undefined {
        if (!obj || !path)
            return defaultVal;

        let cur = obj;
        const split = path.split('.');
        for (const item of split) {
            if (cur[item])
                cur = cur[item];
            else
                return defaultVal;
        }
        return cur;
    }

    static keyBy<T, X extends keyof T, K extends T[X] & string>(obj: T[], prop: X): {[F in K]: T} {
        const result: any = {};
        for (const item of obj) {
            if (item[prop]) {
                result[item[prop]] = item;
            }
        }
        return result;
    }

    static find<T, X extends keyof T>(arr: T[], key: X, val: T[X]): T | null {
        for (const item of arr) {
            if (item[key] && item[key] === val) {
                return item;
            }
        }
        return null;
    }

    static getNextRound(game: Game): Round | undefined {
        let nextRound;

        // TODO: make this more robust
        for (const round of game.rounds) {
            if ((round.roundStatus === 'OPEN' || round.roundStatus === 'INPLAY') && !round.finished.getValue()) {
                nextRound = round;
                break;
            }
        }

        return nextRound;
    }

    static getLastRound(game: Game): Round | undefined {
        let lastRound;
        for (const round of game.rounds) {
            if (round.userJoinedRound) {
                lastRound = round;
            }
        }

        return lastRound;
    }

    static canPlayNow(game: Game): string | null {
        if (game.gameStatus === 'OPEN' && game.hasPoolPlayStarted) {
            const round = Util.getNextRound(game);
            return round ? `Play Round ${round.roundSequence}` : null;
        } else if (game.gameStatus === 'INPLAY' && game.userJoinedGame) {
            return 'Play Tournament';
        }
        return null;
    }

    static shouldShowGame(game: Game): boolean {
        let result = true;
        if (game.gameStatus === 'INPLAY' && !game.userJoinedGame) {
            result = false;
        }
        return result;
    }

    static animateNumber(from: number, to: number, time: number, precision = 0): BehaviorSubject<number> {
        const result: NumAnim = new BehaviorSubject(from);
        result.onFinished = new Subject<void>();
        let curVal = from;
        const [stepSize, stepTime] = calcStepSizeAndStepTime(time, to - curVal, precision);

        result.stepTime = stepTime;
        const interval = setInterval(() => {
            curVal += stepSize;
            curVal = +curVal.toFixed(precision);
            if (stepSize > 0 ? (curVal >= to) : (curVal <= to)) {
                curVal = to;
                clearInterval(interval);
                result.next(curVal);
                result.onFinished!.next();
                result.finished = true;
                result.complete();
            }

            result.next(curVal);
        }, stepTime);

        return result;
    }

    static isEmpty(obj: any): boolean {
        for (const key in obj) {
            if (obj.hasOwnProperty(key))
                return false;
        }
        return true;
    }

    static calculateDistance(aX: number, aY: number, bX: number, bY: number): number {
        const xDistance = aX - bX;
        const yDistance = aY - bY;
        return Math.sqrt(Math.pow(xDistance, 2) + Math.pow(yDistance, 2));
    }

    static hasCategory(game: Game, categoryFilter: CategoryFilter): boolean {
        if (categoryFilter === 'all') {
            return true;
        } else {
            for (const id of game.categoryIds) {
                if (id === '*')
                    return true;

                for (const cat of categoryFilter) {
                    if (cat.id === id)
                        return true;
                }
            }
        }
        return false;
    }
}

function create64BitSalt(): string {
    const numbers = new Array(64);
    for (let i = 0; i < numbers.length; i++) {
        numbers[i] = Math.floor(Math.random() * (95) + 32);
    }
    let output = '';
    for (const number of numbers) {
        output += String.fromCharCode(number);
    }
    output = output.toLowerCase();
    return output;
}


export interface NumAnim extends BehaviorSubject<number> {
    onFinished?: Subject<void>;
    finished?: boolean;
    stepTime?: number;
}

function calcStepSizeAndStepTime(animationTime: number, diff: number, precision: number): [number, number] {
    let stepTime = 30;
    let stepSize = diff / animationTime * stepTime;
    while (Math.abs(stepSize) * Math.pow(10, precision) < 1) {
        stepTime += 10;
        stepSize = diff / animationTime * stepTime;
    }
    return [stepSize, stepTime];
}
