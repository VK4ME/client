package midletintegration;

import java.io.IOException;

/**
 * Part of MIDlet Integration library
 * 
 * @author Shinovon
 * 
 */
public class ProtocolNotSupportedException extends IOException {

	public ProtocolNotSupportedException() {
		super();
	}
	
	public ProtocolNotSupportedException(String s) {
		super(s);
	}

}
