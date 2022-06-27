import {Injectable} from '@angular/core';
import {HttpService, ShoutResponse} from 'app/shared/services/http.service';
import {Router} from '@angular/router';
import {Subscriber} from 'app/model/subscriber';
import {VxDialog} from 'vx-components';
import {encryptKeyLS, sessionKeyLS, sha256LS, subscriberLS} from '../../constants';

import SHA256 from 'crypto-js/sha256';
import Latin1 from 'crypto-js/enc-latin1';
import WordArray from 'crypto-js/lib-typedarrays';
import AES from 'crypto-js/aes';
import {IneligibleSub} from '../../model/ineligible-sub';
import { Observable, of } from 'rxjs';
import {map} from 'rxjs/operators';
import { AlertDialogComponent } from '../alert-dialog/alert-dialog.component';

@Injectable()
export class SubscriberService {

    subscriber?: Subscriber;
    allSubscribers: Subscriber[] = [];
    ineligibleSubs: IneligibleSub[] = [];

    private allRoles?: string[];
    constructor(private httpService: HttpService, private router: Router, private vxDialog: VxDialog) {
        this.httpService.on401.subscribe(() => {
            this.logout();
        });

        const subscriber = localStorage.getItem(subscriberLS);
        if (subscriber) {
            this.subscriber = new Subscriber(JSON.parse(subscriber));
        }
        this.httpService.on401.subscribe(() => {
            this.logout();
        })
    }

    isLoggedIn(): boolean {
        return !!this.httpService.sessionKey;
    }

    async login(username: string, password: string): Promise<void> {
        const sha256Password = SHA256(password);

        try {
            const json = await this.httpService.sendRequest<LoginJson>('/auth/login', {
                email: username,
                password: sha256Password
            }, false, true).toPromise();
            this.parseLoginJson(json)
        } catch (error) {
            this.vxDialog.open(AlertDialogComponent, {
                title: 'Error logging in ',
                body: JSON.stringify(error),
                buttons: ['Ok']
            });
        }
    }

    private async parseLoginJson(json: LoginJson): Promise<void> {
        if (json.success) {
            const subscriberJson = JSON.parse(decryptAES(json.ticketResponse.encryptKey, json.subscriber));
            json.sessionKey = decryptAES(json.ticketResponse.encryptKey, json.sessionKey);

            this.subscriber = new Subscriber(subscriberJson);

            this.httpService.sessionKey = json.sessionKey;
            this.httpService.encryptKey = this.subscriber.encryptKey;
            this.httpService.sha256Hash = this.subscriber.sha256Hash;

            localStorage.setItem(sessionKeyLS, this.httpService.sessionKey);
            localStorage.setItem(encryptKeyLS, this.httpService.encryptKey);
            localStorage.setItem(sha256LS, this.httpService.sha256Hash);

            await this.loadSubscriber();

            localStorage.setItem(subscriberLS, this.subscriber.toJson());

            this.router.navigate(['/game']);

        } else {
            this.vxDialog.open(AlertDialogComponent, {
                title: 'Error',
                body: 'Invalid username / password.',
                buttons: ['Ok']
            });
        }
    }

    logout(): void {
        localStorage.removeItem(sessionKeyLS);
        localStorage.removeItem(encryptKeyLS);
        localStorage.removeItem(sha256LS);
        localStorage.removeItem(subscriberLS);
        this.subscriber = undefined;
        this.httpService.sessionKey = undefined;
        this.httpService.encryptKey = undefined;
        this.httpService.sha256Hash = undefined;
        this.router.navigate(['/login']);
    }

    sendBonusCash(nickname: string, amount: number): Promise<ShoutResponse> {
        return this.httpService.sendRequest('/snowladmin/subscriber/giveBonusCash', {nickname, amount}, true).toPromise();
    }

    async loadSubscriber(): Promise<void> {
        // TODO: load the subscriber
        // const result = await this.httpService.sendRequest<SubscriberResponse>('/subscriber/getPrivateProfile').toPromise()
        // this.subscriber = new Subscriber({...this.subscriber, ...result.profile});
    }

    async loadAllSubscribers(): Promise<void> {
        this.allSubscribers =  await this.searchForSubscribers({fromDate: new Date(2010, 1), toDate: new Date()}).toPromise();
    }

    searchForSubscribers(options: SubscriberSearchOptions): Observable<Subscriber[]> {
        return this.httpService.sendRequest<SearchSubsResponse>('/snowladmin/subscriber/search', options).pipe(
            map(resp => resp.subscribers.map(sub => new Subscriber(sub)))
        );
    }

    async loadIneligibleSubscribers(): Promise<void> {
        const resp = await this.httpService.sendRequest<IneligibleSubsResponse>('/snowladmin/ineligibleSubscriber/get').toPromise();
        this.ineligibleSubs = resp.ineligibleSubscribers.map(is => new IneligibleSub(is));
    }

    async addIneligibleSubscriber(email: string, reason: string, linkedEmail?: string): Promise<ShoutResponse> {
        return await this.httpService.sendRequest('/snowladmin/ineligibleSubscriber/insert', {
            email, reason, linkedEmail
        }).toPromise();
    }

    async removeIneligibleSubscriber(isId: number): Promise<ShoutResponse> {
        return await this.httpService.sendRequest('/snowladmin/ineligibleSubscriber/delete', {isId}).toPromise();
    }

    getRoles(): Observable<string[]> {
        if (this.allRoles) {
            return of(this.allRoles);
        }

        return this.httpService.sendRequest<RolesResponse>('/snowladmin/role/list').pipe(map(r => {
            this.allRoles = r.roles;
            return r.roles
        }));
    }

    getSubscriberRoles(email: string): Observable<string[]> {
        return this.httpService.sendRequest<RolesResponse>('/snowladmin/role/get', {email}).pipe(map(r => r.roles));
    }

    addRole(email: string, role: string): Observable<ShoutResponse> {
        return this.httpService.sendRequest('/snowladmin/role/add', {email, role});
    }

    removeRole(email: string, role: string): Observable<ShoutResponse> {
        return this.httpService.sendRequest('/snowladmin/role/remove', {email, role});
    }
}

interface SubscriberSearchOptions {
    role?: string;
    fromDate?: Date;
    toDate?: Date;
    partialEmail?: string;
}

interface RolesResponse extends ShoutResponse {
    roles: string[];
}

interface SubscriberResponse extends ShoutResponse {
    profile: Subscriber;
}

interface SearchSubsResponse extends ShoutResponse {
    subscribers: Subscriber[];
}

interface IneligibleSubsResponse extends ShoutResponse {
    ineligibleSubscribers: IneligibleSub[];
}

export interface LoginJson extends ShoutResponse {
    success: boolean;
    sessionKey: string;
    subscriber: string
}

function decryptAES(encryptKey: string, text: string): string {
    const all = Latin1.parse(encryptKey);
    const key = WordArray.create(all.words.slice(0, 4));
    const ivWords = WordArray.create(all.words.slice(4, 8));
    const secretWords = Latin1.parse(text);
    const decrypted = AES.decrypt(secretWords.toString(Latin1), key, {iv: ivWords});
    return Latin1.stringify(decrypted);
}
