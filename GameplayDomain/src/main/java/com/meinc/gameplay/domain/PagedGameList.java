package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.List;

public class PagedGameList 
implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** the list of games */
    private List<Event> _games;
    
    /** the page number of the games being returned */
    private int _page;
    
    /** the total number of pages if the full list were returned */
    private int _pageCount;
    
    /** the max page size */
    private int _pageSize;
    
    public PagedGameList()
    {
    }

    public List<Event> getGames()
    {
        return _games;
    }

    public void setGames(List<Event> games)
    {
        _games = games;
    }

    public int getPage()
    {
        return _page;
    }

    public void setPage(int page)
    {
        _page = page;
    }

    public int getPageCount()
    {
        return _pageCount;
    }

    public void setPageCount(int pageCount)
    {
        _pageCount = pageCount;
    }

    public int getPageSize()
    {
        return _pageSize;
    }

    public void setPageSize(int pageSize)
    {
        _pageSize = pageSize;
    }
}
