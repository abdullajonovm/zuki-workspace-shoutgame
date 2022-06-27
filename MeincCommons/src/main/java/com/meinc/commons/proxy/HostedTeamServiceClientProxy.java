package com.meinc.commons.proxy;

import com.meinc.mrsoa.service.ServiceEndpoint;
import com.meinc.mrsoa.service.ServiceMessage;
import com.meinc.mrsoa.service.IMrSoaProxy;

public class HostedTeamServiceClientProxy implements IMrSoaProxy
      , com.meinc.commons.team.ITeam
   {
   public static final ServiceEndpoint endpoint = makeEndpoint();
   
   public HostedTeamServiceClientProxy() {
      ServiceMessage.waitForServiceRegistration(endpoint);
   }
   
   public HostedTeamServiceClientProxy(boolean wait) {
      if (wait)
        ServiceMessage.waitForServiceRegistration(endpoint);
   }
   
   private static ServiceEndpoint makeEndpoint()
   {
      //TODO: inject from spring or JNDI.
      ServiceEndpoint ep = new ServiceEndpoint();
      ep.setNamespace("phoenix-service");
      ep.setServiceName("HostedTeamService");
            return ep;
   }

      @SuppressWarnings("unchecked")
   public boolean isSubscriberActive(
               int accountId
         ,                java.lang.String username
                     )
         {
       return (Boolean)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "isSubscriberActive"
                     , accountId
                     , username
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.Subscriber addSubscriber(
               com.meinc.commons.domain.Account account
         ,                com.meinc.commons.domain.Subscriber subscriber
                     )
         {
       return (com.meinc.commons.domain.Subscriber)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "addSubscriber"
                     , account
                     , subscriber
               );
   }

      @SuppressWarnings("unchecked")
   public java.lang.String getTempPassword(
               int accountId
         ,                com.meinc.commons.domain.Subscriber subscriber
                     )
         {
       return (java.lang.String)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getTempPassword"
                     , accountId
                     , subscriber
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.Subscriber deleteSubscriber(
               int accountId
         ,                com.meinc.commons.domain.Subscriber subscriber
                     )
         {
       return (com.meinc.commons.domain.Subscriber)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "deleteSubscriber"
                     , accountId
                     , subscriber
               );
   }

      @SuppressWarnings("unchecked")
   public void deleteSubscriberForReal(
               int accountId
         ,                int subscriberId
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "deleteSubscriberForReal"
                     , accountId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public java.lang.String getOriginalSubscriberId(
               int accountId
         ,                int subscriberId
                     )
         {
       return (java.lang.String)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getOriginalSubscriberId"
                     , accountId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.Contact getContact(
               int accountId
         ,                int contactId
                     )
         {
       return (com.meinc.commons.domain.Contact)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getContact"
                     , accountId
                     , contactId
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.Contact> getContactsForOwner(
               int accountId
         ,                int subscriberId
                     )
         {
       return (java.util.List<com.meinc.commons.domain.Contact>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getContactsForOwner"
                     , accountId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.Contact> getContactsFromSearch(
               int accountId
         ,                int subscriberId
         ,                java.lang.String username
                     )
         {
       return (java.util.List<com.meinc.commons.domain.Contact>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getContactsFromSearch"
                     , accountId
                     , subscriberId
                     , username
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.Contact> addContacts(
               int accountId
         ,                java.util.List<com.meinc.commons.domain.Contact> contacts
                     )
         {
       return (java.util.List<com.meinc.commons.domain.Contact>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "addContacts"
                     , accountId
                     , contacts
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.Contact> addContactsBatch(
               int accountId
         ,                java.util.List<com.meinc.commons.domain.Contact> contacts
                     )
         {
       return (java.util.List<com.meinc.commons.domain.Contact>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "addContactsBatch"
                     , accountId
                     , contacts
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.Contact addContact(
               int accountId
         ,                com.meinc.commons.domain.Contact contact
                     )
         {
       return (com.meinc.commons.domain.Contact)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "addContact"
                     , accountId
                     , contact
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.Group addGroup(
               int accountId
         ,                com.meinc.commons.domain.Group group
                     )
         {
       return (com.meinc.commons.domain.Group)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "addGroup"
                     , accountId
                     , group
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.Group modifyGroupMembers(
               int accountId
         ,                com.meinc.commons.domain.Group group
                     )
         {
       return (com.meinc.commons.domain.Group)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "modifyGroupMembers"
                     , accountId
                     , group
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.Group> getGroupsForSubscriber(
               int accountId
         ,                int subscriberId
                     )
         {
       return (java.util.List<com.meinc.commons.domain.Group>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getGroupsForSubscriber"
                     , accountId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public void deleteGroup(
               int accountId
         ,                int subscriberId
         ,                int groupId
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "deleteGroup"
                     , accountId
                     , subscriberId
                     , groupId
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.Group> getPublicGroups(
               int accountId
         ,                int subscriberId
                     )
         {
       return (java.util.List<com.meinc.commons.domain.Group>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getPublicGroups"
                     , accountId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<java.lang.Integer> getSubscribersIdsForGroup(
               int accountId
         ,                int groupId
         ,                int subscriberId
                     )
         {
       return (java.util.List<java.lang.Integer>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getSubscribersIdsForGroup"
                     , accountId
                     , groupId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.Contact> getContactsForGroup(
               int accountId
         ,                int groupId
                     )
         {
       return (java.util.List<com.meinc.commons.domain.Contact>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getContactsForGroup"
                     , accountId
                     , groupId
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.Subscriber> getSubscribersForGroup(
               int accountId
         ,                int groupId
         ,                int subscriberId
                     )
         {
       return (java.util.List<com.meinc.commons.domain.Subscriber>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getSubscribersForGroup"
                     , accountId
                     , groupId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.Subscriber authenticate(
               int accountId
         ,                java.lang.String username
         ,                java.lang.String password
                     )
               throws
                     com.meinc.commons.team.exception.TeamAuthenticationException
            ,                      com.meinc.commons.team.exception.TeamSubscriberRequiresUpdateException
            ,                      com.meinc.commons.team.exception.TeamSubscriberRequiresEulaException
                              {
       return (com.meinc.commons.domain.Subscriber)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "authenticate"
                     , accountId
                     , username
                     , password
               );
   }

      @SuppressWarnings("unchecked")
   public void prefetcher(
               int accountId
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "prefetcher"
                     , accountId
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.Subscriber getSubscriber(
               int accountId
         ,                int subscriberId
                     )
         {
       return (com.meinc.commons.domain.Subscriber)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getSubscriber"
                     , accountId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.Subscriber getSubscriberWithoutExtensions(
               int accountId
         ,                int subscriberId
                     )
         {
       return (com.meinc.commons.domain.Subscriber)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getSubscriberWithoutExtensions"
                     , accountId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.Account> getSubscribersByCellNumber(
               java.lang.String cellNumber
                     )
         {
       return (java.util.List<com.meinc.commons.domain.Account>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getSubscribersByCellNumber"
                     , cellNumber
               );
   }

      @SuppressWarnings("unchecked")
   public boolean isCellNumberTaken(
               int accountId
         ,                java.lang.String cellNumber
                     )
         {
       return (Boolean)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "isCellNumberTaken"
                     , accountId
                     , cellNumber
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.Subscriber getSubscriberByCellNumber(
               int accountId
         ,                java.lang.String cellNumber
                     )
         {
       return (com.meinc.commons.domain.Subscriber)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getSubscriberByCellNumber"
                     , accountId
                     , cellNumber
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.Subscriber getSubscriberByUsername(
               int accountId
         ,                java.lang.String userName
                     )
         {
       return (com.meinc.commons.domain.Subscriber)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getSubscriberByUsername"
                     , accountId
                     , userName
               );
   }

      @SuppressWarnings("unchecked")
   public byte[] getSubscriberImage(
               int accountId
         ,                int subscriberId
                     )
         {
       return (byte[])  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getSubscriberImage"
                     , accountId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.Subscriber> getAllSubscribers(
               int accountId
                     )
         {
       return (java.util.List<com.meinc.commons.domain.Subscriber>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getAllSubscribers"
                     , accountId
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.Subscriber> getAllActiveSubscribers(
               int accountId
                     )
         {
       return (java.util.List<com.meinc.commons.domain.Subscriber>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getAllActiveSubscribers"
                     , accountId
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.Subscriber> getSpecificSubscribers(
               int accountId
         ,                java.util.List<java.lang.Integer> subscriberIds
                     )
         {
       return (java.util.List<com.meinc.commons.domain.Subscriber>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getSpecificSubscribers"
                     , accountId
                     , subscriberIds
               );
   }

      @SuppressWarnings("unchecked")
   public boolean isAdmin(
               int accountId
         ,                int subscriberId
                     )
         {
       return (Boolean)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "isAdmin"
                     , accountId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public boolean isPrimary(
               int accountId
         ,                int subscriberId
                     )
         {
       return (Boolean)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "isPrimary"
                     , accountId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public boolean isManager(
               int accountId
         ,                int subscriberId
                     )
         {
       return (Boolean)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "isManager"
                     , accountId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.Organization> getAllOrganizations(
               int accountId
                     )
         {
       return (java.util.List<com.meinc.commons.domain.Organization>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getAllOrganizations"
                     , accountId
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.Subscriber> getSubscribersFromSearch(
               int accountId
         ,                java.lang.String orgId
         ,                java.lang.String username
         ,                java.lang.String location
                     )
         {
       return (java.util.List<com.meinc.commons.domain.Subscriber>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getSubscribersFromSearch"
                     , accountId
                     , orgId
                     , username
                     , location
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.Location> getAllLocations(
               int accountId
                     )
         {
       return (java.util.List<com.meinc.commons.domain.Location>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getAllLocations"
                     , accountId
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.Subscriber updateSubscriber(
               int accountId
         ,                com.meinc.commons.domain.Subscriber subscriber
                     )
         {
       return (com.meinc.commons.domain.Subscriber)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "updateSubscriber"
                     , accountId
                     , subscriber
               );
   }

      @SuppressWarnings("unchecked")
   public boolean isUserNameUnique(
               int accountId
         ,                java.lang.String userName
                     )
         {
       return (Boolean)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "isUserNameUnique"
                     , accountId
                     , userName
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.Subscriber resetSubscriberPassword(
               com.meinc.commons.domain.Account account
         ,                com.meinc.commons.domain.Subscriber subscriber
                     )
         {
       return (com.meinc.commons.domain.Subscriber)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "resetSubscriberPassword"
                     , account
                     , subscriber
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.Subscriber updateSubscriberPassword(
               com.meinc.commons.domain.Account account
         ,                com.meinc.commons.domain.Subscriber subscriber
                     )
         {
       return (com.meinc.commons.domain.Subscriber)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "updateSubscriberPassword"
                     , account
                     , subscriber
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.Group getGroup(
               int accountId
         ,                int groupId
                     )
         {
       return (com.meinc.commons.domain.Group)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getGroup"
                     , accountId
                     , groupId
               );
   }

      @SuppressWarnings("unchecked")
   public void deleteContact(
               int accountId
         ,                int contactId
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "deleteContact"
                     , accountId
                     , contactId
               );
   }

      @SuppressWarnings("unchecked")
   public void deleteContacts(
               int accountId
         ,                java.util.List<java.lang.Integer> contactIds
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "deleteContacts"
                     , accountId
                     , contactIds
               );
   }

      @SuppressWarnings("unchecked")
   public void deleteAllContactsForSubscriberBatch(
               int accountId
         ,                int subscriberId
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "deleteAllContactsForSubscriberBatch"
                     , accountId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.Contact updateContact(
               int accountId
         ,                com.meinc.commons.domain.Contact contact
                     )
         {
       return (com.meinc.commons.domain.Contact)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "updateContact"
                     , accountId
                     , contact
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.Group modifyGroup(
               int accountId
         ,                com.meinc.commons.domain.Group group
                     )
         {
       return (com.meinc.commons.domain.Group)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "modifyGroup"
                     , accountId
                     , group
               );
   }

      @SuppressWarnings("unchecked")
   public void removeContactsFromGroup(
               int accountId
         ,                com.meinc.commons.domain.Group group
         ,                java.util.List<com.meinc.commons.domain.Contact> contacts
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "removeContactsFromGroup"
                     , accountId
                     , group
                     , contacts
               );
   }

      @SuppressWarnings("unchecked")
   public void removeSubscribersFromGroup(
               int accountId
         ,                com.meinc.commons.domain.Group group
         ,                java.util.List<com.meinc.commons.domain.Subscriber> subscribers
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "removeSubscribersFromGroup"
                     , accountId
                     , group
                     , subscribers
               );
   }

      @SuppressWarnings("unchecked")
   public void addContactsToGroup(
               int accountId
         ,                com.meinc.commons.domain.Group group
         ,                java.util.List<com.meinc.commons.domain.Contact> contacts
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "addContactsToGroup"
                     , accountId
                     , group
                     , contacts
               );
   }

      @SuppressWarnings("unchecked")
   public void addSubscribersToGroup(
               int accountId
         ,                com.meinc.commons.domain.Group group
         ,                java.util.List<com.meinc.commons.domain.Subscriber> subscribers
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "addSubscribersToGroup"
                     , accountId
                     , group
                     , subscribers
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.Contact> getPublicContacts(
               int accountId
         ,                int subscriberId
                     )
         {
       return (java.util.List<com.meinc.commons.domain.Contact>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getPublicContacts"
                     , accountId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public void addSubscriberToPublicGroups(
               int accountId
         ,                int subscriberId
         ,                java.util.List<java.lang.Integer> groupIds
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "addSubscriberToPublicGroups"
                     , accountId
                     , subscriberId
                     , groupIds
               );
   }

      @SuppressWarnings("unchecked")
   public void unsubscribeFromPublicGroups(
               int accountId
         ,                int subscriberId
         ,                java.util.List<java.lang.Integer> groupIds
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "unsubscribeFromPublicGroups"
                     , accountId
                     , subscriberId
                     , groupIds
               );
   }

      @SuppressWarnings("unchecked")
   public void unsubscribeFromAllPublicGroups(
               int accountId
         ,                int subscriberId
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "unsubscribeFromAllPublicGroups"
                     , accountId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public void addSubscriberToPublicContacts(
               int accountId
         ,                int subscriberId
         ,                java.util.List<java.lang.Integer> contactIds
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "addSubscriberToPublicContacts"
                     , accountId
                     , subscriberId
                     , contactIds
               );
   }

      @SuppressWarnings("unchecked")
   public void unsubscribeFromPublicContacts(
               int accountId
         ,                int subscriberId
         ,                java.util.List<java.lang.Integer> contactIds
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "unsubscribeFromPublicContacts"
                     , accountId
                     , subscriberId
                     , contactIds
               );
   }

      @SuppressWarnings("unchecked")
   public void unsubscribeFromAllPublicContacts(
               int accountId
         ,                int subscriberId
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "unsubscribeFromAllPublicContacts"
                     , accountId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.Subscriber> getGroupAdmins(
               int accountId
         ,                int groupId
                     )
         {
       return (java.util.List<com.meinc.commons.domain.Subscriber>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getGroupAdmins"
                     , accountId
                     , groupId
               );
   }

      @SuppressWarnings("unchecked")
   public void addGroupAdmin(
               int accountId
         ,                int groupId
         ,                int subscriberId
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "addGroupAdmin"
                     , accountId
                     , groupId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public void deleteGroupAdmin(
               int accountId
         ,                int groupId
         ,                int subscriberId
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "deleteGroupAdmin"
                     , accountId
                     , groupId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public void addGroupAdmins(
               int accountId
         ,                int groupId
         ,                java.util.List<com.meinc.commons.domain.Subscriber> admins
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "addGroupAdmins"
                     , accountId
                     , groupId
                     , admins
               );
   }

      @SuppressWarnings("unchecked")
   public void deleteGroupAdmins(
               int accountId
         ,                int groupId
         ,                java.util.List<com.meinc.commons.domain.Subscriber> admins
                     )
         {
       ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "deleteGroupAdmins"
                     , accountId
                     , groupId
                     , admins
               );
   }

      @SuppressWarnings("unchecked")
   public java.util.List<com.meinc.commons.domain.SubscriberPhone> getSubscriberPhoneBySubscriberId(
               int accountId
         ,                int subscriberId
                     )
         {
       return (java.util.List<com.meinc.commons.domain.SubscriberPhone>)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "getSubscriberPhoneBySubscriberId"
                     , accountId
                     , subscriberId
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.SubscriberPhone addSubscriberPhone(
               int accountId
         ,                int subscriberId
         ,                com.meinc.commons.domain.SubscriberPhone subscriberPhone
                     )
         {
       return (com.meinc.commons.domain.SubscriberPhone)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "addSubscriberPhone"
                     , accountId
                     , subscriberId
                     , subscriberPhone
               );
   }

      @SuppressWarnings("unchecked")
   public com.meinc.commons.domain.SubscriberPhone updateSubscriberPhone(
               int accountId
         ,                int subscriberId
         ,                com.meinc.commons.domain.SubscriberPhone subscriberPhone
                     )
         {
       return (com.meinc.commons.domain.SubscriberPhone)  ServiceMessage.send(HostedTeamServiceClientProxy.endpoint, "updateSubscriberPhone"
                     , accountId
                     , subscriberId
                     , subscriberPhone
               );
   }

      public ServiceEndpoint getEndpoint() {
      return endpoint;
   }
}
