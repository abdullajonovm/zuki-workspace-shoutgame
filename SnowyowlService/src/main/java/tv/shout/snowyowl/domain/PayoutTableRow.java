package tv.shout.snowyowl.domain;

import java.io.Serializable;
import java.text.MessageFormat;

public class PayoutTableRow
implements Serializable
{
    private static final long serialVersionUID = 1L;

    private int _rankFrom;
    private int _rankTo;
    private float _amount;
    private String _type;
    private String _category;
    private String _rowId;

    public PayoutTableRow() {}

    public PayoutTableRow(int rankFrom, int rankTo, float amount, String type, String category, String rowId)
    {
        _rankFrom = rankFrom;
        _rankTo = rankTo;
        _amount = amount;
        _type = type;
        _category = category;
        _rowId = rowId;
    }

    public int getRankFrom()
    {
        return _rankFrom;
    }

    public void setRankFrom(int rankFrom)
    {
        _rankFrom = rankFrom;
    }

    public int getRankTo()
    {
        return _rankTo;
    }

    public void setRankTo(int rankTo)
    {
        _rankTo = rankTo;
    }

    public float getAmount()
    {
        return _amount;
    }

    public void setAmount(float amount)
    {
        _amount = amount;
    }

    public String getType()
    {
        return _type;
    }

    public void setType(String type)
    {
        _type = type;
    }

    public String getCategory()
    {
        return _category;
    }

    public void setCategory(String category)
    {
        _category = category;
    }

    public String getRowId()
    {
        return _rowId;
    }

    public void setRowId(String rowId)
    {
        _rowId = rowId;
    }

    @Override
    public String toString()
    {
        return MessageFormat.format("from: {0}, to: {1}, amount: {2}, id: {3}", _rankFrom, _rankTo, _amount, _rowId);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (!(obj instanceof PayoutTableRow)) return false;

        return ((PayoutTableRow)obj).getRowId().equals(getRowId());
    }

}
