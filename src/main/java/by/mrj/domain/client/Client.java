package by.mrj.domain.client;

import by.mrj.transport.socket.client.BasicNetSocket;
import by.mrj.transport.socket.client.NetSocket;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

@Slf4j
@ToString
@RequiredArgsConstructor
public class Client {

    private final NetSocket netSocket;

    @SneakyThrows
    public Client(String host, int port) {
        netSocket = new BasicNetSocket(new Socket(host, port));
    }

    public InputStream getInputStream() {
        return netSocket.inputStream();
    }

    public OutputStream getOutputStream() {
        return netSocket.outputStream();
    }

    @SneakyThrows
    public void write(byte[] bytes) {
        OutputStream os = netSocket.outputStream();
        os.write(bytes);
        os.flush();
    }

    @Override
    protected void finalize() throws Throwable {
        netSocket.close();
    }

    public void close() {
        try {
            netSocket.inputStream().close();
            netSocket.outputStream().close();
            netSocket.close();
        } catch (Exception e) {
            log.error("Failed closing client connection. Client [{}]", this, e);
        }
    }
}
