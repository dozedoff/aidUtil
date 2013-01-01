package com.github.dozedoff.aidUtil.module.archiveIndexer;

import java.nio.file.Path;

public class UnpackException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	String message, errorMessage = null;
	public final static int INVALID_PASSWORD = 100;

	public UnpackException(int exitcode, Path file){
		check7zipExitCode(exitcode);
		checkCustomExitCode(exitcode);
		
		if(errorMessage == null){
			errorMessage = "Unspecified error";
		}
		
		this.message = errorMessage +" while processing "+ file.toString();
	}
	
	private void check7zipExitCode(int exitCode) {
		if(errorMessage != null){
			return;
		}
		
		switch(exitCode){

		case 1: 
			errorMessage = "Warning";
			break;

		case 2:
			errorMessage = "Fatal error";
			break;

		case 7:
			errorMessage = "Command line error";
			break;
		case 8:
			errorMessage = "Not enough memory for operation";
			break;

		case 255:
			errorMessage = "User stopped the process";
			break;

		default:
			errorMessage = null;
			break;
		}
	}
	
	private void checkCustomExitCode(int exitcode) {
		if(errorMessage != null){
			return;
		}
		
		switch(exitcode){
		
		case 100:
			errorMessage = "Invalid password";
			break;
			
		default:
			errorMessage = null;
			break;
		}
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
