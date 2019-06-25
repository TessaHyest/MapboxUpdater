package com.skyconseil.mapboxupdater;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.json.JSONObject;

/**
 * MclickAeroManager manages the classes to upload files to database
 * 
 * @author Tessa
 * 
 *         version 1.0
 *
 */
public class MclickAeroManager {

	// credentials to connect to MclickAero
	private class MclickAeroCredentials {
		private String mclickAeroUrl;
		private String mclickAeroUsername;
		private String mclickAeroPassword;

		@SuppressWarnings("unused")
		public String getMclickAeroUrl() {
			return mclickAeroUrl;
		}

		@SuppressWarnings("unused")
		public void setMclickAeroUrl(String newMclickAeroUrl) {
			this.mclickAeroUrl = newMclickAeroUrl;
		}

		@SuppressWarnings("unused")
		public String getMclickAeroUserame() {
			return mclickAeroUsername;
		}

		@SuppressWarnings("unused")
		public void setUsername(String newMclickAeroUsername) {
			this.mclickAeroUsername = newMclickAeroUsername;
		}

		@SuppressWarnings("unused")
		public String getPassword() {
			return mclickAeroPassword;
		}

		@SuppressWarnings("unused")
		public void setPassword(String newMclickAeroPassword) {
			this.mclickAeroPassword = newMclickAeroPassword;
		}

		public String toString() {
			return "mclickAeroUrl :" + this.mclickAeroUrl + "\nmclickAeroUsername :" + this.mclickAeroUsername
					+ "\nmclickAeroPassword :" + this.mclickAeroPassword;
		}

	}

	/**
	 * credentials to connects to MclickAero
	 * 
	 * One constant variable per class
	 */
	@SuppressWarnings("unused")
	private static final String MCLICK_AERO_CREDENTIALS = "/MclickAeroCredentials";

	/////////////////////////////////////
	/// C E N T E R F U N C T I O N ///
	/////////////////////////////////////

	public String connectToMclickAero() throws IOException {

		String MclickAeroCredentialsString = new String();
		// parse credentials in Credentials object
		MclickAeroCredentials MclickAeroCredentialsObject = parseMclickAeroCredentials(MclickAeroCredentialsString);

		// @return credentials's object in a String.
		return MclickAeroCredentialsObject.toString();

	}

	// parse credentials in credentials object
	private MclickAeroCredentials parseMclickAeroCredentials(String MclickAeroCredentialsString) {

		// @param new credentials.
		MclickAeroCredentials cred = new MclickAeroCredentials();
		/**
		 * A JSONObject is an unordered collection of name/value pairs.
		 * 
		 * @param source A string beginning with { (left brace) and ending with } (right
		 *               brace).
		 * 
		 * @throws JSONException - If there is a syntax error in the source string or a
		 *                       duplicated key.
		 */
		JSONObject obj = new JSONObject(MclickAeroCredentialsString);
		/**
		 * Get the string associated with a credential.
		 * 
		 * @param credential A credential string.
		 * 
		 * @return A string which is the value.
		 * 
		 * @throws JSONException - if there is no string value for the credential.
		 */
		cred.mclickAeroUrl = obj.getString("mclickAeroUrl");
		cred.mclickAeroUsername = obj.getString("mclickAeroUsername");
		cred.mclickAeroPassword = obj.getString("mclickAeroPassword");
		// @return credentials.
		return cred;
	}

}
