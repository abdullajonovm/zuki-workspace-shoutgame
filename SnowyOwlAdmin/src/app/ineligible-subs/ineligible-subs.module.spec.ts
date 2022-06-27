import { IneligibleSubsModule } from './ineligible-subs.module';

describe('IneligibleSubsModule', () => {
  let ineligibleSubsModule: IneligibleSubsModule;

  beforeEach(() => {
    ineligibleSubsModule = new IneligibleSubsModule();
  });

  it('should create an instance', () => {
    expect(ineligibleSubsModule).toBeTruthy();
  });
});
