package es.um.redes.nanoFiles.tcp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.net.Socket;

import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;

public class NFServerThread extends Thread {
	/*
	 * TODO: Esta clase modela los hilos que son creados desde NFServer y cada uno
	 * de los cuales simplemente se encarga de invocar a
	 * NFServer.serveFilesToClient con el socket retornado por el método accept
	 * (un socket distinto para "conversar" con un cliente)
	 */
	
	Socket socket;
	
	public NFServerThread(Socket _socket) {
		this.socket = _socket;
	}
	
	public void run() {
		try {
			boolean terminate = false;
			while(!terminate) {
					NFServer.serveFilesToClient(socket);
			}
				
			
			
			
		}catch (EOFException e) {
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
			
		}
		
		
		
	}




}
