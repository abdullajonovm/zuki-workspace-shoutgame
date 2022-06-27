package com.shout.unixwareutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

//based on: https://crunchify.com/how-to-run-windowsmac-commands-in-java-and-return-the-text-result/
public class ProcessOutputterForFileExtensions
extends Thread
{
    private InputStream _is;
    private PrintStream _ps;
    private boolean _isTaExtractionViewOnly;
    private Set<String> _filenamesFromTarExtraction;
    private Lock _lock = new ReentrantLock();

    public ProcessOutputterForFileExtensions(InputStream is, PrintStream ps, boolean isTarExtraction)
    {
        _is = is;
        _ps = ps;
        _isTaExtractionViewOnly = isTarExtraction;
        _filenamesFromTarExtraction = new HashSet<>();
    }

    @Override
    public void run()
    {
        long count = 0L;
        String s = null;
        BufferedReader br = new BufferedReader(new InputStreamReader(_is));
        _lock.lock();
        try {
            while ((s = br.readLine()) != null) {
                count++;

                if (_isTaExtractionViewOnly) {
                    //_ps.println("\t" + s);
                    //String[] parts = s.split("\\.");
//System.out.println(">>> adding: " + parts[parts.length-1]);
                    //_filenamesFromTarExtraction.add(parts[parts.length-1]);

                    _filenamesFromTarExtraction.add(s);

                } else {
                    _ps.print(".");
                    if (count % 200 == 0) {
                        _ps.println("");
                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        } finally {
            _lock.unlock();
        }
    }

    public Set<String> getFilenamesFromTarExtraction()
    {
        _lock.lock();
        try {
//System.out.println("returning set: " + _fileExtensionsFromTarExtraction);
            return _filenamesFromTarExtraction;
        } finally {
            _lock.unlock();
        }
    }
}
