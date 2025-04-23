package es.um.redes.nanoFiles.udp.message;

public class DirMessageOps {

	/*
	 * TODO: (Boletín MensajesASCII) Añadir aquí todas las constantes que definen
	 * los diferentes tipos de mensajes del protocolo de comunicación con el
	 * directorio (valores posibles del campo "operation").
	 */
	public static final String OPERATION_INVALID = "invalid_operation";
	public static final String OPERATION_PING = "ping";
	public static final String OPERATION_PING_OK = "pingok";
	public static final String OPERATION_PING_BAD = "pingbad";
	public static final String OPERATION_SEND_FILES = "sendfiles";
	public static final String OPERATION_SERVER_REGISTERED = "serverregistered";
	public static final String OPERATION_REQUEST_FILELIST = "requestfilelist"; 
	
}
