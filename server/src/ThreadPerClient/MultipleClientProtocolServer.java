package ThreadPerClient;

import java.io.*;
import java.net.*;
import Reactor.protocol.ServerProtocol;
import Reactor.protocol.TBGProtocol;
import Reactor.tokenizer.TokenizerFactory;


/**
* This class represents the server In charge of connecting to clients a creating a ConnectionHandler to handle messages from and to the client.
*/

interface ServerProtocolFactory {
   ServerProtocol create();
}


class EchoProtocolFactory implements ServerProtocolFactory {
	public ServerProtocol create(){
		return new TBGProtocol();
	}
}


public class MultipleClientProtocolServer implements Runnable {
	private ServerSocket serverSocket;
	private int listenPort;
	private ServerProtocolFactory protocolFactory;

	
	
	public MultipleClientProtocolServer(int port, ServerProtocolFactory p)
	{
		serverSocket = null;
		listenPort = port;
		protocolFactory = p;
	

	}
	
/**
* The process of connecting clients and creating a ConnectionHandlers for them
*/
	public void run()
	{
		try {
			serverSocket = new ServerSocket(listenPort);
			System.out.println("Listening...");
		}
		catch (IOException e) {
			System.out.println("Cannot listen on port " + listenPort);
		}
		
		while (true)
		{
			try {
				ConnectionHandler newConnection = new ConnectionHandler(serverSocket.accept(), protocolFactory.create());
            new Thread(newConnection).start();
			}
			catch (IOException e)
			{
				System.out.println("Failed to accept on port " + listenPort);
			}
		}
	}
	
/**
* Closes the connection
*/
	public void close() throws IOException
	{
		serverSocket.close();
	}
	public static void main(String[] args) throws IOException
	{
		// Get port
		int port = Integer.decode(args[0]).intValue();
		
		MultipleClientProtocolServer server = new MultipleClientProtocolServer(port, new EchoProtocolFactory());
		Thread serverThread = new Thread(server);
      serverThread.start();
		try {
			serverThread.join();
		}
		catch (InterruptedException e)
		{
			System.out.println("Server stopped");
		}
	}
}
