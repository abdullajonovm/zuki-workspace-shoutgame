package tv.shout.snowyowl.domain;

import java.text.MessageFormat;

import org.apache.commons.text.StringEscapeUtils;

public class ReportStructAffiliatePayout
{
    private String _email;
    private String _nickname;
    private int _networkSize;
    private float _winnings;

    public String getEmail()
    {
        return _email;
    }
    public void setEmail(String email)
    {
        _email = email;
    }
    public String getNickname()
    {
        return _nickname;
    }
    public void setNickname(String nickname)
    {
        _nickname = nickname;
    }
    public int getNetworkSize()
    {
        return _networkSize;
    }
    public void setNetworkSize(int networkSize)
    {
        _networkSize = networkSize;
    }
    public float getWinnings()
    {
        return _winnings;
    }
    public void setWinnings(float winnings)
    {
        _winnings = winnings;
    }

    public String toCsv()
    {
        return MessageFormat.format(
            "{0},{1},{2,number,#},{3,number,#.##}",
            StringEscapeUtils.escapeCsv(_email), StringEscapeUtils.escapeCsv(_nickname), _networkSize, _winnings);
    }
}
