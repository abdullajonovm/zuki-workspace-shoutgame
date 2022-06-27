package tv.shout.snowyowl.common;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import tv.shout.snowyowl.dao.IDaoMapper;

public interface RoundQuestionRetriever
{
    default List<String> getQuestionIdsBasedOnCriteria(IDaoMapper dao, int minDifficulty, int maxDifficulty, String languageCodesAsCommaDelimitedList, Set<String> categoryUuids)
    {
        List<String> questionUuids;
        if (categoryUuids.contains("*")) {
            questionUuids = dao.getQuestionIdsBasedOnFiltersSansCategory(minDifficulty, maxDifficulty, languageCodesAsCommaDelimitedList);
        } else {
            String categoryUuidsAsCommaDelimiatedList = categoryUuids.stream().collect(Collectors.joining(","));
            questionUuids = dao.getQuestionIdsBasedOnFilters(minDifficulty, maxDifficulty, languageCodesAsCommaDelimitedList, categoryUuidsAsCommaDelimiatedList);
        }

        return questionUuids;
    }
}
