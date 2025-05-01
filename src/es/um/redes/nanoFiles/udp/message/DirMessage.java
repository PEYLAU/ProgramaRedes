package es.um.redes.nanoFiles.udp.message;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.LinkedList;

import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; // 65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; // Define el delimitador
	private static final char END_LINE = '\n'; // Define el carácter de fin de línea

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";
	private static final String FIELDNAME_PROTOCOL= "protocolid";
	private static final String FIELDNAME_FILE_NUM = "filenum";
	private static final String FIELDNAME_FILE_HASH = "filehash";
	private static final String FIELDNAME_FILE_NAME = "filename";
	private static final String FIELDNAME_FILE_PATH = "filepath";
	private static final String FIELDNAME_FILE_SIZE = "filesize";
	private static final String FIELDNAME_FILE_ADDRESS = "fileaddress";
	private static final String FIELDNAME_FILE_PORT = "fileport";
	private static final String FIELDNAME_ADDRESS_NUM = "addressnum";
	private static final String FIELDNAME_IPADDRESS = "ipaddress";
	private static final String FIELDNAME_PORT = "port";
	private static final String FIELDNAME_SUBSTRING = "filenamesubstring";
	/*
	 * TODO: (Boletín MensajesASCII) Definir de manera simbólica los nombres de
	 * todos los campos que pueden aparecer en los mensajes de este protocolo
	 * (formato campo:valor)
	 */


	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	
	/**
	 * Identificador de protocolo usado, para comprobar compatibilidad del directorio.
	 */
	private String protocolId;
	private int fileNum;
	private LinkedList<FileInfo> filelist = new LinkedList<FileInfo>();
	private String filenameSubstring; 
	private int addressNum; 
	private LinkedList<InetSocketAddress> addressList = new LinkedList<InetSocketAddress>(); 
	/*
	 * TODO: (Boletín MensajesASCII) Crear un atributo correspondiente a cada uno de
	 * los campos de los diferentes mensajes de este protocolo.
	 */


	public DirMessage() {
		operation = DirMessageOps.OPERATION_INVALID;
	}


	public DirMessage(String op) {
		operation = op;
	}

	/*
	 * TODO: (Boletín MensajesASCII) Crear diferentes constructores adecuados para
	 * construir mensajes de diferentes tipos con sus correspondientes argumentos
	 * (campos del mensaje)
	 */

	


	public String getOperation() {
		return operation;
	}
	
	
	
	/*
	 * TODO: (Boletín MensajesASCII) Crear métodos getter y setter para obtener los
	 * valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	public void setProtocolID(String protocolIdent) {
		if (!operation.equals(DirMessageOps.OPERATION_PING)) {
			throw new RuntimeException(
					"DirMessage: setProtocolId called for message of unexpected type (" + operation + ")");
		}
		protocolId = protocolIdent;
	}

	public String getProtocolId() {

		return protocolId;
	}
	
	public void setFilenameSubstring(String subString) {
		if (!operation.equals(DirMessageOps.OPERATION_REQUEST_SERVER)) {
			throw new RuntimeException(
					"DirMessage: setFilenameSubstring called for message of unexpected type (" + operation + ")");
		}
		filenameSubstring = subString; 
	}
	
	public String getFilenameSubstring() {

		return filenameSubstring;
	}	
	
	public Integer getFileNum() {
		return fileNum;
	}
	
	public void setFileNum(int value) {
		if (!operation.equals(DirMessageOps.OPERATION_SEND_FILES) && !operation.equals(DirMessageOps.OPERATION_UNREGISTER_FILES) && !operation.equals(DirMessageOps.OPERATION_REGISTER_SERVER)) {
			throw new RuntimeException(
					"DirMessage: setFileNum called for message of unexpected type (" + operation + ")");
		}
		fileNum = value;
	}
	
	public Integer getAddressNum() {
		return addressNum;
	}
	
	public void setAddressNum(int value) {
		if (!operation.equals(DirMessageOps.OPERATION_SERVER_LIST)) {
			throw new RuntimeException(
					"DirMessage: setAddressNum called for message of unexpected type (" + operation + ")");
		}
		addressNum = value;
	}
	
	
	public void addFileInfo(FileInfo f) {
		filelist.add(f);
	}
	
	public LinkedList<FileInfo> getFileList(){
		return filelist;
	}
	
	public FileInfo getFileFromPos(int i) {
		return filelist.get(i);
	}

	public void addAddress(InetSocketAddress addr) {
		addressList.add(addr);
	}
	
	public LinkedList<InetSocketAddress> getAddressList(){
		return addressList;
	}
	
	public InetSocketAddress getAddressFromPos(int i) {
		return addressList.get(i);
	}




	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*
		 * TODO: (Boletín MensajesASCII) Usar un bucle para parsear el mensaje línea a
		 * línea, extrayendo para cada línea el nombre del campo y el valor, usando el
		 * delimitador DELIMITER, y guardarlo en variables locales.
		 */

		// System.out.println("DirMessage read from socket:");
		// System.out.println(message);
		String[] lines = message.split(END_LINE+"");
		// Local variables to save data during parsing
		DirMessage m = null;
		FileInfo f = null;
		String currHash = null;
		String currName = null;
		String currPath = null;
		String currSize = null;
		String currAddress = null;
		String currPort = null;


		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); // Posición del delimitador
			if(idx == -1) {
				continue;
			}
			String fieldName = line.substring(0, idx).toLowerCase(); // minúsculas
			String value = line.substring(idx + 1).trim();

			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null);
				m = new DirMessage(value);
				break;
			}
			case FIELDNAME_PROTOCOL:{
				m.setProtocolID(value);
				break;
			}
			case FIELDNAME_FILE_NUM:{
				m.setFileNum(Integer.parseInt(value));
				break;
			}
			case FIELDNAME_FILE_HASH:{
				currHash = value;
				break;
			}
			case FIELDNAME_FILE_NAME:{
				currName = value;
				break;
			}
			case FIELDNAME_FILE_PATH:{
				currPath = value;
				break;
			}
			case FIELDNAME_FILE_SIZE:{
				currSize = value;
				f = new FileInfo(currHash, currName, Long.parseLong(currSize), currPath);
				m.addFileInfo(f);
				break;
			}
			case FIELDNAME_FILE_ADDRESS:{
				currAddress = value;
				break;
			}
			case FIELDNAME_FILE_PORT: {
				currPort = value;
				f.fileAddress.add(new InetSocketAddress(currAddress.substring(1), Integer.parseInt(currPort)));
				break;
			}
			case FIELDNAME_SUBSTRING:{
				m.setFilenameSubstring(value); 
				break; 
			}
			case FIELDNAME_ADDRESS_NUM: {
				m.setAddressNum(Integer.parseInt(value));
				break;
			}
			case FIELDNAME_IPADDRESS: {
				currAddress = value;
				break;
			}
			case FIELDNAME_PORT: {
				currPort = value;
				m.addAddress(new InetSocketAddress(currAddress.substring(1), Integer.parseInt(currPort)));
				break;
			}
			
			default:
				System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
				System.err.println("Message was:\n" + message);
				System.exit(-1);
			}
			
		}




		return m;
	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); // Construimos el campo
		/*
		 * TODO: (Boletín MensajesASCII) En función de la operación del mensaje, crear
		 * una cadena la operación y concatenar el resto de campos necesarios usando los
		 * valores de los atributos del objeto.
		 */
		
		switch(operation) {
		case DirMessageOps.OPERATION_PING:{
			sb.append(FIELDNAME_PROTOCOL + DELIMITER + protocolId + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_REGISTER_SERVER:
		case DirMessageOps.OPERATION_UNREGISTER_FILES:
		case DirMessageOps.OPERATION_SEND_FILES:{
			sb.append(FIELDNAME_FILE_NUM + DELIMITER + fileNum + END_LINE);
			for(FileInfo f : filelist) {
				sb.append(FIELDNAME_FILE_HASH + DELIMITER + f.fileHash + END_LINE);
				sb.append(FIELDNAME_FILE_NAME + DELIMITER + f.fileName + END_LINE);
				sb.append(FIELDNAME_FILE_PATH + DELIMITER + f.filePath + END_LINE);
				sb.append(FIELDNAME_FILE_SIZE + DELIMITER + f.fileSize + END_LINE);
				for(InetSocketAddress addr : f.fileAddress) {
					sb.append(FIELDNAME_FILE_ADDRESS + DELIMITER + addr.getAddress() + END_LINE);
					sb.append(FIELDNAME_FILE_PORT + DELIMITER + addr.getPort() + END_LINE);
				}
				
			}
			break;
		}
		
		case DirMessageOps.OPERATION_REQUEST_SERVER: {
			sb.append(FIELDNAME_SUBSTRING + DELIMITER + filenameSubstring + END_LINE); 
			break;
		}
		
		case DirMessageOps.OPERATION_SERVER_LIST: {
			sb.append(FIELDNAME_ADDRESS_NUM + DELIMITER + addressNum + END_LINE);
			for(InetSocketAddress addr : addressList) {
				sb.append(FIELDNAME_IPADDRESS + DELIMITER + addr.getAddress() + END_LINE);
				sb.append(FIELDNAME_PORT + DELIMITER + addr.getPort() + END_LINE);
			}
			
			break;
		}
		
		}


		//sb.append(END_LINE); // Marcamos el final del mensaje
		return sb.toString();
	}

}
