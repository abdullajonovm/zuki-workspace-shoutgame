package tv.shout.sm.admin;

public class User
{
    public int subscriberId;
    public String email;
    public String encryptKey;
    public String sha256EmailHash;
    public String sessionKey;
    public String deviceId;

    public User(int subscriberId, String email, String encryptKey, String sha256EmailHash,
            String sessionKey, String deviceId)
    {
        this.subscriberId = subscriberId;
        this.email = email;
        this.encryptKey = encryptKey;
        this.sha256EmailHash = sha256EmailHash;
        this.sessionKey = sessionKey;
        this.deviceId = deviceId;
    }

}
