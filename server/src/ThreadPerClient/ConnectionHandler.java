package ThreadPerClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.ByteBuffer;

import Reactor.protocol.ServerProtocol;
import Reactor.tokenizer.MessageTokenizer;
import Reactor.tokenizer.StringMessage;

/**
 * This class handles receiving and sending messages from and to the client.
 * It is individual for each client.
 */
class ConnectionHandler implements Runnable {
	
	private BufferedReader in;
	private PrintWriter out;
	private Socket clientSocket;
	private ServerProtocol protocol;
	
	
	public ConnectionHandler(Socket acceptedSocket, ServerProtocol p)
	{
		in = null;
		out = null;
		clientSocket = acceptedSocket;
		protocol = p;
		System.out.println("Accepted connection from client!");
		System.out.println("The client is from: " + acceptedSocket.getInetAddress() + ":" + acceptedSocket.getPort());
	}
	
	public void run()
	{
		
		try {
			initialize();
		}
		catch (IOException e) {
			System.out.println("Error in initializing I/O");
		}

		try {
			process();
		} 
		catch (IOException e) {
			System.out.println("Error in I/O");
		} 
		
		System.out.println("Connection closed - bye bye...");
		close();

	}
/**
* Read a message from the inputstream a send it to the protocol for processing
*/
	public void process() throws IOException
	{
		String msg;
		boolean shouldEnd = false;
		while ((msg = in.readLine()) != null && !shouldEnd){
			System.out.println("Received \"" + msg + "\" from client");
			protocol.processMessage(msg, response->{
				out.println(response);
			});
			shouldEnd = protocol.isEnd(msg);
		}
		
	}
	
/**
* Initialize I/O
*/
	public void initialize() throws IOException
	{
		in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),"UTF-8"));
		out = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream(),"UTF-8"), true);
		System.out.println("I/O initialized");
	}
	
/**
* Closes the connection
*/
	public void close()
	{
		try {
			if (in != null)
			{
				in.close();
			}
			if (out != null)
			{
				out.close();
			}
			
			clientSocket.close();
		}
		catch (IOException e)
		{
			System.out.println("Exception in closing I/O");
		}
	}
	
}
