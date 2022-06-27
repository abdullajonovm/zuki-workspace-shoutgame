package com.meinc.ergo.util;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.meinc.ergo.domain.Note;
import com.meinc.ergo.domain.Role;
import com.meinc.ergo.domain.Subscriber;
import com.meinc.ergo.domain.Task;
import com.meinc.ergo.exception.InvalidParamException;
import com.meinc.ergo.exception.MissingRequiredParamException;
import com.meinc.ergo.exception.NotAuthenticatedException;
import com.meinc.mrsoa.distdata.core.DistributedMap;

public class ServiceHelper
{
    private static Logger logger = Logger.getLogger(ServiceHelper.class);
    
    public static final String SESSION_TOKEN = "X-APP-SESSION-TOKEN";
    public static final String DEFAULT_TIMEZONE = "America/New_York";
    
    public static final String PARAM_PROVIDER_UUID = "providerId";
    public static final String PARAM_FAIL_FLAG = "failFlag";
    public static final String PARAM_BATCH_ID = "batchId";
    
    public static final String KEY_SUCCESS = "success";
    public static final String KEY_MISSING_REQUIRED_PARAM = "missingRequiredParam";
    public static final String KEY_INVALID_PARAM = "invalidParam";
    public static final String KEY_NOT_AUTHENTICATED = "notAuthenticated";
    public static final String KEY_NO_DATA_TRY_BACK = "noDataTryBack";
    public static final String KEY_STALE_DATA_TRY_BACK = "staleDataTryBack";
    public static final String KEY_OBJECT_OUT_OF_DATE = "objectOutOfDate";
    public static final String KEY_INVALID_CREDENTIALS = "invalidCredentials";
    public static final String KEY_INVALID_PROVIDER_CREDENTIALS = "invalidProviderCredentials";
    public static final String KEY_UNSUPPORTED_VERSION = "unsupportedVersion";
    public static final String KEY_PROVIDER_UNAVAILABLE = "providerUnavailable";
    public static final String KEY_CATEGORIES_UNAVAILABLE = "categoriesUnavailable";
    public static final String KEY_UNSUPPORTED_EXCHANGE_SERVER = "unsupportedExchangeServer";
    public static final String KEY_CONNECTION_ERROR = "connectionError";
    public static final String KEY_UNKNOWN_ERROR = "unknownError";
    public static final String KEY_SERVER_THROTTLED = "serverThrottled";
    public static final String KEY_EMAIL_ERROR = "emailError";
    public static final String KEY_DEVICE_NOT_FOUND = "deviceNotFound";
    public static final String KEY_INVALID_RECEIPT = "invalidReceipt";
    public static final String KEY_STORE_RETRY_ERROR = "retryError";
    public static final String KEY_STORE_DUPLICATE_ITEM = "duplicateItem";
    public static final String KEY_STORE_ERROR = "storeError";
    public static final String KEY_INVALID_PROVIDER = "invalidProvider";
    public static final String KEY_UNKNOWN_HOST = "unknownHost";
    public static final String ERROR_AUTODISCOVER_UNAVAILABLE = "autodiscoverUnavailable";
    public static final String KEY_TRANSACTION_ID = "transactionId";
    public static final String KEY_SUBSCRIBER = "subscriber";
    public static final String KEY_TRANSACTION_NOT_FOUND = "transactionNotFound";
    
    public static final int DEFAULT_INT_VAL = -1;
    public static final int DEFAULT_LONG_VAL = -1;
    public static final String DEFAULT_STRING_VAL = "";
    
    //encapsulates all the types of errors that can come back synchronously from a server call
    public static enum ERROR_TYPE {
        NOT_AUTHENTICATED, SUBSCRIBER_NOT_FOUND, INVALID_CREDENTIALS, UNKNOWN_HOST, NOT_AUTHORIZED, AUTODISCOVER_UNAVAILABLE,
        MISSING_REQUIRED_PARAM, INVALID_PARAM, 
        
        NO_DATA_TRY_BACK, STALE_DATA_TRY_BACK,
        
        NO_DATA_TRY_BACK_SYNC_ALL_ROLES, NO_DATA_TRY_BACK_SYNC_ALL_NOTES, NO_DATA_TRY_BACK_SYNC_ALL_TASKS,
        NO_DATA_TRY_BACK_SYNC_ALL_ROLES_NOTES, NO_DATA_TRY_BACK_SYNC_ALL_ROLES_TASKS, NO_DATA_TRY_BACK_SYNC_ALL_NOTES_TASKS, 
        NO_DATA_TRY_BACK_SYNC_ALL_ROLES_NOTES_TASKS,
        NO_DATA_TRY_BACK_SYNC_ROLES, NO_DATA_TRY_BACK_SYNC_NOTES, NO_DATA_TRY_BACK_SYNC_TASKS,

        STALE_DATA_TRY_BACK_SYNC_ALL_ROLES, STALE_DATA_TRY_BACK_SYNC_ALL_NOTES, STALE_DATA_TRY_BACK_SYNC_ALL_TASKS,
        STALE_DATA_TRY_BACK_SYNC_ALL_ROLES_NOTES, STALE_DATA_TRY_BACK_SYNC_ALL_ROLES_TASKS, STALE_DATA_TRY_BACK_SYNC_ALL_NOTES_TASKS, 
        STALE_DATA_TRY_BACK_SYNC_ALL_ROLES_NOTES_TASKS,
        STALE_DATA_TRY_BACK_SYNC_ROLES, STALE_DATA_TRY_BACK_SYNC_NOTES, STALE_DATA_TRY_BACK_SYNC_TASKS,
        
        UNSUPPORTED_VERSION, CONNECTION_ERROR, SERVER_THROTTLED,
        ROLE_NOT_FOUND, NOTE_NOT_FOUND, TASK_NOT_FOUND, 
        CANNOT_REMOVE_DEFAULT_PROVIDER, INVALID_PROVIDER, DEVICE_NOT_FOUND,
        PASSWORD_TOO_WEAK, CODE_EXPIRED, USERNAME_TAKEN, INVALID_EMAIL, FACEBOOK_ACCESS, POST_OFFICE,
        
        EXCEL_CREATION
    };
    
    /* key == sessionKey */
    private static DistributedMap<Integer, Subscriber> subscriberBySubscriberIdMap = DistributedMap.getMap("subscriberBySubscriberIdMap");
    
    /* sessionKey->subscriberId */
    private static DistributedMap<String, Integer> subscriberIdBySessionKeyMap = DistributedMap.getMap("subscriberIdBySessionKeyMap");
    
    /* subscriberUuid->providerUuid->roleId->Role */
    public static DistributedMap<String, Map<String, Map<String, TimedObject<Role>>>> subscriberRolesMap = DistributedMap.getMap("roles");
    
    /* subscriberUuid->providerUuid->noteId->Note */
    public static DistributedMap<String, Map<String, Map<String, TimedObject<Note>>>> subscriberNotesMap = DistributedMap.getMap("notes");

    /* subscriberUuid->providerUuid->taskId->Task */
    public static DistributedMap<String, Map<String, Map<String, TimedObject<Task>>>> subscriberTasksMap = DistributedMap.getMap("tasks");
    
    /** to do row-level locking to prevent a user from doing multiple async operations at once on roles/notes/tasks */
    public static DistributedMap<Integer, Boolean> subscriberWorkMap = DistributedMap.getMap("subscriber_work");
    
    /**
     * Inserts the provided task directly into the cache. Consider cloning the task before invoking this method.
     * @param subscriberUuid the owner of the task
     * @param providerUuid the provider of the task
     * @param task the task
     */
    public static void updateTaskCache(String subscriberUuid, Task task) {
        Map<String, Map<String, TimedObject<Task>>> taskFromProviderMap = subscriberTasksMap.get(subscriberUuid);
        if (taskFromProviderMap == null)
            taskFromProviderMap = new HashMap<String, Map<String, TimedObject<Task>>>();
        Map<String, TimedObject<Task>> subscriberTasks = taskFromProviderMap.get(task.getProviderUuid());
        if (subscriberTasks == null) {
            subscriberTasks = new HashMap<String, TimedObject<Task>>();
        }
        subscriberTasks.put(task.getUuid(), new TimedObject<Task>(task));
        taskFromProviderMap.put(task.getProviderUuid(), subscriberTasks);
        subscriberTasksMap.put(subscriberUuid, taskFromProviderMap);
    }
    
    /**
     * Inserts the provided note directly into the cache. Consider cloning the note before invoking this method.
     * @param subscriberUuid the owner of the note
     * @param providerUuid the provider of the note
     * @param note the note
     */
    public static void updateNoteCache(String subscriberUuid, Note note) {
        Map<String,Map<String,TimedObject<Note>>> noteFromProviderMap = subscriberNotesMap.get(subscriberUuid);
        if (noteFromProviderMap == null)
            noteFromProviderMap = new HashMap<String, Map<String, TimedObject<Note>>>();
        Map<String,TimedObject<Note>> subscriberNotes = noteFromProviderMap.get(note.getProviderUuid());
        if (subscriberNotes == null) {
            subscriberNotes = new HashMap<String, TimedObject<Note>>();
        }
        subscriberNotes.put(note.getUuid(), new TimedObject<Note>(note));
        noteFromProviderMap.put(note.getProviderUuid(), subscriberNotes);
        subscriberNotesMap.put(subscriberUuid, noteFromProviderMap);
    }
    
    public static boolean getBoolParam(HttpServletRequest request, String paramName, boolean required, boolean defaultVal)
    throws MissingRequiredParamException, InvalidParamException {
        return getBoolParam(request.getParameterMap(), paramName, required, defaultVal);
    }
    
    public static boolean getBoolParam(Map<String,String[]> params, String paramName, boolean required, boolean defaultVal)
    throws MissingRequiredParamException, InvalidParamException
    {
        String[] vals = params.get(paramName);
        String val = (vals == null || vals.length == 0) ? null : vals[0];
        if (val == null && required) {
            throw new MissingRequiredParamException(paramName);
            
        } else if (val == null) {
            return defaultVal;
            
        } else {
            try {
                return Boolean.parseBoolean(val);
            } catch (Exception e) {
                throw new InvalidParamException(paramName);
            }
        }
    }
    
    public static ERROR_TYPE getErrorTypeParam(HttpServletRequest request)
    {
        String  val = request.getParameter("ERROR_TYPE");
        if (val == null) 
            return null;
        else {
            try {
                ERROR_TYPE result = ERROR_TYPE.valueOf(val);
                return result;
            } catch (Exception e) {
                //ignore
                logger.info("tried to pass unknown/unparseable ERROR_TYPE: " + val);
            }
        }
        return null;
    }
    
    public static int getIntParam(HttpServletRequest request, String paramName, boolean required)
    throws MissingRequiredParamException, InvalidParamException
    {
        String val = request.getParameter(paramName);
        if (val == null && required) {
            throw new MissingRequiredParamException(paramName);
                    
        } else if (val == null) {
            return DEFAULT_INT_VAL;
            
        } else {
            try {
                return Integer.parseInt(val);
            } catch (NumberFormatException e) {
                throw new InvalidParamException(paramName);
            }
        }
    }

    public static long getLongParam(HttpServletRequest request, String paramName, boolean required)
    throws MissingRequiredParamException, InvalidParamException
    {
        String val = request.getParameter(paramName);
        if (val == null && required) {
            throw new MissingRequiredParamException(paramName);
                    
        } else if (val == null) {
            return DEFAULT_LONG_VAL;
            
        } else {
            try {
                return Long.parseLong(val);
            } catch (NumberFormatException e) {
                throw new InvalidParamException(paramName);
            }
        }
    }

    public static String getStringParam(Map<String,String[]> params, String paramName, boolean required)
    throws MissingRequiredParamException, InvalidParamException
    {
        String[] vals = params.get(paramName);
        String val = (vals == null || vals.length == 0) ? null : vals[0];
        if (val == null && required) {
            throw new MissingRequiredParamException(paramName);
                    
        } else if (val == null) {
            return DEFAULT_STRING_VAL;
            
        } else {
            return val;
        }
    }

    public static String getStringParam(HttpServletRequest request, String paramName, boolean required)
    throws MissingRequiredParamException, InvalidParamException
    {
        return getStringParam(request.getParameterMap(), paramName, required);
    }

    public static <T extends Object> T getJsonParam(HttpServletRequest request, String paramName, Class<T> classType, boolean required)
    throws MissingRequiredParamException, InvalidParamException
    {
        String val = request.getParameter(paramName);
        if (val == null && required) {
            throw new MissingRequiredParamException(paramName);
            
        } else if (val == null) {
            return null;
            
        } else {
            ObjectMapper mapper = new ObjectMapper();
            //mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            T obj;
            try {
                obj = mapper.readValue(val, classType);
            } catch (JsonParseException e) {
                throw new InvalidParamException(paramName);
            } catch (JsonMappingException e) {
                throw new InvalidParamException(paramName);
            } catch (IOException e) {
                throw new InvalidParamException(paramName);
            }
            return obj;
        }
    }

    public static Subscriber getSubscriberFromCache(HttpServletRequest request) 
    throws NotAuthenticatedException
    {
        String sessionToken = request.getHeader(SESSION_TOKEN);
        if (sessionToken == null)
            throw new NotAuthenticatedException();
        Integer subscriberId = subscriberIdBySessionKeyMap.get(sessionToken);
        if (subscriberId == null)
            throw new NotAuthenticatedException();
        Subscriber s = subscriberBySubscriberIdMap.get(subscriberId);
        if (s == null)
            throw new NotAuthenticatedException();
        return s;
    }
    
    public static Subscriber getSubscriberFromCache(String sessionKey) {
        Subscriber result = null;
        Integer subscriberId = ServiceHelper.subscriberIdBySessionKeyMap.get(sessionKey);
        if (subscriberId != null)
            result = ServiceHelper.subscriberBySubscriberIdMap.get(subscriberId);
        return result;
    }
    
    public static void putSubscriberInCache(String sessionKey, Subscriber subscriber) {
        ServiceHelper.subscriberBySubscriberIdMap.put(subscriber.getId(), subscriber);
        ServiceHelper.subscriberIdBySessionKeyMap.put(sessionKey, subscriber.getId());
    }
    
    public static void putSubscriberInCache(Subscriber subscriber) {
        ServiceHelper.subscriberBySubscriberIdMap.put(subscriber.getId(), subscriber);
    }
    
    public static void removeSubscriberFromCache(String sessionKey) {
        Integer subscriberId = ServiceHelper.subscriberIdBySessionKeyMap.remove(sessionKey);
        if (subscriberId != null)
            ServiceHelper.subscriberBySubscriberIdMap.remove(subscriberId);
    }
    
    public static void removeSubscriberFromCache(int subscriberId)
    {
        ServiceHelper.subscriberBySubscriberIdMap.remove(subscriberId);
    }
    
    /**
     * @return a GUID
     */
    public static String getUniqueId()
    {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Determine how far back to do a sync if no explicit value is given.
     * 
     * @param state the current level of a provider
     * @return
     */
    public static Date getDefaultLastSyncTime(Subscriber.STATE state/*IStoreService storeService, int subscriberId*/)
    {
        //TODO: rather than rely on the subscriber state object (which may not be correct), check entitlements from the store service
        //List<SubscriberEntitlement> entitlements = storeService.getCurrentEntitlementsForSubscriber(subscriberId);
        
        Calendar c;
        switch (state)
        {
            case TRIAL:
                c = Calendar.getInstance();
                c.add(Calendar.MONTH, -3);
                return c.getTime();
                
            case PRO:
                c = Calendar.getInstance();
                c.add(Calendar.YEAR, -1);
                return c.getTime();
                
            case PREMIUM:
                return new Date(0); //go back 'forever'
                
            case FREE:
            default:
                //no sync; defaults to 'now'
                return new Date();
        }
    }
    
    /**
     * Given an Ergo task and a variant task, update the
     * {@link Task#setReminded(boolean) reminded} field of the Ergo task. If the
     * variant's due date or reminder minutes are different, the Ergo task's
     * reminded field is reset.
     * 
     * @param ergoTask
     *            the Ergo task to update
     * @param variantTask
     *            a task that is a variant of the Ergo task. For example a
     *            variant in time, or a variant located on a remote server.
     */
    public static void syncErgoTaskRemindedFlag(Task ergoTask, Task variantTask) {
        boolean timeChanged = 
                //ergoTask.getDueDate() == null ^ variantTask.getDueDate() == null ||
                //ergoTask.getDueDate() != null && ergoTask.getDueDate().compareTo(variantTask.getDueDate()) != 0 ||
                ergoTask.getReminder() == null ^ variantTask.getReminder() == null ||
                ergoTask.getReminder() != null && ergoTask.getReminder().compareTo(variantTask.getReminder()) != 0; //||
                //ergoTask.getReminderMinBefore() != variantTask.getReminderMinBefore();
        ergoTask.setReminded(ergoTask.isReminded() && !timeChanged);
    }

//    /**
//     * Calling this method will block operations on a per-subscriber basis so that multiple operations can't happen at the same time and clobber
//     * subscriber data.  For example, if a subscriber were to update a task, then immediately do a sync all (with forced refresh), or maybe do 
//     * an import that runs for a while, the data structures underlying things can get swept out from under the user and unpredictable results
//     * occur.  This will prevent that by only allowing a subscriber to do one operation at a time.
//     */
//    public static void queueSubscriberOperation(HttpServletRequest request)
//    {
//        //NOTE: this method LOCKS the row.  The dequeueSubscriberOperation method ___MMMMUUUUSSSTTT___ unlock the row
//        
//        try {
//            Subscriber s = getSubscriber(request);
//            int sId = s.getId();
//            
////logger.info("*** queuing operation for: " + sId);
//            subscriberWorkMap.lock(sId); //add a lock (reentrant, so everyone can queue up at this point)
//            subscriberWorkMap.remove(sId); //blocks until all pre-existing locks (if any) are removed; once removed, another item from the queue can now acquire the lock and attempt to remove
////logger.info("*** processing operation for: " + sId);
//            
//        } catch (NotAuthenticatedException e) {
//            //shouldn't happen, but if it does, oh well - it'll fail elsewhere for the same reason
//        }
//    }
//
//    public static void dequeueSubscriberOperation(HttpServletRequest request)
//    {
//        try {
//            Subscriber s = getSubscriber(request);
//            int sId = s.getId();
//            dequeueSubscriberOperation(sId);
//            
//        } catch (NotAuthenticatedException e) {
//            //shouldn't happen
//        }
//    }
//    
//    public static void dequeueSubscriberOperation(int sId)
//    {
//        ServiceHelper.subscriberWorkMap.unlock(sId);
//    }
    
}
