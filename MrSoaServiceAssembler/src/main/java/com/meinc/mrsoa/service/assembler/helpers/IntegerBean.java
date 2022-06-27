package com.meinc.mrsoa.service.assembler.helpers;

/**
 * This class is used by Maven's Jelly/Jexl because it cannot handle casting
 * from a long to an int very well on its own
 * 
 * @author mpontius
 */
public class IntegerBean {
    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = (int)(long)value;
    }
}
