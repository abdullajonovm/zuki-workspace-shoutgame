package com.meinc.commons.helper;

import java.util.Date;

public interface IConcurrentDateFormat {
    
    public Date fromString(String date);
    
    public String toString(Date date);

}
