package com.meinc.commons.account;

import java.util.List;

import com.meinc.commons.domain.Account;

/**
 * This interface contains system-wide access to general-purpose account
 * operations.
 *
 * @author bxgrant
 */
public interface IAccount
{
  /** Max length of an account key value. */
  public static final int MAX_ACCOUNT_KEY_LENGTH = 63;

  /** The default system account id. */
  public static final int DEFAULT_SYSTEM_ACCOUNT_ID = 1;

  /**
   * Add a new account to the system. Note that only the account itself
   * will be added and not any subscribers that may be contained within the
   * account object.  Instead, call the <code>addSubscriber</code> method
   * for each subscriber you wish to add to the account.
   * <br />
   * Note also that as a side-effect, the accountId member of the account
   * object will be set to the id associated with the new account.
   *
   * @see com.meinc.sao.ITeam#addSubscriber(com.meinc.commons.domain.Account, com.meinc.commons.domain.Subscriber)
   * @param account The account to add.
   * @return int The unique id of the newly created account.
   */
  public Account addAccount(Account account);

  public Account getAccount(int accountId);

  /**
   * The basic url for this server: "me-inc.com" for example.
   * @return
   */
  public String getAccountAccessBaseUrl();

  /**
   * deleteAccount marks account inactive. This operation is performed by the
   * administrator of the account to close the account. An account in this state
   * should not be reactivated using this interface.
   *
   * @param accountId The account to delete.
   */
  public void deleteAccount(int accountId);

  /**
   * activateAccount and suspendAccount are operations performed when an account
   * is deemed to be delinquent in payment, or has made payment after the account
   * has been suspended.
   *
   * @param accountId The account to activate or suspend.
   */
  public void activateAccount(int accountId);
  public void suspendAccount(int accountId);

  /**
   * Convert an existing account on the system. No other changes will
   * take place. This is used to turn a non-paying account into a paying
   * account, or vice-versa.
   *
   * @param int accountId The account to be converted.
   * @param String The string representation of the type to which the account
   * should be set. Values include: "BETA", "TRIAL" and "PAYING".
   */
  public void convertAccount(int accountId, String type);

  public List<Account> getAllAccounts();
  public List<Integer> getAllAccountsIds();

  public Account getAccountFromSubdomain(String subdomain);

  /**
   * Each account has a directory on the server where files may be stored
   * that need to be accessible to the outside world through the web server.
   * For example, images uploaded to the server on behalf of the account.
   * <br />
   * <br />
   * This method returns the fully qualified path to the directory on the
   * server that stores them for the account.  It's different for every account.
   * Note that all paths will user forward slashes regardless of operating system
   * and that there will NOT be a trailing forward slash character on the path.
   *
   * @param subdomain
   * @return
   */
  public String getAccountResourcePath(Account account);

  /**
   * Each account has a directory of resources on the server for files that may
   * be accessed from the outside world through the web server.  This method
   * returns the URI that when typed into the browser maps to the directory
   * on the server where the account-specific resources are stored.  For example,
   * http://test.ecp.sco.com/az/... might map to c:/www/accounts/test/...
   * <br /><br />
   * This method will only return only that part of the uri that tells the
   * web server that to look in the account specific directory for the remainder
   * of the uri.  In the above example, it would return: "/az".
   *
   * @param account
   * @return
   */
  public String getAccountResourceUri(Account account);

  /**
   * The physical location on the machine of where the digital client application is stored.
   *<p/>
   * This path is not accessible from the web server.
   * @param account
   * @return
   */
  public String getAccountDigitalClientDownloadPath(Account account);

  /**
   * Returns just that part of the directory that images for an account will
   * be stored in.  For example, if images were stored in a directory on the
   * server c:/xxx/accounts/accname/images then this method would return just
   * this:  images
   *
   * @param account
   * @return
   */
  public String getAccountImagesLocalPath(Account account);

  /**
   * Returns just the part of the uri that will return account images.  To
   * get the fully qualified URI to use prepend this with a call to
   * getAccountResourceUri().
   *
   * @param account
   * @return
   */
  public String getAccountImagesUri(Account account);

  /**
   * Whether or not the given account name is unique.
   *
   * @param accountName The name to check.
   * @return boolean True if unique in the system, false otherwise.
   */
  public boolean isAccountNameUnique(String accountName);

  /**
   * Update account data.
   *
   * @param account The account to update.
   */
  public Account updateAccount(Account account);

  /**
   * Whether or not the given account key is reserved.
   *
   * @param accountKey
   * @return
   */
  public boolean isAccountKeyReserved(String accountKey);

  /**
   * Whether or not the given account key is unique within the system.
   *
   * @param accountKey The name to check.
   * @return boolean True if unique in the system, false otherwise.
   */
  public boolean isAccountKeyUnique(String accountKey);

  /**
   * Given an account name, suggest an account key .  An account key
   * is a string that is unique within the system, is a valid URL
   * subdomain (no spaces/punctuation for example).
   * <p/>
   * It will also check against account name uniqueness and
   * reserved account keys.
   *
   * @param accountName the user-supplied account name
   * @return the system-friendly suggested account name
   */
  public String suggestAccountKey(String accountName);

  /**
   * This method determines whether the given account key
   * is valid or not.
   *
   * @param accountKey The key to validate.
   * @throws AccountKeyValidationError If key isn't valid.
   */
  public void validateAccountKey(String accountKey) throws AccountKeyValidationError;

  /**
   * This method returns the FQDN of the principle host domain. It pertains to
   * all accounts for a given host Edge Processor.  The returned value does not
   * include protocol, subdomain, port, or directories.
   * <br /><br />
   * For example, this might return <code>ecp2.sco.com</code>,
   * or <code>me-inc.com</code>.
   *
   * @return String The FQDN of the host, for all accounts.
   */
  public String getAllAccountsAccessHostname();

  /**
   * Each and every account has its own account key.  The account key is used
   * to create a unique URL by prefixing the account key as a subdomain in front
   * of a principle domain.  This method figures out the fully qualified URL
   * for an account and returns it.
   * <br /><br />
   * Note that the method does not prepend the
   * protocol not knowing whether you wanted secure or not.  Also, The account
   * object is assumed to have been populated with the account key (subdomain)
   * member.  For example, if the account key is: acmerentals then this method
   * might return <code>acmerentals.edgeclickpark.com</code>.
   *
   * @param account The account to get the access URL for.
   * @return String The URL the account should use to access all services.
   */
  public String getAccountAccessUrl(Account account);

  /**
   * Returns a fully qualified URL, including protocol, that an account should
   * use to access the administrative web site.
   *
   * @param account The account who we need the admin URL for.
   * @return String The url.
   */
  public String getAdminAccessUrlForAccount(Account account);

  /**
   * Returns a fully qualified URL, including protocol, that an account should
   * use to access a digital service on an ECP server from a mobile device.
   *
   * @param account The account who we need the admin URL for.
   * @return String The url.
   */
  public String getMobileAccessUrlForAccount(Account account);

  /**
   * Returns the fully qualitied URL, including protocol, that an account shoud
   * use to access the munge servlet.
   *
   * @param account
   * @return
   */
  public String getMungeAccessUrlForAccount(Account account);
}
