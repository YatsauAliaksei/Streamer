package by.mrj.controller;

import by.mrj.domain.*;
import by.mrj.domain.client.ConnectionInfo;
import by.mrj.domain.client.DataClient;
import by.mrj.serialization.DataDeserializer;
import by.mrj.service.register.ClientRegister;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Builder
@RequiredArgsConstructor
public class StreamerController implements CommandListener {

    private final DataDeserializer deserializer;
    private final ClientRegister clientRegister;

    @Override
    public void processRequest(Object msg, StreamingChannel streamChannel) {
    }

    @Override
    public void processRequest(String msgHeader, Object msgBody, StreamingChannel streamChannel) {
        MessageHeader header =  deserializer.deserialize(msgHeader, MessageHeader.class);
        // TODO: Authentication should happen at JWT handler
        log.debug("Header received [{}]", header);

        // TODO: security check
        switch (header.getCommand()) {
            case CONNECT:
                Message<String> msg = deserializer.deserializeMessage(msgBody, String.class);

                log.debug("Connect message received [{}]", msg);

                String loginName = msg.getPayload();
                DataClient dataClient = DataClient.builder()
                        .loginName(loginName)
                        .connectionInfo(ConnectionInfo.from(null)) // todo: stub
                        .streamingChannel(streamChannel)
                        .build();

                clientRegistration(dataClient);
                break;
            case READ:
                break;
            default:
                throw new UnsupportedOperationException();
                // close connection if unrecognized command
        }

    }

    // TODO: Test purpose only. Move to TEST section
//    public static void main(String[] args) {
//        StreamerController dataStreamer = new StreamerController(new JavaDataDeserialization(), client -> true,
//                new BasicNetServerSocket(8181), new InMemoryClientRegister());
//        dataStreamer.clientRegistration();
//    }

/*    @Override
    public void listen() {
        log.info("Listening...");

        while (true) {
            NetSocket socket = netServerSocket.accept();
            serverPool.submit(() -> {
                InputStream clientIS = socket.inputStream();
                Message<ClientConnection> connectionMessage = deserializer.deserializeMessage(clientIS, ClientConnection.class);
                ClientConnection clientConnection = connectionMessage.getPayload();

                if (!authenticateService.isAuthenticated(clientConnection)) {
                    closeConnection(socket, clientIS);
                    return;
                }

                clientRegister.register(clientConnection, socket);
            });
        }
    }*/


    @Override
    public void clientRegistration(DataClient dataClient) {
        log.debug("Received [{}]", dataClient);
        clientRegister.register(dataClient);
    }

    @Override
    public void getDataFromPoint(Point point) {


    }

    @Override
    public void getDataAtPoints(List<Point> points) {

    }

    private void closeConnection(StreamingChannel streamingChannel) {
        // TODO: Close channel
    }
}
