import { TippingPointPage } from './app.po';

describe('tipping-point App', () => {
  let page: TippingPointPage;

  beforeEach(() => {
    page = new TippingPointPage();
  });

  it('should display welcome message', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('Welcome to app!');
  });
});
