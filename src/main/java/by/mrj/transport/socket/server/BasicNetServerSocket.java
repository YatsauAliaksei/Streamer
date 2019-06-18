package by.mrj.transport.socket.server;

import by.mrj.transport.socket.client.BasicNetSocket;
import by.mrj.transport.socket.client.NetSocket;
import lombok.SneakyThrows;

import java.net.ServerSocket;
import java.net.Socket;

public class BasicNetServerSocket implements NetServerSocket {

    private ServerSocket serverSocket;

    @SneakyThrows
    public BasicNetServerSocket(int socket) {
            serverSocket = new ServerSocket(socket, 5_000);
    }

    @Override
    @SneakyThrows
    public NetSocket accept() {
        Socket socket = serverSocket.accept();
        return new BasicNetSocket(socket);
    }

    @Override
    public void close() throws Exception {
        serverSocket.close();
    }
}
