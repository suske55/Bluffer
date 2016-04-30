package Reactor.protocol;

public interface ServerProtocolFactory<T> {
   AsyncServerProtocol<T> create();
}
