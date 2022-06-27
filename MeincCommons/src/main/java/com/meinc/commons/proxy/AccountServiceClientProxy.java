package com.meinc.commons.proxy;

import com.meinc.commons.account.IAccount;
import com.meinc.commons.domain.Account;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.ServiceMessage;

public class AccountServiceClientProxy implements IAccount {
  private ServiceEndpoint _endpoint;

  public AccountServiceClientProxy() {
    // TODO: inject from spring or JNDI.
    ServiceEndpoint ep = new ServiceEndpoint();
    ep.setNamespace("default");
    ep.setServiceName("AccountService");
    ServiceMessage.waitForServiceRegistration(ep);
    _endpoint = ep;
  }

  public Account addAccount(com.meinc.commons.domain.Account account) {
    return (Account) ServiceMessage.send(_endpoint, "addAccount",
        account);
  }

  public com.meinc.commons.domain.Account getAccount(int accountId) {
    return (com.meinc.commons.domain.Account) ServiceMessage.send(
        _endpoint, "getAccount", accountId);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<com.meinc.commons.domain.Account> getAllAccounts() {
    return (java.util.List<com.meinc.commons.domain.Account>) ServiceMessage
        .send(_endpoint, "getAllAccounts");
  }

  @SuppressWarnings("unchecked")
  public java.util.List<java.lang.Integer> getAllAccountsIds() {
    return (java.util.List<java.lang.Integer>) ServiceMessage.send(
        _endpoint, "getAllAccountsIds");
  }

  public com.meinc.commons.domain.Account getAccountFromSubdomain(
      java.lang.String subdomain) {
    return (com.meinc.commons.domain.Account) ServiceMessage.send(
        _endpoint, "getAccountFromSubdomain", subdomain);
  }

  public String getAccountAccessBaseUrl()
  {
  	return (String) ServiceMessage.send(_endpoint, "getAccountAccessBaseUrl");
  }

  public boolean isAccountNameUnique(java.lang.String accountName) {
    return (Boolean) ServiceMessage.send(_endpoint,
        "isAccountNameUnique", accountName);
  }

  public Account updateAccount(com.meinc.commons.domain.Account account) {
    return (Account) ServiceMessage.send(_endpoint, "updateAccount", account);
  }

  public java.lang.String getTempPassword(int accountId,
      com.meinc.commons.domain.Subscriber subscriber) {
    return (java.lang.String) ServiceMessage.send(_endpoint,
        "getTempPassword", accountId, subscriber);
  }

  public boolean isAccountKeyReserved(java.lang.String accountName) {
    return (Boolean) ServiceMessage.send(_endpoint,
        "isAccountKeyReserved", accountName);
  }

  public boolean isAccountKeyUnique(java.lang.String accountKey) {
    return (Boolean) ServiceMessage.send(_endpoint,
        "isAccountKeyUnique", accountKey);
  }

  public java.lang.String suggestAccountKey(java.lang.String accountName) {
    return (java.lang.String) ServiceMessage.send(_endpoint,
        "suggestAccountKey", accountName);
  }

  public void validateAccountKey(java.lang.String accountKey) {
    ServiceMessage.send(_endpoint, "validateAccountKey", accountKey);
  }

  public java.lang.String getAllAccountsAccessHostname() {
    return (java.lang.String) ServiceMessage.send(_endpoint,
        "getAllAccountsAccessHostname");
  }

  public java.lang.String getAccountAccessUrl(
      com.meinc.commons.domain.Account account) {
    return (java.lang.String) ServiceMessage.send(_endpoint,
        "getAccountAccessUrl", account);
  }

  public java.lang.String getAdminAccessUrlForAccount(
      com.meinc.commons.domain.Account account) {
    return (java.lang.String) ServiceMessage.send(_endpoint,
        "getAdminAccessUrlForAccount", account);
  }

  public java.lang.String getMobileAccessUrlForAccount(
      com.meinc.commons.domain.Account account) {
    return (java.lang.String) ServiceMessage.send(_endpoint,
        "getMobileAccessUrlForAccount", account);
  }

  public java.lang.String getMungeAccessUrlForAccount(
      com.meinc.commons.domain.Account account) {
    return (java.lang.String) ServiceMessage.send(_endpoint,
        "getMungeAccessUrlForAccount", account);
  }

  public java.lang.String getAccountResourcePath(
      com.meinc.commons.domain.Account account) {
    return (java.lang.String) ServiceMessage.send(_endpoint,
        "getAccountResourcePath", account);
  }

  public java.lang.String getAccountResourceUri(
      com.meinc.commons.domain.Account account) {
    return (java.lang.String) ServiceMessage.send(_endpoint,
        "getAccountResourceUri", account);
  }

  public java.lang.String getAccountImagesLocalPath(
      com.meinc.commons.domain.Account account) {
    return (java.lang.String) ServiceMessage.send(_endpoint,
        "getAccountImagesLocalPath", account);
  }

  public java.lang.String getAccountImagesUri(
      com.meinc.commons.domain.Account account) {
    return (java.lang.String) ServiceMessage.send(_endpoint,
        "getAccountImagesUri", account);
  }

  public void deleteAccount(int accountId) {
    ServiceMessage.send(_endpoint, "deleteAccount", accountId);
  }

  public void activateAccount(int accountId) {
    ServiceMessage.send(_endpoint, "activateAccount", accountId);
  }

  public void suspendAccount(int accountId) {
    ServiceMessage.send(_endpoint, "suspendAccount", accountId);
  }

  public void convertAccount(int accountId, java.lang.String type) {
    ServiceMessage.send(_endpoint, "convertAccount", accountId, type);
  }

	public String getAccountDigitalClientDownloadPath(Account account)
	{
		return (java.lang.String) ServiceMessage.send(_endpoint,
        "getAccountDigitalClientDownloadPath", account);
	}

}
