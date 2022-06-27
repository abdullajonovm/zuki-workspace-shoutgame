import { Mock } from 'app/mock/mock';
import { Category } from 'app/model/category';

const categories = [
    {key: 'key1'},
    {key: 'key2'},
    {key: 'key3'},
    {key: 'key4'},
    {key: 'key5'}
];

export const CATEGORY_SERVICE_MOCK = new Mock([
    {match: /smadmin\/categories\/getIdsToKeys/, data: {success: true, result: categories}}
]);
