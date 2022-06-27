import {HttpUrlEncodingCodec} from '@angular/common/http';

export class CustomHttpParamCodec extends HttpUrlEncodingCodec {
    encodeKey(key: string): string {
        return encodeURIComponent(key);
    }

    encodeValue(value: string): string {
        return encodeURIComponent(value);
    }
}
