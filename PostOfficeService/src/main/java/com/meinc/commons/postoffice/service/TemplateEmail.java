package com.meinc.commons.postoffice.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

public class TemplateEmail
implements Serializable
{
    private static final long serialVersionUID = -7567495256349711950L;
//    private static Log _logger = LogFactory.getLog(TemplateEmail.class);

    private Email _email;
    private String _templatePath;
    private List<String> _disabledSectionNames = new ArrayList<String>();
    private Map<String, String> _templateVariables = new LinkedHashMap<String, String>();
    private String _templatePathPrefix;

    /**
     * Constructs a template email.
     *
     * @param templatePath
     *            the PostOfficeService relative path to an email template file
     * @param purpose
     *            the type or purpose of this email
     * @param from
     *            the sender's email address
     * @param to
     *            the recipient's email address
     * @param subject
     *            the email subject line
     */
    public TemplateEmail(String templatePath, EmailPurpose purpose, EmailAddress from, EmailAddress to, String subject)
    {
        this(templatePath, purpose, from, Arrays.asList(new EmailAddress[] { to }), subject);
    }

    /**
     * Constructs a template email.
     *
     * @param templatePath
     *            the PostOfficeService relative path to an email template file
     * @param purpose
     *            the type or purpose of this email
     * @param from
     *            the sender's email address
     * @param to
     *            the recipient email addresses
     * @param subject
     *            the email subject line
     */
    public TemplateEmail(String templatePath, EmailPurpose purpose, EmailAddress from, List<EmailAddress> to, String subject)
    {
        _email = new Email(purpose, from, to, subject);
        _templatePath = templatePath;
    }

    public void disableSection(String sectionName)
    {
        _disabledSectionNames.add(sectionName);
    }

    public void addVariable(String key, String value)
    {
        _templateVariables.put(key, value);
    }

    public String getVariable(String key)
    {
        return _templateVariables.get(key);
    }

    void setTemplatePathPrefix(String prefix)
    {
        _templatePathPrefix = prefix;
    }

    /** get a 'verify-email' templated email */
    public Email getVerifyEmail(int appId, String languageCode)
    throws IOException
    {
        List<String> templatePathFIFOQueue = new ArrayList<String>();
        templatePathFIFOQueue.add("shout_generic_email.html"); // fallback
        if (appId == 2) {
            templatePathFIFOQueue.add(0, "MyMadrid-es-v1.html");
            if ("en".equals(languageCode)) {
                templatePathFIFOQueue.add(0, "MyMadrid-en-v1.html");
            }
        }

        return getEmailRefactor(templatePathFIFOQueue);
    }

    /** get a rewards templated email */
    public Email getEmail(int appId, String languageCode) throws IOException
    {
//_logger.warn(">>>EMAIL>>> choosing template, appId: " + appId + ", languageCode: " + languageCode);
//        // there are many assumptions made in here that should be revisited at
//        // some point. i'm doing this under time crunch and so i'm cutting corners
//
//        // use as a LIFO queue by always adding to the front and then looping from front to back
//        List<String> templatePathFIFOQueue = new ArrayList<String>();
//        templatePathFIFOQueue.add("shout_generic_email.html"); // fallback
////_logger.warn(">>>EMAIL>>> adding: 'shout_generic_email.html'");
//        if ("shout_rewards_email.html".equals(_templatePath)) {
//            // quick way to see if this is a "rewards" email
//            templatePathFIFOQueue.add(0, "shout_rewards_email.html");
////_logger.warn(">>>EMAIL>>> adding: 'shout_rewards_email.html'");
//        }
//
//        if (appId == 2) {
//            // MyMadrid uses different templates
//
//            //add spanish as the default fallback
//            templatePathFIFOQueue.add(0, "MyMadrid-es-v1.html");
//
//            if ("en".equals(languageCode)) {
//                //if the user is using mymadrid, but is in english, use the english template
//                templatePathFIFOQueue.add(0, "MyMadrid-en-v1.html");
//            }
//
//        } else {
//            xxx
//        }

        List<String> templatePathFIFOQueue = new ArrayList<String>();

        //strip off anything after the "." (if any)
        String templateName = _templatePath.split("\\.")[0];

        //add in english as the fallback
        templatePathFIFOQueue.add(0, templateName + "_en.html");
//_logger.warn(">>>EMAIL>>> added template name to queue: " + templatePathFIFOQueue.get(0));

        //add in the specific language if something other than english
        templatePathFIFOQueue.add(0, templateName + "_" + languageCode + ".html");
//_logger.warn(">>>EMAIL>>> added template name to queue: " + templatePathFIFOQueue.get(0));

        return getEmailRefactor(templatePathFIFOQueue);
    }

    private Email getEmailRefactor(List<String> templatePathFIFOQueue)
    throws IOException
    {
        String emailMessage = null;
        for (String templatePath : templatePathFIFOQueue) {
//_logger.warn(">>>EMAIL>>> attempting to use template: " + templatePath);
            File _templateFile = null;
            _templateFile = new File(_templatePathPrefix, templatePath);
//_logger.warn(">>>EMAIL>>> template path: " + _templateFile.getPath());
            InputStream templateStream = getClass().getResourceAsStream(_templateFile.getPath());

            if (templateStream == null) {
//_logger.warn(">>>EMAIL>>> template not found!");
                continue;
            }
//_logger.warn(">>>EMAIL>>> using template: " + _templateFile.getPath());
            //emailMessage = new String(IOUtils.readFully(templateStream, -1, true));
            emailMessage = new String(IOUtils.toByteArray(templateStream));
            break;
        }
        if (emailMessage == null)
            throw new FileNotFoundException(String.format("Could not find %s in classpath", _templatePath));

        for (String sectionName : _disabledSectionNames) {
//_logger.warn(">>>EMAIL>>> ignoring section: " + sectionName);
            Pattern p = Pattern.compile("<!--" + sectionName + "-->.*?<!--/" + sectionName + "-->", Pattern.DOTALL);
            Matcher m = p.matcher(emailMessage);
            emailMessage = m.replaceAll("");
        }

        for (Entry<String, String> entry : _templateVariables.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            key = "%{" + key + "}";
//_logger.warn(">>>EMAIL>>> replacing all " + key + " with " + value);
            key = Pattern.quote(key);
            value = Matcher.quoteReplacement(value);
            emailMessage = emailMessage.replaceAll(key, value);
        }

        String replacement = Matcher.quoteReplacement("&nbsp;");
        emailMessage = emailMessage.replaceAll("%\\{.*?}", replacement);
//_logger.warn(">>>EMAIL>>> final message:\n" + emailMessage);

        _email.setMessage(new EmailMessage(emailMessage));
        return _email;
    }

    @Override
    public String toString()
    {
        return _email.toString();
    }
}
