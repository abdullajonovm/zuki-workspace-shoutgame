import {Directive, Input} from '@angular/core';

@Directive({
    selector: '[default]',
    host: {
        '[attr.src]': 'src',
        '[style.backgroundImage]': `'url(' + backgroundImage + ')'`
    }
})
export class DefaultImageDirective {
    @Input() src: string;
    @Input() backgroundImage: string;

    @Input('default')
    set defaultImg(img: string) {
        const origSrc = this.src;
        const origBack = this.backgroundImage;
        if (img) {
            this.src = this.src ? img : '';
            this.backgroundImage = this.backgroundImage ? img : '';
            const image = document.createElement('img') as HTMLImageElement;
            image.onload = () => {
                if (this.src)
                    this.src = img;
                if (this.backgroundImage)
                    this.backgroundImage = img;
            };
            image.onerror = () => {
                if (origSrc)
                    this.src = origSrc;
                if (origBack)
                    this.backgroundImage = origBack;
            };
            image.src = img;
        }
    }

}
