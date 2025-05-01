package es.um.redes.nanoFiles.logic;

import java.io.IOException;
import java.net.InetSocketAddress;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.client.DirectoryConnector;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicDir {

	// Conector para enviar y recibir mensajes del directorio
	private DirectoryConnector directoryConnector;

	/**
	 * Construye el controlador encargado de implementar la lógica de los comandos
	 * que requieren interactuar con el servidor de directorio dado a través de la
	 * clase DirectoryConnector.
	 * 
	 * @param directoryHostname el nombre de host/IP en el que se está ejecutando el
	 *                          directorio
	 */
	protected NFControllerLogicDir(String directoryHostname) {
		/*
		 * Crear un objeto DirectoryConnector a partir del parámetro directoryHostname y
		 * guardarlo en el atributo correspondiente para que pueda ser utilizado por el
		 * resto de métodos de esta clase.
		 */
		try {
			directoryConnector = new DirectoryConnector(directoryHostname);
		} catch (IOException e1) {
			System.err.println(
					"* Check your connection, the directory server at " + directoryHostname + " is not available.");
			System.exit(-1);
		}
	}

	/**
	 * Método para comprobar que la comunicación con el directorio es exitosa (se
	 * pueden enviar y recibir datagramas) haciendo uso de la clase
	 * DirectoryConnector
	 * 
	 * @return true si se ha conseguido contactar con el directorio.
	 */
	protected void testCommunicationWithDirectory() {
		assert (NanoFiles.testModeUDP);
		System.out.println(
				"[testMode] Testing communication with directory: " + this.directoryConnector.getDirectoryHostname());
		/*
		 * Utiliza el DirectoryConnector para hacer una prueba de comunicación con el
		 * directorio. Primero testSendAndReceive envía un mensaje "ping" y espera
		 * obtener "welcome" como respuesta. Luego pingDirectoryRaw hace lo mismo que
		 * testSendAndReceive pero enviando además el "protocol ID" para ver si el
		 * directorio es compatible
		 */
		if (directoryConnector.testSendAndReceive()) {
			System.out.println("[testMode] testSendAndReceived - TEST PASSED!");
			/*
			 * (Boletín EstructuraNanoFiles) Test similar al de testSendAndReceive, pero
			 * ampliado para comprobar si el directorio es compatible con el protocol ID,
			 * usando para la comunicación mensajes "en crudo" (sin un formato bien
			 * definido).
			 */
			if (directoryConnector.pingDirectoryRaw()) {
				System.out.println("[testMode] pingDirectoryRaw - SUCCESS!");
			} else {
				System.err.println("[testMode] pingDirectoryRaw - FAILED!");
			}
		} else {
			System.err.println("[testMode] testSendAndReceived - TEST FAILED!");
		}
	}

	/**
	 * Método para comprobar el directorio utiliza un protocolo compatible
	 * 
	 * @return true si se ha conseguido contactar con el directorio.
	 */
	protected boolean ping() {

		/*
		 * (Boletín SocketsUDP) Utilizar el DirectoryConnector para comunicarse con el
		 * directorio y tratar de realizar el "ping", informar por pantalla y devolver
		 * éxito/fracaso de la operación.
		 */
		boolean result = false;
		System.out.println(
				"* Checking if the directory at " + directoryConnector.getDirectoryHostname() + " is available...");
		result = directoryConnector.pingDirectory();
		if (result) {
			System.out.println("* Directory is active and uses compatible protocol " + NanoFiles.PROTOCOL_ID);
		} else {
			System.err.println("* Ping failed");
		}
		return result;
	}

	/**
	 * Método para obtener y mostrar la lista de ficheros que los peer servidores
	 * han publicado al directorio
	 */
	protected void getAndPrintFileList() {
		/*
		 * Obtener la lista de ficheros servidos. Comunicarse con el directorio (a
		 * través del directoryConnector) para obtener la lista de ficheros e imprimirla
		 * por pantalla (método FileInfo.printToSysout). Devolver éxito/fracaso de la
		 * operación.
		 */
		FileInfo[] trackedFiles = directoryConnector.getFileList();
		if(trackedFiles == null) {
			System.err.println("Information of files couldn't be obtained");
		}
		else {
			System.out.println(
					"* These are the files tracked by the directory at " + directoryConnector.getDirectoryHostname());
			FileInfo.printToSysout(trackedFiles);
		}
	}

	/**
	 * Método para registrarse en el directorio como servidor de ficheros en un
	 * puerto determinado y enviar al directorio la lista de ficheros que este peer
	 * servidor comparte con el resto (ver método getAndPrintFileList).
	 * 
	 * @param serverPort El puerto TCP en el que está escuchando el servidor de
	 *                   ficheros.
	 * @param filelist   La lista de ficheros a publicar en el directorio
	 * @return Verdadero si el registro se hace con éxito
	 */
	protected boolean registerFileServer(int serverPort, FileInfo[] filelist) {
		/*
		 * Comunicarse con el directorio (a través del directoryConnector) para enviar
		 * la lista de ficheros servidos por este peer. Los ficheros de la carpeta local
		 * compartida están disponibles en NanoFiles.db). Devolver éxito/fracaso de la
		 * operación.
		 */
		boolean result = false;
		if (this.directoryConnector.registerFileServer(serverPort, filelist)) {
			System.out.println("* File server successfully registered with the directory");
			result = true;
		} else {
			System.err.println("* File server failed to register with the directory");
		}
		return result;
	}

	/**
	 * Método para consultar al directorio las direcciones de socket de los
	 * servidores que tienen un determinado fichero identificado por una subcadena
	 * del nombre.
	 * 
	 * @param filenameSubstring una subcadena del nombre del fichero por el que se
	 *                          pregunta
	 * @return Una lista de direcciones de socket de los servidores que comparten
	 *         dicho fichero, o null si dicha subcadena del nombre no identifica
	 *         ningún fichero concreto (no existe o es una subcadena ambigua)
	 * 
	 */
	protected InetSocketAddress[] getServerAddressesSharingThisFile(String filenameSubstring) {
		/*
		 * Comunicarse con el directorio (a través del directoryConnector) para
		 * preguntar por aquellos servidores que están sirviendo ficheros cuyo nombre
		 * contiene la subcadena dada.
		 * 
		 */
		return directoryConnector.getServersSharingThisFile(filenameSubstring);
	}

	/**
	 * Método para dar de baja a nuestro servidor de ficheros en el directorio.
	 * 
	 * @return Éxito o fracaso de la operación
	 */
	protected boolean unregisterFileServer(int serverPort) {
		/*
		 * Comunicarse con el directorio (a través del directoryConnector) para enviar
		 * la lista de ficheros servidos por este peer. Los ficheros de la carpeta local
		 * compartida están disponibles en NanoFiles.db). Devolver éxito/fracaso de la
		 * operación.
		 */
		boolean result = false;
		if (this.directoryConnector.unregisterFileServer(serverPort)) {
			System.out.println("* File server successfully unregistered with the directory");
			result = true;
		} else {
			System.err.println("* File server failed to unregister with the directory");
		}
		return result;
	}

	protected String getDirectoryHostname() {
		return directoryConnector.getDirectoryHostname();
	}

}
