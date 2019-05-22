package by.mrj.performance.socket;

public interface NetServerSocket extends AutoCloseable {
    NetSocket accept();
}
