package by.mrj.performance.socket;

import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

//@RequiredArgsConstructor(staticName = "of")
//@AllArgsConstructor
public class BasicNetSocket implements NetSocket {

    private final Socket socket;

    public BasicNetSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    @SneakyThrows
    public InputStream inputStream() {
        return socket.getInputStream();
    }

    @Override
    @SneakyThrows
    public OutputStream outputStream() {
        return socket.getOutputStream();
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }
}
