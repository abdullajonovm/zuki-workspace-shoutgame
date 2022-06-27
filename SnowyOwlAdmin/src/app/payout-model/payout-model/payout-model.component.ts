import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {PayoutModelService} from '../../shared/services/payout-model.service';
import {PayoutModel} from '../../model/payout-model';
import {payoutModelHintLS} from '../../constants';
import {createNumberMask} from 'text-mask-addons/dist/textMaskAddons';
import {from, Subject} from 'rxjs';
import { VxDialog, VxToast } from 'vx-components';
import {debounceTime, tap} from 'rxjs/operators';
import {DecimalPipe} from '@angular/common';
import {PayoutModelRound} from '../../model/payout-model-round';
import {Util} from '../../util';
import {NgForm} from '@angular/forms';
import {TitleService} from '../../shared/services/title.service';
import {LoadingDialogComponent} from '../../shared/loading-dialog.component';

let hasShownHint = localStorage.getItem(payoutModelHintLS);

@Component({
    selector: 'app-payout-model',
    templateUrl: './payout-model.component.html',
    styleUrls: ['./payout-model.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class PayoutModelComponent implements OnInit, OnDestroy {
    numberMask = createNumberMask({prefix: '', allowDecimal: true});
    moneyMask = createNumberMask({prefix: '$', allowDecimal: true});
    loading = false;
    model = new PayoutModel();
    playerCountChange = new Subject();
    @ViewChild('form', { static: true }) form!: NgForm;
    editable = true;

    cloning = false;
    numSaves = 0;
    constructor(private route: ActivatedRoute, private payoutModelService: PayoutModelService, private cdr: ChangeDetectorRef,
                private router: Router, private titleService: TitleService, private dialog: VxDialog,
                private toast: VxToast) {
    }

    get payoutAmount(): number {
        let count = 0;
        this.model.payoutModelRounds.forEach(round => {
            const money = round.eliminatedPayoutAmount || 0;
            count += money * round.eliminatedPlayerCount;
        });
        return count;
    }

    get totalMoney(): number {
        return (this.model.basePlayerCount || 0) * (this.model.entranceFeeAmount || 0);
    }

    ngOnDestroy(): void {
        this.playerCountChange.complete();
    }

    ngOnInit(): void {
        this.watchPlayerCount();

        this.route.params.subscribe((params) => {
            if (params.id) {
                this.loadExistingModel(+params.id);
                this.titleService.title = 'Edit Payout Model';
            } else if (params.cloneId) {
                this.cloning = true;
                this.loadExistingModel(+params.cloneId);
                this.titleService.title = 'Clone Payout Model';
            } else {
                this.titleService.title = 'Create Payout Model';
            }
        })
    }

    submit(): void {
        const form = this.form;
        if (form.valid) {
            const loader = this.dialog.open(LoadingDialogComponent,  'Creating Payout Model...');
            this.payoutModelService.createPayoutModel(this.model).subscribe(resp => {
                loader.close();
                if (resp.success) {
                    this.toast.open({
                        text: 'Successfully created payout model!',
                        type: 'success'
                    });
                    this.router.navigate(['/payout-model/']);
                } else {
                    this.toast.open({
                        text: 'Failed to create payout model.',
                        type: 'error'
                    });
                }
            })
        } else {
            const message = Util.findInvalidControlErrorMessage(form.control);
            if (message) {
                this.toast.open({
                    text: message,
                    type: 'error'
                });
            }
        }
    }

    handleInputFocus(): void {
        if (!hasShownHint) {
            this.toast.open({
                title: 'Hint!',
                text: 'You can copy and paste payout amounts from a spreadsheet for easy input.',
                type: 'info',
                position: 'bottom-middle',
                showClose: true,
                duration: 10 * 1000
            });
            localStorage.setItem(payoutModelHintLS, 'true');
            hasShownHint = 'true';
        }
    }

    generateRounds(): void {
        const rounds = this.model.payoutModelRounds;
        const oldAmounts: number[] = [];
        rounds.forEach(round => {
            if (round.eliminatedPayoutAmount) {
                oldAmounts.push(round.eliminatedPayoutAmount);
            }
        });
        oldAmounts.sort((a, b) => a > b ? -1 : 1);

        let fromPeople = this.model.basePlayerCount;
        if (fromPeople % 2) {
            this.model.basePlayerCount++;
            fromPeople++;
        }

        if (!fromPeople) {
            rounds.length = 0;
            return;
        }

        const numRounds = Math.ceil(Math.log2(fromPeople)) + 1;

        let i = numRounds - 1, toPeople = lowestPow(fromPeople);

        this.numSaves = toPeople - (fromPeople / 2);

        // Already a power of 2
        if (toPeople === fromPeople) {
            toPeople = fromPeople / 2;
            this.numSaves = 0;
        }

        for (; fromPeople >= 1; fromPeople = toPeople, toPeople /= 2, i--) {
            if (toPeople <= 1) {
                toPeople = 1;
            }

            const round = rounds[i] = new PayoutModelRound();
            round.startingPlayerCount = fromPeople;
            round.eliminatedPlayerCount = (fromPeople - toPeople) || 1;
            round.sortOrder = i;
            round.description = getPayoutRoundDescription(round);

            if (fromPeople === 1)
                break;
        }

        rounds.length = numRounds;

        oldAmounts.forEach((amount, idx) => {
            const round = rounds[idx];
            if (round)
                round.eliminatedPayoutAmount = amount;
        });
    }

    onPaste(event: ClipboardEvent, roundIdx: number): boolean {
        if (!event.clipboardData) return true;

        const data = event.clipboardData.getData('text/plain');

        if (data && data.length) {
            const splitChar = data.includes('\n') ? '\n' : '\t';
            const numbers = data.split(splitChar).map(Util.parseNumber);
            while (numbers.length) {
                const number = numbers.shift();
                if (number || number === 0) {
                    const round = this.model.payoutModelRounds[roundIdx];
                    if (!round) {
                        break;
                    }

                    round.eliminatedPayoutAmount = number;
                }
                roundIdx++;
            }
            event.preventDefault();
            return false;
        }
        return true;
    }

    private loadExistingModel(id: number): void {
        this.loading = true;
        this.editable = this.cloning;
        this.cdr.markForCheck();

        this.payoutModelService.getPayoutModel(id).subscribe(model => {
            if (!model) {
                this.toast.open({
                    title: 'Error',
                    text: 'Failed to find Payout Model!',
                    type: 'error'
                });

                this.router.navigate(['/payout-model']);
                return;
            }
            this.model = new PayoutModel(model.toJsonObject());
            this.loading = false;
            this.cdr.markForCheck();
        })
    }

    private watchPlayerCount(): void {
        this.playerCountChange.pipe(
            tap(() => this.loading = true),
            debounceTime(1000)
        ).subscribe(() => {
            this.generateRounds();
            this.loading = false;
            this.cdr.markForCheck();
        })
    }
}

function getPayoutRoundDescription(round: PayoutModelRound): string {
    if (round.startingPlayerCount === 1) {
        return '1st Place Winner';
    } else if (round.startingPlayerCount === 2) {
        return '2nd Place Winner';
    } else {
        const pipe = new DecimalPipe('en');
        const to = round.startingPlayerCount;
        const fromAmount = to - round.eliminatedPlayerCount + 1;
        return `Winners ${pipe.transform(fromAmount)} - ${pipe.transform(to)}`
    }
}

/**
 * Finds the next lowest power of 2
 */
function lowestPow(num: number): number {
    return Math.pow(2, Math.floor(Math.log2(num)));
}
