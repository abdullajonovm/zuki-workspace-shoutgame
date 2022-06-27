import {Pipe, PipeTransform} from '@angular/core';
import {GameService} from './services/game.service';
import {Category} from '../model/category';

@Pipe({
    name: 'categoryName'
})
export class CategoryNamePipe implements PipeTransform {
    constructor(private gameService: GameService) {

    }

    async transform(category: string): Promise<Category> {
        const cat = await this.gameService.loadCategory(category);
        return cat.categoryName;
    }

}
