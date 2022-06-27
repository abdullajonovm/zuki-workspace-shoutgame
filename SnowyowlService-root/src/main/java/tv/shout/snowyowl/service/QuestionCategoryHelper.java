package tv.shout.snowyowl.service;

import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.QuestionCategory;

public class QuestionCategoryHelper
extends LocalizationHelper
{
    public static QuestionCategory getQuestionCategoryByKey(String categoryKey, IDaoMapper dao)
    {
        QuestionCategory category = dao.getQuestionCategoryByKey(categoryKey);
        if (category == null) return null;

        //add in the localization
        category.setCategoryName(tupleListToMap(dao.getMultiLocalizationValues(category.getId(), "categoryName")));

        return category;
    }

    public static QuestionCategory getQuestionCategoryById(String categoryId, IDaoMapper dao)
    {
        QuestionCategory category = dao.getQuestionCategoryById(categoryId);
        if (category == null) return null;

        //add in the localization
        category.setCategoryName(tupleListToMap(dao.getMultiLocalizationValues(category.getId(), "categoryName")));

        return category;
    }

    public static void deleteQuestionCategory(String categoryId, IDaoMapper dao)
    {
        dao.removeQuestionCategoryNames(categoryId);
        dao.deleteQuestionCategory(categoryId);
    }

}
