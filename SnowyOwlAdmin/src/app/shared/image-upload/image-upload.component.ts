import {ChangeDetectionStrategy, ChangeDetectorRef, Component, ElementRef, EventEmitter, HostBinding, HostListener, Input, NgZone, Output, Renderer2, ViewChild} from '@angular/core';
import {VxDialog} from 'vx-components';
import {DomSanitizer, SafeResourceUrl} from '@angular/platform-browser';
import { AlertDialogComponent } from '../alert-dialog/alert-dialog.component';
import {Asyncify} from '../services/util/asyncify';

type kludge = DragEvent;
@Component({
    selector: 'app-image-upload',
    templateUrl: './image-upload.component.html',
    styleUrls: ['./image-upload.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ImageUploadComponent {
    @HostBinding('class.dragged')
    draggingIn = false;


    @ViewChild('input', { static: true })
    input!: ElementRef;

    @Input()
    set image(image: SafeResourceUrl | string) {
        if (typeof image === 'string') {
            this.getBlobForUrl(image, true).then(blob => {
                this.handleFile(blob, true);
            });
        }
    }

    @Input()
    @HostBinding('class.disabled')
    disabled = false;

    @Output()
    readonly blobChange = new EventEmitter<Blob>();

    loading = false;

    blob?: Blob;
    constructor(private vxDialog: VxDialog, private cdr: ChangeDetectorRef, private zone: NgZone,
                private el: ElementRef, private renderer: Renderer2) {
    }

    @HostListener('dragenter.out-zone', ['$event', 'true'])
    @HostListener('dragover.out-zone', ['$event', 'true'])
    @HostListener('dragleave.out-zone', ['$event', 'false'])
    @HostListener('dragend.out-zone', ['$event', 'false'])
    onDrag(e: Event, inside: boolean): void {
        if (inside !== this.draggingIn) {
            this.zone.run(() => this.draggingIn = inside);
        }
        e.stopPropagation();
        e.preventDefault();
    }

    @HostListener('drop', ['$event'])
    async onDrop(e: kludge): Promise<void> {
        e.stopPropagation();
        e.preventDefault();
        this.draggingIn = false;

        const dt = e.dataTransfer;
        if (!dt) {
            return;
        }

        if (dt.files.length) {
            this.handleFile(dt.files[0]);
        } else {
            const url = dt.getData('text/html') || dt.getData('url') || dt.getData('text/plain') || dt.getData('text/uri-list');
            if (!url)
                return;

            const imageRegex = /img[\s\S]*?src="([\s\S]*?)"/i;

            console.log('Attempting to handle url: ' + url)
            const match = url.match(imageRegex);
            if (match && match.length) {
                console.log('Found match: ', match[1]);
                const blob = await this.getBlobForUrl(match[1]);
                this.handleFile(blob);
            }
        }
    }

    @HostListener('click')
    onClick(): void {
        this.input.nativeElement.click();
    }

    async handleFile(file: Blob | null, skipChange = false): Promise<void> {
        this.loading = true;
        if (file && file.type.includes('image')) {
            const image = URL.createObjectURL(file);
            this.renderer.setStyle(this.el.nativeElement, 'background', `url(${image})`);

            // await Asyncify.resolveWhenCalled(img, 'onload');
            // ctx.clearRect(0, 0, canvas.width, canvas.height);
            // document.body.appendChild(img);
            // ctx.drawImage(img, 0, 0, canvas.width, canvas.height);

            this.loading = false;

            this.blob = file;
            this.cdr.detectChanges();

            if (!skipChange)
                this.blobChange.emit(this.blob);
        }
    }

    async getBlobForUrl(url: string, hideError = false): Promise<Blob | null> {
        this.loading = true;
        const img = document.createElement('img') as HTMLImageElement;
        const error = () => {
            if (!hideError) {
                this.vxDialog.open(AlertDialogComponent, {
                    title: 'Error',
                    body: 'Failed to get image to download.  Please save and then upload it.',
                    buttons: ['Dismiss']
                });
            }
            this.loading = false;
            this.cdr.detectChanges();
        };

        img.onerror = error;
        img.crossOrigin = 'Anonymous';
        img.src = url;
        await Asyncify.resolveWhenCalled(img, 'onload');

        try {
            const canvas = document.createElement('canvas');
            canvas.width = img.width;
            canvas.height = img.height;
            const context = canvas.getContext('2d')!;
            context.drawImage(img, 0, 0);
            const [blob] = await Asyncify.callback(canvas, 'toBlob', '__callback__', 'image/jpeg');
            return blob;
        } catch (e) {
            console.log('Error with image: ', e);
            error();
            return null;
        }
    }
}
