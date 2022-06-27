package com.meinc.trigger.filter;

import com.meinc.trigger.domain.Trigger;

/**
 * A link in a chain of Filters that work to process a piece of work.
 */
public interface Filter
{
    /**
     * Process a piece of work _if_ the filter knows how to process the given key.
     * 
     * @param key 
     * @param payload
     * @return true if the chain should continue; false if the chain of processing should terminate
     */
    public boolean process(Trigger trigger);
}
