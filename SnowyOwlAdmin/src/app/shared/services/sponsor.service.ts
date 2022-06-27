import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpService, ShoutResponse } from './http.service';

@Injectable()
export class SponsorService {
    constructor(private http: HttpService) {
    }

    addSponsorPlayersToGame(gameId: string, numberOfPlayers: number, sponsorEmail: string): Observable<AddSponsorPlayersResponse> {
        return this.http.sendRequest<AddSponsorPlayersResponse>('/snowladmin/game/addSponsorPlayers', {gameId, numberOfPlayers, sponsorEmail});
    }

    addCashToSponsor(sponsorEmail: string, amount: number): Observable<AddCashResponse> {
        return this.http.sendRequest<AddCashResponse>('/snowladmin/sponsor/addCash', {sponsorEmail, amount});
    }
}

interface AddSponsorPlayersResponse extends ShoutResponse {
    notEnoughSponsorPoolCash?: boolean;
    notASponsor?: boolean;
    invalidParam?: boolean;
}

interface AddCashResponse extends ShoutResponse {
    notASponsor?: boolean;
}
