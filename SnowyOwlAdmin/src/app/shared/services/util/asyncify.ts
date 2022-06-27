export class Asyncify {
    static callback<T extends {[k in K]: Function | null}, K extends keyof T>(obj: T, func: K, ...args: any[]): Promise<any[]> {
        let foundCallback = false;
        const result = new Promise((resolve, reject) => {
            for (let i = 0; i < args.length; i++) {
                if (args[i] === '__callback__') {
                    args[i] = (...inner: any[]) => {
                        resolve(inner);
                    };
                    foundCallback = true;
                    return;
                }
            }
        });
        if (!foundCallback)
            throw new Error('Could not find string "__callback__" in arguments!');

        if (obj[func]) {
            obj[func]!!(...args);
        }
        return result as any;
    }

    static resolveWhenCalled<T extends {[k in K]: Function | null}, K extends keyof T>(obj: T, func: K): Promise<any[]> {
        return new Promise((resolve) => {
            const orig = obj[func];
            (obj as any)[func] = (...args: any[]) => {
                if (orig)
                    orig(...args);
                resolve(...args);
            }
        })
    }

    static timer(f: Function, num = 0): Promise<void> {
        return new Promise((resolve) => {
            setTimeout(() => {
                f();
                resolve();
            }, num);
        })
    }

}
