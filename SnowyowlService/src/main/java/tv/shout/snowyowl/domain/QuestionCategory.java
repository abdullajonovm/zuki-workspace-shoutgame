package tv.shout.snowyowl.domain;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;

@SuppressWarnings("serial")
public class QuestionCategory
implements Serializable
{
    private String _id;
    private String _categoryKey; //HISTORY, POLITICS, SCIENCE, etc..
    private Map<String,String> _categoryName;
    
    public QuestionCategory() {}
    
    public QuestionCategory(String uuid, String categoryKey, Map<String, String> categoryName)
    {
        _id = uuid;
        _categoryKey = categoryKey;
        _categoryName = categoryName;
    }
    
    public String getId()
    {
        return _id;
    }
    public void setId(String id)
    {
        _id = id;
    }
    
    public String getCategoryKey()
    {
        return _categoryKey;
    }
    public void setCategoryKey(String categoryKey)
    {
        _categoryKey = categoryKey;
    }
    
    public Map<String,String> getCategoryName()
    {
        return _categoryName;
    }
    public void setCategoryName(Map<String,String> categoryName)
    {
        _categoryName = categoryName;
    }
    
    @Override
    public String toString()
    {
        return MessageFormat.format("id: {0}, key: {1}", _id, _categoryKey);
    }
    
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof QuestionCategory)) 
            return false;
        else
            return _id.equals( ((QuestionCategory)obj).getId() );
    }    
}
