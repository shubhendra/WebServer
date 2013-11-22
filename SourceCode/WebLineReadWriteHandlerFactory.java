
import java.nio.channels.SocketChannel;

public class WebLineReadWriteHandlerFactory 
    implements ISocketReadWriteHandlerFactory {
    public IReadWriteHandler createHandler(Dispatcher d, SocketChannel client, String documentRoot) {
	return new WebLineReadWriteHandler(d, client, documentRoot);
    }
}
