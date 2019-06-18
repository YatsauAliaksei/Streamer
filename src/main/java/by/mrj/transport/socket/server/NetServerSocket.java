package by.mrj.transport.socket.server;

import by.mrj.transport.socket.client.NetSocket;

public interface NetServerSocket extends AutoCloseable {
    NetSocket accept();
}
