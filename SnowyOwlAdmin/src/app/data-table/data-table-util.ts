export class DataTableUtil {
    static isString(n: any): n is string {
        return (typeof n === 'string');
    };

    static isDefined<T>(val: T | undefined): val is T {
        return typeof val !== 'undefined' && val !== null;
    };
}
