import {autoserialize, autoserializeAs} from 'cerialize';
import {BaseModel} from 'app/model/base-model';
import {GamePlayer} from 'app/model/game-player';
import {Game} from 'app/model/game';
import {LocalizedText} from 'app/model/localized-text';
import {PublicProfile} from './match-player';

export class Subscriber extends BaseModel<Subscriber> {

    @autoserialize subscriberId: number;
    @autoserialize encryptKey: string;
    @autoserialize nickname: string;
    @autoserialize photoUrl: string;
    @autoserialize firstName: string;
    @autoserialize lastName: string;
    @autoserialize email: string;
    @autoserialize languageCode: keyof LocalizedText;
    @autoserialize phone?: string;
    @autoserialize appId: number;
    @autoserialize countryCode: string;
    @autoserializeAs(Date) dateOfBirth: Date;

    roundNotificationPref?: 'NONE' | 'SMS' | 'EMAIL';
    wallet = 0;
    availableWallet = 0;
    gamePlayers: GamePlayer[] = [];
    games: Game[] = [];
    publicProfile?: PublicProfile;

    clear(): void {
        this.subscriberId = 0;
        this.encryptKey = '';
        this.nickname = '';
        this.photoUrl = '';
        this.gamePlayers = [];
        this.games = [];
        this.wallet = 0;
        this.languageCode = 'en';
    }
}
