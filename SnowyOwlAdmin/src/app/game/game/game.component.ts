import { addHours, addMilliseconds, subMilliseconds } from 'date-fns';
import {debounceTime} from 'rxjs/operators';
import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {Game} from 'app/model/game';
import { AlertDialogComponent } from '../../shared/alert-dialog/alert-dialog.component';
import {CategoryService} from '../../shared/services/category.service';
import {Round} from 'app/model/round';
import * as uuid from 'uuid/v4';
import {TitleService} from '../../shared/services/title.service';
import {NgForm} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {DeviceService} from '../../shared/services/device.service';
import {CreateGameOptions, GameService} from '../game.service';
import { VxDialog, VxToast } from 'vx-components';
import {dateMask} from '../../shared/date.directive';
import {MediaService} from '../../shared/services/media.service';
import {LoadingDialogComponent} from '../../shared/loading-dialog.component';
import {Util} from '../../util';
import {cashedGameLS} from 'app/constants';

import {GameGuideComponent} from '../game-guide/game-guide.component';
import {RoundsFormComponent} from '../rounds-form/rounds-form.component';
import {Asyncify} from '../../shared/services/util/asyncify';
import {PayoutModelService} from '../../shared/services/payout-model.service';
import {PayoutModel} from '../../model/payout-model';
import {GeneratePayoutsDialogComponent} from '../../shared/generate-payouts-dialog/generate-payouts-dialog.component';
import {createNumberMask} from 'text-mask-addons/dist/textMaskAddons';

@Component({
    selector: 'app-create-game',
    templateUrl: './game.component.html',
    styleUrls: ['./game.component.scss']
})
export class GameComponent implements OnInit, AfterViewInit {
    dateMask = dateMask;
    numberMask = createNumberMask({prefix: '', allowDecimal: false});
    numberMaskMinutes = createNumberMask({prefix: '', suffix: ' Minutes', allowDecimal: true});
    moneyMask = createNumberMask({prefix: '$', allowDecimal: true});
    moneyMaskNoDec = createNumberMask({prefix: '$', allowDecimal: false});
    loading = false;
    game: Game = new Game();
    expectedStartDateForBracketPlay!: Date;
    expectedStartDateForPoolPlay!: Date;
    bracketPlayCountdownMins = '1';

    minDate!: Date;

    @ViewChild('form', { static: true })
    form!: NgForm;

    @ViewChild(GameGuideComponent, { static: true }) gameGuideComponent!: GameGuideComponent;
    @ViewChild(RoundsFormComponent, { static: true }) roundsForm!: RoundsFormComponent;

    editingGame = false;
    editable = true;
    imageBlob?: Blob;
    payoutModels: PayoutModel[] = [];
    minimumPayoutAmount = 1;
    payoutModelId!: number;

    get minBracketDate(): Date {
        return new Date();
    }

    get minPoolDate(): Date {
        return new Date();
    }

    private _secondsBetween = 15;
    private submitted = false

    constructor(public categoryService: CategoryService, public deviceService: DeviceService, private titleService: TitleService,
                private gameService: GameService, private router: Router,
                private route: ActivatedRoute, private mediaService: MediaService, private vxDialog: VxDialog,
                private payoutModelService: PayoutModelService, private toast: VxToast) {
    }

    maximumPlayerCount!: string;

    ngAfterViewInit(): void {
        if (!this.form)
            return;

        const storedGame = localStorage.getItem(cashedGameLS);
        if (storedGame && this.editable) {
            setTimeout(() => {
                const dialog = this.vxDialog.open(AlertDialogComponent, {
                    title: 'Restore Game',
                    body: `Looks like you have a game that you haven't finished.  Would you like to restore that game or start over?`,
                    buttons: ['Restore', 'Start Over']
                });

                dialog.onClose.subscribe((val: any) => {
                    if (val !== 'Restore') {
                        localStorage.removeItem(cashedGameLS);
                        return;
                    }
                    const parsed = JSON.parse(storedGame);
                    const parsedGame = new Game(parsed);
                    this.fillFromGame(parsedGame);
                    this.gameGuideComponent.fromLocalStorage(parsed);
                })
            })
        }

        this.form.control.valueChanges.pipe(debounceTime(500)).subscribe(change => {
            if (this.form.dirty && this.form.touched) {
                const json = this.game.toJsonObject();
                this.gameGuideComponent.prepareForLocalStorage(json);
                json.rounds = this.game.rounds = this.roundsForm.getRounds(Util.parseNumber(this.maximumPlayerCount), this.game.allowableLanguageCodes, this.game.id);
                localStorage.setItem(cashedGameLS, JSON.stringify(json));
            }
        });
    }

    ngOnInit(): void {
        this.titleService.title = 'Create Game';
        this.minDate = new Date();
        this.route.params.subscribe((params) => {
            if (params.id) {
                this.editingGame = true;
                this.loadGame(params.id);
                this.titleService.title = 'View Game';
                this.editable = false; // TODO: Allow them to edit gamed
            } else if (params.cloneId) {
                this.editingGame = false;
                this.submitted = false;
                this.editable = true;
                this.titleService.title = 'Clone Game';
                this.cloneGame(params.cloneId);
            } else {
                this.editingGame = false;
                this.initializeGame();
                this.submitted = false;
                this.editable = true;
            }
        });
        this.payoutModelService.getPayoutModels().subscribe((models) => {
            this.payoutModels = models;
        });
    }

    refreshLanguages(): void {
        this.game.allowableLanguageCodes.forEach(code => {
            this.game.fetchingActivityTitles[code] = this.game.fetchingActivityTitles[code] || 'Retrieving Question';
            this.game.submittingActivityTitles[code] = this.game.submittingActivityTitles[code] || 'Submitting Answer';
        });

        // TODO: Round Names?
        // this.game.rounds.forEach(round => {
        //     this.game.allowableLanguageCodes.forEach(code => {
        //         const name = round.isBracketRound ? 'Knockout Play Round' : 'Round ' + round.roundSequence;
        //         round.roundNames[code] = round.roundNames[code] || name;
        //     });
        // });
        // if (this.roundsForm) {
        //     this.roundsForm.cdr.markForCheck();
        // }
    }

    async validate(): Promise<void> {

        if (this.form.valid) {
            const now = new Date();
            if (this.expectedStartDateForBracketPlay < now || this.expectedStartDateForPoolPlay < now) {
                this.vxDialog.open(AlertDialogComponent, {
                    title: 'Error',
                    body: 'Please ensure that both the pool and tournament start dates are in the future.',
                    buttons: ['Dismiss']
                });
                return;
            } else if (this.expectedStartDateForBracketPlay < this.expectedStartDateForPoolPlay) {
                this.vxDialog.open(AlertDialogComponent, {
                    title: 'Error',
                    body: 'Please ensure that the tournament start date is after the pool start date.',
                    buttons: ['Dismiss']
                });
                return;
            }

            if (!this.imageBlob && !this.game.gamePhotoUrl) {
                const areYouSure = this.vxDialog.open(AlertDialogComponent, {
                    title: 'Confirm',
                    body: `Are you sure you don't want to add an image? If not, a default will be used.`,
                    buttons: ['Yes', 'No']
                });
                const resp = await areYouSure.onClose.toPromise();
                if (resp === 'No') {
                    return;
                }
            }


            const dialog = this.vxDialog.open(LoadingDialogComponent,  'Creating Game...');
            let photoUrl: string | undefined = this.game.gamePhotoUrl;
            if (this.imageBlob) {
                photoUrl = await this.mediaService.uploadImage(this.imageBlob);
                this.game.gamePhotoUrl = photoUrl;
            }

            this.game.autoBracketPlayPreStartNotificationTimeMs = Util.parseNumber(this.bracketPlayCountdownMins) * 60 * 1000;
            // Shift the start date forward to account for the notification duration.
            const expectedStartDateForBracketPlay = addMilliseconds(this.expectedStartDateForBracketPlay, this.game.autoBracketPlayPreStartNotificationTimeMs);
            this.gameGuideComponent.prepareForServer();
            this.game.rounds = this.roundsForm.getRounds(Util.parseNumber(this.maximumPlayerCount), this.game.allowableLanguageCodes, this.game.id);

            const options: CreateGameOptions = {
                game: this.game,
                expectedStartDateForPoolPlay: this.expectedStartDateForPoolPlay,
                expectedStartDateForBracketPlay,
                photoUrl,
                imageExternallyManaged: !!photoUrl && !this.imageBlob,
                minimumPayoutAmount: this.minimumPayoutAmount,
                payoutModelId: this.payoutModelId,
                // TODO: give a way to edit this
                giveSponsorPlayerWinningsBackToSponsor: true
            };

            this.gameService.createGame(options).subscribe(response => {
                dialog.close();
                if (response.success) {
                    this.toast.open({
                        text: 'Successfully Created Game!',
                        duration: 4000
                    });
                    this.submitted = true;
                    localStorage.removeItem(cashedGameLS);
                    this.router.navigate(['/game']);
                } else {
                    this.vxDialog.open(AlertDialogComponent, {
                        title: 'Error creating game: ',
                        body: JSON.stringify(response),
                        buttons: ['Ok']
                    });
                }
            });
        } else {
            const message = Util.findInvalidControlErrorMessage(this.form.control);
            if (message) {
                this.toast.open({
                    text: message,
                    type: 'error'
                });
            }
        }
    }

    generatePayoutTable(): void {
        this.vxDialog.open(GeneratePayoutsDialogComponent, {
            payoutModelId: this.payoutModelId,
            minimumPayoutAmount: this.minimumPayoutAmount
        })
        // Util.markAllTouched(group.control);
        // if (group.valid) {
        //     const dialog = this.vxDialog.open(LoadingDialogComponent,  'Loading Payouts...');
        //     const payouts = await this.gameService.generatePayoutTable(this.game, this.bracketRound.roundPurse);
        //     dialog.close();
        //     this.vxDialog.open(PayoutTableComponent, null, payouts);
        // }
    }

    private initializeGame(): void {
        const game = new Game();
        this.game = game;
        game.id = uuid();
        game.gameStatus = 'PENDING';
        game.forbiddenCountryCodes = [];
        this.expectedStartDateForBracketPlay = addHours(new Date(), 1);
        this.expectedStartDateForPoolPlay = addHours(new Date(), 1);
        game.allowBots = true;
        game.allowableAppIds = [6];
        game.gameEngine = 'SNOWYOWL';
        game.engineType = 'MULTI_LIFE';
        game.gameType = 'DEFAULT';
        game.speedUpBots = true;
        game.fillWithBots = true;
        game.pairImmediately = false;
        game.allowableLanguageCodes = ['en'];
        game.bracketEliminationCount = 1;
        game.productionGame = true;
        game.autoStartPoolPlay = true;
        game.autoStartBracketPlay = true;
        game.startingLivesCount = 3;
        game.maxLivesCount = 5;
        game.additionalLivesCost = 0.99;


        this.refreshLanguages();
    }

    private async loadGame(gameId: string): Promise<void> {
        this.loading = true;
        const game = await this.gameService.getGameById(gameId);
        await this.fillFromGame(game);
        this.loading = false;
    }

    private async cloneGame(gameId: string): Promise<void> {
        await this.loadGame(gameId);
        this.game.id = uuid();
        this.game.gameStatus = 'PENDING';
        this.game.rounds.forEach(round => {
            round.id = uuid();
            round.gameId = this.game.id;
            round.roundStatus = 'PENDING';
        })

    }

    private async fillFromGame(game: Game): Promise<void> {
        this.game = game;

        // Timer to allow the child form component to have their inputs set
        await Asyncify.timer(() => {
            const bracket = this.game.rounds[this.game.rounds.length - 1];
            const first = this.game.rounds[0];
            this.expectedStartDateForBracketPlay = bracket && bracket.expectedOpenDate ? bracket.expectedOpenDate : new Date();
            this.expectedStartDateForPoolPlay = first && first.expectedOpenDate ? first.expectedOpenDate : new Date();

            this.roundsForm.initFromExisting(this.game.rounds);
            this.gameGuideComponent.initFromGame(game);
        });
    }

}
