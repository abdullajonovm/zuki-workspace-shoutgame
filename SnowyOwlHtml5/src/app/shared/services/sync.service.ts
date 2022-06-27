import {Injectable} from '@angular/core';
import io from 'socket.io-client';
import {HttpService, ShoutResponse} from './http.service';
import {SyncMessage} from 'app/model/sync-message';
import {Subject} from 'rxjs/Subject';
import {LogService} from 'app/shared/services/log.service';
import {DialogService} from 'app/shared/dialog/dialog.service';
import {SRD} from '../../model/srd';

@Injectable()
export class SyncService {
    syncMessages = new Subject<SyncMessage>();
    private socket?: SocketIOClient.Socket;

    constructor(private http: HttpService, private logService: LogService, private dialog: DialogService) {

    }

    async connect(primaryIdHash: string): Promise<void> {
        if (this.socket)
            return;

        const srd: SRD = await this.http.srd.filter((val) => !!val).take(1).toPromise() as any;
        const socket = io(srd.socketIOUrl, {
            forceNew: true
        });
        socket.on('duplicate_checkin', () => {
            this.disconnect();
            const dialog = this.dialog.open({
                body: 'Your account is already open on another browser / app.  Please close the other browser / app and press retry to continue.',
                buttons: [{text: 'Retry'}],
                disableClose: true,
                width: '350px'
            });
            dialog.onClose.subscribe(() => {
                this.connect(primaryIdHash);
            })
        });
        socket.on('connect', () => {
            socket.emit('client_checkin', primaryIdHash);
        });
        socket.on('sync_message', (syncMessage: string) => {
            this.syncMessages.next(SyncMessage.createSyncMessage(JSON.parse(syncMessage)));
        });
        socket.on('arbitrary_message', (test: string) => {
            this.logService.log({
                message: 'ARBITRARY_MESSAGE',
                body: test
            });
        });
        this.socket = socket;
    }

    disconnect(): void {
        if (this.socket) {
            this.socket.close();
            this.socket = undefined;
        }
    }

    async getSyncMessagesForGame(gameId: string, fromDate = new Date('2001-07-19T22:17:00.683Z')): Promise<SyncMessage[]> {
        const resp = await this.http.sendCollectorRequest<SyncMessageResponse>('/snowl/game/getSyncMessages', {
            fromDate,
            gameId
        }, false).toPromise();
        return resp.syncMessages.map(sm => SyncMessage.createSyncMessage(sm)).sort((a, b) => a.createDate > b.createDate ? 1 : -1);
    }
}

interface SyncMessageResponse extends ShoutResponse {
    syncMessages: SyncMessage[];
}
