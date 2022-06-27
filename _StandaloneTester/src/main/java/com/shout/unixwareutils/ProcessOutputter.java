package com.shout.unixwareutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//based on: https://crunchify.com/how-to-run-windowsmac-commands-in-java-and-return-the-text-result/
public class ProcessOutputter
extends Thread
{
    private InputStream _is;
    private List<String> _items;
    private Lock _lock = new ReentrantLock();

    public ProcessOutputter(InputStream is)
    {
        _is = is;
        _items = new ArrayList<>();
    }

    @Override
    public void run()
    {
//        long count = 0L;
        String s = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(_is));
        _lock.lock();
        try {
            while ((s = br.readLine()) != null) {
//                count++;

//System.out.println("\t" + s);
                    //String[] parts = s.split("\\.");
//System.out.println(">>> adding: " + parts[parts.length-1]);
                    //_filenamesFromTarExtraction.add(parts[parts.length-1]);

                _items.add(s);
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        } finally {
            _lock.unlock();
        }
    }

    public List<String> getOutput()
    {
        _lock.lock();
        try {
//System.out.println("returning set: " + _fileExtensionsFromTarExtraction);
            return _items;
        } finally {
            _lock.unlock();
        }
    }
}
