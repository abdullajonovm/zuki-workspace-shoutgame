package tv.shout.snowyowl.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface FileHandler
{
    public default void writeToFile(File file, Object item)
    throws IOException
    {

        if (!file.getParentFile().exists()) {
            boolean created = file.getParentFile().mkdirs();
            if (!created) {
                throw new IOException("unable to create file: " + file.getAbsolutePath());
            }
        }
        FileOutputStream fos = new FileOutputStream(file);
        try (ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(item);
        }
    }

    public default Object readFromFile(File file)
    throws IOException, ClassNotFoundException
    {
        if (!file.exists()) {
            throw new IOException("attempting to read from non-existant file: " + file.getAbsolutePath());
        }
        FileInputStream fis = new FileInputStream(file);
        try (ObjectInputStream ois = new ObjectInputStream(fis)) {
            return ois.readObject();
        }
    }
}
