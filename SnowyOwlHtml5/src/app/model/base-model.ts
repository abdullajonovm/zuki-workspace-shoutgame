import {DeserializeInto, Serialize} from 'cerialize';

export abstract class BaseModel<T> {
    constructor(json?: Partial<T>) {
        if (json) {
            this.initialize();
            this.fillFromJson(json);
        } else if (this.initialize) {
            this.initialize()
        }
    }

    fillFromJson(json?: {[P in keyof T]?: T[P]}): void {
        DeserializeInto(json, this.constructor, this);
    }

    toJSON(): Object {
        return Serialize(this);
    }

    toJSONString(): string {
        return JSON.stringify(this.toJSON());
    }

    toString(): string {
        return JSON.stringify(this.toJSON());
    }

    protected initialize(): void {

    }

}
