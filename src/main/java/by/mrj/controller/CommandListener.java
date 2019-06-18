package by.mrj.controller;


import by.mrj.domain.Point;
import by.mrj.domain.StreamingChannel;
import by.mrj.domain.client.DataClient;

import java.util.List;

public interface CommandListener {

    /**
     * Listens client commands at port using either WS/WSS, HTTP/S-Long polling, HTTP/S-polling.
     */
//    void listen();

    void processRequest(String msgHeader, Object msgBody, StreamingChannel streamChannel);

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

    void processRequest(Object msg, StreamingChannel streamingChannel);
}
