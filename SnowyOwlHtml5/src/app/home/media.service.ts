import {Injectable} from '@angular/core';
import * as uuid from 'uuid/v4';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {HttpService} from '../shared/services/http.service';
import {SRD} from '../model/srd';

@Injectable()
export class MediaService {

    constructor(private http: HttpClient, private httpService: HttpService) {

    }

    async uploadImage(imageBlob: Blob): Promise<string> {
        const srd: SRD = await this.httpService.srd.filter((val) => !!val).take(1).toPromise() as any;


        const extension = imageBlob.type.replace('image/', '').replace('jpeg', 'jpg');
        const name = uuid() + '.' + extension;
        const formData: FormData = new FormData();
        formData.append('file', imageBlob, name);

        let headers = new HttpHeaders();
        headers = headers.append('Accept', 'application/json');

        await this.http.post(`${srd.mediaUrl}/${name}`, formData, {headers, responseType: 'text'}).toPromise();

        return `${srd.mediaUrl}/${name}`;
    }

}
