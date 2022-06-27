import {HttpError} from './http-error';
import {LoginOrSignupJsonError} from 'app/shared/services/subscriber.service';

const HANDLED = {
    // signup
    emailAlreadyUsed: 'This email has already been used.',
    nicknameAlreadyUsed: 'This nickname has already been used.',
    nicknameInvalid: 'This nickname has already been used.', // TODO: explain what makes a nickname valid
    // login
    invalidLogin: 'Invalid Username/Password.',
    accountDeactivated: 'Your account has been deactivated.  Please contact us for help.',
    passwordChangeRequired: 'Your account requires a password reset' // TODO: give a way to reset password
};

export class LoginOrSignupError extends HttpError {
    constructor(response: LoginOrSignupJsonError) {
        super(response, HANDLED);
        this.name = 'LoginOrSignupError';
    }
}
