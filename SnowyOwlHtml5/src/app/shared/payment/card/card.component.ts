import {Component, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {NgForm, NgModel} from '@angular/forms';
import * as validCard from 'card-validator';
import {CardData} from '../../services/payment.service';

@Component({
    selector: 'sh-card',
    templateUrl: './card.component.html',
    styleUrls: ['./card.component.scss']
})
export class CardComponent implements OnInit {

    @ViewChild('form') form: NgForm;

    @Input() card: CardData;

    cardImage?: string;
    cardCodeType = 'CVC';
    isAmex = false;
    @Output() saveCardChange = new EventEmitter<boolean>();

    constructor() {
    }

    ngOnInit(): void {
    }


    handleCard(cardInput: NgModel, input: HTMLInputElement): void {
        input.value = input.value.replace(/[^0-9]/g, '');
        let value = input.value;
        let validated = validCard.number(value);
        setTimeout(() => {
            cardInput.control.setErrors(validated.isValid ? null : {invalidCard: true});
        });

        if (validated.card) {
            this.cardImage = `url("assets/img/cards/${validated.card.type}.png")`;
            this.cardCodeType = validated.card.code.name;
            this.isAmex = validated.card.isAmex;

            let changed = false;
            let offset = 0;
            for (let gap of validated.card.gaps) {
                gap += offset;
                if (value.length > gap && value[gap] !== ' ') {
                    value = value.slice(0, gap) + ' ' + value.slice(gap);
                    changed = true;
                }
                offset++;
            }
            const max = Math.max(...validated.card.lengths) + offset;
            if (value.length > max) {
                value = value.slice(0, max);
                changed = true;
            }

            if (changed) {
                input.value = value;
            }
            if (changed) {
                validated = validCard.number(value);
                cardInput.control.setErrors(validated.isValid ? null : {invalidCard: true});
            }
        } else {
            this.cardImage = undefined;
            this.cardCodeType = 'CVC';
            this.isAmex = false;
        }
    }

    handleExpiry(expiryInput: NgModel, input: HTMLInputElement): void {
        input.value = input.value.replace(/[^0-9\/]/g, '');

        const validated = validCard.expirationDate(input.value);
        console.log(validated);
        setTimeout(() => {
            expiryInput.control.setErrors(validated.isValid ? null : {invalidCard: true});
        });

        if (input.value.length === 3 && input.value[2] !== '/' && input.value[1] !== '/') {
            input.value = input.value.slice(0, 2) + '/' + input.value.slice(2)
        }
        if (input.value.length > 7)
            input.value = input.value.slice(0, 7);

        if (validated.isValid) {
            this.card.month = validated.month!;
            this.card.year = validated.year!;
        }
    }

    handleCvv(cvvInput: NgModel, input: HTMLInputElement): void {
        input.value = input.value.replace(/[^0-9]/g, '');

        const validated = validCard.cvv(input.value, this.isAmex ? 4 : 3);
        setTimeout(() => {
            cvvInput.control.setErrors(validated.isValid ? null : {invalidCard: true});
        });

        if (input.value.length > 4)
            input.value = input.value.slice(0, 4);
    }

}
