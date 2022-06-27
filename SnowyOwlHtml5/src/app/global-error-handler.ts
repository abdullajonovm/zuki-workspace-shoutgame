import {ErrorHandler, Injectable} from '@angular/core';
import {LogService} from 'app/shared/services/log.service';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
    constructor(private logService: LogService) {
    }

    handleError(error: any): void {
        this.logService.error('UNCAUGHT EXCEPTION: ', {
            error: error.toString(),
            stack: error.stack,
            rejection: error.rejection
        });

        // IMPORTANT: Rethrow the error otherwise it gets swallowed
        throw error;
    }

}
