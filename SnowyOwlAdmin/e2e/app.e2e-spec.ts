import { MillionaireAdminPage } from './app.po';

describe('millionaire-admin App', () => {
  let page: MillionaireAdminPage;

  beforeEach(() => {
    page = new MillionaireAdminPage();
  });

  it('should display welcome message', () => {
    page.navigateTo();
    expect(page.getParagraphText()).toEqual('Welcome to app!!');
  });
});
