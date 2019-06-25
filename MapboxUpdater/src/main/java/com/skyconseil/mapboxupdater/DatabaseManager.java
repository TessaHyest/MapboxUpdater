package com.skyconseil.mapboxupdater;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseManager manages the classes to upload files to database
 * 
 * @author Tessa
 * 
 *         version 1.0
 *
 */
public class DatabaseManager {

	/////////////////////////////////////////
	/// C O N N E C T I O N T O D B //////
	////////////////////////////////////////

	@SuppressWarnings("unused")
	private class CredentialsDB {
		private String url;
		private String username;
		private String psswd;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPsswd() {
			return psswd;
		}

		public void setPsswd(String psswd) {
			this.psswd = psswd;
		}

	}

	/////////////////////////////////////////
	/// U P L O A D F I L E S T O D B ///
	/////////////////////////////////////////

	@SuppressWarnings("unused")
	private class UploadFileToDB {
		private String id;
		private String fileName;
		private String fileType;

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getFileName() {
			return fileName;
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		public String getFileType() {
			return fileType;
		}

		public void setFileType(String fileType) {
			this.fileType = fileType;
		}

		public String toString() {
			return "url : " + this.id + "\nusername : " + this.fileName + "\npsswd : " + this.fileType;
		}

	}

	// Connect MclickAero to DB
	private static Connection connect = null;

	public Connection getConnect() {

		// File path to postgresql (DB).
		String url = "jdbc:postgresql://localhost:5433/UpdateTilesets";
		// credentials for connection to DB
		String username = "postgres";
		String psswd = "Sky31";

		if (connect == null) {
			try {
				//
				Class.forName("org.postgresql.Driver");
				System.out.println("Driver ok");

				// authorization to connect to DB
				@SuppressWarnings("unused")
				Connection conn = DriverManager.getConnection(url, username, psswd);
				System.out.println("Connexion ok");
				// if driver failure
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				// if connection failure
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return connect;
	}

	// method to close connection.
	public void closeConnexion() {

		try {
			connect.close();

		} catch (SQLException e) {
			e.printStackTrace();

		}
	}

}
