package es.um.redes.nanoFiles.tcp.server;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileDatabase;
import es.um.redes.nanoFiles.util.FileDigest;
import es.um.redes.nanoFiles.util.FileInfo;




public class NFServer extends Thread implements Runnable {

	public static final int PORT = 10000;



	private ServerSocket serverSocket = null;
	private boolean stop = false;
	
	public NFServer() throws IOException {
		/*
		 * TODO: (Boletín SocketsTCP) Crear una direción de socket a partir del puerto
		 * especificado (PORT)
		 */
		
		InetSocketAddress serverscktAddress = new InetSocketAddress(PORT);
		/*
		 * TODO: (Boletín SocketsTCP) Crear un socket servidor y ligarlo a la dirección
		 * de socket anterior
		 */
		
		serverSocket = new ServerSocket();
		serverSocket.bind(serverscktAddress);
		
		
		



	}

	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación.
	 * 
	 */
	public void test() {
		if (serverSocket == null || !serverSocket.isBound()) {
			System.err.println(
					"[fileServerTestMode] Failed to run file server, server socket is null or not bound to any port");
			return;
		} else {
			System.out
					.println("[fileServerTestMode] NFServer running on " + serverSocket.getLocalSocketAddress() + ".");
		}

		while (true) {
			/*
			 * TODO: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
			 * otros peers que soliciten descargar ficheros.
			 */
			
			boolean connectionOk = false;
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				connectionOk = true;
			}catch(IOException e) {
				e.printStackTrace();
			}
			
			/*
			 * TODO: (Boletín SocketsTCP) Tras aceptar la conexión con un peer cliente, la
			 * comunicación con dicho cliente para servir los ficheros solicitados se debe
			 * implementar en el método serveFilesToClient, al cual hay que pasarle el
			 * socket devuelto por accept.
			 */
			
			if(connectionOk) {
				serveFilesToClient(socket);
			}
			

		}
	}

	/**
	 * Método que ejecuta el hilo principal del servidor en segundo plano, esperando
	 * conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		/*
		 * TODO: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
		 * otros peers que soliciten descargar ficheros
		 */
		while(!stop) {
			try {
				Socket socket = serverSocket.accept();
				
				NFServerThread serverThread = new NFServerThread(socket);
				serverThread.start();
				
				if(stop) {
					serverSocket.close();
				}
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		
		/*
		 * TODO: (Boletín SocketsTCP) Al establecerse la conexión con un peer, la
		 * comunicación con dicho cliente se hace en el método
		 * serveFilesToClient(socket), al cual hay que pasarle el socket devuelto por
		 * accept
		 */
		/*
		 * TODO: (Boletín TCPConcurrente) Crear un hilo nuevo de la clase
		 * NFServerThread, que llevará a cabo la comunicación con el cliente que se
		 * acaba de conectar, mientras este hilo vuelve a quedar a la escucha de
		 * conexiones de nuevos clientes (para soportar múltiples clientes). Si este
		 * hilo es el que se encarga de atender al cliente conectado, no podremos tener
		 * más de un cliente conectado a este servidor.
		 */




	}
	/*
	 * TODO: (Boletín SocketsTCP) Añadir métodos a esta clase para: 1) Arrancar el
	 * servidor en un hilo nuevo que se ejecutará en segundo plano 2) Detener el
	 * servidor (stopserver) 3) Obtener el puerto de escucha del servidor etc.
	 */

	
	public void stopServer() {
		stop = true;
	}
	
	public int getServerPort() {
		return this.serverSocket.getLocalPort();
	}


	/**
	 * Método de clase que implementa el extremo del servidor del protocolo de
	 * transferencia de ficheros entre pares.
	 * 
	 * @param socket El socket para la comunicación con un cliente que desea
	 *               descargar ficheros.
	 */
	public static void serveFilesToClient(Socket socket) {
		/*
		 * TODO: (Boletín SocketsTCP) Crear dis/dos a partir del socket
		 */
		
		try {
			DataInputStream dis = new DataInputStream(socket.getInputStream());
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			PeerMessage responseToServer = null;
			while(true) {
				PeerMessage messageFromClient = PeerMessage.readMessageFromInputStream(dis);
				switch(messageFromClient.getOpcode()) {
					case(PeerMessageOps.OPCODE_CHECK_FILE): {
						
						

						FileInfo file = FileInfo.lookupFilenameSubstring(NanoFiles.db.getFiles(), (String) messageFromClient.getFileName())[0];
					
						if(file == null) {
							responseToServer = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
						}
						else {
							responseToServer = new PeerMessage(PeerMessageOps.OPCODE_FILE_INFO);
							responseToServer.setFileSize(file.fileSize);
							responseToServer.setFileHash(file.fileHash);
							responseToServer.setFileName(file.fileName);
							
						}
					
						break;
					}
					case(PeerMessageOps.OPCODE_DOWNLOAD_CHUNK):{
						FileInfo file = FileInfo.lookupFilenameSubstring(NanoFiles.db.getFiles(), (String) messageFromClient.getFileName())[0];
						
						long pos = messageFromClient.getPosition();
						int size = messageFromClient.getChunkSize();
						
						
						
						File f = new File(NanoFiles.db.lookupFilePath(file.fileHash));
						if(!f.exists()) {
							responseToServer = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
						}
						else {
							System.out.println("Sending " + size + " bytes from file " + f.getName() + " in position " + pos);
							RandomAccessFile archivo = new RandomAccessFile(f, "r");
							archivo.seek(pos);
							
							byte[] datos = new byte[size];
							
							
							archivo.readFully(datos);
							
							archivo.close();
							
							responseToServer = new PeerMessage(PeerMessageOps.OPCODE_CHUNK_DOWNLOADED);
							responseToServer.setFileData(datos);
							responseToServer.setChunkSize(size);
						}
						
						
						break;
					}
				
				
				}
				
			
				responseToServer.writeMessageToOutputStream(dos);
				
			}
		}catch(FileNotFoundException f){
			System.err.println("Error, file doesn't exist");
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		/*
		 * TODO: (Boletín SocketsTCP) Mientras el cliente esté conectado, leer mensajes
		 * de socket, convertirlo a un objeto PeerMessage y luego actuar en función del
		 * tipo de mensaje recibido, enviando los correspondientes mensajes de
		 * respuesta.
		 */
		
		
		/*
		 * TODO: (Boletín SocketsTCP) Para servir un fichero, hay que localizarlo a
		 * partir de su hash (o subcadena) en nuestra base de datos de ficheros
		 * compartidos. Los ficheros compartidos se pueden obtener con
		 * NanoFiles.db.getFiles(). Los métodos lookupHashSubstring y
		 * lookupFilenameSubstring de la clase FileInfo son útiles para buscar ficheros
		 * coincidentes con una subcadena dada del hash o del nombre del fichero. El
		 * método lookupFilePath() de FileDatabase devuelve la ruta al fichero a partir
		 * de su hash completo.
		 */



	}




}