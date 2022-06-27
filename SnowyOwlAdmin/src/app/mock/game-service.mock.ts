import { Mock } from 'app/mock/mock';
import { Game } from 'app/model/game';
import { Round } from 'app/model/round';

//noinspection TsLint
const allGames: Game[] = [
    new Game({
        id: 'id1',
        // createDate: new Date(),
        // expectedStartDate: new Date(),
        gameNames: {en: 'game 1'},
        gameStatus: 'PENDING',
        allowableLanguageCodes: ['en']
    }),
    new Game({
        id: 'id2',
        // createDate: new Date(),
        // expectedStartDate: new Date(),
        gameNames: {en: 'game 2'},
        gameStatus: 'PENDING',
        allowableLanguageCodes: ['en']
    }),
    new Game({
        id: 'id3',
        // createDate: new Date(),
        // expectedStartDate: new Date(),
        gameNames: {en: 'game 3'},
        gameStatus: 'PENDING',
        allowableLanguageCodes: ['en']
    }),
    new Game({
        id: 'id4',
        // createDate: new Date(),
        // expectedStartDate: new Date(),
        gameNames: {en: 'game 4'},
        gameStatus: 'PENDING',
        allowableLanguageCodes: ['en']
    }),
    new Game({
        id: 'id5',
        // createDate: new Date(),
        // expectedStartDate: new Date(),
        gameNames: {en: 'game 5'},
        gameStatus: 'OPEN',
        allowableLanguageCodes: ['en']
    }),
    new Game({
        id: 'id6',
        // createDate: new Date(),
        // expectedStartDate: new Date(),
        gameNames: {en: 'game 6'},
        gameStatus: 'OPEN',
        allowableLanguageCodes: ['en']
    }),
    new Game({
        id: 'id7',
        // createDate: new Date(),
        // expectedStartDate: new Date(),
        gameNames: {en: 'game 7'},
        gameStatus: 'INPLAY',
        allowableLanguageCodes: ['en']
    }),
    new Game({
        id: 'id8',
        // createDate: new Date(),
        // expectedStartDate: new Date(),
        gameNames: {en: 'game 8'},
        gameStatus: 'INPLAY',
        allowableLanguageCodes: ['en']
    }),
    new Game({
        id: 'id9',
        // createDate: new Date(),
        // expectedStartDate: new Date(),
        gameNames: {en: 'game 9'},
        gameStatus: 'INPLAY',
        allowableLanguageCodes: ['en']
    }),
    new Game({
        id: 'id10',
        // createDate: new Date(),
        // expectedStartDate: new Date(),
        gameNames: {en: 'game 10'},
        gameStatus: 'INPLAY',
        allowableLanguageCodes: ['en']
    }),
    new Game({
        id: 'id11',
        // createDate: new Date(),
        // expectedStartDate: new Date(),
        gameNames: {en: 'game 11'},
        gameStatus: 'CLOSED',
        allowableLanguageCodes: ['en']
    }),
    new Game({
        id: 'id12',
        // createDate: new Date(),
        // expectedStartDate: new Date(),
        gameNames: {en: 'game 12'},
        gameStatus: 'CLOSED',
        allowableLanguageCodes: ['en']
    }),
    new Game({
        id: 'id13',
        // createDate: new Date(),
        // expectedStartDate: new Date(),
        gameNames: {en: 'game 13'},
        gameStatus: 'CLOSED',
        allowableLanguageCodes: ['en']
    }),
    new Game({
        id: 'id14',
        // createDate: new Date(),
        // expectedStartDate: new Date(),
        gameNames: {en: 'game 14'},
        gameStatus: 'CANCELLED',
        allowableLanguageCodes: ['en']
    }),
    new Game({
        id: 'id15',
        // createDate: new Date(),
        // expectedStartDate: new Date(),
        gameNames: {en: 'game 15'},
        gameStatus: 'CANCELLED',
        allowableLanguageCodes: ['en']
    })
];

const mockRounds = [
    new Round({id: 'round1', roundNames: {en: 'Round 1'}, roundStatus: 'PENDING', roundSequence: 1, roundType: 'POOL'}),
    new Round({id: 'round2', roundNames: {en: 'Round 2'}, roundStatus: 'PENDING', roundSequence: 2, roundType: 'POOL'}),
    new Round({id: 'round3', roundNames: {en: 'Round 3'}, roundStatus: 'PENDING', roundSequence: 3, roundType: 'POOL'}),
    new Round({id: 'round4', roundNames: {en: 'Round 4'}, roundStatus: 'PENDING', roundSequence: 4, roundType: 'BRACKET'})
];
function listGames(data: any) {
    const games = allGames.filter(game => data.statuses.indexOf(game.gameStatus) !== -1);
    return {success: true, games: games}
}

export const GAME_SERVICE_MOCK = new Mock([
    {match: /smadmin\/games\/list/, loadData: listGames},
    {match: /smadmin\/game\/getrounds/, data: {success: true, rounds: mockRounds}},
    {match: /smadmin\/game\/beginPoolPlay/, data: {success: true}},
    {match: /smadmin\/game\/beginBracketPlay/, data: {success: true}},
    {match: /smadmin\/game\/cancel/, data: {success: true}},
    {match: /smadmin\/game\/open/, data: {success: true}},
    {
        match: /smadmin\/game\/create/, loadData: (data) => {
        allGames.push(new Game(JSON.parse(data.game)));
        return {success: true}
    }
    },
    {match: /smadmin\/game\/clone/, data: {success: true}}
]);
