package es.um.redes.nanoFiles.logic;

import java.net.InetSocketAddress;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.shell.NFCommands;
import es.um.redes.nanoFiles.shell.NFShell;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFController {
	/**
	 * Diferentes estados del cliente de acuerdo con el autómata
	 */
	private static final byte OFFLINE = 0;
	/*
	 * TODO: (Boletín Autómatas) Añadir más constantes que representen los estados
	 * del autómata del cliente de directorio.
	 */




	/**
	 * Shell para leer comandos de usuario de la entrada estándar
	 */
	private NFShell shell;
	/**
	 * Último comando proporcionado por el usuario
	 */
	private byte currentCommand;

	/**
	 * Objeto controlador encargado de la comunicación con el directorio
	 */
	private NFControllerLogicDir controllerDir;
	/**
	 * Objeto controlador encargado de la comunicación con otros peers (como
	 * servidor o cliente)
	 */
	private NFControllerLogicP2P controllerPeer;

	/**
	 * El estado en que se encuentra este peer (según el autómata). El estado debe
	 * actualizarse cuando se produce un evento (comando) que supone un cambio en el
	 * autómata.
	 */
	private byte currentState;
	/**
	 * Atributos donde se establecen los argumentos pasados a los distintos comandos
	 * del shell. Estos atributos se establecen automáticamente según la orden y se
	 * deben usar para pasar los valores de los parámetros a las funciones invocadas
	 * desde este controlador.
	 */
	private String targetFilenameSubstring; // Nombre del fichero a descargar/subir (download/upload)
	private String downloadLocalFileName; // Nombre con el que se guardará el fichero descargado (download)
	private String uploadToServer; // Servidor al que se subirá el fichero indicado (upload)

	// Constructor
	public NFController(String defaultDirectory) {
		shell = new NFShell();

		String directory = shell.chooseDirectory(defaultDirectory);

		controllerDir = new NFControllerLogicDir(directory);
		controllerPeer = new NFControllerLogicP2P();
		// Estado inicial del autómata
		currentState = OFFLINE;

	}

	/**
	 * Método que procesa los comandos introducidos por un usuario. Se encarga
	 * principalmente de invocar los métodos adecuados de NFControllerLogicDir y
	 * NFControllerLogicP2P según el comando.
	 */
	public void testCommunication() {
		assert (NanoFiles.testModeUDP);
		System.out
				.println("[testMode] Attempting to reach directory server at " + controllerDir.getDirectoryHostname());
		controllerDir.testCommunicationWithDirectory();
		System.out.println("[testMode] Test terminated!");
		return;
	}

	/**
	 * Método que procesa los comandos introducidos por un usuario. Se encarga
	 * principalmente de invocar los métodos adecuados de NFControllerLogicDir y
	 * NFControllerLogicP2P según el comando.
	 */
	public void processCommand() {

		if (!canProcessCommandInCurrentState()) {
			return;
		}
		/*
		 * En función del comando, invocar los métodos adecuados de NFControllerLogicDir
		 * y NFControllerLogicP2P, ya que son estas dos clases las que implementan
		 * realmente la lógica de cada comando y procesan la información recibida
		 * mediante la comunicación con el directorio u otros pares de NanoFiles
		 * (imprimir por pantalla el resultado de la acción y los datos recibidos,
		 * etc.).
		 */
		boolean commandSucceeded = false;
		switch (currentCommand) {
		case NFCommands.COM_MYFILES:
			showMyLocalFiles(); // Muestra los ficheros en el directorio local compartido
			break;
		case NFCommands.COM_PING:
			/*
			 * Pedir al controllerDir enviar un "ping" al directorio, para comprobar que
			 * está activo y disponible, y comprobar que es compatible.
			 */
			commandSucceeded = controllerDir.ping();
			break;
		case NFCommands.COM_FILELIST:
			/*
			 * Pedir al controllerDir que obtenga del directorio la lista de ficheros que
			 * hay publicados (los ficheros que otros peers están sirviendo), y la imprima
			 * por pantalla (método getAndPrintFileList)
			 */
			controllerDir.getAndPrintFileList();
			break;
		case NFCommands.COM_SERVE:
			/*
			 * Pedir al controllerPeer que lance un servidor de ficheros. Si el servidor se
			 * ha podido iniciar correctamente, pedir al controllerDir darnos de alta como
			 * servidor de ficheros en el directorio, indicando el puerto en el que nuestro
			 * servidor escucha conexiones de otros peers así como la lista de ficheros
			 * disponibles.
			 */
			if (NanoFiles.testModeTCP) {
				controllerPeer.testTCPServer();
			} else {
				boolean serverRunning = controllerPeer.startFileServer();
				if (serverRunning) {
					commandSucceeded = controllerDir.registerFileServer(controllerPeer.getServerPort(),
							NanoFiles.db.getFiles());
				} else {
					System.err.println("Cannot start file server");
				}
			}
			break;
		case NFCommands.COM_DOWNLOAD:
			/*
			 * Pedir al controllerDir que obtenga del directorio la lista de servidores que
			 * comparten el fichero cuyo nombre contenga la subcadena indicada (1er
			 * argumento pasado al comando). Si existen servidores que comparten ficheros
			 * con dicho nombre, pedir al controllerPeer que descargue el fichero indicado
			 * de los servidores obtenidos, y lo guarde con el nombre indicado en
			 * downloadLocalFileName (2º argumento)
			 */
			if (NanoFiles.testModeTCP) {
				controllerPeer.testTCPClient();
			} else {
				InetSocketAddress[] serverAddressList = controllerDir
						.getServerAddressesSharingThisFile(targetFilenameSubstring);
				commandSucceeded = controllerPeer.downloadFileFromServers(serverAddressList, targetFilenameSubstring,
						downloadLocalFileName);
			}
			break;
		case NFCommands.COM_QUIT:
			/*
			 * Pedir al controllerPeer que pare el servidor en segundo plano (método método
			 * stopBackgroundFileServer). A continuación, pedir al controllerDir que
			 * solicite al directorio darnos de baja como servidor de ficheros (método
			 * unregisterFileServer).
			 */
			if (controllerPeer.serving()) {
				controllerPeer.stopFileServer();
				commandSucceeded = controllerDir.unregisterFileServer(controllerPeer.getServerPort());
			}
			break;
		case NFCommands.COM_UPLOAD:
			/*
			 * Pedir al controllerPeer que envíe un fichero a un servidor de ficheros.
			 * Localiza el fichero a enviar de entre los disponibles en NanoFiles.db, a
			 * partir de la subcadena del nombre proporcionada.
			 */
			FileInfo[] matchingFiles = FileInfo.lookupFilenameSubstring(NanoFiles.db.getFiles(),
					targetFilenameSubstring);
			if (matchingFiles.length == 1) {
				commandSucceeded = controllerPeer.uploadFileToServer(matchingFiles[0], uploadToServer);
			} else if (matchingFiles.length == 0) {
				System.err.println("Cannot locate file to upload! No matching files found");
			} else {
				System.err.println("Ambiguos filename substring! Candidate files are:");
				FileInfo.printToSysout(matchingFiles);
			}
			break;
		default:
		}

		updateCurrentState(commandSucceeded);
	}

	/**
	 * Método que comprueba si se puede procesar un comando introducidos por un
	 * usuario, en función del estado del autómata en el que nos encontramos.
	 */
	private boolean canProcessCommandInCurrentState() {
		/*
		 * TODO: (Boletín Autómatas) Para cada comando tecleado en el shell
		 * (currentCommand), comprobar "currentState" para ver si dicho comando es
		 * válido según el estado actual del autómata, ya que no todos los comandos
		 * serán válidos en cualquier estado. Este método NO debe modificar
		 * clientStatus.
		 */
		boolean commandAllowed = true;
		switch (currentCommand) {
		case NFCommands.COM_MYFILES: {
			commandAllowed = true;
			break;
		}
		default:
			// System.err.println("ERROR: undefined behaviour for " + currentCommand + "
			// command!");
		}
		return commandAllowed;
	}

	private void updateCurrentState(boolean success) {
		/*
		 * TODO: (Boletín Autómatas) Si el comando ha sido procesado con éxito, debemos
		 * actualizar currentState de acuerdo con el autómata diseñado para pasar al
		 * siguiente estado y así permitir unos u otros comandos en cada caso.
		 */
		if (!success) {
			return;
		}
		switch (currentCommand) {
		default:
		}

	}

	private void showMyLocalFiles() {
		System.out.println("List of files in local folder:");
		FileInfo.printToSysout(NanoFiles.db.getFiles());
	}

	/**
	 * Método que comprueba si el usuario ha introducido el comando para salir de la
	 * aplicación
	 */
	public boolean shouldQuit() {
		return currentCommand == NFCommands.COM_QUIT;
	}

	/**
	 * Establece el comando actual
	 * 
	 * @param command el comando tecleado en el shell
	 */
	private void setCurrentCommand(byte command) {
		currentCommand = command;
	}

	/**
	 * Registra en atributos internos los posibles parámetros del comando tecleado
	 * por el usuario.
	 */
	private void setCurrentCommandArguments(String[] args) {
		switch (currentCommand) {
		case NFCommands.COM_DOWNLOAD:
			targetFilenameSubstring = args[0];
			downloadLocalFileName = args[1];
			break;
		case NFCommands.COM_UPLOAD:
			targetFilenameSubstring = args[0];
			uploadToServer = args[1];
			break;
		default:
		}
	}

	/**
	 * Método para leer un comando general
	 */
	public void readGeneralCommandFromShell() {
		// Pedimos el comando al shell
		shell.readGeneralCommand();
		// Establecemos que el comando actual es el que ha obtenido el shell
		setCurrentCommand(shell.getCommand());
		// Analizamos los posibles parámetros asociados al comando
		setCurrentCommandArguments(shell.getCommandArguments());
	}

}
