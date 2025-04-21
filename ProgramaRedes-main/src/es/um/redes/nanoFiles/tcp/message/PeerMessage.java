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
	private long parametro1;
	private Object parametro2; 

	/*
	 * TODO: (Boletín MensajesBinarios) Añadir atributos u otros constructores
	 * específicos para crear mensajes con otros campos, según sea necesario
	 * 
	 */




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
	
	public long getParametro1() {
		return this.parametro1;
	}
	public Object getParametro2() {
		return this.parametro2; 
	}
	public void setParametro1(long p1) {
		this.parametro1 = p1;
	}
	
	public void setParametro2(String p2) {
		this.parametro2 = p2;
	}
	public void setParametro2(int p2) {
		this.parametro2 = p2;
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
			message.setParametro1(dis.readLong());
			message.setParametro2(dis.readInt());
			break;
		case PeerMessageOps.OPCODE_FILE_INFO:
			message.setParametro1(dis.readLong());
			message.setParametro2(dis.readUTF());
			break;
		case PeerMessageOps.OPCODE_CHECK_HASH:
			message.setParametro1(dis.readLong());
			message.setParametro2(dis.readInt());
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
			dos.writeLong(parametro1); 
			dos.writeInt((int) parametro2); 
			break;
		case PeerMessageOps.OPCODE_FILE_INFO:
			dos.writeLong(parametro1); 
			dos.writeUTF((String) parametro2); 
			break;
		case PeerMessageOps.OPCODE_CHECK_HASH:
			dos.writeLong(parametro1); 
			dos.writeInt((int) parametro2); 
			break; 




		default:
			System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
					+ PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}

	
	public String toString() {
		String s = "";
		s += opcode;
		s += parametro1;
		s += parametro2;
		
		return s;
		
	}



}
