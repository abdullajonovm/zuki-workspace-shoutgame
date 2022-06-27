package tv.shout.sc.domain;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * <pre>
{
    "id": string[uuid],
    "gameId": string[uuid],
    "roundNames": {
        languageCode1: string,
        languageCode2: string,
        ...
    },
    "roundType": enum[POOL, BRACKET, BINARY],
    "roundStatus": enum[PENDING, CANCELLED, VISIBLE, OPEN, FULL, INPLAY, CLOSED],
    "roundSequence": int,
    "finalRound": boolean,
    "roundPurse": double[optional],
    "currentPlayerCount": int,
    "maximumPlayerCount": int,
    "minimumMatchCount": int,
    "maximumMatchCount": int[optional],
    "costPerPlayer": double[optional],
    "roundActivityType": string,
    "roundActivityValue": string,
    "minimumActivityToWinCount": int,
    "maximumActivityCount": int[optional],
    "activityMinimumDifficulty": int[optional, >=0],
    "activityMaximumDifficulty": int[optional, <=10]
    "activityMaximumDurationSeconds": int,
    "playerMaximumDurationSeconds"; int,
    "durationBetweenActivitiesSeconds": int,
    "categoryList": [
        categoryUuid1, categoryUuid2, ...
    ],
    "matchGlobal": boolean,
    "maximumDurationMinutes": int[null],
    "matchPlayerCount": int,
    "pendingDate": string[iso8601],
    "cancelledDate": string[iso8601],
    "visibleDate": string[iso8601],
    "expectedOpenDate": string[iso8601],
    "openDate": string[iso8601],
    "inPlayDate": string[iso8601],
    "closedDate": string[iso8601]
}
* </pre>
*/
public class Round
implements Serializable
{
    public static enum ROUND_TYPE {
        POOL, BRACKET, BINARY
    }
    public static enum ROUND_STATUS {
    	 PENDING   //round is created but not yet ready to play
    	,CANCELLED //round has been cancelled
    	,VISIBLE   //round is no longer pending (can be seen), but isn't started (can't be joined)
    	,OPEN      //round is ready to join (or leave)
    	,FULL      //round is ready to join, but is full
    	//,READY     //round is ready to being pairing
    	,INPLAY    //round is in play (no more joining/leaving/pairing)
    	,CLOSED    //round is complete
    }

    public static final int MIN_DIFFICULTY = 0;
    public static final int MAX_DIFFICULTY = 10;

    private static final long serialVersionUID = 1L;

    private String _id;
    private String _gameId;
    private Map<String, String> _roundNames;  // Pool, Bracket0, Level1
	private ROUND_TYPE _roundType;
    private ROUND_STATUS _roundStatus;
    private int _roundSequence;
    private boolean _finalRound; // Marks the last round in DM or Tournament play
    private Double _roundPurse;
    private int _currentPlayerCount;   // TODO: manage this number to test against allowedPlayerCount
    private int _maximumPlayerCount;    //maximum # of players to let into a round (this round supports 100, for example)
    private int _minimumMatchCount;     //you have to play (and win) at least this many to move on
    private Integer _maximumMatchCount;     //you can't play more than this many or we kick you out
    private Double _costPerPlayer;
    private String _roundActivityType;      // HEX, Trivia, Tetris
    private String _roundActivityValue;     //how much the activity is worth (for example, if doing jeopardy, the questions in this round are worth: $100)
    private int _minimumActivityToWinCount;  //minimum number of activities (questions) needed to win/move on
    private Integer _maximumActivityCount;
    private Integer _activityMinimumDifficulty;
    private Integer _activityMaximumDifficulty;
    private int _activityMaximumDurationSeconds; //how long to timeout if they never actually are shown the activity
    private int _playerMaximumDurationSeconds; //how long to answer/play/etc
    private int _durationBetweenActivitiesSeconds; //how long after sending one activity before sending the next

    private Set<String> _categories; // *="no category filtering; i.e. ALL categories are up for grabs) the ActivityCategory id's. for example: politics, real madrid, WWII, Pets

    private boolean _matchGlobal; //1 wds doc or many (for example, for DM it's many, for Shout, it's 1)
    private Integer _maximumDurationMinutes;
    private int _matchPlayerCount; //how many people comprise a match (2=quiz, 3=jeopardy, etc)

    private Date _pendingDate;
    private Date _cancelledDate;
    private Date _visibleDate;
    private Date _expectedOpenDate; //when this round is expected to start (useful for UI to show expected start date)
    private Date _openDate;
    private Date _inplayDate;
    private Date _closedDate;

    public String getId(){
        return this._id;
    }
    public void setId(String id){
        this._id = id;
    }

	public String getGameId() {
		return _gameId;
	}
	public void setGameId(String gameId) {
		this._gameId = gameId;
	}

	public Map<String, String> getRoundNames() {
		return _roundNames;
	}
	public void setRoundNames(Map<String, String> roundNames) {
		this._roundNames = roundNames;
	}

	public ROUND_TYPE getRoundType() {
		return _roundType;
	}
	public void setRoundType(ROUND_TYPE roundType) {
		this._roundType = roundType;
	}

	public ROUND_STATUS getRoundStatus() {
		return _roundStatus;
	}
	public void setRoundStatus(ROUND_STATUS roundStatus) {
		this._roundStatus = roundStatus;
	}

	public int getRoundSequence() {
		return _roundSequence;
	}
	public void setRoundSequence(int roundSequence) {
		this._roundSequence = roundSequence;
	}

    public boolean isFinalRound()
    {
        return _finalRound;
    }
    public void setFinalRound(boolean finalRound)
    {
        _finalRound = finalRound;
    }

	public Double getRoundPurse() {
		return _roundPurse;
	}
	public void setRoundPurse(Double roundPurse) {
		this._roundPurse = roundPurse;
	}

	public int getCurrentPlayerCount() {
		return _currentPlayerCount;
	}
	public void setCurrentPlayerCount(int currentPlayerCount) {
		this._currentPlayerCount = currentPlayerCount;
	}

	public int getMaximumPlayerCount() {
		return _maximumPlayerCount;
	}
	public void setMaximumPlayerCount(int maximumPlayerCount) {
		this._maximumPlayerCount = maximumPlayerCount;
	}

	public int getMinimumMatchCount() {
		return _minimumMatchCount;
	}
	public void setMinimumMatchCount(int minimumMatchCount) {
		this._minimumMatchCount = minimumMatchCount;
	}

	public Integer getMaximumMatchCount() {
		return _maximumMatchCount;
	}
	public void setMaximumMatchCount(Integer maximumMatchCount) {
		this._maximumMatchCount = maximumMatchCount;
	}

	public int getMinimumActivityToWinCount() {
		return _minimumActivityToWinCount;
	}
	public void setMinimumActivityToWinCount(int minimumActivityToWinCount) {
		this._minimumActivityToWinCount = minimumActivityToWinCount;
	}

	public Integer getMaximumActivityCount() {
		return _maximumActivityCount;
	}
	public void setMaximumActivityCount(Integer maximumActivityCount) {
		this._maximumActivityCount = maximumActivityCount;
	}

	public Double getCostPerPlayer() {
		return _costPerPlayer;
	}
	public void setCostPerPlayer(Double costPerPlayer) {
		this._costPerPlayer = costPerPlayer;
	}

	public String getRoundActivityType() {
		return _roundActivityType;
	}
	public void setRoundActivityType(String roundActivityType) {
		this._roundActivityType = roundActivityType;
	}

	public String getRoundActivityValue() {
		return _roundActivityValue;
	}
	public void setRoundActivityValue(String roundActivityValue) {
		this._roundActivityValue = roundActivityValue;
	}

	public Integer getActivityMinimumDifficulty() {
		return _activityMinimumDifficulty;
	}
	public void setActivityMinimumDifficulty(Integer activityMinimumDifficulty) {
	    if (activityMinimumDifficulty != null && activityMinimumDifficulty < MIN_DIFFICULTY)
	        activityMinimumDifficulty = MIN_DIFFICULTY;

		this._activityMinimumDifficulty = activityMinimumDifficulty;
	}

    public Integer getActivityMaximumDifficulty()
    {
        return _activityMaximumDifficulty;
    }
    public void setActivityMaximumDifficulty(Integer activityMaximumDifficulty)
    {
        if (activityMaximumDifficulty != null && activityMaximumDifficulty > MAX_DIFFICULTY)
            activityMaximumDifficulty = MAX_DIFFICULTY;

        _activityMaximumDifficulty = activityMaximumDifficulty;
    }

	public int getActivityMaximumDurationSeconds() {
		return _activityMaximumDurationSeconds;
	}
	public void setActivityMaximumDurationSeconds(int activityMaximumDurationSeconds) {
		this._activityMaximumDurationSeconds = activityMaximumDurationSeconds;
	}

    public int getPlayerMaximumDurationSeconds()
    {
        return _playerMaximumDurationSeconds;
    }
    public void setPlayerMaximumDurationSeconds(int playerMaximumDurationSeconds)
    {
        _playerMaximumDurationSeconds = playerMaximumDurationSeconds;
    }

	public int getDurationBetweenActivitiesSeconds()
    {
        return _durationBetweenActivitiesSeconds;
    }

    public void setDurationBetweenActivitiesSeconds(int durationBetweenActivitiesSeconds)
    {
        _durationBetweenActivitiesSeconds = durationBetweenActivitiesSeconds;
    }

    public Set<String> getCategories() {
		return _categories;
	}
	public void setCategories(Set<String> categories) {
		this._categories = categories;
	}

    public boolean isMatchGlobal()
    {
        return _matchGlobal;
    }
    public void setMatchGlobal(boolean matchGlobal)
    {
        _matchGlobal = matchGlobal;
    }

	public Integer getMaximumDurationMinutes() {
		return _maximumDurationMinutes;
	}
	public void setMaximumDurationMinutes(Integer maximumDurationMinutes) {
		this._maximumDurationMinutes = maximumDurationMinutes;
	}

    public int getMatchPlayerCount()
    {
        return _matchPlayerCount;
    }
    public void setMatchPlayerCount(int matchPlayerCount)
    {
        _matchPlayerCount = matchPlayerCount;
    }

    public Date getPendingDate()
    {
        return _pendingDate;
    }
    public void setPendingDate(Date pendingDate)
    {
        _pendingDate = pendingDate;
    }
    public Date getCancelledDate()
    {
        return _cancelledDate;
    }
    public void setCancelledDate(Date cancelledDate)
    {
        _cancelledDate = cancelledDate;
    }
    public Date getVisibleDate()
    {
        return _visibleDate;
    }
    public void setVisibleDate(Date visibleDate)
    {
        _visibleDate = visibleDate;
    }
    public Date getExpectedOpenDate()
    {
        return _expectedOpenDate;
    }
    public void setExpectedOpenDate(Date expectedOpenDate)
    {
        _expectedOpenDate = expectedOpenDate;
    }
    public Date getOpenDate()
    {
        return _openDate;
    }
    public void setOpenDate(Date openDate)
    {
        _openDate = openDate;
    }
    public Date getInplayDate()
    {
        return _inplayDate;
    }
    public void setInplayDate(Date inplayDate)
    {
        _inplayDate = inplayDate;
    }
    public Date getClosedDate()
    {
        return _closedDate;
    }
    public void setClosedDate(Date closedDate)
    {
        _closedDate = closedDate;
    }
    public Round cloneForNextRound()
    {
        Round round = new Round();
        round._id = UUID.randomUUID().toString();
        round._gameId = _gameId;
        round._roundType = _roundType;
        round._roundStatus = Round.ROUND_STATUS.PENDING;
        round._roundSequence = _roundSequence+1;
        round._finalRound = true;
        round._roundPurse = _roundPurse;
        round._currentPlayerCount = 0;
        round._maximumPlayerCount = _maximumPlayerCount;
        round._minimumMatchCount = _minimumMatchCount;
        round._maximumMatchCount = _maximumMatchCount;
        round._costPerPlayer = _costPerPlayer;
        round._roundActivityType = _roundActivityType;
        round._roundActivityValue = _roundActivityValue;
        round._minimumActivityToWinCount = _minimumActivityToWinCount;
        round._maximumActivityCount = _maximumActivityCount;
        round._activityMinimumDifficulty = _activityMinimumDifficulty;
        round._activityMaximumDifficulty = _activityMaximumDifficulty;
        round._activityMaximumDurationSeconds = _activityMaximumDurationSeconds;
        round._playerMaximumDurationSeconds = _playerMaximumDurationSeconds;
        round._durationBetweenActivitiesSeconds = _durationBetweenActivitiesSeconds;
        round._categories = _categories;
        round._matchGlobal = _matchGlobal;
        round._maximumDurationMinutes = _maximumDurationMinutes;
        round._matchPlayerCount = _matchPlayerCount;
        round._pendingDate = new Date();
        round._expectedOpenDate = new Date();

        //take each of the round names, and increment the number by 1 (if it ends in a number)
        //ex: "Tournament Round 1" becomes "Tournament Round 2"
        Map<String, String> incrementedRoundNames = new HashMap<>();
        _roundNames.entrySet().stream().forEach(entry -> {
            String languageCode = entry.getKey();
            String name = entry.getValue();

            int lastSpaceIdx = name.lastIndexOf(" ");
            if (lastSpaceIdx != -1) {
                try {
                    int roundNum = Integer.parseInt(name.substring(lastSpaceIdx+1));
                    roundNum++;
                    name = name.substring(0, lastSpaceIdx) + " " + roundNum;
                    incrementedRoundNames.put(languageCode, name);
                } catch (Exception ignored) {
                    incrementedRoundNames.put(languageCode, name);
                }
            } else {
                incrementedRoundNames.put(languageCode, name);
            }
        });
        round._roundNames = incrementedRoundNames;

        return round;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof Round))
            return false;
        else
            return _id.equals( ((Round)obj).getId() );
    }

    @Override
    public String toString()
    {
        return MessageFormat.format(
            "startCount: {0}, elimCount: --, payout: {1,number}, type: --, category: --, roundNumber: {2}",
            _maximumPlayerCount, _roundPurse, _roundSequence
        );
    }
}
