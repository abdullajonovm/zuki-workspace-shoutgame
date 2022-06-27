import {
    BehaviorSubject,
    Observable,
    of,
    Subject,
    throwError as observableThrowError,
    timer as observableTimer
} from 'rxjs';

import {catchError, filter, map, mergeMap, switchMap, take} from 'rxjs/operators';
import {Injectable} from '@angular/core';
import * as uuid from 'uuid/v4';


import { VxDialog, VxDialogRef } from 'vx-components';
import {deviceIdLS, encryptKeyLS, sessionKeyLS, sha256LS} from '../../constants';
import {environment} from '../../../environments/environment';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {SRD, SRDUrl} from '../../model/srd';
import {getDeviceInfo} from 'app/shared/services/util/device-info';
import { AlertDialogComponent } from '../alert-dialog/alert-dialog.component';


@Injectable()
export class HttpService {
    sessionKey?: string;
    sha256Hash?: string;
    encryptKey?: string;

    on401 = new Subject();
    srd = new BehaviorSubject<SRD | null>(null);
    private deviceId: string;
    private dialog401?: VxDialogRef<AlertDialogComponent>;
    private deviceInfo = getDeviceInfo();

    constructor(private http: HttpClient, private vxDialog: VxDialog) {
        this.loadSrd();
        this.sessionKey = localStorage.getItem(sessionKeyLS) || undefined;
        this.sha256Hash = localStorage.getItem(sha256LS) || undefined;
        this.encryptKey = localStorage.getItem(encryptKeyLS) || undefined;

        const deviceId = localStorage.getItem(deviceIdLS);
        if (deviceId) {
            this.deviceId = deviceId;
        } else {
            this.deviceId = uuid();
            localStorage.setItem(deviceIdLS, this.deviceId);
        }
    }

    sendRequest<T extends ShoutResponse>(url: string, body: any = {}, ignore401 = false, deviceInfo = false): Observable<T> {
        return this.srd.pipe(filter((val): val is SRD => !!val), take(1), mergeMap((srd: SRD) => {
            const requestBody = prepareRequestBody(body, srd);

            let ticketResponse: ResponseJson;
            return this.http.post<ResponseJson>(srd.collectorUrl + url, requestBody, {headers: this.getHeaders(deviceInfo)}).pipe(switchMap((response) => {
                ticketResponse = response;
                return this.sendAndRetryOnError<T>(response, srd, 0);
            }), map(response => {
                response.ticketResponse = ticketResponse;
                if (!ignore401 && !response.success && (response.unauthorized || response.subscriberNotFound)) {
                    console.log(url);
                    this.send401(response, response.unauthorized);
                }
                return response;
            }));
        }));
    }

    sendSynchronousCollectorRequest<T extends ShoutResponse>(url: string, body: any = {}): Observable<T> {
        return this.srd.pipe(filter((val): val is SRD => !!val), take(1),
            switchMap(srd => {
                const requestBody = prepareRequestBody(body, srd);
                return this.http.post<T>(srd.collectorUrl + url, requestBody, { headers: this.getHeaders(false) }).pipe(
                    map(response => {
                        return response;
                    })
                );
            })
        );
    }

    getWdsDoc<T>(url: string): Observable<T> {
        return this.srd.pipe(filter((val): val is SRD => !!val), take(1), switchMap((srd: SRD) => {
            return this.http.get<T>(srd.wdsUrl + url);
        }));
    }

    private sendAndRetryOnError<T>(json: ResponseJson, srd: SRD, attempt: number): Observable<T> {
        // TODO: these times are super long to support generating a large payout table
        const retryTimes = [500, 1000, 3000, 6000];

        const request = this.http.get<T>(`${srd.wdsUrl}/${json.ticket}/response.json`).pipe(
            catchError((error) => {
                if (error.status === 404 && attempt <= retryTimes.length) {
                    const retryTime = retryTimes[attempt - 1];
                    return observableTimer(retryTime).pipe(switchMap(() =>
                        this.sendAndRetryOnError<T>(json, srd, attempt + 1)
                    ))
                } else if (error.status === 401) {
                    this.vxDialog.open(AlertDialogComponent, {
                        title: 'Error',
                        body: 'Session expired.  Please log back in.',
                        buttons: ['Ok']
                    });
                    this.on401.next();
                }

                return of({success: false, timedOut: true}) as any as Observable<T>;
            })
        );

        return observableTimer(json.estimatedWaitTime).pipe(switchMap(() => request));
    }

    private getHeaders(deviceInfo: boolean): HttpHeaders {
        const deviceHeaders: {[key: string]: string} = deviceInfo ? {
            deviceName: this.deviceInfo.name,
            deviceModel: this.deviceInfo.model,
            deviceVersion: this.deviceInfo.version,
            deviceOsName: this.deviceInfo.osName,
            deviceOsType: this.deviceInfo.osType
        } : {};

        let headers = new HttpHeaders({
            'X-REST-DEVICE-ID': this.deviceId,
            'X-REST-APPLICATION-ID': 'SnowyOwl',
            'X-REST-APPLICATION-VERSION': '1.0',
            'Content-Type': 'application/x-www-form-urlencoded',
            ...deviceHeaders
        });

        if (this.sessionKey) {
            headers = headers.set('X-REST-SESSION-KEY', this.sessionKey);
        }

        return headers;
    }

    private send401(message?: any, unauthorized = false): void {
        this.on401.next();

        if (this.dialog401)
            return;

        console.error(message);
        this.dialog401 = this.vxDialog.open(AlertDialogComponent, {
            title: 'Error',
            body: unauthorized ? 'You are not authorized to use the admin console.' : 'Session expired.  Please log back in.',
            buttons: ['Ok']
        });
        this.dialog401.onClose.subscribe(() => {
            this.dialog401 = undefined;
        })
    }

    private loadSrd(): void {
        if (this.srd.value)
            return;

        if (environment.srd) {
            this.srd.next(environment.srd);
            return;
        }
        this.http.get<SRDUrl>('assets/srd-url.json').subscribe((url: SRDUrl) => {
            this.http.get<SRD>(url.srd).subscribe((srd: SRD) => {
                this.srd.next(srd);
            });
        });
    }
}


function prepareRequestBody(body: any, srd: SRD): string {
    body.toWds = srd.wdsUrl;
    body.appId = 'snowyowl';

    const params = new URLSearchParams();

    for (const key in body) {
        if (body.hasOwnProperty(key) && body[key] !== undefined) {
            if (body[key] && body[key].toJSON) {
                body[key] = body[key].toJSON();
            }

            params.set(key, body[key]);
        }
    }


    return params.toString();
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
