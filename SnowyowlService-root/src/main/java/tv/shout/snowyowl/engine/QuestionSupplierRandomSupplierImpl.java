package tv.shout.snowyowl.engine;

import java.util.Random;
import java.util.UUID;

//this was broken out into an interface so a mock impl could be supplied for testing purposes
public class QuestionSupplierRandomSupplierImpl
implements QuestionSupplierRandomSupplier
{

    @Override
    public long getSeedForRandomGameQuestionShuffle()
    {
        return UUID.randomUUID().getMostSignificantBits();
    }

    @Override
    public int getRandomIntForSubscriberQuestionShuffle(int size)
    {
        return new Random(UUID.randomUUID().getMostSignificantBits()).nextInt(size);
    }

}
