/* SystemJS module definition */
declare var module: NodeModule;

interface NodeModule {
    id: string;
    hot: any;
}

declare module 'uuid/v4' {
    function f(): string;

    namespace f {
    }
    export = f;
}

declare module 'credit-card-regex' {
    function f(a?: any): RegExp;

    namespace f {
    }
    export = f;
}

declare let _LTracker: any[];
declare module 'offline-js';

interface IAccept {
    dispatchData(secureData: SecureData, callback: (DispatchResponse) => void): void;
}

interface SecureData {
    authData: AuthData;
    cardData: CardData;
}

interface AuthData {
    clientKey: string;
    apiLoginID: string;
}

interface DispatchResponse {
    messages: {
        resultCode: string;
        message: Array<{ code: string, text: string }>
    };
    opaqueData: {
        dataDescriptor: string;
        dataValue: string;
    }
}

declare let Accept: IAccept;
declare module 'progressbar.js';

interface Window {
    mozRequestAnimationFrame: any;
    requestAnimFrame: any;
}
