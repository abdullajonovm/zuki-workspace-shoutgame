package com.meinc.commons.proxy;

import java.util.List;

import com.meinc.commons.domain.Contact;
import com.meinc.commons.domain.Subscriber;
import com.meinc.commons.domain.SubscriberPhone;
import com.meinc.commons.team.ITeam;
import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.ServiceMessage;

public class TeamDispatcherServiceClientProxy implements ITeam {
  private ServiceEndpoint _endpoint;

  public TeamDispatcherServiceClientProxy() {
    // TODO: inject from spring or JNDI.
    ServiceEndpoint ep = new ServiceEndpoint();
    ep.setNamespace("phoenix-service");
    ep.setServiceName("TeamDispatcherService");
    ServiceMessage.waitForServiceRegistration(ep);
    _endpoint = ep;
  }

  public String getOriginalSubscriberId(int accountId, int subscriberId)
  {
  	return (String) ServiceMessage.send(_endpoint,
        "getOriginalSubscriberId", accountId, subscriberId);
  }

  public boolean isSubscriberActive(int accountId, java.lang.String username) {
    return (Boolean) ServiceMessage.send(_endpoint,
        "isSubscriberActive", accountId, username);
  }

  public com.meinc.commons.domain.Subscriber addSubscriber(
      com.meinc.commons.domain.Account account,
      com.meinc.commons.domain.Subscriber subscriber) {
    return (com.meinc.commons.domain.Subscriber) ServiceMessage
        .send(_endpoint, "addSubscriber", account, subscriber);
  }

  public java.lang.String getTempPassword(int accountId,
      com.meinc.commons.domain.Subscriber subscriber) {
    return (java.lang.String) ServiceMessage.send(_endpoint,
        "getTempPassword", accountId, subscriber);
  }

  public com.meinc.commons.domain.Subscriber deleteSubscriber(
      int accountId, com.meinc.commons.domain.Subscriber subscriber) {
    return (com.meinc.commons.domain.Subscriber) ServiceMessage
        .send(_endpoint, "deleteSubscriber", accountId, subscriber);
  }

  public void deleteSubscriberForReal(
      int accountId, int subscriberId) {
    ServiceMessage.send(_endpoint, "deleteSubscriberForReal", accountId, subscriberId);
  }
  
  public com.meinc.commons.domain.Contact getContact(int accountId,
      int contactId) {
    return (com.meinc.commons.domain.Contact) ServiceMessage.send(
        _endpoint, "getContact", accountId, contactId);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<com.meinc.commons.domain.Contact> getContactsForOwner(
      int accountId, int subscriberId) {
    return (java.util.List<com.meinc.commons.domain.Contact>) ServiceMessage
        .send(_endpoint, "getContactsForOwner", accountId, subscriberId);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<com.meinc.commons.domain.Contact> getContactsFromSearch(
      int accountId, int subscriberId, java.lang.String username) {
    return (java.util.List<com.meinc.commons.domain.Contact>) ServiceMessage
        .send(_endpoint, "getContactsFromSearch", accountId, subscriberId,
            username);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<com.meinc.commons.domain.Contact> addContacts(
      int accountId,
      java.util.List<com.meinc.commons.domain.Contact> contacts) {
    return (java.util.List<com.meinc.commons.domain.Contact>) ServiceMessage
        .send(_endpoint, "addContacts", accountId, contacts);
  }

  @SuppressWarnings("unchecked")
  public List<Contact> addContactsBatch(int accountId,
      java.util.List<com.meinc.commons.domain.Contact> contacts) {
    return (List<Contact>) ServiceMessage.send(_endpoint, "addContactsBatch", accountId,
        contacts);
  }

  public com.meinc.commons.domain.Contact addContact(int accountId,
      com.meinc.commons.domain.Contact contact) {
    return (com.meinc.commons.domain.Contact) ServiceMessage.send(
        _endpoint, "addContact", accountId, contact);
  }

  public com.meinc.commons.domain.Group addGroup(int accountId,
      com.meinc.commons.domain.Group group) {
    return (com.meinc.commons.domain.Group) ServiceMessage.send(
        _endpoint, "addGroup", accountId, group);
  }

  public com.meinc.commons.domain.Group modifyGroupMembers(int accountId,
      com.meinc.commons.domain.Group group) {
    return (com.meinc.commons.domain.Group) ServiceMessage.send(
        _endpoint, "modifyGroupMembers", accountId, group);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<com.meinc.commons.domain.Group> getGroupsForSubscriber(
      int accountId, int subscriberId) {
    return (java.util.List<com.meinc.commons.domain.Group>) ServiceMessage
        .send(_endpoint, "getGroupsForSubscriber", accountId, subscriberId);
  }

  public void deleteGroup(int accountId, int subscriberId, int groupId) {
    ServiceMessage.send(_endpoint, "deleteGroup", accountId,
        subscriberId, groupId);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<com.meinc.commons.domain.Group> getPublicGroups(
      int accountId, int subscriberId) {
    return (java.util.List<com.meinc.commons.domain.Group>) ServiceMessage
        .send(_endpoint, "getPublicGroups", accountId, subscriberId);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<java.lang.Integer> getSubscribersIdsForGroup(
      int accountId, int groupId, int subscriberId) {
    return (java.util.List<java.lang.Integer>) ServiceMessage.send(
        _endpoint, "getSubscribersIdsForGroup", accountId, groupId,
        subscriberId);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<com.meinc.commons.domain.Contact> getContactsForGroup(
      int accountId, int groupId) {
    return (java.util.List<com.meinc.commons.domain.Contact>) ServiceMessage
        .send(_endpoint, "getContactsForGroup", accountId, groupId);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<com.meinc.commons.domain.Subscriber> getSubscribersForGroup(
      int accountId, int groupId, int subscriberId) {
    return (java.util.List<com.meinc.commons.domain.Subscriber>) ServiceMessage
        .send(_endpoint, "getSubscribersForGroup", accountId, groupId,
            subscriberId);
  }

  public com.meinc.commons.domain.Subscriber authenticate(int accountId,
      java.lang.String username, java.lang.String password) {
    return (com.meinc.commons.domain.Subscriber) ServiceMessage
        .send(_endpoint, "authenticate", accountId, username, password);
  }

  public void prefetcher(int accountId) {
    ServiceMessage.send(_endpoint, "prefetcher", accountId);
  }

  public com.meinc.commons.domain.Subscriber getSubscriber(int accountId,
      int subscriberId) {
    return (com.meinc.commons.domain.Subscriber) ServiceMessage
        .send(_endpoint, "getSubscriber", accountId, subscriberId);
  }

  public com.meinc.commons.domain.Subscriber getSubscriberWithoutExtensions(int accountId,
      int subscriberId) {
    return (com.meinc.commons.domain.Subscriber) ServiceMessage
        .send(_endpoint, "getSubscriberWithoutExtensions", accountId, subscriberId);
  }
  
  @SuppressWarnings("unchecked")
  public java.util.List<com.meinc.commons.domain.Account> getSubscribersByCellNumber(
      java.lang.String cellNumber) {
    return (java.util.List<com.meinc.commons.domain.Account>) ServiceMessage
        .send(_endpoint, "getSubscribersByCellNumber", cellNumber);
  }
  
    public boolean isCellNumberTaken(int accountId, String cellNumber)
    {
        return (Boolean) ServiceMessage.send(_endpoint, "isCellNumberTaken", accountId, cellNumber);
    }
    
    public Subscriber getSubscriberByCellNumber(int accountId, String cellNumber)
    {
        return (Subscriber) ServiceMessage.send(_endpoint, "getSubscriberByCellNumber", accountId, cellNumber);
    }

  public com.meinc.commons.domain.Subscriber getSubscriberByUsername(
      int accountId, java.lang.String userName) {
    return (com.meinc.commons.domain.Subscriber) ServiceMessage
        .send(_endpoint, "getSubscriberByUsername", accountId, userName);
  }

  public byte[] getSubscriberImage(int accountId, int subscriberId) {
    return (byte[]) ServiceMessage.send(_endpoint, "getSubscriberImage",
        accountId, subscriberId);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<com.meinc.commons.domain.Subscriber> getAllSubscribers(
      int accountId) {
    return (java.util.List<com.meinc.commons.domain.Subscriber>) ServiceMessage
        .send(_endpoint, "getAllSubscribers", accountId);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<com.meinc.commons.domain.Subscriber> getAllActiveSubscribers(
      int accountId) {
    return (java.util.List<com.meinc.commons.domain.Subscriber>) ServiceMessage
        .send(_endpoint, "getAllActiveSubscribers", accountId);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<com.meinc.commons.domain.Subscriber> getSpecificSubscribers(
      int accountId, java.util.List<java.lang.Integer> subscriberIds) {
    return (java.util.List<com.meinc.commons.domain.Subscriber>) ServiceMessage
        .send(_endpoint, "getSpecificSubscribers", accountId, subscriberIds);
  }

  public boolean isAdmin(int accountId, int subscriberId) {
    return (Boolean) ServiceMessage.send(_endpoint, "isAdmin",
        accountId, subscriberId);
  }

  public boolean isPrimary(int accountId, int subscriberId) {
    return (Boolean) ServiceMessage.send(_endpoint, "isPrimary",
        accountId, subscriberId);
  }

  public boolean isManager(int accountId, int subscriberId) {
    return (Boolean) ServiceMessage.send(_endpoint, "isManager",
        accountId, subscriberId);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<com.meinc.commons.domain.Organization> getAllOrganizations(
      int accountId) {
    return (java.util.List<com.meinc.commons.domain.Organization>) ServiceMessage
        .send(_endpoint, "getAllOrganizations", accountId);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<com.meinc.commons.domain.Subscriber> getSubscribersFromSearch(
      int accountId, java.lang.String orgId, java.lang.String username,
      java.lang.String location) {
    return (java.util.List<com.meinc.commons.domain.Subscriber>) ServiceMessage
        .send(_endpoint, "getSubscribersFromSearch", accountId, orgId,
            username, location);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<com.meinc.commons.domain.Location> getAllLocations(
      int accountId) {
    return (java.util.List<com.meinc.commons.domain.Location>) ServiceMessage
        .send(_endpoint, "getAllLocations", accountId);
  }

  public com.meinc.commons.domain.Subscriber updateSubscriber(
      int accountId, com.meinc.commons.domain.Subscriber subscriber) {
    return (com.meinc.commons.domain.Subscriber) ServiceMessage
        .send(_endpoint, "updateSubscriber", accountId, subscriber);
  }

  public boolean isUserNameUnique(int accountId, java.lang.String userName) {
    return (Boolean) ServiceMessage.send(_endpoint, "isUserNameUnique",
        accountId, userName);
  }

  public com.meinc.commons.domain.Subscriber resetSubscriberPassword(
      com.meinc.commons.domain.Account account,
      com.meinc.commons.domain.Subscriber subscriber) {
    return (com.meinc.commons.domain.Subscriber) ServiceMessage
        .send(_endpoint, "resetSubscriberPassword", account, subscriber);
  }

  public com.meinc.commons.domain.Subscriber updateSubscriberPassword(
      com.meinc.commons.domain.Account account,
      com.meinc.commons.domain.Subscriber subscriber) {
    return (com.meinc.commons.domain.Subscriber) ServiceMessage
        .send(_endpoint, "updateSubscriberPassword", account, subscriber);
  }

  public com.meinc.commons.domain.Group getGroup(int accountId,
      int groupId) {
    return (com.meinc.commons.domain.Group) ServiceMessage.send(
        _endpoint, "getGroup", accountId, groupId);
  }

  public void deleteContact(int accountId, int contactId) {
    ServiceMessage
        .send(_endpoint, "deleteContact", accountId, contactId);
  }

  public void deleteContacts(int accountId,
      java.util.List<java.lang.Integer> contactIds) {
    ServiceMessage.send(_endpoint, "deleteContacts", accountId,
        contactIds);
  }

  public void deleteAllContactsForSubscriberBatch(int accountId, int subscriberId) {
    ServiceMessage.send(_endpoint, "deleteAllContactsForSubscriberBatch", accountId,
        subscriberId);
  }

  public com.meinc.commons.domain.Contact updateContact(int accountId,
      com.meinc.commons.domain.Contact contact) {
    return (com.meinc.commons.domain.Contact) ServiceMessage.send(
        _endpoint, "updateContact", accountId, contact);
  }

  public com.meinc.commons.domain.Group modifyGroup(int accountId,
      com.meinc.commons.domain.Group group) {
    return (com.meinc.commons.domain.Group) ServiceMessage.send(
        _endpoint, "modifyGroup", accountId, group);
  }

  public void removeContactsFromGroup(int accountId,
      com.meinc.commons.domain.Group group,
      java.util.List<com.meinc.commons.domain.Contact> contacts) {
    ServiceMessage.send(_endpoint, "removeContactsFromGroup", accountId,
        group, contacts);
  }

  public void removeSubscribersFromGroup(int accountId,
      com.meinc.commons.domain.Group group,
      java.util.List<com.meinc.commons.domain.Subscriber> subscribers) {
    ServiceMessage.send(_endpoint, "removeSubscribersFromGroup",
        accountId, group, subscribers);
  }

  public void addContactsToGroup(int accountId,
      com.meinc.commons.domain.Group group,
      java.util.List<com.meinc.commons.domain.Contact> contacts) {
    ServiceMessage.send(_endpoint, "addContactsToGroup", accountId,
        group, contacts);
  }

  public void addSubscribersToGroup(int accountId,
      com.meinc.commons.domain.Group group,
      java.util.List<com.meinc.commons.domain.Subscriber> subscribers) {
    ServiceMessage.send(_endpoint, "addSubscribersToGroup", accountId,
        group, subscribers);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<com.meinc.commons.domain.Contact> getPublicContacts(
      int accountId, int subscriberId) {
    return (java.util.List<com.meinc.commons.domain.Contact>) ServiceMessage
        .send(_endpoint, "getPublicContacts", accountId, subscriberId);
  }

  public void addSubscriberToPublicGroups(int accountId, int subscriberId,
      java.util.List<java.lang.Integer> groupIds) {
    ServiceMessage.send(_endpoint, "addSubscriberToPublicGroups",
        accountId, subscriberId, groupIds);
  }

  public void unsubscribeFromPublicGroups(int accountId, int subscriberId,
      java.util.List<java.lang.Integer> groupIds) {
    ServiceMessage.send(_endpoint, "unsubscribeFromPublicGroups",
        accountId, subscriberId, groupIds);
  }

  public void unsubscribeFromAllPublicGroups(int accountId, int subscriberId) {
    ServiceMessage.send(_endpoint, "unsubscribeFromAllPublicGroups",
        accountId, subscriberId);
  }

  public void addSubscriberToPublicContacts(int accountId, int subscriberId,
      java.util.List<java.lang.Integer> contactIds) {
    ServiceMessage.send(_endpoint, "addSubscriberToPublicContacts",
        accountId, subscriberId, contactIds);
  }

  public void unsubscribeFromPublicContacts(int accountId, int subscriberId,
      java.util.List<java.lang.Integer> contactIds) {
    ServiceMessage.send(_endpoint, "unsubscribeFromPublicContacts",
        accountId, subscriberId, contactIds);
  }

  public void unsubscribeFromAllPublicContacts(int accountId, int subscriberId) {
    ServiceMessage.send(_endpoint, "unsubscribeFromAllPublicContacts",
        accountId, subscriberId);
  }

  @SuppressWarnings("unchecked")
  public java.util.List<com.meinc.commons.domain.Subscriber> getGroupAdmins(
      int accountId, int groupId) {
    return (java.util.List<com.meinc.commons.domain.Subscriber>) ServiceMessage
        .send(_endpoint, "getGroupAdmins", accountId, groupId);
  }

  public void addGroupAdmin(int accountId, int groupId, int subscriberId) {
    ServiceMessage.send(_endpoint, "addGroupAdmin", accountId, groupId,
        subscriberId);
  }

  public void deleteGroupAdmin(int accountId, int groupId, int subscriberId) {
    ServiceMessage.send(_endpoint, "deleteGroupAdmin", accountId,
        groupId, subscriberId);
  }

  public void addGroupAdmins(int accountId, int groupId,
      java.util.List<com.meinc.commons.domain.Subscriber> admins) {
    ServiceMessage.send(_endpoint, "addGroupAdmins", accountId, groupId,
        admins);
  }

  public void deleteGroupAdmins(int accountId, int groupId,
      java.util.List<com.meinc.commons.domain.Subscriber> admins) {
    ServiceMessage.send(_endpoint, "deleteGroupAdmins", accountId,
        groupId, admins);
  }
  
  public void registerServiceNameAsDefaulExtensiontNamespaceAlias(String extensionAlias)
  {
  	ServiceMessage.send(_endpoint, "registerServiceNameAsDefaulExtensiontNamespaceAlias", extensionAlias);
  }
  
  public void registerServiceNameAsExtensionNamespace(String serviceName, List<String> aliases)
  {
  	ServiceMessage.send(_endpoint, "registerServiceNameAsExtensionNamespace", serviceName, aliases);
  }

@SuppressWarnings("unchecked")
public List<SubscriberPhone> getSubscriberPhoneBySubscriberId(int accountId, int subscriberId) {
	return (java.util.List<com.meinc.commons.domain.SubscriberPhone>)ServiceMessage.send(_endpoint, "getSubscriberPhoneBySubscriberId", accountId, subscriberId);
}

public SubscriberPhone addSubscriberPhone(int accountId, int subscriberId, SubscriberPhone subscriberPhone) {
	return (SubscriberPhone) ServiceMessage.send(_endpoint, "addSubscriberPhone", accountId, subscriberId, subscriberPhone);
}

public SubscriberPhone updateSubscriberPhone(int accountId, int subscriberId, SubscriberPhone subscriberPhone) {
	return (SubscriberPhone) ServiceMessage.send(_endpoint, "updateSubscriberPhone", accountId, subscriberId, subscriberPhone);
}

}
