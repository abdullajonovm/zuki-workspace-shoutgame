import {Injectable} from '@angular/core';
import {HttpService, ShoutResponse} from '../shared/services/http.service';
import {map} from 'rxjs/operators';

@Injectable()
export class OtherService {
    constructor(private http: HttpService) {

    }

    setCanSeeContentWithoutLogin(canSeeContentWithoutLogin: boolean): Promise<ShoutResponse> {
        return this.http.sendRequest('/snowladmin/setCanSeeContentWithoutLogin', {canSeeContentWithoutLogin}).toPromise();
    }

    getCanSeeContentWithoutLogin(): Promise<boolean> {
        return this.http.getWdsDoc<CanSeeContentResponse>('/canSeeContentWithoutLogin.json').pipe(
            map(resp => {
                return resp.canSeeContentWithoutLogin;
            })
        ).toPromise();
    }
}

interface CanSeeContentResponse {
    canSeeContentWithoutLogin: boolean;
}
