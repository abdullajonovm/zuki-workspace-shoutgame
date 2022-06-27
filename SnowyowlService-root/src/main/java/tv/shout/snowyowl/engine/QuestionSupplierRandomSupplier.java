package tv.shout.snowyowl.engine;

public interface QuestionSupplierRandomSupplier
{
    long getSeedForRandomGameQuestionShuffle();

    int getRandomIntForSubscriberQuestionShuffle(int size);
}
