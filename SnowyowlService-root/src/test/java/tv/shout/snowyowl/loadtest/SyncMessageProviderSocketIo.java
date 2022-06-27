package tv.shout.snowyowl.loadtest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.socket.client.IO;
import io.socket.client.Socket;
import tv.shout.sync.domain.SyncMessage;
import tv.shout.util.JsonUtil;

class SyncMessageProviderSocketIo
implements SyncMessageProvider
{
    private static Logger _logger = Logger.getLogger(SyncMessageProviderSocketIo.class);
    private static ObjectMapper _jsonMapper = JsonUtil.getObjectMapper();

    private Socket _socket;
    private SyncMessageReceiver _receiver;
    private String _nickname;
    private String _primaryIdHash;

    SyncMessageProviderSocketIo(
        final SyncMessageReceiver receiver, final String socketIoUrl, final String nickname, final String primaryIdHash)
    {
        if (receiver == null) throw new IllegalArgumentException("receiver is null");
        if (socketIoUrl == null) throw new IllegalArgumentException("socketIoUrl is null");
        if (nickname == null) throw new IllegalArgumentException("nickname is null");
        if (primaryIdHash == null) throw new IllegalArgumentException("primaryIdHash is null");

        _receiver = receiver;
        _nickname = nickname;
        _primaryIdHash = primaryIdHash;

        //setup socket.io listener
        try {
            _socket = IO.socket(socketIoUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException("unable to create socket.io connection to: " + socketIoUrl, e);
        }
        _socket
        .on(Socket.EVENT_CONNECTING, args -> {
            //_logger.info("SOCKETIO: connecting to: " + socketIoUrl);
        })
        .on(Socket.EVENT_CONNECT, args -> {
            _logger.info(MessageFormat.format("SOCKETIO: {0} connected, doing client_checking with primaryIdHash: {1}", _nickname, _primaryIdHash));
            _socket.emit("client_checkin", _primaryIdHash);

            _receiver.syncMessageReceiverReady();
        })
        .on(Socket.EVENT_CONNECT_ERROR, args -> {
            if (args.length > 0 && args[0] instanceof Exception) {
                _logger.error(MessageFormat.format("SOCKETIO: {0} unable to connect", _nickname), (Exception)args[0]);
            } else {
                _logger.error(MessageFormat.format("SOCKETIO: {0} connect error", _nickname));
            }

            _receiver.syncMessageReceiverError();
        })
        .on(Socket.EVENT_DISCONNECT, args -> {
            _logger.info(MessageFormat.format("SOCKETIO: {0} disconnected", _nickname));
        })
        .on("ping",  args -> {
            //_logger.info("SOCKETIO: >>>ping");
            _socket.emit("pong");
        })
        .on("sync_message",  args -> {
            try {
                SyncMessage sm = _jsonMapper.readValue((String) args[0], new TypeReference<SyncMessage>(){});
//                _logger.info(MessageFormat.format("SOCKETIO: {0} received sync message: {1} ({2,date,yyyy-MM-dd hh:mm:ss.SSS})", _nickname, sm.getMessageType(), sm.getCreateDate()));

                _receiver.syncMessageReceived(sm);

            } catch (IOException e) {
                throw new IllegalStateException("received invalid sync message json from socket.io", e);
            }
        });

        _socket.connect();
    }

    @Override
    public void start(String gameId)
    {
        //no-op
    }

    @Override
    public void stop()
    {
        //disconnect from socket.io
        if (_socket != null && _socket.connected()) {
            _socket.disconnect();
        }
        if (_socket != null) {
            _socket.close();
        }
        _socket = null;
    }
}
