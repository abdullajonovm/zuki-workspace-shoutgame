// The file contents for the current environment will overwrite these during build.
// The build system defaults to the dev environment which uses `environment.ts`, but if you do
// `ng build --env=prod` then `environment.prod.ts` will be used instead.
// The list of which env maps to which file can be found in `.angular-cli.json`.

import {STRINGS_EN} from 'i18n/en';

export const environment = {
    production: false,
    hmr: true,
    strings: STRINGS_EN,
    languageCode: 'en',
    srd: {
        mediaUrl: 'https://snowl-wms-origin--0--nc11-1.shoutgameplay.com',
        socketIOUrl: 'https://snowl-socketio--0--nc11-1.shoutgameplay.com',
        wdsUrl: 'https://snowl-wds-origin--0--nc11-1.shoutgameplay.com',
        collectorUrl: 'https://snowl-collector--0--nc11-1.shoutgameplay.com'
    }
};
