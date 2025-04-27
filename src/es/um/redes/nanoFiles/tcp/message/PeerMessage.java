package es.um.redes.nanoFiles.tcp.message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import es.um.redes.nanoFiles.util.FileInfo;

public class PeerMessage {




	private byte opcode;


	/*
	 * TODO: (Boletín MensajesBinarios) Añadir atributos u otros constructores
	 * específicos para crear mensajes con otros campos, según sea necesario
	 * 
	 */
	
	private long position;
	private long chunkSize; 
	
	private String fileName;
	private long fileSize;
	private String fileHash;
	private byte[] fileData;
	


	public PeerMessage() {
		opcode = PeerMessageOps.OPCODE_INVALID_CODE;
	}

	public PeerMessage(byte op) {
		opcode = op;
	}

	/*
	 * TODO: (Boletín MensajesBinarios) Crear métodos getter y setter para obtener
	 * los valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	public byte getOpcode() {
		return opcode;
	}
	
	public void setOpcode(byte op) {
		this.opcode = op;
	}
	
	
	
	public long getPosition() {
		return this.position;
	}
	public long getChunkSize() {
		return this.chunkSize; 
	}
	public void setPosition(long p1) {
		this.position = p1;
	}
	
	public void setChunkSize(long p2) {
		this.chunkSize = p2;
	}
	
	
	
	
	public String getFileName() {
		return this.fileName;
	}
	
	public void setFileName(String value) {
		this.fileName = value;
	}
	
	
	

	public String getFileHash() {
		return this.fileHash;
	}
	
	public void setFileHash(String value) {
		this.fileHash = value;
	}
	
	
	
	
	public byte[] getFileData() {
		return this.fileData;
	}
	
	public void setFileData(byte[] value) {
		this.fileData = value;
	}

	
	public long getFileSize() {
		return this.fileSize;
	}
	
	public void setFileSize(long value) {
		this.fileSize = value;
	}



	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * DirMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */
	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
		/*
		 * TODO: (Boletín MensajesBinarios) En función del tipo de mensaje, leer del
		 * socket a través del "dis" el resto de campos para ir extrayendo con los
		 * valores y establecer los atributos del un objeto DirMessage que contendrá
		 * toda la información del mensaje, y que será devuelto como resultado. NOTA:
		 * Usar dis.readFully para leer un array de bytes, dis.readInt para leer un
		 * entero, etc.
		 */
		PeerMessage message = new PeerMessage();
		byte opcode = dis.readByte();
		message.setOpcode(opcode);
		switch (opcode) {
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			break;
		case PeerMessageOps.OPCODE_DOWNLOAD_CHUNK: 
			message.setFileName(dis.readUTF());
			message.setPosition(dis.readLong());
			message.setChunkSize(dis.readLong());
			break;
		case PeerMessageOps.OPCODE_CHECK_FILE:
			message.setFileName(dis.readUTF());
			break;
		case PeerMessageOps.OPCODE_FILE_INFO:
			message.setFileSize(dis.readLong());
			message.setFileHash(dis.readUTF());
			break; 

		default:
			System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: "
					+ PeerMessageOps.opcodeToOperation(opcode));
			System.exit(-1);
		}
		return message;
	}

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*
		 * TODO (Boletín MensajesBinarios): Escribir los bytes en los que se codifica el
		 * mensaje en el socket a través del "dos", teniendo en cuenta opcode del
		 * mensaje del que se trata y los campos relevantes en cada caso. NOTA: Usar
		 * dos.write para leer un array de bytes, dos.writeInt para escribir un entero,
		 * etc.
		 */

		dos.writeByte(opcode);
		switch (opcode) {
		
		case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
			break;
		case PeerMessageOps.OPCODE_DOWNLOAD_CHUNK: 
			dos.writeUTF(fileName);
			dos.writeLong(position); 
			dos.writeLong(chunkSize); 
			break;
		case PeerMessageOps.OPCODE_CHECK_FILE:
			dos.writeUTF(fileName);
			break;
		case PeerMessageOps.OPCODE_FILE_INFO:
			dos.writeLong(fileSize); 
			dos.writeUTF(fileHash); 
			break; 




		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}

	
	public String toString() {
		String s = "";
		s += opcode;
		switch (opcode) {
		
			case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
				break;
			case PeerMessageOps.OPCODE_DOWNLOAD_CHUNK: 
				s += fileName;
				s += position; 
				s += chunkSize; 
				break;
			case PeerMessageOps.OPCODE_CHECK_FILE:
				s += fileName;
				break;
			case PeerMessageOps.OPCODE_FILE_INFO:
				s += fileSize; 
				s += fileHash; 
			break;
			
			}
		
		
			return s;
		}

		

}
