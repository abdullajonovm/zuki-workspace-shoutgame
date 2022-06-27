package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents a list of objects that can be paged through.
 * @author bxgrant
 */
public class PageableList<T> implements Serializable
{
    private static final long serialVersionUID = 811636087230654955L;

    /**
     * The list of objects of type T.
     */
    private List<T> _list; 
    
    /**
     * The page number being returned in this instance, starts counting at zero.
     */
    private int _pageNumber;
    
    /**
     * The number of pages available.  If page count is 1 then all objects are
     * being returned in the list member.
     */
    private int _pageCount;
    
    /**
     * The max number of pages in each list.
     */
    private int _pageSize;
    
    public PageableList()
    {
    }
    
    public PageableList(List<T> list, int pageNumber, int pageCount, int pageSize)
    {
        _list = list;
        _pageNumber = pageNumber;
        _pageCount = pageCount;
        _pageSize = pageSize;
    }

    public List<T> getList()
    {
        return _list;
    }

    public void setList(List<T> list)
    {
        _list = list;
    }

    public int getPageNumber()
    {
        return _pageNumber;
    }

    public void setPageNumber(int pageNumber)
    {
        _pageNumber = pageNumber;
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
