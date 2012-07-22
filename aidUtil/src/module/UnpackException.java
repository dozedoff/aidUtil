package module;

import java.nio.file.Path;

public class UnpackException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	String message;

	public UnpackException(int exitcode, Path file){
		String tmp = "";
		switch(exitcode){

		case 1: 
			tmp = "Warning";
			break;

		case 2:
			tmp = "Fatal error";
			break;

		case 7:
			tmp = "Command line error";
			break;
		case 8:
			tmp = "Not enough memory for operation";
			break;

		case 255:
			tmp = "User stopped the process";
			break;

		default:
			tmp = "Unspecified error";
			break;
		}

		this.message = tmp +" while processing "+ file.toString();
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public String getLocalizedMessage() {
		return getMessage();
	}

}
