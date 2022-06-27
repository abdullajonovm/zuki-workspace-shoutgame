import {Component, OnDestroy} from '@angular/core';
import {DialogComponent, OnDialogOpen} from 'app/shared/dialog/dialog.component';
import {SubscriberService} from 'app/shared/services/subscriber.service';
import {Game} from 'app/model/game';
import {GameplayService} from 'app/shared/services/gameplay.service';
import {Round} from 'app/model/round';
import {GameService} from 'app/shared/services/game.service';
import {MatchResultSyncMessage, QuestionSyncMessage, SyncMessage} from 'app/model/sync-message';
import {Question} from 'app/model/question';
import {NumAnim, Util} from 'app/util';
import {Answer} from 'app/model/answer';
import {DialogService} from 'app/shared/dialog/dialog.service';
import {Subject} from 'rxjs/Subject';
import {environment} from 'environments/environment';
import {Subscription} from 'rxjs/Subscription';
import {LogService} from 'app/shared/services/log.service';

// TODO: allow setting of these dials based on some type of percentage check
const dialNetworkSpeedMinMs = 250;
const dialNetworkSpeedMaxMs = 2500;

const STRINGS = environment.strings.round;

@Component({
    selector: 'sh-round',
    templateUrl: './round.component.html',
    styleUrls: ['./round.component.scss']
})
export class RoundComponent implements OnDialogOpen<DialogParams>, OnDestroy {

    countdown?: NumAnim;

    question?: Question;

    round: Round;
    game: Game;
    state: State;
    answerId?: string;

    private dialog: DialogComponent;
    private encryptedQuestion: QuestionSyncMessage;
    private ngUnsubscribe: Subject<void> = new Subject<void>();
    private animatingSubscription?: Subscription;
    private tooSlowDialog?: DialogComponent;
    private presentedTime?: Date;

    constructor(public subscriberService: SubscriberService, private gameplayService: GameplayService,
                private gameService: GameService, private dialogService: DialogService, private logService: LogService) {
    }

    ngOnDestroy(): void {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }

    onDialogOpen(dialog: DialogComponent, data: DialogParams): void {
        dialog.dialogOptions.width = '450px';
        dialog.dialogOptions.height = '600px';
        dialog.dialogOptions.disableClose = true;
        this.dialog = dialog;
        this.round = data.round;
        this.game = data.game;
        this.game.syncMessages.takeUntil(this.ngUnsubscribe).subscribe(message => this.handleGameSyncMessage(message));
        this.game.state.takeUntil(this.ngUnsubscribe).subscribe(status => {
            switch (status) {
                case 'WAITING_FOR_QUESTION':
                    this.state = 'AWAITING_QUESTION';
                    if (this.tooSlowDialog)
                        this.tooSlowDialog.close();

                    break;
                case 'HAS_QUESTION':
                    this.encryptedQuestion = this.game.stateData;
                    setTimeout(() => this.decryptQuestion(), Util.randomInt(dialNetworkSpeedMinMs, dialNetworkSpeedMaxMs));
                    break;
            }
        });

        // this.question = new Question({
        //     questionText: {en: 'How many questions are there?'},
        //     answers: [
        //         new Answer({id: '1', answerText: {en: 'Answer 1'}}),
        //         new Answer({id: '12', answerText: {en: 'Answer 1'}}),
        //         new Answer({id: '13', answerText: {en: 'Answer 1'}}),
        //         new Answer({id: '14', answerText: {en: 'Answer 1'}})
        //     ],
        // });
        // this.question.answer = new SubscriberQuestionAnswer({
        //     determination: 'WON_CORRECT'
        // });
        // this.question.opponentAnswer =  new SubscriberQuestionAnswer({
        //     determination: 'LOST_TIME',
        //     selectedAnswerId: '12'
        // });
        // this.question.correctAnswerId = '12';
        // this.answerId = '12';
        // this.state = 'AWAITING_QUESTION';

        this.round.finished.takeUntil(this.ngUnsubscribe).subscribe((finished) => {
            if (finished) {
                this.logService.log('Round finished: ', this.round, 'closing round component');
                this.dialog.close();
            }
        })
    }

    async answerQuestion(answer: Answer): Promise<void> {
        this.logService.log('Answered question:', this.question);
        this.answerId = answer.id;
        if (this.question)
            this.question.answered = true;

        if (this.animatingSubscription) {
            this.logService.info('Stopping the countdown timer');
            this.animatingSubscription.unsubscribe();
        }

        this.state = 'AWAITING_RESULT';
        const resp = await this.gameplayService.submitAnswer(this.encryptedQuestion.subscriberQuestionAnswerId, answer.id);
        // TODO: handle other errors
        if (!resp.success) {
            this.logService.log(`TOO SLOW FROM SCOTT, resp:`, resp);
            this.tooSlow();
        }
    }

    private async decryptQuestion(): Promise<void> {
        this.logService.log('Decrypting Question: ', this.encryptedQuestion);
        const response = await this.gameplayService.getDecryptKey(this.encryptedQuestion.subscriberQuestionAnswerId);
        // TODO: handle error
        if (response.success) {
            const question = Util.decryptAES(response.decryptKey, this.encryptedQuestion.encryptedQuestion);
            this.question = new Question(JSON.parse(question));
            this.round.questions.push(this.question);
            this.presentedTime = new Date(response.questionPresentedTimestamp);
            this.answerId = undefined;
        } else {
            this.logService.error('FAILED DECRYPTING QUESTION:', this.encryptedQuestion, 'Response:', response);
            this.state = 'AWAITING_QUESTION';
            return;
        }

        this.state = 'HAS_QUESTION';
        if (this.tooSlowDialog)
            this.tooSlowDialog.close();

        this.resetCountdown();
    }

    private handleGameSyncMessage(message: SyncMessage): void {
        if (message instanceof MatchResultSyncMessage) {
            this.dialog.close();
        }
    }

    private resetCountdown(): void {
        let countTime = this.round.playerMaximumDurationSeconds * 1000;
        if (this.presentedTime) {
            countTime -= Date.now() - this.presentedTime.getTime()
        }

        const question = this.question;
        this.logService.log('Animating the countdown with time:', countTime, 'For question:', question);
        this.countdown = Util.animateNumber(countTime / 1000, 0, countTime, 1);

        if (this.countdown.onFinished) {
            this.animatingSubscription = this.countdown.onFinished.subscribe(() => {
                if (question && !question.answered) {
                    this.logService.log('TOO SLOW FROM ME:', question);
                    this.tooSlow();
                }
                if (this.animatingSubscription) {
                    this.animatingSubscription.unsubscribe();
                }
            })
        }
    }

    private tooSlow(): void {
        this.question = undefined;
        if (this.tooSlowDialog && !this.tooSlowDialog.closed)
            return;

        this.tooSlowDialog = this.dialogService.open({
            body: STRINGS.tooLate,
            buttons: [{text: STRINGS.core.ok}]
        });
        this.state = 'AWAITING_RESULT';
    }
}

interface DialogParams {
    game: Game;
    round: Round;
}

type State = 'AWAITING_RESULT' | 'AWAITING_QUESTION' | 'HAS_QUESTION';
