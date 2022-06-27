package tv.shout.reactive;

import java.util.Date;

public class MockSponsorPlayer
{
    public long subscriberId;
    public boolean busyFlag;
    public String gameId;
    public int sponsorCashPoolId = 1;
    public Date lastUsedDate;

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof MockSponsorPlayer))
            return false;
        else
            return subscriberId == ((MockSponsorPlayer)obj).subscriberId;
    }
}
