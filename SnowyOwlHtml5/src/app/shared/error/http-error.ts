import {ShoutErrorResponse} from 'app/shared/services/http.service';
import {Util} from 'app/util';

const DEFAULT_MESSAGE = 'An unexpected error occured.  Please try again later.';

export class HttpError extends Error {
    errorResponse: ShoutErrorResponse;

    constructor(resp: ShoutErrorResponse, handledErrors: HandledErrors)
    constructor(message?: string);
    constructor(messageOrResp?: ShoutErrorResponse | string, handledErrors?: HandledErrors) {
        super();
        this.name = 'HttpError';

        if (!messageOrResp) {
            this.message = DEFAULT_MESSAGE;
        } else if (Util.isString(messageOrResp)) {
            this.message = messageOrResp;
        } else {
            this.errorResponse = messageOrResp;
            this.message = getErrorMessage(handledErrors || {}, messageOrResp);
        }

    }

}

function getErrorMessage(handled: HandledErrors, error: ShoutErrorResponse): string {
    for (const key in error) {
        if (error.hasOwnProperty(key) && handled[key]) {
            return handled[key];
        }
    }
    return DEFAULT_MESSAGE;
}

interface HandledErrors {
    [errorName: string]: string
}
