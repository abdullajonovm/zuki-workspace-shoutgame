package tv.shout.shoutcontestaward.eventprocessor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.meinc.gameplay.domain.App;
import com.meinc.mrsoa.distdata.core.DistributedMap;
import com.meinc.mrsoa.distdata.core.DistributedSQLPredicate;

/*
 * This class was wholesale copied from GamePlayService. Need a better solution to get this data...
 */
public class AppHelper {
    
    private DistributedMap<String, App> _appMap;
    
    public AppHelper() {
        
    }
    
    public void setAppMap(DistributedMap<String, App> appMap) {
        _appMap = appMap;
    }
    
    public List<App> getAllApps() {
        Collection<App> apps = _appMap.values();
        Iterator<App> appsIt = apps.iterator();
        if (appsIt.hasNext()) {
            Object appObj = appsIt.next();
            try {
                @SuppressWarnings("unused")
                App app = (App) appObj;
                return new ArrayList<App>(apps);
            } catch (ClassCastException e) {
                ArrayList<App> appsList = new ArrayList<App>(apps.size());
                appsList.add(fromAlternateClassLoaderApp((Serializable) appObj));
                while (appsIt.hasNext())
                    appsList.add(fromAlternateClassLoaderApp(appsIt.next()));
                return appsList;
            }
        }
        return new ArrayList<App>();
    }
    
    public App getAppByName(String name) {
        Object appObj = _appMap.get(name);
        App app = null;
        try {
            app = (App) appObj;
        } catch (ClassCastException e) {
            app = fromAlternateClassLoaderApp((Serializable) appObj);
        }
        return app;
    }
    
    private App fromAlternateClassLoaderApp(Serializable appObj) {
        // Auth servers have GamePlayService and WebCollectorService (both of which embed GameplayDomain) registering
        // Hazelcast class definitions for App. Whichever service registers first causes class cast exceptions in the
        // other. This is a workaround.
        // TODO: serialization is slow - solve this some other way. e.g. custom reflection based clone, or figure out
        // how to use one gameplaydomain for both services
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(appObj);
            oos.flush();

            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
            return (App) ois.readObject();
        } catch (IOException e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public App getAppById(int appId) {
        App result = null;
        Collection<App> queryResults = _appMap.values(new DistributedSQLPredicate(String.format("appId = %d", appId)));
        if (queryResults != null && queryResults.size() > 0) {
            Object[] results = queryResults.toArray();
            try {
                result = (App) results[0];
            } catch (ClassCastException e) {
                result = fromAlternateClassLoaderApp((Serializable) results[0]);
            }
        }
        return result;
    }

}
