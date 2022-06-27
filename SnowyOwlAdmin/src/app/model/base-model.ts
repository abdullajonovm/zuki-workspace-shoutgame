import { DeserializeInto, Serialize } from 'cerialize';
export abstract class BaseModel<T> {
    constructor(json?: {[P in keyof T]?: T[P]}) {
        if (json) {
            DeserializeInto(json, this.constructor, this);
        } else if (this.initialize) {
            this.initialize()
        }
    }

    toJson(): string {
        return JSON.stringify(Serialize(this));
    }

    toJsonObject(): any {
        return Serialize(this);
    }

    fillFromJson(json?: {[P in keyof T]?: T[P]}): void {
        DeserializeInto(json, this.constructor, this);
    }

    toString(): string {
        return this.toJson();
    }

    protected initialize(): void {

    }

}
