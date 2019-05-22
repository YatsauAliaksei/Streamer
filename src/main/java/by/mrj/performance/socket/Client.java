package by.mrj.performance.socket;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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
}
