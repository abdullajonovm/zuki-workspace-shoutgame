import {Injectable} from '@angular/core';
import uuid from 'uuid/v4';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/observable/timer';
import 'rxjs/add/operator/switchMap';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import 'rxjs/add/observable/throw';
import {HttpClient, HttpErrorResponse, HttpHeaders, HttpParams} from '@angular/common/http';
import 'rxjs/add/operator/toPromise';
import {CustomHttpParamCodec} from 'app/shared/custom-http-param-codec';
import {DialogService} from 'app/shared/dialog/dialog.service';
import {Subject} from 'rxjs/Subject';
import {LogService} from 'app/shared/services/log.service';
import {DialogComponent} from 'app/shared/dialog/dialog.component';
import {deviceIdLS, encryptKeyLS, sessionKeyLS} from '../../constants';
import {environment} from '../../../environments/environment';
import {SRD, SRDUrl} from '../../model/srd';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import 'rxjs/add/operator/mergeMap';

const encoder = new CustomHttpParamCodec();

@Injectable()
export class HttpService {
    sessionKey?: string;
    encryptKey?: string;

    on401 = new Subject();
    srd = new BehaviorSubject<SRD | null>(null);

    private deviceId: string;
    private dialog401?: DialogComponent;

    constructor(private http: HttpClient, private dialog: DialogService, private logService: LogService) {
        this.loadSrd();
        this.sessionKey = localStorage.getItem(sessionKeyLS) || undefined;
        this.encryptKey = localStorage.getItem(encryptKeyLS) || undefined;
        const deviceId = localStorage.getItem(deviceIdLS) || undefined;

        if (deviceId) {
            this.deviceId = deviceId;
        } else {
            this.deviceId = uuid();
            localStorage.setItem(deviceIdLS, this.deviceId);
        }
    }

    getWdsDoc<T>(url: string): Observable<T> {
        return this.srd.filter((val) => !!val).take(1).flatMap((srd: SRD) => {
            return this.http.get<T>(srd.wdsUrl + url)
        });
    }

    sendSynchronousCollectorRequest<T extends ShoutResponse>(url: string, body: any = {}): Observable<T> {
        return this.srd.filter((val) => !!val).take(1).flatMap((srd: SRD) => {
            const requestOptions = this.prepareRequestOptions(body, srd);
            return this.http.post<T>(srd.collectorUrl + url, null, requestOptions).map((response) => {
                if (!response.success && (response.unauthorized || response.subscriberNotFound)) {
                    this.send401(response);
                }
                return response;
            });
        });
    }

    sendCollectorRequest<T extends ShoutResponse>(url: string, body: any = {}, requireSessionKey = true): Observable<T> {
        return this.srd.filter((val) => !!val).take(1).flatMap((srd: SRD) => {
            const requestOptions = this.prepareRequestOptions(body, srd, requireSessionKey);

            let ticketResponse: ResponseJson;
            return this.http.post<ResponseJson>(srd.collectorUrl + url, null, requestOptions).switchMap((response) => {
                ticketResponse = response;
                return this.sendAndRetryOnError<T>(response, srd, 0);
            }).map(response => {
                response.ticketResponse = ticketResponse;
                if (!response.success && (response.unauthorized || response.subscriberNotFound)) {
                    console.log(url);
                    this.send401(response);
                }
                return response;
            });
        });
    }

    private sendAndRetryOnError<T>(json: ResponseJson, srd: SRD, attempt: number): Observable<T> {
        // TODO: dynamic retry times
        const retryTimes = [500, 1000, 5000, 15000, 20000];
        const waitTime = attempt === 0 ? json.estimatedWaitTime : retryTimes[attempt - 1];

        const request = this.http.get<T>(`${srd.wdsUrl}/${json.ticket}/response.json`).catch((error: HttpErrorResponse) => {
            if (error.status === 404 && attempt <= retryTimes.length) {
                return this.sendAndRetryOnError(json, srd, attempt + 1);
            } else if (error.status === 401) {
                this.send401(error);
            }
            return Observable.throw(error);
        });

        return Observable.timer(waitTime).switchMap(() => request);
    }

    private send401(message?: any): void {
        this.on401.next();

        if (this.dialog401 && !this.dialog401.closed)
            return;

        this.logService.error('Got a 401: ', message);
        this.dialog401 = this.dialog.open({
            body: 'Session expired.  Please log back in.',
            buttons: [{text: 'Ok'}]
        });
    }

    // Pull to refresh all games
    // Sync logout or retry

    private getHeaders(requireSessionKey = true): HttpHeaders {
        if (!this.sessionKey && requireSessionKey) {
            this.send401('httpService getHeaders no session key!');
            throw new Error('httpService getHeaders no session key!');
        } else if (!this.deviceId) {
            this.send401('httpService getHeaders no device id!');
            throw new Error('httpService getHeaders no device id!');
        }

        let headers = new HttpHeaders({
            'X-REST-DEVICE-ID': this.deviceId,
            'X-REST-APPLICATION-ID': 'snowyowl',
            'X-REST-APPLICATION-VERSION': '1.0',
            'Content-Type': 'application/x-www-form-urlencoded'
        });

        if (this.sessionKey) {
            headers = headers.set('X-REST-SESSION-KEY', this.sessionKey);
        }

        return headers;
    }

    private prepareRequestOptions(body: any, srd: SRD, requireSessionKey = true): { headers: HttpHeaders; params: HttpParams } {
        const headers = this.getHeaders(requireSessionKey);
        body.toWds = srd.wdsUrl;
        body.appId = 'snowyowl';

        let params = new HttpParams({encoder});

        for (const key in body) {
            if (body.hasOwnProperty(key) && body[key]) {
                if (body[key].toJSON) {
                    body[key] = body[key].toJSON();
                }
                params = params.set(key, body[key]);
            }
        }

        return {headers, params};
    }

    private loadSrd(): void {
        if (this.srd.value)
            return;

        if (environment.srd) {
            this.srd.next(environment.srd);
            return;
        }
        this.http.get<SRDUrl>('assets/srd-url.json').subscribe(url => {
            this.http.get<SRD>(url.srd).subscribe((srd) => {
                this.srd.next(srd);
            });
        });
    }

}

interface ResponseJson {
    ticket: string,
    estimatedWaitTime: number;
    encryptKey: string;
}

export interface ShoutResponse {
    success: boolean;
    ticketResponse: ResponseJson;
    unauthorized?: boolean;
    subscriberNotFound?: boolean;
}

export interface ShoutSuccessResponse extends ShoutResponse {
    success: true;
}

export interface ShoutErrorResponse extends ShoutResponse {
    success: false;
}
