package tv.shout.sync.collector;

import com.meinc.webcollector.message.handler.BadRequestException;

public class PublishResponseError extends Exception {
    private static final long serialVersionUID = 1L;

    private String toWds;
    private String messageId;
    private String docType;
    private boolean success;
    private String failureType;
    private String failureMessage;

    public PublishResponseError(String toWds, String messageId, String docType, boolean success, String failureType, String failureMessage) {
        this.toWds = toWds;
        this.messageId = messageId;
        this.docType = docType;
        this.success = success;
        this.failureType = failureType;
        this.failureMessage = failureMessage;
    }
    
    public BadRequestException getBadMessageRequestException() {
        BadRequestException bmre = new BadRequestException()
                                                  .withErrorResponseBodyJsonKeyValue("success", false)
                                                  .withErrorResponseBodyJsonKeyValue(failureType, true);
        if (failureMessage != null)
            return bmre.withErrorResponseBodyJsonKeyValue("message", failureMessage);
        return bmre;
    }

    public String getToWds() {
        return toWds;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getDocType() {
        return docType;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getFailureType() {
        return failureType;
    }

    public String getFailureMessage() {
        return failureMessage;
    }
    
    @Override
    public String toString() {
        return super.toString() + " -> " + "<toWds="+toWds+",messageId="+messageId+",docType="+docType+",success="+success+",failureType="+failureType+",failureMessage="+failureMessage+">";
    }
}
