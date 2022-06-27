package com.meinc.commons.team;

import java.util.List;

import com.meinc.commons.domain.Account;
import com.meinc.commons.domain.Contact;
import com.meinc.commons.domain.Group;
import com.meinc.commons.domain.Location;
import com.meinc.commons.domain.Organization;
import com.meinc.commons.domain.Subscriber;
import com.meinc.commons.domain.SubscriberPhone;
import com.meinc.commons.team.exception.TeamAuthenticationException;
import com.meinc.commons.team.exception.TeamSubscriberRequiresEulaException;
import com.meinc.commons.team.exception.TeamSubscriberRequiresUpdateException;

public interface ITeam
{
    /** The default system subscriber id. */
    public static final int DEFAULT_SYSTEM_SUBSCRIBER_ID = 1;

    /************************ Subscriber Methods ********************************/
    
    public Subscriber getSubscriber(int accountId, int subscriberId);

    public Subscriber getSubscriberWithoutExtensions(int accountId, int subscriberId);

    /**
     * Query Subscribers by cell number. A list of Account objects is returned
     * with the getSubscribers() method loaded with matching Subscribers.
     * 
     * @param cellNumber
     *            The Subscriber's cell number
     * @return Accounts of Subscribers with specified cell number
     */
    public List<Account> getSubscribersByCellNumber(String cellNumber);
    
    /**
     * Determines if anyone is using a given cell# for the account.
     * 
     * @param cellNumber
     * @return
     */
    public boolean isCellNumberTaken(int accountId, String cellNumber);
    
    public Subscriber getSubscriberByCellNumber(int accountId, String cellNumber);

    /**
     * Get a subscriber by his/her username.
     * 
     * @param accountId
     *            The account the subscriber is in.
     * @param userName
     *            The username of the subscriber we want.
     * @return The subscriber that matched the username or null if not found.
     */
    public Subscriber getSubscriberByUsername(int accountId, String userName);

    public boolean isSubscriberActive(int accountId, String username);

    public byte[] getSubscriberImage(int accountId, int subscriberId);

    public List<Subscriber> getAllSubscribers(int accountId);

    public List<Subscriber> getAllActiveSubscribers(int accountId);

    public List<Subscriber> getSpecificSubscribers(int accountId, List<Integer> subscriberIds);

    public boolean isAdmin(int accountId, int subscriberId);

    public boolean isPrimary(int accountId, int subscriberId);

    public boolean isManager(int accountId, int subscriberId);

    public List<Subscriber> getSubscribersFromSearch(int accountId, String orgId, String username, String location);

    /**
     * Check for a unique username. All user names must be unique within an
     * account.
     * 
     * @param accountId
     *            The account to check within.
     * @param userName
     *            The name to check.
     * @return boolean Whether it's unique or not.
     */
    public boolean isUserNameUnique(int accountId, String userName);

    /**
     * This method updates data on the subscriber object.
     * 
     * @param accountId
     *            The account the subscriber is a member of.
     * @param subscriber
     *            The subscriber to update.
     */
    public Subscriber updateSubscriber(int accountId, Subscriber subscriber);

    /**
     * Add a subscriber to the database and associate the subscriber with the
     * given account. <br />
     * Note that as a side-effect, the subscriberId member of the account object
     * will be set to the id associated with the new subscriber.
     * 
     * @param account
     *            The account to add the new subscriber to.
     * @param subscriber
     *            The new subscriber to add.
     * @return int The unique id of the newly created subscriber.
     */
    public Subscriber addSubscriber(Account account, Subscriber subscriber);

    /**
     * mark a subscriber in the database as inactive for the given account. <br />
     * 
     * @param accountId
     *            The account ID the subscriber belongs to.
     * @param subscriber
     *            The subscriber being deactivated.
     */
    public Subscriber deleteSubscriber(int accountId, Subscriber subscriber);

    /**
     * Honestly and truly remove all the rows associated with the given
     * subscriber; wipe them from existence as if they'd never been...
     * 
     * @param accountId
     * @param subscriberId
     */
    public void deleteSubscriberForReal(int accountId, int subscriberId);

    /**
     * Resets the subscriber's password.
     * 
     * @param account
     *            The account the subscriber is associated with.
     * @param subscriber
     *            The subscriber who needs a new temporary password.
     */
    public Subscriber resetSubscriberPassword(Account account, Subscriber subscriber);

    /**
     * Updates a subscriber's password to a new password. This password must
     * have come from the subscriber him/her self.
     * 
     * @param account
     *            The account the subscriber is associated with.
     * @param subscriber
     *            The subscriber who needs to update his/her password.
     */
    public Subscriber updateSubscriberPassword(Account account, Subscriber subscriber);

    /**
     * Given a subscriber ID, return the underlying HR representation of the
     * subscribers id.
     * 
     * @param accountId
     * @param subscriberId
     * @return
     */
    public String getOriginalSubscriberId(int accountId, int subscriberId);

    public List<SubscriberPhone> getSubscriberPhoneBySubscriberId(int accountId, int subscriberId);

    public SubscriberPhone addSubscriberPhone(int accountId, int subscriberId, SubscriberPhone subscriberPhone);

    public SubscriberPhone updateSubscriberPhone(int accountId, int subscriberId, SubscriberPhone subscriberPhone);

    /************************ Contact Methods ***********************************/

    public Contact getContact(int accountId, int contactId);

    public List<Contact> getContactsForOwner(int accountId, int subscriberId);

    public List<Contact> getContactsFromSearch(int accountId, int subscriberId, String username);

    public List<Contact> addContacts(int accountId, List<Contact> contacts);

    public List<Contact> addContactsBatch(int accountId, List<Contact> contacts);

    public Contact addContact(int accountId, Contact contact);

    public Contact updateContact(int accountId, Contact contact);

    public void deleteContact(int accountId, int contactId);

    public void deleteContacts(int accountId, List<Integer> contactIds);

    public void deleteAllContactsForSubscriberBatch(int accountId, int subscriberId);

    public List<Contact> getPublicContacts(int accountId, int subscriberId);

    public void addSubscriberToPublicContacts(int accountId, int subscriberId, List<Integer> contactIds);

    public void unsubscribeFromPublicContacts(int accountId, int subscriberId, List<Integer> contactIds);

    public void unsubscribeFromAllPublicContacts(int accountId, int subscriberId);

    /************************** Group Methods ***********************************/

    public Group addGroup(int accountId, Group group);

    /**
     * Updates all group data EXCLUDING the members of the group contained
     * within the object. Call modifyGroupMembers to modify the group members.
     * 
     * @param accountId
     * @param group
     * @return
     */
    public Group modifyGroup(int accountId, Group group);

    public Group modifyGroupMembers(int accountId, Group group);

    public Group getGroup(int accountId, int groupId);

    /**
     * Returns ALL groups associated with a user including groups he/she
     * createed, public groups he/she added and groups the account admin added
     * to everyone. <br />
     * <br />
     * Note the groups should be returned in ascending sort order by group name.
     * 
     * @param accountId
     * @param subscriberId
     * @return
     */
    public List<Group> getGroupsForSubscriber(int accountId, int subscriberId);

    public void deleteGroup(int accountId, int subscriberId, int groupId);

    public List<Group> getPublicGroups(int accountId, int subscriberId);

    /**
     * Get all the IDs of the members of the group.
     * 
     * @param subscriberId
     *            The subscriber making the request. Used for authorization.
     * @param accountId
     *            The account to look in for the group.
     * @param groupId
     *            The group we want the members for.
     * @return
     */
    public List<Integer> getSubscribersIdsForGroup(int accountId, int groupId, int subscriberId);

    /**
     * Gets the temporary password for a given subscriber. If there isn't one,
     * it returns null.
     * 
     * @param accountId
     *            The account the subscriber is a member of.
     * @param subscriber
     *            The subscriber whose un-encrypted temp password we want.
     * @return String The un-encrypted temporary password.
     */
    public String getTempPassword(int accountId, Subscriber subscriber);

    public List<Contact> getContactsForGroup(int accountId, int groupId);

    public List<Subscriber> getSubscribersForGroup(int accountId, int groupId, int subscriberId);

    public void removeSubscribersFromGroup(int accountId, Group group, List<Subscriber> subscribers);

    public void removeContactsFromGroup(int accountId, Group group, List<Contact> contacts);

    public void addContactsToGroup(int accountId, Group group, List<Contact> contacts);

    public void addSubscribersToGroup(int accountId, Group group, List<Subscriber> subscribers);

    public void addSubscriberToPublicGroups(int accountId, int subscriberId, List<Integer> groupIds);

    public void unsubscribeFromPublicGroups(int accountId, int subscriberId, List<Integer> groupIds);

    public void unsubscribeFromAllPublicGroups(int accountId, int subscriberId);

    public List<Subscriber> getGroupAdmins(int accountId, int groupId);

    public void addGroupAdmin(int accountId, int groupId, int subscriberId);

    public void addGroupAdmins(int accountId, int groupId, List<Subscriber> admins);

    public void deleteGroupAdmin(int accountId, int groupId, int subscriber);

    public void deleteGroupAdmins(int accountId, int groupId, List<Subscriber> admins);

    /************************** Other Methods ***********************************/

    public Subscriber authenticate(int accountId, String username, String password) throws TeamAuthenticationException,
            TeamSubscriberRequiresUpdateException, TeamSubscriberRequiresEulaException;

    public void prefetcher(int accountId);

    public List<Organization> getAllOrganizations(int accountId);

    public List<Location> getAllLocations(int accountId);
}
