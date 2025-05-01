package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileDigest;
import es.um.redes.nanoFiles.util.FileInfo;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector {
	private Socket socket;
	private InetSocketAddress serverAddr;
	private DataInputStream dis;
	private DataOutputStream dos;
	
	private long fileSize = -1;
	private String fileHash = null;
	private String trueFileName = null;



	public NFConnector(InetSocketAddress fserverAddr) throws UnknownHostException, IOException {
		serverAddr = fserverAddr;
		
		if(serverAddr == null) {
			throw new UnknownHostException();
		}
		/*
		 * TODO: (Boletín SocketsTCP) Se crea el socket a partir de la dirección del
		 * servidor (IP, puerto). La creación exitosa del socket significa que la
		 * conexión TCP ha sido establecida.
		 */
		
		
		socket = new Socket(serverAddr.getAddress(), serverAddr.getPort());
		/*
		 * TODO: (Boletín SocketsTCP) Se crean los DataInputStream/DataOutputStream a
		 * partir de los streams de entrada/salida del socket creado. Se usarán para
		 * enviar (dos) y recibir (dis) datos del servidor.
		 */
		
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
		if(dis == null || dos == null) {
			throw new IOException();
		}

	}
	
	
	
	
	public void test() {
		/*
		 * TODO: (Boletín SocketsTCP) Enviar entero cualquiera a través del socket y
		 * después recibir otro entero, comprobando que se trata del mismo valor.
		 
		
		try {
			PeerMessage message = new PeerMessage(PeerMessageOps.OPCODE_FILE_INFO);
			message.setParametro1(500);
			message.setParametro2("hola");
			message.writeMessageToOutputStream(dos);
			PeerMessage respuesta = PeerMessage.readMessageFromInputStream(dis);
			System.out.println((int) respuesta.getOpcode());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}
	
	
	public boolean getFileInfo(String fileName) {
		try {
			PeerMessage message = new PeerMessage(PeerMessageOps.OPCODE_CHECK_FILE);
			message.setFileName(fileName);
			message.writeMessageToOutputStream(dos);
			PeerMessage respuesta = PeerMessage.readMessageFromInputStream(dis);
			if(respuesta.getOpcode() == PeerMessageOps.OPCODE_FILE_NOT_FOUND) {
				System.err.println("File could not be found when checking size/hash");
				return false;
			}
			else if(respuesta.getOpcode() == PeerMessageOps.OPCODE_FILE_INFO) {
				this.fileSize = respuesta.getFileSize();
				this.fileHash = respuesta.getFileHash();
				this.trueFileName = respuesta.getFileName();
			}
			else {
				return false;
			}
		}catch(IOException e) {
			System.err.println("IOException when asking for fileInfo");
			return false;
		}
		
		return true;
	}

	
	public String getFileHash(String fileName) {
		if(this.fileHash == null) {
			boolean success = getFileInfo(fileName);
			if(!success) {
				System.err.println("Could not obtain Hash");
				return null;
			}
		}
		
		
		return this.fileHash;
	}
	
	public long getFileSize(String fileName) {
		if(this.fileSize == -1) {
			boolean success = getFileInfo(fileName);
			if(!success) {
				System.err.println("Could not obtain Size");
				return -1;
			}
		}
		
		return this.fileSize;
	}
	
	public String getFileTrueName(String fileName) {
		if(this.trueFileName == null) {
			boolean success = getFileInfo(fileName);
			if(!success) {
				System.err.println("Could not obtain FileName");
				return null;
			}
		}
		
		return this.trueFileName;
	}
	
	public byte[] getFileChunk(String fileName, long inPos, int chunkSize) {
		try {
			PeerMessage message = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_CHUNK);
			
			
			
			message.setFileName(fileName);
			message.setPosition(inPos);
			message.setChunkSize(chunkSize);
			System.out.println("Downloading " + chunkSize + " bytes from file " + fileName + " in position " + inPos);
			message.writeMessageToOutputStream(dos);
			PeerMessage respuesta = PeerMessage.readMessageFromInputStream(dis);
			if(respuesta.getOpcode() == PeerMessageOps.OPCODE_FILE_NOT_FOUND) {
				System.err.println("File could not be found when checking size and hash");
				return null;
			}
			else if(respuesta.getOpcode() == PeerMessageOps.OPCODE_CHUNK_DOWNLOADED) {
				return respuesta.getFileData();
			}
			return null;
			
		}catch(IOException e) {
			System.err.println("IOException when asking for fileHash");
			return null;
		}
	}


	
	public boolean uploadFile(FileInfo file, String serverToUpload) {
		
	}
	


	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}

}
