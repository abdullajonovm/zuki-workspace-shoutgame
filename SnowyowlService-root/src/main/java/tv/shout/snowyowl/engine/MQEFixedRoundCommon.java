package tv.shout.snowyowl.engine;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.meinc.trigger.service.ITriggerService;

import io.socket.client.Socket;
import tv.shout.sc.domain.Game;
import tv.shout.snowyowl.common.PushSender;
import tv.shout.snowyowl.service.SnowyowlService;

public abstract class MQEFixedRoundCommon
extends BaseEngine
implements MQE, PushSender
{
    private static Logger _logger = Logger.getLogger(MQEFixedRoundCommon.class);

    @Autowired
    private ITriggerService _triggerService;

    @Autowired
    private MQECommon _mqeCommon;

    //threading
    //this engine is built around only 1 run at a time (not for concurrent runs)
    private ArrayBlockingQueue<Integer> _workQueue = new ArrayBlockingQueue<>(1);
    private boolean _runnerGracefulInterrupt;
    private MQERunner _runner;
    private MQESanityChecker _sanityChecker;

    abstract public String getFilePrefix();
    abstract public MME getMME();

    //must be called when service starts
    @Override
    public void start(Socket socketIoSocket)
    {
//_logger.info(">>> MQE starting...");
        if (_runner == null) {
            _runner = new MQERunner();
            _runner.setDaemon(true);
            _runner.start();
        }

        if (_sanityChecker == null) {
            _sanityChecker = new MQESanityChecker();
            _sanityChecker.setDaemon(true);
            _sanityChecker.start();
        }

        _socketIoSocket = socketIoSocket;

        _mqeCommon.loadState(getFilePrefix(), this::run);
//_logger.info(">>> MQE started");
    }

    //must be called when service stops
    @Override
    public void stop()
    {
//_logger.info(">>> MQE stopping...");
        _runnerGracefulInterrupt = true;

        if (_runner != null) {
            _runner.interrupt();
        }

        if (_sanityChecker != null) {
            _sanityChecker.interrupt();
        }
//_logger.info(">>> MQE stopped");
    }

    //called from the game handler when a subscriber cancels a pool play match
    @Override
    public void subscriberCancelledQueuing(long subscriberId)
    {
        _mqeCommon.subscriberCancelledQueuing(subscriberId);
    }

    //tell the MQE to run
    @Override
    public void run(Void x)
    {
        try {
//_logger.info(">>> MQE run has been called. _runner alive: " + _runner.isAlive());
            //if something is already processing, "put" will block, but the engine should complete fairly quickly
            _workQueue.put(1); //value doesn't matter. just that something goes into the queue so the thread will run
        } catch (InterruptedException e) {
            if (!_runnerGracefulInterrupt) {
                _logger.error("MQE run interrupted!", e);
            }
        }
    }

    private class MQESanityChecker
    extends Thread
    {
        @Override
        public void run()
        {
            try {
                while (!isInterrupted()) {
                    if (SnowyowlService.SUB_EVENT_LOGGER.isDebugEnabled()) {
                        StringBuilder buf = new StringBuilder();
                        if (_mqeCommon.getSubscriberMatchQueueWaitingMap().size() > 0) {
                            buf.append("MQE: sanity check: subscriberMatchQueueWaitingMap has ");
                            buf.append(_mqeCommon.getSubscriberMatchQueueWaitingMap().size());
                            buf.append(" entries, the oldest has been waiting for ");
                            long oldestEntry = System.currentTimeMillis();
                            for (Entry<Long, Long> entry : _mqeCommon.getSubscriberMatchQueueWaitingMap().entrySet()) {
                                long val = entry.getValue();
                                if (val < oldestEntry) {
                                    oldestEntry = val;
                                }
                            }
                            buf.append(System.currentTimeMillis() - oldestEntry);
                            buf.append(" ms.");

                            SnowyowlService.SUB_EVENT_LOGGER.debug(buf.toString());
                        }
                    }

                    Thread.sleep(60_000L);
                }
            } catch (InterruptedException e) {
                if (!_runnerGracefulInterrupt) {
                    _logger.error("MQE sanity check was interrupted!", e);
                }
            }
        }
    };

    private class MQERunner
    extends Thread
    {
        @Override
        public void run()
        {
            try {
                while (!isInterrupted()) {
                    _workQueue.take(); //block until input is available
                    process();
                }
            } catch (InterruptedException e) {
                if (!_runnerGracefulInterrupt) {
                    _logger.error("MQERunner was interrupted. work queue no longer being processed!", e);
                }
            }
        }
    }

    private void process()
    {
        if (_logger.isDebugEnabled()) {
            _logger.debug("MQE running...");
        }

        //process each open/inplay game that matches this engine's type
        List<Game> games = _mqeCommon.getGamesForProcessing(getType());
        //_logger.info("# of games for mqe to process: " + games.size());
        games.stream()
            .forEach(game -> {
                if (_logger.isDebugEnabled()) {
                    _logger.debug(MessageFormat.format("processing game: {0}, id: {1}", game.getGameNames().get("en"), game.getId()));
                }
                _mqeCommon.processGame(game, this::run, getFilePrefix(), _socketIoSocket, _triggerService, getMME());
            });
    }

}
