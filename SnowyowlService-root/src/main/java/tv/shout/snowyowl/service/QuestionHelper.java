package tv.shout.snowyowl.service;

import java.util.List;

import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.domain.Question;
import tv.shout.snowyowl.domain.QuestionAnswer;

public class QuestionHelper
extends LocalizationHelper
{
    public static Question getQuestion(String questionId, IDaoMapper dao)
    {
        Question q = dao.getQuestion(questionId);
        if (q == null) return null;

        q.setLanguageCodes(dao.getQuestionLanguageCodes(questionId));
        q.setForbiddenCountryCodes(dao.getQuestionForbiddenCountryCodes(questionId));
        q.setQuestionCategoryUuids(dao.getQuestionCategoryUuids(questionId));
        q.setQuestionText(tupleListToMap(dao.getMultiLocalizationValues(questionId, "questionText")));

        //flesh out the answers
        List<QuestionAnswer> answers = dao.getQuestionAnswersForQuestion(questionId);
        for (QuestionAnswer answer : answers) {
            answer.setAnswerText(tupleListToMap(dao.getMultiLocalizationValues(answer.getId(), "answerText")));
        }
        q.setAnswers(answers);

        return q;
    }

}
