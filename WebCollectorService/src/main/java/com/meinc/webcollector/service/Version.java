package com.meinc.webcollector.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version {
    private static Map<String,Integer> _compareCache = new HashMap<String,Integer>();
    
    private List<Integer> _rangeToNumbers = new ArrayList<Integer>();
    
    public Version(String version) throws BadVersionException {
        setVersion(version);
    }
    
    public void setVersion(String version) throws BadVersionException {
        String versionsString = version.trim();
        _rangeToNumbers = versionStringToNumbers(versionsString);
        _compareCache = new HashMap<String,Integer>();
    }

    private List<Integer> versionStringToNumbers(String versionString) throws BadVersionException {
        String[] numberStrings = versionString.split("\\.");
        List<Integer> versionNumbers = new ArrayList<Integer>();
        for (String numberString : numberStrings) {
            Integer versionNumber;
            try {
                versionNumber = Integer.parseInt(numberString);
            } catch (NumberFormatException e) {
                throw new BadVersionException(versionString);
            }
            versionNumbers.add(versionNumber);
        }
        return versionNumbers;
    }
    
    private Pattern _clientVersionPattern = Pattern.compile("\\d+(?:\\.\\d+)*");
    
    public int compareToVersionString(String versionString) throws BadVersionException {
        Integer cachedResult = _compareCache.get(versionString);
        if (cachedResult != null)
            return cachedResult;
        
        String version;
        Matcher m = _clientVersionPattern.matcher(versionString);
        if (m.find()) {
            version = m.group();
        } else {
            throw new BadVersionException(versionString);
        }
        
        List<Integer> thisVersionNumbers = new ArrayList<Integer>(_rangeToNumbers);
        List<Integer> otherVersionNumbers = versionStringToNumbers(version);
        
        if (otherVersionNumbers.size() > thisVersionNumbers.size())
            for (int i = thisVersionNumbers.size(); i < otherVersionNumbers.size(); i++)
                thisVersionNumbers.add(0);
        else if (otherVersionNumbers.size() < thisVersionNumbers.size())
            for (int i = otherVersionNumbers.size(); i < thisVersionNumbers.size(); i++)
                otherVersionNumbers.add(0);
        
        int result = 0;
        for (int i = 0; i < thisVersionNumbers.size(); i++) {
            Integer thisNumber = thisVersionNumbers.get(i);
            Integer otherNumber = otherVersionNumbers.get(i);
            int numberComparison = thisNumber.compareTo(otherNumber);
            if (numberComparison != 0) {
                result = numberComparison;
                break;
            }
        }
        
        Map<String,Integer> newCompareCache = new HashMap<String,Integer>(_compareCache);
        newCompareCache.put(versionString, result);
        _compareCache = newCompareCache;
        
        return result;
    }
}
