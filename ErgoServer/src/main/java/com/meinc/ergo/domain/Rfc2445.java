package com.meinc.ergo.domain;

import java.io.Serializable;
import java.util.Date;

//https://lists.oasis-open.org/archives/obix-xml/200708/msg00001.html
public class Rfc2445
implements Serializable
{
    private static final long serialVersionUID = 3407972554677585938L;
    
    private String iCalString;
    private Date startDate;
    private boolean regenerative;
    
    public Rfc2445()
    {
    }
    
    public Rfc2445(String iCalString, Date startDate, boolean regenerative)
    {
        this.iCalString = iCalString;
        this.startDate = startDate;
        this.regenerative = regenerative;
    }
    
    public String getiCalString()
    {
        return iCalString;
    }
    public void setiCalString(String iCalString)
    {
        this.iCalString = iCalString;
    }
    public Date getStartDate()
    {
        return startDate;
    }
    public void setStartDate(Date startDate)
    {
        this.startDate = startDate;
    }
    public boolean isRegenerative()
    {
        return regenerative;
    }
    public void setRegenerative(boolean regenerative)
    {
        this.regenerative = regenerative;
    }
    
    
    //take an existing RFC2445 object and clone it, but replace the startDate with the new date, and also decrement the count (if applicable)
    public static Rfc2445 getNew(Rfc2445 oldRfc2445, Date newStartDate, boolean decrementCount)
    {
        Rfc2445 newRfc2445 = new Rfc2445();
        newRfc2445.setStartDate(newStartDate);
        newRfc2445.setRegenerative(oldRfc2445.isRegenerative());
        
        String s;
        if (decrementCount) {
            int idx;
            if (oldRfc2445.getiCalString() != null)
                idx = oldRfc2445.getiCalString().indexOf("COUNT");
            else
                idx = -1;
            if (idx != -1) {
                int bIdx = oldRfc2445.getiCalString().indexOf("=", idx+1)+1;
                int eIdx = oldRfc2445.getiCalString().indexOf(";", bIdx);
                if (eIdx == -1) {
                    eIdx = oldRfc2445.getiCalString().length();
                }
                
                int count = Integer.parseInt(oldRfc2445.getiCalString().substring(bIdx, eIdx));
                count--;
                if (count <= 0) {
                    return null; //no more entries 
                }
                
                if (idx > 0)
                    s = oldRfc2445.getiCalString().substring(0, idx);
                else
                    s = "";
                s += "COUNT=" + count;
                if (eIdx < oldRfc2445.getiCalString().length()-1)
                    s += ";" + oldRfc2445.getiCalString().substring(eIdx+1);
            } else
                s = oldRfc2445.getiCalString();
        } else
            s = oldRfc2445.getiCalString();
        
        newRfc2445.setiCalString(s);
        
        return newRfc2445;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (! (obj instanceof Rfc2445)) return false;
        Rfc2445 o2 = (Rfc2445) obj;

        if (getStartDate() != null && o2.getStartDate() == null)
            return false;
        else if (getStartDate() == null && o2.getStartDate() != null)
            return false;
        else if (getStartDate().getTime() != o2.getStartDate().getTime())
            return false;
        
        if (isRegenerative() != o2.isRegenerative())
            return false;

        if (getiCalString() == null && o2.getiCalString() == null)
            return false;
            else if (getiCalString() != null && o2.getiCalString() == null)
            return false;
        else if (getiCalString() == null && o2.getiCalString() != null)
            return false;
        else if (!getiCalString().equals(o2.getiCalString()))
            return false;
        
        return true;
    }
    
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();

        buf.append("startDate: ").append(startDate).append(" (").append(startDate.getTime()).append(")");
        buf.append(", regenerative: ").append(regenerative);
        buf.append(", iCal: ").append(iCalString);

        return buf.toString();
    }
    
//    public static void main(String[] args)
//    {
//        Rfc2445[] rfcs = {
//            new Rfc2445("RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=MO", new Date(), false), //no count
//            new Rfc2445("COUNT=10;RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=MO", new Date(), false), //count at start
//            new Rfc2445("RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=MO;COUNT=10", new Date(), false), //count at end
//            new Rfc2445("RRULE:FREQ=WEEKLY;INTERVAL=1;COUNT=10;BYDAY=MO", new Date(), false), //count in middle
//            new Rfc2445("RRULE:FREQ=WEEKLY;INTERVAL=1;COUNT=8;BYDAY=MO", new Date(), false), //count in middle, single digit
//            new Rfc2445("RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=MO;COUNT=1", new Date(), false) //count at 1
//        };
//        
//        try {
//            for (Rfc2445 rfc : rfcs) {
//                Rfc2445 newRfc2445 = Rfc2445.getNew(rfc, new Date(), true);
//                System.out.println(newRfc2445);
//            }
//            
//            //and do one without decrementing the count
//            Rfc2445 newRfc2445 = Rfc2445.getNew(new Rfc2445("RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=MO;COUNT=1", new Date(), false), new Date(), false);
//            System.out.println(newRfc2445);
//            
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
