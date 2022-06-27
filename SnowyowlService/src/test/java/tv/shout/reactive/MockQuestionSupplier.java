package tv.shout.reactive;

import tv.shout.snowyowl.dao.IDaoMapper;
import tv.shout.snowyowl.engine.QuestionSupplier;

public class MockQuestionSupplier
extends QuestionSupplier
{
    public MockQuestionSupplier(IDaoMapper dao)
    {
        _dao = dao;
    }
}
