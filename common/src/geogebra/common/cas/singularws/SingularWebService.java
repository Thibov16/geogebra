package geogebra.common.cas.singularws;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import geogebra.common.factories.UtilFactory;
import geogebra.common.util.HttpRequest;

/**
 * Maintains a Singular WebService.
 * For the SingularWS API please see the documentation of SingularWS
 * (or http://code.google.com/p/singularws/source/browse/inc/commands.php).   
 * 
 * @author Zoltan Kovacs <zoltan@geogebra.org>
 */
public class SingularWebService {

	private int timeout = 10;
	//private String wsHostDefault = "http://ws.geogebra.org/Singular";
	private final String wsHostDefault = "http://140.78.116.130:8085";
	private final String testConnectionCommand = "t";
	private final String singularDirectCommand = "s";
	
	private String wsHost = wsHostDefault;
	private Boolean available; 
	
	public SingularWebService() {}
	
	private String swsCommandResult(String command) {
		return swsCommandResult(command, "");
	}
	
	private String swsCommandResult(String command, String parameters) {
		String getRequest = wsHost + "/?c=" + command;
		if (parameters != null) {
			getRequest += "&p=" + parameters;
		}
		HttpRequest httpr = UtilFactory.prototype.newHttpRequest();
		String response = httpr.getResponse(getRequest); // FIXME: unimplemented in GeoGebraWeb!
		return response;
	}
	
	/**
	 * Reports if SingularWS is available. (It must be initialized by enable() first.) 
	 * @return true if SingularWS is available
	 */
	public boolean isAvailable() {
		if (available == null)
			return false;
		if (available)
			return true;
		return false;
	}
	
	/**
	 * Create a connection to the SingularWS server for testing.

	 * @return true if the connection works properly
	 */
	public boolean testConnection() {
		String result = swsCommandResult(testConnectionCommand); 
		if (result == null)
			return false;
		if (result.equals("ok"))
			return true;
		return false;
	}
	
	/**
	 * Sends a Singular program to the SingularWS server and returns the answer.

	 * @param singularProgram
	 * @return the answer
	 */
	public String directCommand(String singularProgram) {
		String encodedSingularProgram;
		try {
			encodedSingularProgram = URLEncoder.encode(singularProgram, "UTF-8");
			return swsCommandResult(singularDirectCommand, encodedSingularProgram);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/** Sets the remote server being used for SingularWS.
	 * 
	 * @param site
	 */
	public void setConnectionSite(String site) {
		this.wsHost = site;
	}

	/** Reports what remote server is used for SingularWS.
	 * 
	 * @return the URL of the remote server
	 */
	public String getConnectionSite() {
		return this.wsHost;
	}
	
	/**
	 * If the test connection is working, then set the webservice "available".
	 */
	public void enable() {
		if (testConnection()) {
			this.available = true;
		}
		else this.available = false;
	}
	
	public void disable() {
		this.available = false;
	}
	
	/**
	 * Sets the maximal time spent in SingularWS for a program.
	 * 
	 * @param timeout the timeout in seconds
	 */
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}		

}
