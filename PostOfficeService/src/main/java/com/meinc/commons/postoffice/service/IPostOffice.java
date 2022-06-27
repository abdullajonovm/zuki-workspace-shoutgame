package com.meinc.commons.postoffice.service;

import com.meinc.commons.postoffice.exception.PostOfficeException;
import com.meinc.http.domain.HttpRequest;
import com.meinc.http.domain.HttpResponse;
import com.meinc.mrsoa.service.ServiceEndpoint;

/**
 * Classes that implement this will know how to send e-mail/SMS message.
 */
public interface IPostOffice
{
    public String POSTOFFICE_ENCRYPT_NAMESPACE = "PostOfficeService";

    public void load();

    void unload();

    HttpResponse doGet(HttpRequest request);

    HttpResponse doPost(HttpRequest request);

    HttpResponse doGetVerifyEmail(HttpRequest request);

    HttpResponse doPostVerifyEmail(HttpRequest request);

    boolean registerEmailVerifiedCallback(ServiceEndpoint endpoint, String methodName);

    boolean unregisterEmailVerifiedCallback(ServiceEndpoint endpoint);

    /**
     * Send an email.
     *
     * @param email
     *            the email to send
     * @throws PostOfficeException
     */
    void sendEmail(Email email) throws PostOfficeException;

    /**
     * Send an email by using an email template.
     *
     * @param email
     *            the email to send
     *
     * @throws PostOfficeException
     */
    void sendTemplateEmail(TemplateEmail email, int appId, String languageCode) throws PostOfficeException;

    /**
     * Send an email with an attachment.
     *
     */
    void sendEmailWithAttachment(Email email, String attachmentData, String attachmentFilename) throws PostOfficeException;

    /**
     * Add an email to optout of communication from a specific email address.
     *
     * @param senderAddress
     *            The sender address.
     * @param recipientAddress
     *            The email that is to no longer receive communication.
     */
    public void addEmailOptout(String senderAddress, String recipientAddress);

    /**
     * Allow an email to once again begin receiving communication from a
     * specific sender email address.
     *
     * @param senderAddress
     *            The account in question.
     * @param emailAddress
     *            The email in question.
     */
    public void removeEmailOptout(String senderAddress, String recipientAddress);

    /**
     * Allow an email to once again begin receiving communication from <i>any
     * email address</i>.
     *
     * @param recipientAddress
     *            The email address in question.
     */
    public void removeEmailOptout(String recipientAddress);

    /**
     * Whether or not the given email address has opted out of receiving emails
     * from the specified sender address.
     *
     * @param senderAddress
     *            The sender email address.
     * @param recipientAddress
     *            The recipient email address.
     * @return
     */
    public boolean isEmailOptedOut(String senderAddress, String recipientAddress);

    /**
     * Whether or not the given email address has opted out of receiving emails
     * from <i>any email address</i>.
     *
     * @param recipientAddress
     *            The recipient email address.
     * @return
     */
    public boolean isEmailOptedOut(String recipientAddress);

    /**
     * Send an email to check whether or not the email is valid. If a link comes
     * back that matches, it's valid. <br/>
     * <br/>
     * The emailBody MUST contain this:
     *
     * <pre>
     * http://%{serverBaseUrl}/eps/verifyEmail?id=%{munge}
     * </pre>
     */
    public void verifyEmail(int appId, long subscriberId, String emailAddress, String emailBody, String subject, String languageCode)
    throws PostOfficeException;
}
