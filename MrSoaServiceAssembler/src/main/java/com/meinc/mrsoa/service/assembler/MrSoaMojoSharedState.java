package com.meinc.mrsoa.service.assembler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MrSoaMojoSharedState implements Serializable, AutoCloseable {
    private static final long serialVersionUID = 2L;
    
    // Used to store the authoritative instance of this class where it is reached by reflection
    @SuppressWarnings("unused")
    private static MrSoaMojoSharedState state;

    public String pluginFactoryClassName;
    public Map<String,String> pluginParms;
    public List<String> serviceFqNames = new ArrayList<>();
    public List<String> serviceStubFqNames = new ArrayList<>();
    public List<String> generatedServiceFiles = new ArrayList<>();
    public List<String> generatedClientFiles = new ArrayList<>();
    public File generatedSourcesFile;

    private MrSoaMojoSharedState() { }
    
    public static MrSoaMojoSharedState readState() {
        Class<?> mojoClass;
        try {
            mojoClass = Class.forName(MrSoaMojoSharedState.class.getName(), true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        Field stateField;
        try {
            stateField = mojoClass.getDeclaredField("state");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(e);
        }
        try {
            stateField.setAccessible(true);
        } catch (SecurityException e) {
            throw new IllegalStateException(e);
        }
        Object stateObject;
        try {
            stateObject = stateField.get(null);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        if (stateObject == null)
            return new MrSoaMojoSharedState();

        byte[] stateObjectBytes;
        try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
              ObjectOutputStream oos = new ObjectOutputStream(baos) )
        {
            oos.writeObject(stateObject);
            stateObjectBytes = baos.toByteArray();
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
        try ( ByteArrayInputStream bais = new ByteArrayInputStream(stateObjectBytes);
              ObjectInputStream ois = new ObjectInputStream(bais) )
        {
            stateObject = ois.readObject();
        }
        catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        return (MrSoaMojoSharedState) stateObject;
    }
    
    public static void saveState(MrSoaMojoSharedState state) {
        ClassLoader targetClassLoader = Thread.currentThread().getContextClassLoader();
        Object stateObject = null;

        if (state != null) {
            byte[] stateObjectBytes;
            try ( ByteArrayOutputStream baos = new ByteArrayOutputStream();
                  ObjectOutputStream oos = new ObjectOutputStream(baos) )
            {
                oos.writeObject(state);
                stateObjectBytes = baos.toByteArray();
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
            try ( ByteArrayInputStream bais = new ByteArrayInputStream(stateObjectBytes);
                  ObjectInputStream ois = new MojoStateObjectInputStream(bais, targetClassLoader); )
            {
                stateObject = ois.readObject();
            }
            catch (SecurityException | IOException | ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }

        Class<?> mojoClass;
        try {
            mojoClass = Class.forName(MrSoaMojoSharedState.class.getName(), true, targetClassLoader);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        Field stateField;
        try {
            stateField = mojoClass.getDeclaredField("state");
        } catch (NoSuchFieldException | SecurityException e) {
            throw new IllegalStateException(e);
        }

        try {
            stateField.setAccessible(true);
        } catch (SecurityException e) {
            throw new IllegalStateException(e);
        }
        try {
            stateField.set(null, stateObject);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void close() throws Exception {
        saveState(this);
    }

    /**
     * Deserializes objects using the provided classloader
     */
    private static class MojoStateObjectInputStream extends ObjectInputStream {
        private ClassLoader targetClassLoader;

        private MojoStateObjectInputStream(InputStream in, ClassLoader targetClassLoader) throws IOException {
            super(in);
            this.targetClassLoader = targetClassLoader;
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            String objectClassName = desc.getName();
            Class<?> objectClass;
            try {
                objectClass = Class.forName(objectClassName, true, targetClassLoader);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
            return objectClass;
        }
    }
}
