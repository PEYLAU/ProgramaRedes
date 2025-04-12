package es.um.redes.nanoFiles.application;

import es.um.redes.nanoFiles.logic.NFController;
import es.um.redes.nanoFiles.util.FileDatabase;

public class NanoFiles {

	public static final String DEFAULT_SHARED_DIRNAME = "nf-shared";
	/**
	 * Identificador único para cada grupo de prácticas. TODO: Establecer a un valor
	 * que combine los DNIs de ambos miembros del grupo de prácticas.
	 */
	public static final String PROTOCOL_ID = "123456789A";
	private static final String DEFAULT_DIRECTORY_HOSTNAME = "localhost";
	public static String sharedDirname = DEFAULT_SHARED_DIRNAME;
	public static FileDatabase db;
	/**
	 * Flag para pruebas iniciales con UDP, desactivado una vez que la comunicación
	 * cliente-directorio está implementada y probada.
	 */
	public static boolean testModeUDP = false;
	/**
	 * Flag para pruebas iniciales con TCP, desactivado una vez que la comunicación
	 * cliente-servidor de ficheros está implementada y probada.
	 */
	public static boolean testModeTCP = false;

	public static void main(String[] args) {
		// Comprobamos los argumentos
		if (args.length > 1) {
			System.out.println("Usage: java -jar NanoFiles.jar [<local_shared_directory>]");
			return;
		} else if (args.length == 1) {
			// Establecemos el directorio compartido especificado
			sharedDirname = args[0];
		}

		db = new FileDatabase(sharedDirname);

		// Creamos el controlador que aceptará y procesará los comandos
		NFController controller = new NFController(DEFAULT_DIRECTORY_HOSTNAME);

		if (testModeUDP) {
			controller.testCommunication();
		} else {
			// Entramos en el bucle para pedirle al controlador que procese comandos del
			// shell hasta que el usuario quiera salir de la aplicación.
			do {
				controller.readGeneralCommandFromShell();
				controller.processCommand();
			} while (controller.shouldQuit() == false);
			System.out.println("Bye.");
		}
	}
}
