import { PayoutModelModule } from './payout-model.module';

describe('PayoutModelModule', () => {
  let payoutModelModule: PayoutModelModule;

  beforeEach(() => {
    payoutModelModule = new PayoutModelModule();
  });

  it('should create an instance', () => {
    expect(payoutModelModule).toBeTruthy();
  });
});
