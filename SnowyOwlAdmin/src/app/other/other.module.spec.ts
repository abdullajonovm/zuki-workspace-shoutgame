import { OtherModule } from './other.module';

describe('OtherModule', () => {
  let otherModule: OtherModule;

  beforeEach(() => {
    otherModule = new OtherModule();
  });

  it('should create an instance', () => {
    expect(otherModule).toBeTruthy();
  });
});
