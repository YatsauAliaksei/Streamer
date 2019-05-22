package by.mrj.performance.socket;

import lombok.SneakyThrows;

import java.net.ServerSocket;
import java.net.Socket;

public class BasicNetServerSocket implements NetServerSocket {

    private ServerSocket serverSocket;

    @SneakyThrows
    public BasicNetServerSocket(int socket) {
            serverSocket = new ServerSocket(socket);
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
