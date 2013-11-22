
import java.nio.channels.SocketChannel;

public interface ISocketReadWriteHandlerFactory {
    public IReadWriteHandler createHandler(Dispatcher d, SocketChannel client, String documentRoot);
}