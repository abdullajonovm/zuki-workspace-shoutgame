package com.meinc.gameplay.domain;

import java.io.Serializable;
import java.util.List;

public class PollResults 
implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4176482007312884512L;
	private int _pollId;
	private int _totalNumberAnswered;
	private int _totalAnsweredCorrectly;
	private List<Integer> _totalAnsweredByQuestion;
	private List<Integer> _totalAnsweredCorrectlyByQuestion;
	private List<Integer> _winningSubscriberIds;
	private int _totalRecipients;
	private int _avgResponseTimeS;

	// TODO: later: avg response time by carrier (carrier name, carrier code,
	// response time)

	public PollResults() {
	}

	public int getPollId() {
		return _pollId;
	}

	public void setPollId(int pollId) {
		_pollId = pollId;
	}

	public int getTotalNumberAnswered() {
		return _totalNumberAnswered;
	}

	public void setTotalNumberAnswered(int totalNumberAnswered) {
		_totalNumberAnswered = totalNumberAnswered;
	}

	public int getTotalAnsweredCorrectly() {
		return _totalAnsweredCorrectly;
	}

	public void setTotalAnsweredCorrectly(int totalAnsweredCorrectly) {
		_totalAnsweredCorrectly = totalAnsweredCorrectly;
	}

	public List<Integer> getTotalAnsweredByQuestion() {
		return _totalAnsweredByQuestion;
	}

	public void setTotalAnsweredByQuestion(List<Integer> totalAnsweredByQuestion) {
		_totalAnsweredByQuestion = totalAnsweredByQuestion;
	}

	public List<Integer> getTotalAnsweredCorrectlyByQuestion() {
		return _totalAnsweredCorrectlyByQuestion;
	}

	public void setTotalAnsweredCorrectlyByQuestion(
			List<Integer> totalAnsweredCorrectlyByQuestion) {
		_totalAnsweredCorrectlyByQuestion = totalAnsweredCorrectlyByQuestion;
	}

	public List<Integer> getWinningSubscriberIds() {
		return _winningSubscriberIds;
	}

	public void setWinningSubscriberIds(List<Integer> winningSubscriberIds) {
		_winningSubscriberIds = winningSubscriberIds;
	}

	public void setTotalRecipients(int totalRecipients) {
		_totalRecipients = totalRecipients;
	}

	public int getTotalRecipients() {
		return _totalRecipients;
	}

	public void setAvgResponseTimeS(int avgResponseTimeS) {
		_avgResponseTimeS = avgResponseTimeS;
	}

	public int getAvgResponseTimeS() {
		return _avgResponseTimeS;
	}
}
