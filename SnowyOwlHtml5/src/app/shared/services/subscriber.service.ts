import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Subscriber} from 'app/model/subscriber';
import {HttpService, ShoutErrorResponse, ShoutResponse, ShoutSuccessResponse} from 'app/shared/services/http.service';
import {GamePlayer} from 'app/model/game-player';
import {Util} from 'app/util';
import {LoginOrSignupError} from 'app/shared/error/signup-error';
import {MatchPlayer, PublicProfile} from 'app/model/match-player';
import {SyncService} from './sync.service';
import {LogService} from 'app/shared/services/log.service';
import {encryptKeyLS, sessionKeyLS, subscriberLS} from '../../constants';
import {SubscriberAddress} from '../../model/subscriber-address';
import {Country} from '../../model/country';

// TODO: find out why local storage gets cleared on occasion
@Injectable()
export class SubscriberService {

    subscriber = new Subscriber(); // Will be populated when we have more info
    publicProfiles: { [id: string]: PublicProfile } = {};

    redirectUrl?: string;

    private countries: Country[] = [];
    constructor(private httpService: HttpService, private router: Router,
                private syncService: SyncService, private logService: LogService) {
        this.httpService.on401.subscribe(() => {
            this.logout();
        });

        const subscriber = localStorage.getItem(subscriberLS);
        if (subscriber) {
            this.subscriber.fillFromJson(JSON.parse(subscriber));
            this.handleSubscriber();
        }

    }

    isLoggedIn(): boolean {
        return !!this.httpService.sessionKey;
    }

    async login(email: string, password: string): Promise<void> {
        const sha256Password = Util.sha256(password);
        console.log('PASSWORD SHA256 LOGIN:' + sha256Password);

        const loginResp = await this.httpService.sendCollectorRequest<LoginOrSignupJson>('/auth/login', {
            email,
            password: sha256Password
        }, false).toPromise();
        this.parseLoginJson(loginResp);
    }

    async signup(email: string, password: string, birthDate: Date, nickName: string): Promise<void> {
        password = Util.sha256(password);
        console.log('PASSWORD SHA256 SIGNUP:' + password);
        password = await Util.scryptString(password);
        console.log('PASSWORD SCRYPT SIGNUP:' + password);

        const loginResp = await this.httpService.sendCollectorRequest<LoginOrSignupJson>('/auth/signup', {
            email,
            password,
            nickName,
            birthDate,
            languageCode: 'en', // TODO: don't hardcode these
            countryCode: 'US'
        }, false).toPromise();
        this.parseLoginJson(loginResp);
    }

    async update(toUpdate: Partial<Subscriber>): Promise<void> {
        const toSend: any = toUpdate;
        toSend.nickName = toUpdate.nickname;

        const updateResp = await this.httpService.sendCollectorRequest<LoginOrSignupJson>('/subscriber/update', toSend).toPromise();

        if (updateResp.success) {
            this.subscriber.fillFromJson(toUpdate);
            localStorage.setItem(subscriberLS, this.subscriber.toJSONString());
        } else {
            throw new LoginOrSignupError(updateResp);
        }
    }

    async loadPublicProfile(subscriber: MatchPlayer | Subscriber): Promise<void> {
        if (this.publicProfiles[subscriber.subscriberId]) {
            subscriber.publicProfile = this.publicProfiles[subscriber.subscriberId];
            return;
        }

        const profileResponse = await this.httpService.sendCollectorRequest<ProfileResponse>('/subscriber/getPublicProfile', {
            subscriberId: subscriber.subscriberId
        }).toPromise();

        if (!profileResponse.success) {
            this.logService.error('Failed to get public profile for subscriber', subscriber, 'Response: ', profileResponse);
            return;
        }

        const profile = profileResponse.profile;

        this.publicProfiles[subscriber.subscriberId] = profile;
        subscriber.publicProfile = profile;

        if (subscriber.subscriberId === this.subscriber.subscriberId) {
            this.subscriber.photoUrl = profile.photoUrl;
            this.subscriber.nickname = profile.nickname;
            localStorage.setItem(subscriberLS, this.subscriber.toJSONString());
        }
    }

    async loadPlayerDetails(): Promise<void> {
        const info = await this.httpService.sendCollectorRequest<SubscriberInfoJson>('/snowl/player/details').toPromise();
        this.subscriber.wallet = Math.round(info.balance);
        this.subscriber.availableWallet = info.availableBalance;
        this.subscriber.gamePlayers = info.gamePlayers.map(g => new GamePlayer(g));
        await this.loadPublicProfile(this.subscriber);
        if (this.subscriber.publicProfile)
            this.syncService.connect(this.subscriber.publicProfile.primaryIdHash);
    }

    async sendVerificationCode(phone: number): Promise<void> {
        await this.httpService.sendCollectorRequest('/snowl/phone/sendVerificationCode', {phone}).toPromise();
    }

    async verify(code: number): Promise<ShoutResponse> {
        return await this.httpService.sendCollectorRequest('/snowl/phone/verifyCode', {code}).toPromise();
    }

    async setNotificationPreference(pref: 'NONE' | 'SMS' | 'EMAIL'): Promise<ShoutResponse> {
        const resp = await this.httpService.sendCollectorRequest('/snowl/notification/setPref', {
            prefValue: pref,
            prefType: 'ROUND_START'
        }).toPromise();
        this.loadNotificationPreference();
        return resp;
    }

    async loadNotificationPreference(): Promise<void> {
        const ROUND_PREF_TYPE = 13;
        const resp = await this.httpService.sendCollectorRequest<PreferenceResponse>('/snowl/notification/getPrefs').toPromise();
        for (const pref of resp.prefs) {
            if (pref.prefType === ROUND_PREF_TYPE) {
                this.subscriber.roundNotificationPref = pref.value;
            }
        }
    }

    async getCashPoolTransactions(types: CashPoolTransactionTypes[] = ['PAYOUT', 'ABANDONED_ROUND', 'JOINED_ROUND', 'PURCHASE']): Promise<any> {
        return this.httpService.sendCollectorRequest('/snowl/player/cashPoolTransactions', {cashPoolTransactionTypes: types.join()}).toPromise();
    }

    async addSubscriberAddress(address: SubscriberAddress): Promise<AddAddressResponse> {
        return this.httpService.sendCollectorRequest<AddAddressResponse>('/subscriber/address/add', {address: address.toJSONString()}).toPromise();
    }

    async getEmailsAndAddresses(): Promise<EmailsAddressesResponse> {
        const resp = await this.httpService.sendCollectorRequest<EmailsAddressesResponse>('/subscriber/getEmailsAndAddresses').toPromise();
        resp.addresses = resp.addresses.map(a => new SubscriberAddress(a));
        return resp;
    }

    async getCountries(): Promise<Country[]> {
        if (!this.countries.length) {
            const resp = await this.httpService.getWdsDoc<Country[]>('/countries.json').toPromise();
            this.countries = resp.map(c => new Country(c));
        }
        return this.countries;
    }


    logout(): void {
        localStorage.removeItem(sessionKeyLS);
        localStorage.removeItem(encryptKeyLS);
        localStorage.removeItem(subscriberLS);
        this.subscriber.clear();
        this.httpService.sessionKey = undefined;
        this.httpService.encryptKey = undefined;
        this.router.navigate(['/home/me']);
        this.syncService.disconnect();
    }

    private parseLoginJson(json: LoginOrSignupJson): void {
        if (json.success) {
            const subscriberJson = JSON.parse(Util.decryptAES(json.ticketResponse.encryptKey, json.subscriber));
            json.sessionKey = Util.decryptAES(json.ticketResponse.encryptKey, json.sessionKey);

            this.subscriber.fillFromJson(subscriberJson);

            this.httpService.sessionKey = json.sessionKey;
            this.httpService.encryptKey = this.subscriber.encryptKey;

            localStorage.setItem(sessionKeyLS, this.httpService.sessionKey);
            localStorage.setItem(encryptKeyLS, this.httpService.encryptKey);
            localStorage.setItem(subscriberLS, this.subscriber.toJSONString());
            this.handleSubscriber();
            this.loadNotificationPreference();
        } else {
            throw new LoginOrSignupError(json);
        }
    }

    private handleSubscriber(): void {
        this.logService.subscriber = this.subscriber;
        this.loadPlayerDetails();
        this.loadNotificationPreference();
    }

}

export type CashPoolTransactionTypes = 'PAYOUT' | 'PURCHASE' | 'JOINED_ROUND' | 'ABANDONED_ROUND';

type LoginOrSignupJson = LoginOrSignupJsonSuccess | LoginOrSignupJsonError;

interface LoginOrSignupJsonSuccess extends ShoutSuccessResponse {
    success: true;
    sessionKey: string;
    subscriber: string;
}

export interface LoginOrSignupJsonError extends ShoutErrorResponse {
    success: false;
    // signup
    emailAlreadyUsed?: boolean;
    nicknameAlreadyUsed?: boolean
    nicknameInvalid?: boolean;
    // login
    invalidLogin?: boolean;
    accountDeactivated?: boolean;
    passwordChangeRequired?: boolean;
}

interface SubscriberInfoJson extends ShoutResponse {
    balance: number;
    availableBalance: number;
    gamePlayers: GamePlayer[];
}

interface ProfileResponse extends ShoutResponse {
    profile: PublicProfile;
}

interface PreferenceResponse extends ShoutResponse {
    prefs: { prefType: number, value: 'NONE' | 'SMS' | 'EMAIL' }[];
}

interface EmailsAddressesResponse extends ShoutResponse {
    emails: string[];
    addresses: SubscriberAddress[];
}

interface AddAddressResponse extends ShoutResponse {
    addressId: number;
}
