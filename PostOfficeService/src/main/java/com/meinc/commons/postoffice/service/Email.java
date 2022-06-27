package com.meinc.commons.postoffice.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Email implements Serializable
{
    private static final long serialVersionUID = 6650914703409936817L;

    protected EmailPurpose _purpose;
    protected EmailAddress _from;
    protected List<EmailAddress> _to;
    protected String _subject;
    protected EmailMessage _message;

    public Email(EmailPurpose purpose, EmailAddress from, EmailAddress to, String subject, EmailMessage message)
    {
        // Opt-out filtering uses remove on a list iterator - so we need a real
        // list
        List<EmailAddress> toList = new ArrayList<EmailAddress>();
        toList.add(to);

        _purpose = purpose;
        _from = from;
        _to = toList;
        _subject = subject;
        _message = message;
    }

    public Email(EmailPurpose purpose, EmailAddress from, List<EmailAddress> to, String subject, EmailMessage message)
    {
        _purpose = purpose;
        _from = from;
        _to = to;
        _subject = subject;
        _message = message;
    }

    Email(EmailPurpose purpose, EmailAddress from, List<EmailAddress> to, String subject)
    {
        this(purpose, from, to, subject, null);
    }

    public EmailPurpose getPurpose()
    {
        return _purpose;
    }

    public void setPurpose(EmailPurpose purpose)
    {
        _purpose = purpose;
    }

    public EmailAddress getFrom()
    {
        return _from;
    }

    public void setFrom(EmailAddress from)
    {
        _from = from;
    }

    public List<EmailAddress> getTo()
    {
        return _to;
    }

    public void setTo(List<EmailAddress> to)
    {
        _to = to;
    }

    public String getSubject()
    {
        return _subject;
    }

    public void setSubject(String subject)
    {
        _subject = subject;
    }

    public EmailMessage getMessage()
    {
        return _message;
    }

    public void setMessage(EmailMessage message)
    {
        _message = message;
    }

    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("[PURPOSE]: ").append(_purpose).append("\n");
        buf.append("[   FROM]: ").append(_from).append("\n");
        buf.append("[     TO]: ");_to.forEach(e -> {buf.append("\n\t" + e); });
        buf.append("\n[SUBJECT]: ").append(_subject).append("\n");
        buf.append("[MESSAGE]: ").append("\n").append(_message != null ? _message.getMessage() : "<EMPTY BODY>").append("\n");

        return buf.toString();
    }
}
