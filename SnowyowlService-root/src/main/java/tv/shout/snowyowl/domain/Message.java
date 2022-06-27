package tv.shout.snowyowl.domain;

/**
 * A message that will be passed to the message bus for handling
 */
public class Message
{
    //a message can have any type. for convenience, known types are listed here

    public static final String MESSAGE_TYPE_GAME_CANCELLED = "game_cancelled";

    public String type;
    public Object payload;

    public Message(String type, Object payload)
    {
        this.type = type;
        this.payload = payload;
    }
}
