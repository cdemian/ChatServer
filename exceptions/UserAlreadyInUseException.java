package exceptions;

public class UserAlreadyInUseException extends Exception {


	private static final long serialVersionUID = 1L;
	
	public UserAlreadyInUseException(){
		super();
	}
	
	public UserAlreadyInUseException(String message){
		super(message);
	}
	

}
