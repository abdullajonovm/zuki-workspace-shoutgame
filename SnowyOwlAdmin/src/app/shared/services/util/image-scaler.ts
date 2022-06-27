import {Asyncify} from './asyncify';

export class ImageScaler {
    static async resizeImage(file: Blob, maxSize: number): Promise<Blob> {
        if (file.size <= maxSize)
            return file;

        const img = new Image();
        img.src = URL.createObjectURL(file);
        await Asyncify.resolveWhenCalled(img, 'onload');

        if (!file.type.match(/jpe?g/)) {
            return this.resizeImage(await makeJpeg(img), maxSize);
        }
        return this.resizeImage(await scaleImage(img, maxSize / file.size), maxSize);
    }
}

async function makeJpeg(img: HTMLImageElement): Promise<Blob> {
    const canvas = document.createElement('canvas');
    canvas.width = img.width;
    canvas.height = img.height;
    const ctx = canvas.getContext('2d')!;
    ctx.drawImage(img, 0, 0, img.width, img.height);
    const [blob] = await Asyncify.callback(canvas, 'toBlob', '__callback__', 'image/jpeg');
    return blob as Blob;
}

async function scaleImage(img: HTMLImageElement, ratio: number): Promise<Blob> {
    ratio += (1 - ratio) / 3;
    const newW = img.width * ratio;
    const newH = img.height * ratio;
    const canvas = document.createElement('canvas');
    canvas.width = newW;
    canvas.height = newH;
    const ctx = canvas.getContext('2d')!;
    ctx.drawImage(img, 0, 0, newW, newH);
    const [blob] = await Asyncify.callback(canvas, 'toBlob', '__callback__', 'image/jpeg');
    return blob as Blob;
}
