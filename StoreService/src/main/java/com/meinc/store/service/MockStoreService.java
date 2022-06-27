package com.meinc.store.service;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.meinc.mrsoa.service.annotation.Service;
import com.meinc.mrsoa.service.annotation.ServiceMethod;
import com.meinc.store.domain.CreditCardInfo;
import com.meinc.store.domain.CustomerProfile;
import com.meinc.store.domain.Entitlement;
import com.meinc.store.domain.Item;
import com.meinc.store.domain.ItemPrice;
import com.meinc.store.domain.Receipt;
import com.meinc.store.domain.ReceiptItem;
import com.meinc.store.domain.ReceiptResult;
import com.meinc.store.domain.SubscriberEntitlement;
import com.meinc.store.exception.AlreadyConsumedException;
import com.meinc.store.exception.AlreadyReservedException;
import com.meinc.store.exception.InvalidItemException;
import com.meinc.store.exception.InvalidJsonException;
import com.meinc.store.exception.InvalidReceiptException;
import com.meinc.store.exception.NoSuchCreditCardException;
import com.meinc.store.exception.StoreException;

@Service(namespace = StoreService.MEINC_NAMESPACE, name = MockStoreService.SERVICE_NAME, interfaces = StoreService.NOTIFICATION_INTERFACE, version = StoreService.SERVICE_VERSION, exposeAs = IStoreService.class)
public class MockStoreService
implements IStoreService
{
    private static Logger _logger = Logger.getLogger(MockStoreService.class);
    private static final String DB_NAME = "db_mock_store";

    public static final String SERVICE_NAME = "MockStoreService";

    private String _stateDir;

    private List<SubscriberEntitlement> _subscriberEntitlements;
    private List<Item> _items;
    private List<Receipt> _receipts;

    private Lock _subscriberEntitlementsLock = new ReentrantLock();
    private Lock _itemsLock = new ReentrantLock();
    private Lock _receiptsLock = new ReentrantLock();

    public void setStateDir(String val)
    {
        _stateDir = val;

        //see if there is already an instance of the db saved to disk. if so, use it
        if (_stateDir != null) {
            File dbParentDir = new File(_stateDir, DB_NAME);
            if (dbParentDir.exists()) {
                File subscriberEntitlement = new File(dbParentDir, "subscriberEntitlement.db");
                File item = new File(dbParentDir, "item.db");
                File receipt = new File(dbParentDir, "receipt.db");

                if (subscriberEntitlement.exists() && item.exists() && receipt.exists()) {
                    try {
                        _subscriberEntitlements = readFromFile(subscriberEntitlement);
                        _items = readFromFile(item);
                        _receipts = readFromFile(receipt);

                    } catch (ClassNotFoundException | IOException e) {
                        _logger.debug("using default "+DB_NAME+": exception wile loading mockdb file", e);
                        initNewDb();
                    }
                } else {
                    _logger.debug("using default "+DB_NAME+": missing one or more mockdb files");
                    initNewDb();
                }
            } else {
                _logger.debug("using default "+DB_NAME+": "+DB_NAME+" subdir doesn't exist");
                initNewDb();
            }
        } else {
            _logger.debug("using default "+DB_NAME+": sm.engine.statedir property not set");
            initNewDb();
        }
    }

    private void initNewDb()
    {
        _items = Arrays.asList(new Item[] {
            new Item(1, UUID.randomUUID().toString(), "tv.shout.shoutmillionaire", "$1", "$1", "1.00", true, null, null,
                    Arrays.asList(new ItemPrice[] {})),
            new Item(5, UUID.randomUUID().toString(), "tv.shout.shoutmillionaire", "$5", "$5", "5.00", true, null, null,
                    Arrays.asList(new ItemPrice[] {})),
            new Item(10, UUID.randomUUID().toString(), "tv.shout.shoutmillionaire", "$10", "$10", "10.00", true, null, null,
                    Arrays.asList(new ItemPrice[] {})),
            new Item(38, "e8056253-7c6c-11e7-970d-0242ac110004", "tv.shout.snowyowl", "$1", "$1", "1.00", true, null, null,
                    Arrays.asList(new ItemPrice[] {})),
            new Item(39, "0b7c7d65-7c6d-11e7-970d-0242ac110004", "tv.shout.snowyowl", "$5", "$5", "5.00", true, null, null,
                    Arrays.asList(new ItemPrice[] {})),
            new Item(40, "2252534e-7c6d-11e7-970d-0242ac110004", "tv.shout.snowyowl", "$10", "$10", "10.00", true, null, null,
                    Arrays.asList(new ItemPrice[] {})),
            new Item(41, "a05ba3bf-9887-11e7-bc47-0242ac110008", "tv.shout.snowyowl", "$20", "$20", "20.00", true, null, null,
                    Arrays.asList(new ItemPrice[] {})),
            new Item(42, "a05d16b9-9887-11e7-bc47-0242ac110008", "tv.shout.snowyowl", "$50", "$50", "50.00", true, null, null,
                    Arrays.asList(new ItemPrice[] {})),
            new Item(43, "a05d3bc1-9887-11e7-bc47-0242ac110008", "tv.shout.snowyowl", "$100", "$100", "100.00", true, null, null,
                    Arrays.asList(new ItemPrice[] {})),
        });

        _subscriberEntitlements = new ArrayList<>();
        _receipts = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> readFromFile(File file)
    throws IOException, ClassNotFoundException
    {
        FileInputStream fis = new FileInputStream(file);
        try (ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (List<T>) ois.readObject();
        }
    }

    private void persistDb()
    {
        //do we know where to store the files
        if (_stateDir == null) {
            _logger.debug("unable to persist "+DB_NAME+": sm.engine.statedir property not set");
            return;
        }

        //does the subdir exist (or if not, can we create it)
        File dbParentDir = new File(_stateDir, DB_NAME);
        if (!dbParentDir.exists()) {
            boolean parentCreated = dbParentDir.mkdirs();
            if (!parentCreated) {
                _logger.debug("unable to persist "+DB_NAME+": unable to create "+DB_NAME+" subdir");
                return;
            }
        }

        File subscriberEntitlement = new File(dbParentDir, "subscriberEntitlement.db");
        File item = new File(dbParentDir, "item.db");
        File receipt = new File(dbParentDir, "receipt.db");

        try {
            _subscriberEntitlementsLock.lock();
            _itemsLock.lock();
            _receiptsLock.lock();

            writeToFile(subscriberEntitlement, _subscriberEntitlements);
            writeToFile(item, _items);
            writeToFile(receipt, _receipts);

        } catch (IOException e) {
            _logger.debug("unable to persist storemockdb: unable to write one or more files", e);

        } finally {
            _subscriberEntitlementsLock.unlock();
            _itemsLock.unlock();
            _receiptsLock.unlock();
        }
    }

    private <T> void writeToFile(File file, List<T> list)
    throws IOException
    {
        FileOutputStream fos = new FileOutputStream(file);
        try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(list);
        }
    }

    @Override
    @ServiceMethod
    public List<Item> getAllActiveItems()
    {
        _itemsLock.lock();
        try {
            return _items.stream()
                    .filter(i -> i.isActive())
                    .collect(Collectors.toList());
        } finally {
            _itemsLock.unlock();
        }
    }

    @Override
    @ServiceMethod
    public Receipt purchaseItemViaNonce(long subscriberId, String customerId, String itemUuid, String nonce, String firstname, String lastname)
    throws InvalidItemException, StoreException, NoSuchCreditCardException
    {
        //find a matching item
        Optional<Item> oItem;
        _itemsLock.lock();
        try {
            oItem = _items.stream()
                    .filter(i -> i.isActive())
                    .filter(i -> i.getUuid().equals(itemUuid))
                    .findFirst();
        } finally {
            _itemsLock.unlock();
        }

        if (!oItem.isPresent()) {
            throw new InvalidItemException();
        }

        // Copy Hack - from DM GameHandler.java
        final String SM_ENTITLEMENT_UUID = "608bc230-4551-11e7-a82c-0242ac110004";
        //add entitlements
        int count = oItem.get().getItemId();
        _subscriberEntitlementsLock.lock();
        try {
            for (int i=0; i<count; i++) {
                _subscriberEntitlements.add(new SubscriberEntitlement(UUID.randomUUID().toString(), subscriberId, count, SM_ENTITLEMENT_UUID, -1));
            }
        } finally {
            _subscriberEntitlementsLock.unlock();
        }

        //create a receipt
        String receiptPayload;
        try {
            receiptPayload = Receipt.createInternalPayload(UUID.randomUUID().toString(), oItem.get().getDescription());
        } catch (InvalidJsonException e) {
            throw new StoreException(e.getMessage());
        }
        Receipt r = new Receipt();
        //r.setReceiptId((int)System.currentTimeMillis());
        r.setReceiptId(getAutoIncPrimaryKeyForReceipt());
        r.setUuid(UUID.randomUUID().toString());
        r.setType(Receipt.ReceiptType.INTERNAL);
        r.setItemUuid(oItem.get().getUuid());
        r.setStoreUid(oItem.get().getStoreBundleId());
        r.setSubscriberId(subscriberId);
        r.setPayload(receiptPayload.getBytes());
        r.setExpirationDate(null);
        r.setSkipVerify(true);
        r.setCreatedDate(new Date());
        r.setUpdatedDate(new Date());
        r.setSubscriptionState(null);

        //store in the "db"
        _receiptsLock.lock();
        try {
            _receipts.add(r);
        } finally {
            _receiptsLock.unlock();
        }

        persistDb();

        return r;
    }

    private int getAutoIncPrimaryKeyForReceipt()
    {
        return _receipts.size()+1;
    }

    @Override
    @ServiceMethod
    public void addSubscriberEntitlements(List<SubscriberEntitlement> entitlements)
    {
        _subscriberEntitlementsLock.lock();
        try {
            _subscriberEntitlements.addAll(entitlements);
        } finally {
            _subscriberEntitlementsLock.unlock();
        }

        persistDb();
    }

    @Override
    @ServiceMethod
    public List<SubscriberEntitlement> getCurrentEntitlementsForSubscriber(long subscriberId)
    {
        _subscriberEntitlementsLock.lock();
        try {
            return _subscriberEntitlements.stream()
                    .filter(se -> se.getSubscriberId() == subscriberId)
                    .filter(se -> se.getConsumedDate() == null)
                    .collect(Collectors.toList());
        } finally {
            _subscriberEntitlementsLock.unlock();
        }
    }

    @Override
    @ServiceMethod
    public void consumeSubscriberEntitlement(String subscriberEntitlementUuid, int contextId)
    throws InvalidItemException, AlreadyConsumedException
    {
        Optional<SubscriberEntitlement> oSubscriberEntitlement;
        _subscriberEntitlementsLock.lock();
        try {
            oSubscriberEntitlement = _subscriberEntitlements.stream()
                    .filter(se -> se.getUuid().equals(subscriberEntitlementUuid))
                    .findAny();
        } finally {
            _subscriberEntitlementsLock.unlock();
        }

        if (!oSubscriberEntitlement.isPresent()) {
            throw new InvalidItemException();
        } else {
            SubscriberEntitlement se = oSubscriberEntitlement.get();
            if (se.getConsumedDate() != null) {
                throw new AlreadyConsumedException();
            } else {
                se.setConsumedDate(new Date());
            }
        }

        persistDb();
    }

    @Override
    @ServiceMethod
    public List<SubscriberEntitlement> getSubscriberEntitlementConsumed(long subscriberIds, String entitlementUuid)
    {
        _subscriberEntitlementsLock.lock();
        try {
            return _subscriberEntitlements.stream()
                    .filter(se -> se.getSubscriberId() == subscriberIds)
                    .filter(se -> se.getEntitlementUuid().equals(entitlementUuid))
                    .filter(se -> se.getConsumedDate() != null)
                    .collect(Collectors.toList());
        } finally {
            _subscriberEntitlementsLock.unlock();
        }
    }

    @Override
    @ServiceMethod
    public void restoreSubscriberEntitlement(String subscriberEntitlementUuid)
    {
        Optional<SubscriberEntitlement> oSubscriberEntitlement;
        _subscriberEntitlementsLock.lock();
        try {
            oSubscriberEntitlement = _subscriberEntitlements.stream()
                    .filter(se -> se.getUuid().equals(subscriberEntitlementUuid))
                    .findAny();
        } finally {
            _subscriberEntitlementsLock.unlock();
        }

        if (oSubscriberEntitlement.isPresent()) {
            SubscriberEntitlement se = oSubscriberEntitlement.get();
            se.setConsumedDate(null);
        }

        persistDb();
    }

    @Override
    @ServiceMethod
    public ReceiptItem getReceiptItemForReceiptId(int receiptId)
    {
        Optional<Receipt> oReceipt;
        _receiptsLock.lock();
        try {
            oReceipt = _receipts.stream()
                    .filter(r -> r.getReceiptId() == receiptId)
                    .findFirst();
        } finally {
            _receiptsLock.unlock();
        }

        if (!oReceipt.isPresent()) {
            return null;
        }
        Receipt r = oReceipt.get();

        Optional<Item> oItem;
        _itemsLock.lock();
        try {
            oItem = _items.stream()
                    .filter(i -> i.getUuid().equals(r.getItemUuid()))
                    .findFirst();
        } finally {
            _itemsLock.unlock();
        }

        if (!oItem.isPresent()) {
            return null;
        }
        Item item = oItem.get();

        ReceiptItem ri = new ReceiptItem();
        ri.setReceiptId(receiptId);
        ri.setReceiptUuid(r.getUuid());
        ri.setItemUuid(r.getItemUuid());
        ri.setItemName(item.getTitle());
        ri.setItemPrice(Double.parseDouble(item.getPrice()));
        ri.setPurchaseType(r.getType().toString());
        ri.setPurchaseDate(r.getCreatedDate());
        ri.setPurchaseCurrency("USD");
        ri.setSubscriberId(r.getSubscriberId());

        return ri;
    }

    @Override
    @ServiceMethod
    public String getClientToken(long subscriberId)
    {
        return UUID.randomUUID().toString();
    }















    @Override
    @ServiceMethod
    public void start()
    {
    }

    @Override
    @ServiceMethod
    public void stop()
    {
    }

    @Override
    @ServiceMethod
    public Receipt purchaseItemViaCreditCard(long subscriberId, String customerId, String itemUuid,
            CreditCardInfo ccInfo) throws InvalidItemException, StoreException, NoSuchCreditCardException
    {
        return null;
    }

    @Override
    @ServiceMethod
    public CreditCardInfo getDefaultPaymentInfo(long subscriberId) throws StoreException, NoSuchCreditCardException
    {
        return null;
    }

    @Override
    @ServiceMethod
    public boolean auditReceipts(long subscriberId, List<Receipt> receipts) throws InvalidItemException
    {
        return false;
    }

    @Override
    @ServiceMethod
    public ReceiptResult addReceipt(Receipt receipt) throws InvalidItemException
    {
        return null;
    }

    @Override
    @ServiceMethod
    public void updateSubscriptionPayment(long subscriberId, String receiptUuid, CreditCardInfo ccInfo)
            throws StoreException, InvalidReceiptException
    {
    }

    @Override
    @ServiceMethod
    public void updateSubscriptionPaymentViaNonce(int subscriberId, String receiptUuid, String nonce)
            throws StoreException, InvalidReceiptException
    {
    }

    @Override
    @ServiceMethod
    public void cancelSubscription(long subscriberId, String receiptUuid) throws InvalidReceiptException, StoreException
    {
    }

    @Override
    @ServiceMethod
    public List<Entitlement> getEntitlements()
    {
        return null;
    }

    @Override
    @ServiceMethod
    public Entitlement getEntitlement(String entitlementUuid)
    {
        return null;
    }

    @Override
    @ServiceMethod
    public List<Entitlement> getEntitlementsForItem(int itemId)
    {
        return null;
    }

    @Override
    @ServiceMethod
    public List<Entitlement> getEntitlementsForItemRaw(int itemId)
    {
        return null;
    }

    @Override
    @ServiceMethod
    public List<Item> getItemsForEntitlements(String entitlementUuid)
    {
        return null;
    }

    @Override
    @ServiceMethod
    public List<SubscriberEntitlement> getUnreservedEntitlementsForSubscriber(long subscriberId)
    {
        return null;
    }

    @Override
    @ServiceMethod
    public List<SubscriberEntitlement> getUnreservedEntitlementsForSubscribers(List<Long> subscriberIds)
    {
        return null;
    }

    @Override
    @ServiceMethod
    public SubscriberEntitlement getSubscriberEntitlement(String subscriberEntitlementUuid)
    {
        return null;
    }

    @Override
    @ServiceMethod

    public void addSubscriberEntitlementsBatch(List<SubscriberEntitlement> entitlements)
    {
    }

    @Override
    @ServiceMethod
    public List<SubscriberEntitlement> getExpiredEntitlementsForSubscriber(long subscriberId)
    {
        return null;
    }

    @Override
    @ServiceMethod
    public void removeSubscriberEntitlement(int subscriberEntitlementId)
    {
    }

    @Override
    @ServiceMethod
    public void reserveSubscriberEntitlement(String subscriberEntitlementUuid, int contextId)
            throws InvalidItemException, AlreadyReservedException, AlreadyConsumedException
    {
    }

    @Override
    @ServiceMethod
    public void unreserveSubscriberEntitlement(String subscriberEntitlementUuid)
    {
    }

    @Override
    @ServiceMethod
    public Item getItemById(int itemId)
    {
        return null;
    }

    @Override
    @ServiceMethod
    public Item getItemByUuid(String itemUuid)
    {
        return null;
    }

    @Override
    @ServiceMethod
    public Item getItemByUuidRaw(String itemUuid)
    {
        return null;
    }

    @Override
    @ServiceMethod
    public List<Receipt> getReceiptsBySubscriberId(long subscriberId)
    {
        return null;
    }

    @Override
    @ServiceMethod
    public void removeSubscriberEntitlements(long subscriberId)
    {
    }

    @Override
    @ServiceMethod
    public Date expireSubscriberEntitlements(long subscriberId, boolean remove)
    {
        return null;
    }

    @Override
    @ServiceMethod
    public Date changeExpirationOfSubscriberEntitlements(long subscriberId, int changeExpirationHours)
    {
        return null;
    }

    @Override
    @ServiceMethod
    public CreditCardInfo getReceiptPaymentInfo(long subscriberId, String receiptUuid)
            throws InvalidReceiptException, StoreException
    {
        return null;
    }

    @Override
    @ServiceMethod
    public CreditCardInfo getSubscriptionPaymentInfo(long subscriberId, String receiptUuid)
            throws InvalidReceiptException, StoreException
    {
        return null;
    }

    @Override
    @ServiceMethod
    public List<ReceiptItem> getReceiptsBySubscriberIdFromDate(long subscriberId, Date fromDate)
    {
        return null;
    }

    @Override
    @ServiceMethod
    public String addPaymentMethodToCustomerProfile(long subscriberId, String nonce, boolean makeDefault, String firstname, String lastname) throws StoreException
    {
        return null;
    }

    @Override
    @ServiceMethod
    public String createCustomerProfile(long subscriberId, String nonce) throws StoreException
    {
        return null;
    }

    @Override
    @ServiceMethod
    public void deleteCustomerProfile(long subscriberId) throws StoreException
    {
    }

    @Override
    @ServiceMethod
    public CustomerProfile getCustomerProfile(long subscriberId) throws StoreException
    {
        return null;
    }

    @Override
    @ServiceMethod
    public Receipt purchaseViaCustomerProfile(long subscriberId, String itemUuid,
            String customerProfileCreditCardInfoExternalRefId) throws InvalidItemException, StoreException
    {
        return null;
    }

    @Override
    @ServiceMethod
    public List<CreditCardInfo> getPaymentMethodsForCustomerProfile(long subscriberId) throws StoreException
    {
        return null;
    }

    @Override
    @ServiceMethod
    public void deletePaymentMethod(String paymentMethodToken) throws StoreException
    {
    }

    @Override
    @ServiceMethod
    public List<Item> getItemsForEntitlement(int entitlementId)
    {
        return null;
    }

}
