package es.um.redes.nanoFiles.shell;

public class NFCommands {
	/**
	 * Códigos para todos los comandos soportados por el shell
	 */
	public static final byte COM_INVALID = 0;
	public static final byte COM_QUIT = 1;
	public static final byte COM_PING = 2;
	public static final byte COM_FILELIST = 4;
	public static final byte COM_MYFILES = 6;
	public static final byte COM_SERVE = 11;
	public static final byte COM_DOWNLOAD = 25;
	public static final byte COM_UPLOAD = 30;
	public static final byte COM_HELP = 50;
	public static final byte COM_SOCKET_IN = 100;


	
	/**
	 * Códigos de los comandos válidos que puede
	 * introducir el usuario del shell. El orden
	 * es importante para relacionarlos con la cadena
	 * que debe introducir el usuario y con la ayuda
	 */
	private static final Byte[] _valid_user_commands = { 
		COM_QUIT,
		COM_PING,
		COM_FILELIST,
		COM_MYFILES,
		COM_SERVE,
		COM_DOWNLOAD,
		COM_UPLOAD,
		COM_HELP,
		COM_SOCKET_IN
		};

	/**
	 * cadena exacta de cada orden
	 */
	private static final String[] _valid_user_commands_str = {
			"quit",
			"ping",
			"filelist",
			"myfiles",
			"serve",
			"download",
			"upload",
			"help"
		};

	/**
	 * Mensaje de ayuda para cada orden
	 */
	private static final String[] _valid_user_commands_help = {
			"quit the application",
			"ping directory to check protocol compatibility",
			"show list of files tracked by the directory",
			"show contents of local folder (files that may be served)",
			"run file server and publish served files to directory",
			"download file from all available server(s)",
			"upload file to server",
			"shows this information"
			};

	/**
	 * Transforma una cadena introducida en el código de comando correspondiente
	 */
	public static byte stringToCommand(String comStr) {
		//Busca entre los comandos si es válido y devuelve su código
		for (int i = 0;
		i < _valid_user_commands_str.length; i++) {
			if (_valid_user_commands_str[i].equalsIgnoreCase(comStr)) {
				return _valid_user_commands[i];
			}
		}
		//Si no se corresponde con ninguna cadena entonces devuelve el código de comando no válido
		return COM_INVALID;
	}

	public static String commandToString(byte command) {
		for (int i = 0;
		i < _valid_user_commands.length; i++) {
			if (_valid_user_commands[i] == command) {
				return _valid_user_commands_str[i];
			}
		}
		return null;
	}

	/**
	 * Imprime la lista de comandos y la ayuda de cada uno
	 */
	public static void printCommandsHelp() {
		System.out.println("List of commands:");
		for (int i = 0; i < _valid_user_commands_str.length; i++) {
			System.out.println(String.format("%1$15s", _valid_user_commands_str[i]) + " -- "
					+ _valid_user_commands_help[i]);
		}		
	}
}	

