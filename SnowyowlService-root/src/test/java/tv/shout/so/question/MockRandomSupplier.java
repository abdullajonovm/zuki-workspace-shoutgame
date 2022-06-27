package tv.shout.so.question;

import java.util.Arrays;
import java.util.List;

import tv.shout.snowyowl.engine.QuestionSupplierRandomSupplier;

public class MockRandomSupplier
implements QuestionSupplierRandomSupplier
{
    private int _idx = -1;
    private List<Integer> _randomValues = Arrays.asList(
        2
    );

    @Override
    public long getSeedForRandomGameQuestionShuffle()
    {
        return 5050L;
    }

    @Override
    public int getRandomIntForSubscriberQuestionShuffle(int size)
    {
        _idx++;
        return _randomValues.get(_idx);
    }

}
