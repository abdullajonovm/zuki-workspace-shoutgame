package com.meinc.gameplay.powerup;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.meinc.store.domain.SubscriberEntitlement;

public class Powerup
implements Serializable
{
    private static final long serialVersionUID = 1L;

    public static final int classId = 4000;

    public static enum POWERUP_TYPE {
        CLOCK_FREEZE("ClockFreeze"),
        FAN_CHECK   ("FanCheck"),
        VOTE2       ("Vote2"),
        MULLIGAN    ("Mulligan"),
        THROWDOWN   ("Throwdown"),
        SAFETY_NET  ("SafetyNet");

        private String label;

        private POWERUP_TYPE(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }
    public static enum POWERUP_USAGE_STATUS {RESERVED, CONSUMED}

    static Map<String, POWERUP_TYPE> KNOWN_POWERUP_TYPES = new HashMap<String, POWERUP_TYPE>();
    static Map<POWERUP_TYPE, String> KNOWN_POWERUP_TYPES_REVERSE = new HashMap<POWERUP_TYPE, String>();
    static Map<POWERUP_TYPE, String> POWERUP_TO_ITEM = new HashMap<POWERUP_TYPE, String>();
    public static Map<Integer, POWERUP_TYPE> ENTITLEMENTID_TO_POWERUP_TYPE = new HashMap<Integer, Powerup.POWERUP_TYPE>();
    static
    {
        //entitlement_uuid -> powerup_type
        KNOWN_POWERUP_TYPES.put("05efd2a6-ed6a-11e2-b353-12313932f90c", POWERUP_TYPE.CLOCK_FREEZE);
        KNOWN_POWERUP_TYPES.put("0f401924-ed6a-11e2-b353-12313932f90c", POWERUP_TYPE.FAN_CHECK);
        KNOWN_POWERUP_TYPES.put("1dd6afac-ed6a-11e2-b353-12313932f90c", POWERUP_TYPE.VOTE2);
        KNOWN_POWERUP_TYPES.put("23ace45a-ed6a-11e2-b353-12313932f90c", POWERUP_TYPE.MULLIGAN);
        KNOWN_POWERUP_TYPES.put("28a74a04-ed6a-11e2-b353-12313932f90c", POWERUP_TYPE.THROWDOWN);
        KNOWN_POWERUP_TYPES.put("333cae50-ed6a-11e2-b353-12313932f90c", POWERUP_TYPE.SAFETY_NET);

        //powerup_type -> entitlement_uuid
        KNOWN_POWERUP_TYPES_REVERSE.put(POWERUP_TYPE.CLOCK_FREEZE,    "05efd2a6-ed6a-11e2-b353-12313932f90c");
        KNOWN_POWERUP_TYPES_REVERSE.put(POWERUP_TYPE.FAN_CHECK,       "0f401924-ed6a-11e2-b353-12313932f90c");
        KNOWN_POWERUP_TYPES_REVERSE.put(POWERUP_TYPE.VOTE2,           "1dd6afac-ed6a-11e2-b353-12313932f90c");
        KNOWN_POWERUP_TYPES_REVERSE.put(POWERUP_TYPE.MULLIGAN,        "23ace45a-ed6a-11e2-b353-12313932f90c");
        KNOWN_POWERUP_TYPES_REVERSE.put(POWERUP_TYPE.THROWDOWN,       "28a74a04-ed6a-11e2-b353-12313932f90c");
        KNOWN_POWERUP_TYPES_REVERSE.put(POWERUP_TYPE.SAFETY_NET,      "333cae50-ed6a-11e2-b353-12313932f90c");

        //powerup_type -> item_uuid
        POWERUP_TO_ITEM.put(POWERUP_TYPE.CLOCK_FREEZE,    "2c6e68c6-eda0-11e2-b353-12313932f90c");
        POWERUP_TO_ITEM.put(POWERUP_TYPE.FAN_CHECK,       "8da0e79a-eda0-11e2-b353-12313932f90c");
        POWERUP_TO_ITEM.put(POWERUP_TYPE.VOTE2,           "af71a3a0-eda0-11e2-b353-12313932f90c");
        POWERUP_TO_ITEM.put(POWERUP_TYPE.MULLIGAN,        "bea72656-eda0-11e2-b353-12313932f90c");
        POWERUP_TO_ITEM.put(POWERUP_TYPE.THROWDOWN,       "cca5ec1a-eda0-11e2-b353-12313932f90c");
        POWERUP_TO_ITEM.put(POWERUP_TYPE.SAFETY_NET,      "daf34ea2-eda0-11e2-b353-12313932f90c");

        //entitlement_id -> powerup_type
        ENTITLEMENTID_TO_POWERUP_TYPE.put(1, POWERUP_TYPE.CLOCK_FREEZE);
        ENTITLEMENTID_TO_POWERUP_TYPE.put(2, POWERUP_TYPE.FAN_CHECK);
        ENTITLEMENTID_TO_POWERUP_TYPE.put(3, POWERUP_TYPE.VOTE2);
        ENTITLEMENTID_TO_POWERUP_TYPE.put(4, POWERUP_TYPE.MULLIGAN);
        ENTITLEMENTID_TO_POWERUP_TYPE.put(5, POWERUP_TYPE.THROWDOWN);
        ENTITLEMENTID_TO_POWERUP_TYPE.put(6, POWERUP_TYPE.SAFETY_NET);
    }

    public static final String PASS_1_DAY_UUID =               "07dee6f4-fa0a-11e2-b2ff-12313932f90c";
    public static final String PASS_1_WEEK_UUID =              "b71d6f52-fa12-11e2-b2ff-12313932f90c";
    public static final String PASS_1_MONTH_UUID =             "ef6baaa4-fa12-11e2-b2ff-12313932f90c";
    public static final String CREDS_SMALL_BUNDLE_UUID =       "df4e71e6-fabd-11e2-b2ff-12313932f90c";
    public static final String CREDS_MEDIUM_BUNDLE_UUID =      "397e0b2c-fabe-11e2-b2ff-12313932f90c";
    public static final String CREDS_LARGE_BUNDLE_UUID =       "5ca6ba40-fabe-11e2-b2ff-12313932f90c";
    public static final String PASS_EVENT_UUID =               "2ce06722-ec01-11e3-90d9-22000a66836a";

    private POWERUP_TYPE _type;
    private String id; //uuid
    private long subscriberId; // for powerups that are really wrappers for SubscriberEntitlements
    private int typeId;
    private boolean reserved;
    private boolean consumed;
    private boolean comboEnabled;
    private int eventLimit;

    public POWERUP_TYPE getType()
    {
        return _type;
    }
    public void setType(POWERUP_TYPE type)
    {
        _type = type;
    }
    public String getId()
    {
        return id;
    }
    public void setId(String id)
    {
        this.id = id;
    }

    public long getSubscriberId() {
        return subscriberId;
    }

    public void setSubscriberId(long subscriberId) {
        this.subscriberId = subscriberId;
    }

    public int getTypeId()
    {
        return typeId;
    }

    public void setTypeId(int typeId)
    {
        this.typeId = typeId;
    }

    public boolean isReserved() {
        return reserved;
    }

    public void setReserved(boolean reserved) {
        this.reserved = reserved;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void setConsumed(boolean consumed) {
        this.consumed = consumed;
    }

    public boolean isComboEnabled()
    {
        return comboEnabled;
    }

    public void setComboEnabled(boolean comboEnabled)
    {
        this.comboEnabled = comboEnabled;
    }

    public int getEventLimit()
    {
        return eventLimit;
    }

    public void setEventLimit(int eventLimit)
    {
        this.eventLimit = eventLimit;
    }

    public static Powerup convertSubscriberEntitlementToPowerup(SubscriberEntitlement se)
    {
        POWERUP_TYPE type = KNOWN_POWERUP_TYPES.get(se.getEntitlementUuid());
        if (type == null)
            return null;
        else {
            Powerup p = new Powerup();
            p.setType(type);
            p.setId(se.getUuid());
            p.setSubscriberId(se.getSubscriberId());
            p.setReserved(se.getReservedDate() != null);
            p.setConsumed(se.getConsumedDate() != null);
            return p;
        }
    }

    public static Powerup convertSubscriberEntitlementToPowerup(String subscriberEntitlementId, POWERUP_TYPE powerupType)
    {
        Powerup p = new Powerup();
        p.setType(powerupType);
        p.setId(subscriberEntitlementId);
        return p;
    }

    public static String getEntitlementUuidFromPowerupType(POWERUP_TYPE type)
    {
        return KNOWN_POWERUP_TYPES_REVERSE.get(type);
    }

    public static String getItemUuidFromPowerupType(POWERUP_TYPE type)
    {
        return POWERUP_TO_ITEM.get(type);
    }

    @Override
    public String toString() {
        return String.format("powerup type=%s typeId=%d itemId=%s comboEnabled=%b eventLimit=%d",
                             _type.name(), typeId, id, comboEnabled, eventLimit);
    }
}
