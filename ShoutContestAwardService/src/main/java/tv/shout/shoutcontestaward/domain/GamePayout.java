package tv.shout.shoutcontestaward.domain;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.joda.time.DateTime;

public class GamePayout implements Serializable {
	
	public enum PayoutStatusEnum { 
		NEW, INPROCESS, INREVIEW, DENIED, UNCLAIMED, PAID 
	};
	public enum PayoutChannelEnum { 
		EMAIL, WIRE, CHECK 
	};
	public enum PayoutCurrencyEnum { 
		USD 
	};
	
	private Integer gamePayoutId; 
	private String prizeKey; 
	private Integer contextId; 
	private String gameId;
	private String levelId; 
	private String levelNumber; 
	private Integer subscriberId;
	private String subscriberDetails; 
	private String payoutKey; 
	private String payoutEmail; 
	private PayoutChannelEnum payoutChannel; 
	private String payoutDescription; 
	private PayoutStatusEnum payoutStatus;
	private String payoutProcessorStatus;
	private Double payoutRequestAmount;
	private Double payoutActualAmount; 
	private PayoutCurrencyEnum payoutCurrency;
	private Date finalizedDate; 
	private Date updatedDate; 
	private Date createdDate;
	
	public GamePayout(){
		this.payoutChannel   = PayoutChannelEnum.EMAIL;
		this.payoutStatus    = PayoutStatusEnum.NEW;
		this.payoutCurrency  = PayoutCurrencyEnum.USD;
  		this.createdDate     = java.util.Calendar.getInstance().getTime();
	}
	
	public GamePayout(int contextId, int subscriberId, HashMap<String, Object> winningJsonMap){
		// TODO
		
		this.contextId       = contextId;
		this.subscriberId    = subscriberId;
		
		if (winningJsonMap.containsKey("winning_id"))
			this.prizeKey    = winningJsonMap.get("winning_id") + "";
		if (winningJsonMap.containsKey("game_id"))		
			this.gameId      = winningJsonMap.get("game_id") + "";
		if (winningJsonMap.containsKey("level_id"))		
			this.levelId     = winningJsonMap.get("level_id") + "";
		if (winningJsonMap.containsKey("level_number"))		
			this.levelNumber = winningJsonMap.get("level_number") + "";
		if (winningJsonMap.containsKey("prize_amount"))				
			this.payoutRequestAmount = Double.parseDouble(winningJsonMap.get("prize_amount") + "");
	}
	
	public String getProcessorBatchKey(){
		String key = MessageFormat.format("{0}_{1}_{2}_{3}_{4}_{5}", this.gamePayoutId + "", this.subscriberId + "", this.gameId, this.levelId, this.payoutRequestAmount + "", this.prizeKey);
		return key.substring(0, Math.min(key.length(), 30));
	}
	
	public Integer getGamePayoutId() {
		return gamePayoutId;
	}
	public void setGamePayoutId(Integer gamePayoutId) {
		this.gamePayoutId = gamePayoutId;
	}
	public String getPrizeKey() {
		return prizeKey;
	}
	public void setPrizeKey(String prizeKey) {
		this.prizeKey = prizeKey;
	}
	public Integer getContextId() {
		return contextId;
	}
	public void setContextId(Integer contextId) {
		this.contextId = contextId;
	}
	public String getGameId() {
		return gameId;
	}
	public void setGameId(String gameId) {
		this.gameId = gameId;
	}
	public String getLevelId() {
		return levelId;
	}
	public void setLevelId(String levelId) {
		this.levelId = levelId;
	}
	public String getLevelNumber() {
		return levelNumber;
	}
	public void setLevelNumber(String levelNumber) {
		this.levelNumber = levelNumber;
	}
	public Integer getSubscriberId() {
		return subscriberId;
	}
	public void setSubscriberId(Integer subscriberId) {
		this.subscriberId = subscriberId;
	}
	public String getSubscriberDetails() {
		return subscriberDetails;
	}
	public void setSubscriberDetails(String subscriberDetails) {
		this.subscriberDetails = subscriberDetails;
	}
	public String getPayoutKey() {
		return payoutKey;
	}
	public void setPayoutKey(String payoutKey) {
		this.payoutKey = payoutKey;
	}
	public String getPayoutEmail() {
		return payoutEmail;
	}
	public void setPayoutEmail(String payoutEmail) {
		this.payoutEmail = payoutEmail;
	}
	public PayoutChannelEnum getPayoutChannel() {
		return payoutChannel;
	}
	public void setPayoutChannel(PayoutChannelEnum payoutChannel) {
		this.payoutChannel = payoutChannel;
	}
	public String getPayoutDescription() {
		return payoutDescription;
	}
	public void setPayoutDescription(String payoutDescription) {
		this.payoutDescription = payoutDescription.substring(0, Math.min(payoutDescription.length(), 2040)); // max size in database;
	}
	public PayoutStatusEnum getPayoutStatus() {
		return payoutStatus;
	}
	public void setPayoutStatus(PayoutStatusEnum payoutStatus) {
		this.payoutStatus = payoutStatus;
	}
	public String getProcessorPayoutStatus() {
		return payoutProcessorStatus;
	}
	public void setProcessorPayoutStatus(String payoutProcessorStatus) {
		this.payoutProcessorStatus = payoutProcessorStatus;
	}
	public Double getPayoutRequestAmount() {
		return payoutRequestAmount;
	}
	public void setPayoutRequestAmount(Double payoutRequestAmount) {
		this.payoutRequestAmount = payoutRequestAmount;
	}
	public Double getPayoutActualAmount() {
		return payoutActualAmount;
	}
	public void setPayoutActualAmount(Double payoutActualAmount) {
		this.payoutActualAmount = payoutActualAmount;
	}
	public PayoutCurrencyEnum getPayoutCurrency() {
		return payoutCurrency;
	}
	public void setPayoutCurrency(PayoutCurrencyEnum payoutCurrency) {
		this.payoutCurrency = payoutCurrency;
	}
	public Date getFinalizedDate() {
		return finalizedDate;
	}
	public void setFinalizedDate(Date finalizedDate) {
		this.finalizedDate = finalizedDate;
	}
	public Date getUpdatedDate() {
		return updatedDate;
	}
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	public Date getCreatedDate() {
		return createdDate;
	}
	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	} 
}
