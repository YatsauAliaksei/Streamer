package by.mrj.server.controller;


import by.mrj.common.domain.Point;
import by.mrj.common.domain.client.DataClient;
import by.mrj.domain.client.channel.ClientChannel;

import java.util.List;


public interface CommandListener {

    /**
     * Listens client commands at port using either WS/WSS, HTTP/S-Long polling, HTTP/S-polling.
     */
//    void listen();

    void processRequest(String msgHeader, Object msgBody, ClientChannel streamChannel);

    /**
     * Authenticated client registration
     * Receives all events starting from Registration point
     */
    void clientRegistration(DataClient dataClient);

    /**
     * Repeats data from {@param point}
     */
    void getDataFromPoint(Point point);

    /**
     * Repeats data at {@param points}
     */
    void getDataAtPoints(List<Point> points);

    void processRequest(Object msg, ClientChannel streamingChannel);
}
