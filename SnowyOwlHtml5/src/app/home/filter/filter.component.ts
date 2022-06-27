import {Component, ElementRef, EventEmitter, OnDestroy, Output, ViewChild} from '@angular/core';
import {Category} from '../../model/category';
import {GameService} from '../../shared/services/game.service';
import {Observable} from 'rxjs/Observable';
import {Subject} from 'rxjs/Subject';
import 'rxjs/add/operator/takeUntil';
import {VxDialogComponent} from 'vx-components/dialog/dialog.component';
import {VxDropdownComponent} from 'vx-components';

@Component({
    selector: 'sh-search',
    templateUrl: './filter.component.html',
    styleUrls: ['./filter.component.scss']
})
export class FilterComponent implements OnDestroy {
    searching = false;
    categories: Category[] = [];
    allCategories = true;
    selectedCategories: Category[] = [];
    @ViewChild('drop') dropdown: VxDropdownComponent;
    @ViewChild('drop', {read: ElementRef}) dropdownEl: ElementRef;

    @Output() onSelect = new EventEmitter<CategoryFilter>();

    private ngOnDestroyed = new Subject();
    constructor(private gameService: GameService) {
        Observable.fromEvent(window, 'click', true).takeUntil(this.ngOnDestroyed).subscribe((event: Event) => {
                if (this.dropdownEl && !this.dropdownEl.nativeElement.contains(event.target) && this.dropdown.visible) {
                    this.dropdown.toggle();
                    event.preventDefault();
                    event.stopPropagation();
                }
        });
    }

    ngOnDestroy(): void {
        this.ngOnDestroyed.next();
        this.ngOnDestroyed.complete();
    }

    handleVisibilityChange(visible: boolean): void {
        const categories = new Set();
        if (visible) {
            this.gameService.games.forEach((game) => {
                if (game.gameStatus !== 'CANCELLED' && game.gameStatus !== 'CLOSED') {
                    game.categoryIds.forEach((id) => {
                        if (this.gameService.categoryMap[id] && id !== '*') {
                            categories.add(this.gameService.categoryMap[id]);
                        }
                    });
                }
            });
            this.categories = Array.from(categories);
        }
    }
    toggleCategory(cat: Category): void {
        const idx = this.selectedCategories.indexOf(cat);
        if (idx !== -1) {
            this.selectedCategories.splice(idx, 1);
        } else {
            this.selectedCategories.push(cat);
        }
        this.onSelect.emit(this.selectedCategories);
    }
    toggleAllCategories(): void {
        this.allCategories = !this.allCategories;
        if (this.allCategories) {
            this.onSelect.emit('all');
        } else {
            this.onSelect.emit(this.selectedCategories);
        }
    }
}
export type CategoryFilter = Category[] | 'all';
