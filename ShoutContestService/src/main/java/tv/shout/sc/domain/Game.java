package tv.shout.sc.domain;

import java.io.Serializable;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
<pre>
{
    "id": string[uuid],
    "gameEngine": string,
    "gameNames": {
        languageCode1: string,
        languageCode2: string,
        ...
    },
    "gameDescriptions": {
        languageCode1: string,
        languageCode2: string,
        ...
    },
    "gameType": enum[NORMAL, TESTER],
    "producer": string,
    "gamePhotoUrl": string[optional-url],
    "gameStatus": enum[PENDING, CANCELLED, OPEN, INPLAY, CLOSED],
    "bracketEliminationCount": int,
    "allowBots": boolean,
    "useDoctoredTimeForBots": boolean,
    "fillWithBots": boolean,
    "maxBotFillCount": int,
    "pairImmediately": boolean,
    "canAppearInMobile": boolean,
    "productionGame": boolean,
    "privateGame": boolean,
    "inviteCode": string,
    "fetchingActivityTitles": {
        languageCode1: string,
        languageCode2: string,
        ...
    },
    "submittingActivityTitles": {
        languageCode1: string,
        languageCode2: string,
        ...
    },
    "allowableAppIds": [
        int1, int2, ...
    ],
    "allowableLanguageCodes": [
        languageCode1, languageCode2 ...
    ],
    "forbiddenCountryCodes": [
        countryCode1, countryCode2, ...
    ],
    "engineType": string,
    "includeActivityAnswersBeforeScoring": boolean,
    "startingLivesCount": int,
    "additionalLifeCost": double,
    "maxLivesCount": int,
    "guideUrl": string[optional-url],
    "guideHtmls": {
        languageCode1: string,
        languageCode2: string,
        ...
    },
    "autoStartPoolPlay": boolean,
    "autoStartBracketPlay": boolean,
    "autoBracketPlayPreStartNotificationTimeMs": long,
    "pendingDate": string[iso8601],
    "cancelledDate": string[iso8601],
    "openDate": string[iso8601],
    "inPlayDate": string[iso8601],
    "closedDate": string[iso8601]
}
</pre>
*/
public class Game
implements Serializable
{
    public enum GAME_TYPE {DEFAULT, TESTER}

    public enum GAME_STATUS {
        PENDING     //game is created, but not ready for public consumption
        ,CANCELLED
        ,OPEN       //game is ready for players to join (and leave)
        ,INPLAY     //game is currently ongoing
        ,CLOSED
    }

    public static final int INVITE_CODE_LENGTH = 6;

	private static final long serialVersionUID = 1L;

    private String _id;
    private String _gameEngine;
    private Map<String, String> _gameNames;
    private Map<String, String> _gameDescriptions;
    private GAME_TYPE _gameType = Game.GAME_TYPE.DEFAULT;
    private String _producer;
    private String _gamePhotoUrl;
    private GAME_STATUS _gameStatus;
    private Integer _bracketEliminationCount; //how many times can the player lose in bracket rounds before being eliminated; null=no limit

    private boolean _canAppearInMobile; //if true, only physical good are given away and it is safe to show this game in a mobile app. if false, virtual good are given away and it is NOT safe to show this game in a mobile app.
    private boolean _productionGame=true; // true means this is a "real" game with actual money on the line and marketing behind it

    private boolean _privateGame; //if true, means this is meant mainly for investors or testing and should not show up in any game lists
    private String _inviteCode; //only relevant if this is a private game. it is the only way to join the game - you must have the code

    //TODO: bot should be independent of game. this needs to be moved down into subprojects (which is where the bot logic lives), and can be added as an "extra" from there
    private boolean _allowBots; //does the game allow bots to be spun up to play for pairing
    private boolean _useDoctoredTimeForBots; //should bots actually wait X time before submitting their answers, or just doctor their time so it looks as if they took the time
    private boolean _fillWithBots; //fill up unused player slots with bots
    private Integer _maxBotFillCount; //if non-null, when bracket play begins, fill in bots UP TO this amount (this must be smaller than the max amount allowed on the first bracket round)
    private boolean _pairImmediately; //in POOL play, false=wait for another human (using a bot after timeout), OR true-pair immediately with a bot (don't wait for another human)

    //This allows the game to customize the message shown by the client when fetching an activity. For example:
    // Retrieving Question   (in a trivia game)
    // Walking to Tee box    (in a golf based trivia game)
    private Map<String, String> _fetchingActivityTitles;

    //This allows the game to customize the message shown by the client when sending an answer. For example:
    // Submitting Answer        (in a trivia game)
    // Marking the Scorecard    (in a golf based trivia game)
    private Map<String, String> _submittingActivityTitles;

    private Set<Integer> _allowableAppIds;
    private Set<String> _allowableLanguageCodes;
    private Set<String> _forbiddenCountryCodes;

    private String _engineType;

    private boolean _includeActivityAnswersBeforeScoring;

    private int _startingLivesCount;
    private Double _additionalLifeCost;
    private int _maxLivesCount;

    /** if given, is a link to an html page that contains information about the game (marketing, help, hints, etc) */
    private String _guideUrl;

    /** if given, is an html page that contains information about the game (marketing, help, hints, etc) */
    private Map<String, String> _guideHtmls;

    private boolean _autoStartPoolPlay; //whether or not to automatically start pool play when it's time for the pool play rounds to begin
    private boolean _autoStartBracketPlay; //whether or not to automatically start bracket play when it's time for bracket play rounds to begin
    private long _autoBracketPlayPreStartNotificationTimeMs; //how long before bracket play auto begins to start notifying players

    private Date _pendingDate;
    private Date _cancelledDate;
    private Date _openDate;
    private Date _inplayDate;
    private Date _closedDate;
    private Date _startingSmsSentDate;

    //NOTE: if any fields are added/changed/removed, don't forget:
    // 1) update the clone method
    // 2) update ShoutContestService.GameWithRounds.fromGame method
    // 3) modify the dao.insertOrReplaceGame sql
    // 4) modify the dao.updateGame sql
    // 5) modify the dao.cloneGame sql
    // 6) update the database
    // 7) change the .../meincoverlord/server/sql/snowl-mysql/snowl_stripped.sql file

    public String getGameName(String languageCode)
    {
        return LocalizationHelper.getLocalizedString(_gameNames, languageCode);
    }

    public String getGameDescription(String languageCode)
    {
        return LocalizationHelper.getLocalizedString(_gameDescriptions, languageCode);
    }

    public String getId(){
        return this._id;
    }
    public void setId(String id){
        this._id = id;
    }

    public String getGameEngine()
    {
        return _gameEngine;
    }
    public void setGameEngine(String gameEngine)
    {
        _gameEngine = gameEngine;
    }

    public Map<String, String> getGameNames() {
    	return this._gameNames;
	}
	public void setGameNames(Map<String, String> gameNames) {
		this._gameNames = gameNames;
	}

	public Map<String, String> getGameDescriptions()
    {
        return _gameDescriptions;
    }

    public void setGameDescriptions(Map<String, String> gameDescriptions)
    {
        _gameDescriptions = gameDescriptions;
    }

    public GAME_TYPE getGameType()
    {
        return _gameType;
    }

    public void setGameType(GAME_TYPE gameType)
    {
        _gameType = gameType;
    }

    public String getProducer()
    {
        return _producer;
    }

    public void setProducer(String producer)
    {
        if (producer != null && producer.length() > 400) producer = producer.substring(0, 400); //business rule per bruce
        _producer = producer;
    }

    public String getGamePhotoUrl() {
		return this._gamePhotoUrl;
	}
	public void setGamePhotoUrl(String gamePhotoUrl) {
		this._gamePhotoUrl = gamePhotoUrl;
	}

	public GAME_STATUS getGameStatus() {
		return this._gameStatus;
	}
	public void setGameStatus(GAME_STATUS gameStatus) {
		this._gameStatus = gameStatus;
	}

	public Integer getBracketEliminationCount()
    {
        return _bracketEliminationCount;
    }

    public void setBracketEliminationCount(Integer bracketEliminationCount)
    {
        _bracketEliminationCount = bracketEliminationCount;
    }

    public boolean isAllowBots()
    {
        return _allowBots;
    }

    public void setAllowBots(boolean allowBots)
    {
        _allowBots = allowBots;
    }

    public boolean isUseDoctoredTimeForBots()
    {
        return _useDoctoredTimeForBots;
    }

    public void setUseDoctoredTimeForBots(boolean useDoctoredTimeForBots)
    {
        _useDoctoredTimeForBots = useDoctoredTimeForBots;
    }

    public boolean isFillWithBots()
    {
        return _fillWithBots;
    }

    public void setFillWithBots(boolean fillWithBots)
    {
        _fillWithBots = fillWithBots;
    }

    public Integer getMaxBotFillCount()
    {
        return _maxBotFillCount;
    }

    public void setMaxBotFillCount(Integer maxBotFillCount)
    {
        _maxBotFillCount = maxBotFillCount;
    }

    public boolean isPairImmediately()
    {
        return _pairImmediately;
    }

    public void setPairImmediately(boolean pairImmediately)
    {
        _pairImmediately = pairImmediately;
    }

    public boolean isCanAppearInMobile()
    {
        return _canAppearInMobile;
    }

    public void setCanAppearInMobile(boolean canAppearInMobile)
    {
        _canAppearInMobile = canAppearInMobile;
    }

    public boolean isProductionGame()
    {
        return _productionGame;
    }

    public void setProductionGame(boolean productionGame)
    {
        _productionGame = productionGame;
    }

    public boolean isPrivateGame()
    {
        return _privateGame;
    }

    public void setPrivateGame(boolean privateGame)
    {
        _privateGame = privateGame;
    }

    public String getInviteCode()
    {
        return _inviteCode;
    }

    public void setInviteCode(String inviteCode)
    {
        _inviteCode = inviteCode;
    }

    public Map<String, String> getFetchingActivityTitles()
    {
        return _fetchingActivityTitles;
    }

    public void setFetchingActivityTitles(Map<String, String> fetchingActivityTitles)
    {
        _fetchingActivityTitles = fetchingActivityTitles;
    }

    public Map<String, String> getSubmittingActivityTitles()
    {
        return _submittingActivityTitles;
    }

    public void setSubmittingActivityTitles(Map<String, String> submittingActivityTitles)
    {
        _submittingActivityTitles = submittingActivityTitles;
    }

	public Set<Integer> getAllowableAppIds() {
		return this._allowableAppIds;
	}
	public void setAllowableAppIds(Set<Integer> allowableAppIds) {
		this._allowableAppIds = allowableAppIds;
	}

	public Set<String> getAllowableLanguageCodes() {
		return this._allowableLanguageCodes;
	}
	public void setAllowableLanguageCodes(Set<String> allowableLanguageCodes) {
		this._allowableLanguageCodes = allowableLanguageCodes;
	}

	public Set<String> getForbiddenCountryCodes() {
		return this._forbiddenCountryCodes;
	}
	public void setForbiddenCountryCodes(Set<String> forbiddenCountryCodes) {
		this._forbiddenCountryCodes = forbiddenCountryCodes;
	}

    public String getEngineType()
    {
        return _engineType;
    }

    public void setEngineType(String engineType)
    {
        _engineType = engineType;
    }

    public boolean isIncludeActivityAnswersBeforeScoring()
    {
        return _includeActivityAnswersBeforeScoring;
    }

    public void setIncludeActivityAnswersBeforeScoring(boolean includeActivityAnswersBeforeScoring)
    {
        _includeActivityAnswersBeforeScoring = includeActivityAnswersBeforeScoring;
    }

    public int getStartingLivesCount()
    {
        return _startingLivesCount;
    }

    public void setStartingLivesCount(int startingLivesCount)
    {
        _startingLivesCount = startingLivesCount;
    }

    public Double getAdditionalLifeCost()
    {
        return _additionalLifeCost;
    }

    public void setAdditionalLifeCost(Double additionalLifeCost)
    {
        _additionalLifeCost = additionalLifeCost;
    }

    public int getMaxLivesCount()
    {
        return _maxLivesCount;
    }

    public void setMaxLivesCount(int maxLivesCount)
    {
        _maxLivesCount = maxLivesCount;
    }

    public String getGuideUrl()
    {
        return _guideUrl;
    }

    public void setGuideUrl(String guideUrl)
    {
        _guideUrl = guideUrl;
    }

    public Map<String, String> getGuideHtmls()
    {
        return _guideHtmls;
    }

    public void setGuideHtmls(Map<String, String> guideHtmls)
    {
        _guideHtmls = guideHtmls;
    }

    public boolean isAutoStartPoolPlay()
    {
        return _autoStartPoolPlay;
    }

    public void setAutoStartPoolPlay(boolean autoStartPoolPlay)
    {
        _autoStartPoolPlay = autoStartPoolPlay;
    }

    public boolean isAutoStartBracketPlay()
    {
        return _autoStartBracketPlay;
    }

    public void setAutoStartBracketPlay(boolean autoStartBracketPlay)
    {
        _autoStartBracketPlay = autoStartBracketPlay;
    }

    public long getAutoBracketPlayPreStartNotificationTimeMs()
    {
        return _autoBracketPlayPreStartNotificationTimeMs;
    }

    public void setAutoBracketPlayPreStartNotificationTimeMs(long autoBracketPlayPreStartNotificationTimeMs)
    {
        _autoBracketPlayPreStartNotificationTimeMs = autoBracketPlayPreStartNotificationTimeMs;
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

    public Date getStartingSmsSentDate()
    {
        return _startingSmsSentDate;
    }

    public void setStartingSmsSentDate(Date startingSmsSentDate)
    {
        _startingSmsSentDate = startingSmsSentDate;
    }

    //static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String AB = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static SecureRandom rnd = new SecureRandom();
    public static String generateRandomString(int len)
    {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        }
        return sb.toString();
    }

    @JsonIgnore
    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        Game game = new Game();

        //copy everything (don't care about using the same ref as the parent in this case, so no need to create new lists, and sets and maps, etc)
        game._id = _id;
        game._gameEngine = _gameEngine;
        game._gameNames = _gameNames;
        game._gameDescriptions = _gameDescriptions;
        game._gameType = _gameType;
        game._producer = _producer;
        game._gamePhotoUrl = _gamePhotoUrl;
        game._gameStatus = _gameStatus;
        game._bracketEliminationCount = _bracketEliminationCount;
        game._allowBots = _allowBots;
        game._useDoctoredTimeForBots = _useDoctoredTimeForBots;
        game._fillWithBots = _fillWithBots;
        game._maxBotFillCount = _maxBotFillCount;
        game._pairImmediately = _pairImmediately;
        game._canAppearInMobile = _canAppearInMobile;
        game._productionGame = _productionGame;
        game._privateGame = _privateGame;

        //this must be unique for each game
        if (_inviteCode != null) {
            game._inviteCode = generateRandomString(INVITE_CODE_LENGTH);
        }

        game._fetchingActivityTitles = _fetchingActivityTitles;
        game._submittingActivityTitles = _submittingActivityTitles;
        game._allowableAppIds = _allowableAppIds;
        game._allowableLanguageCodes = _allowableLanguageCodes;
        game._forbiddenCountryCodes = _forbiddenCountryCodes;
        game._engineType = _engineType;
        game._includeActivityAnswersBeforeScoring = _includeActivityAnswersBeforeScoring;
        game._startingLivesCount = _startingLivesCount;
        game._additionalLifeCost = _additionalLifeCost;
        game._maxLivesCount = _maxLivesCount;
        game._guideUrl = _guideUrl;
        game._guideHtmls = _guideHtmls;

        game._autoStartPoolPlay = _autoStartPoolPlay;
        game._autoStartBracketPlay = _autoStartBracketPlay;
        game._autoBracketPlayPreStartNotificationTimeMs = _autoBracketPlayPreStartNotificationTimeMs;

        game._pendingDate = _pendingDate;
        game._cancelledDate = _cancelledDate;
        game._openDate = _openDate;
        game._inplayDate = _inplayDate;
        game._closedDate = _closedDate;

        return game;
    }

    @JsonIgnore
    @Override
    public String toString() {
        return MessageFormat.format(
            "id: {0}, name: {1}, desc: {2}, engineType: {3}, gameType: {4}, apps: {5}, elimCount: {6}",
            _id, _gameNames.get("en"), _gameDescriptions.get("en"), _engineType, _gameType, _allowableAppIds, _bracketEliminationCount);
    }

    @JsonIgnore
    @Override
    public boolean equals(Object obj)
    {
        if (obj == null || !(obj instanceof Game))
            return false;
        else
            return _id.equals( ((Game)obj).getId() );
    }
}
