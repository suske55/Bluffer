package Reactor.reactor;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;

import Reactor.protocol.ServerProtocol;
import Reactor.tokenizer.MessageTokenizer;
import Reactor.protocol.*;
import Reactor.tokenizer.*;

/**
 * This class supplies some data to the protocol, which then processes the data,
 * possibly returning a reply. This class is implemented as an executor task.
 * 
 */
public class ProtocolTask<T> implements Runnable {

	private final AsyncTBGProtocol _protocol;
	private final FixedSeparatorMessageTokenizer _tokenizer;
	private final ConnectionHandler<T> _handler;

	public ProtocolTask(final AsyncTBGProtocol protocol, final FixedSeparatorMessageTokenizer tokenizer, final ConnectionHandler<T> h) {
		this._protocol = protocol;
		this._tokenizer = tokenizer;
		this._handler = h;
	}

	// we synchronize on ourselves, in case we are executed by several threads
	// from the thread pool.
	public synchronized void run() {
      // go over all complete messages and process them.
      while (_tokenizer.hasMessage()) {
         StringMessage msg = _tokenizer.nextMessage();
         this._protocol.processMessage(msg.getMessage() , T->{
        	 _handler.addOutData(_tokenizer.getBytesForMessage(new StringMessage((String)T)));
        	 if(_protocol.shouldClose()){
        	 }
         });
      }
	}

	public void addBytes(ByteBuffer b) {
		_tokenizer.addBytes(b);
	}
}
