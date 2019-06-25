package com.skyconseil.mapboxupdater;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

/**
 * MapboxManager manages the classes to update MAPBOX TILESETS
 * 
 * @author Tessa
 * 
 * @version 1.0
 */
public class MapboxManager {

	/**
	 * variables of private class Credentials of the AWS S3 bucket provides by
	 * MAPBOX
	 *
	 * private is restricted class, can only be accessed within the same class (an
	 * outside class has no access to it).
	 */
	private class Credentials {
		private String bucket;
		private String accessKeyId;
		private String key;
		private String secretAccessKey;
		private String sessionToken;
		private String url;

		/**
		 * Constructor of credentials class
		 * 
		 * getter and setter method to access to private variables.
		 * 
		 * @return instance of a String which is credentials.
		 */
		public String getBucket() {
			return bucket;
		}

		// set method to modify the value of variable newBucket.
		@SuppressWarnings("unused")
		public void setBucket(String newBucket) {
			this.bucket = newBucket;
		}

		// get method to read the value of the variable "accessKeyId".
		public String getAccessKeyId() {
			return accessKeyId;
		}

		@SuppressWarnings("unused")
		public void setAccessKeyId(String newAccessKeyId) {
			this.accessKeyId = newAccessKeyId;
		}

		@SuppressWarnings("unused")
		public String getKey() {
			return key;
		}

		@SuppressWarnings("unused")
		public void setKey(String key) {
			this.key = key;
		}

		public String getSecretAccessKey() {
			return secretAccessKey;
		}

		@SuppressWarnings("unused")
		public void setSecretAccessKey(String secretAccessKey) {
			this.secretAccessKey = secretAccessKey;
		}

		public String getSessionToken() {
			return sessionToken;
		}

		@SuppressWarnings("unused")
		public void setSessionToken(String sessionToken) {
			this.sessionToken = sessionToken;
		}

		public String getUrl() {
			return url;
		}

		@SuppressWarnings("unused")
		public void setUrl(String url) {
			this.url = url;
		}

		/**
		 * Retrieve AWS S3 credentials.
		 * 
		 * @return a string with the credentials: bucket, key, secretAccessKey,
		 *         sessionToken, URL and accessKeyId.
		 */
		public String toString() {
			return "bucket : " + this.bucket + "\nkey : " + this.key + "\nsecretAccessKey : " + this.secretAccessKey
					+ "\nsessionToken : " + this.sessionToken + "\nUrl : " + this.url + "\naccessKeyId : "
					+ this.accessKeyId;
		}

	}

	/////////////////////////////////
	/// U P L O A D S T A T U S ///
	/////////////////////////////////

	// private class with variables of the Upload Status to track the upload.
	public class UploadStatus {
		// true or false.
		private boolean iscomplete;
		private String tilesetId;
		private String error;
		private String uploadId;
		private String tilesetName;
		private Date DateModified;
		private Date DateCreated;
		@SuppressWarnings("unused")
		private String ownerId;
		// upload have a progress property that start at 0 and end at 1 when the upload
		// is complete.
		private Float progress;

		/**
		 * Constructor of uploadStatus class.
		 * 
		 * getter and setter method to access to private variables.
		 * 
		 * @return instance of a String which is an upload status.
		 * 
		 * @param iscomplete a String
		 */
		public boolean getIscomplete(String iscomplete) {
			return true;
		}

		public void setIscomplete(boolean complete) {
			this.iscomplete = complete;
		}

		public String getTilesetId(String tilesetId) {
			return tilesetId;
		}

		public void setTilesetId(String newTilesetId) {
			this.tilesetId = newTilesetId;
		}

		public String getError(String error) {
			return error;
		}

		public void setError(String nError) {
			this.tilesetId = nError;
		}

		public String getUploadId(String uploadId) {
			return uploadId;
		}

		public void setUploadId(String newUploadId) {
			this.uploadId = newUploadId;
		}

		public String getTilesetName(String tilesetName) {
			return tilesetName;
		}

		public void setTilesetName(String newTilesetName) {
			this.tilesetName = newTilesetName;
		}

		public String getOwnerId(String ownerId) {
			return ownerId;
		}

		public void setOwnerId(String newOwnerId) {
			this.ownerId = newOwnerId;
		}

		// @return a string with the variables of the upload status.
		public String toString() {
			return "complete : " + Boolean.toString(this.iscomplete) + "\ntilesetId : " + this.tilesetId + "\nerror : "
					+ this.error + "\nuploadId :" + this.getUploadId() + "\ntilesetName :" + this.tilesetName
					+ "\nModified : " + toISO8601UTC(this.DateModified) + "\nCreated : "
					+ toISO8601UTC(this.DateCreated) + "\nprogress : " + Float.toString(this.progress);
		}

		public String getUploadId() {
			return uploadId;
		}
	}

	/**
	 * One constant variable per class
	 *
	 * AWS S3 bucket's URL provides by MAPBOX.
	 */
	private static final String GLOBAL_URL = "https://api.mapbox.com/uploads/v1/skyconseil";

	/**
	 * One constant variable per class
	 * 
	 * temporary AWS S3 credentials provides by MAPBOX.
	 */
	private static final String CREDENTIALS = "/credentials";

	/**
	 * One constant variable per class
	 * 
	 * access token provides by MAPBOX to get the AWS S3's bucket.
	 * 
	 */
	private static final String ACCESS_TOKEN = "access_token=sk.eyJ1Ijoic2t5Y29uc2VpbCIsImEiOiJjanU4ZXpjdzgxMWN5NDNtc2Z5MTA3Ymp2In0.jX2RQNj_lxuRJxS14e-3bw";

	/**
	 * One constant variable per class.
	 * 
	 * TILESETS are the main mechanism for map view, a collection of vector data.
	 * GeoJson(geographic JSON) layer.
	 *
	 * Tileset's name.
	 */
	@SuppressWarnings("unused")
	private static final String TILESET = "skyconseil.aacdr86o";

	/////////////////////////////////////
	/// C E N T E R F U N C T I O N ///
	/////////////////////////////////////

	/**
	 * main function MapoxManager calling sub-functions
	 * 
	 * String to upload files.
	 * 
	 * @param file_path a String 
	 * 
	 * @param tilesetId a String 
	 * 
	 * @param tilesetName a String
	 * 
	 * @return uploadStatusObject
	 * 
	 * @throws JSONException if a JSON error occurs
	 * 
	 * @throws IOException if an error occurs
	 */
	public UploadStatus uploadFile (String file_path, String tilesetId, String tilesetName) throws IOException, JSONException {

		// Retrieve AWS S3 credentials provides by MAPBOX.
		String credentialsString = getCredentials();
		/**
		 * parse credentials String in Credentials object.
		 * 
		 * @param credentialsString
		 */
		Credentials credentialsObject = parseCredentials(credentialsString);

		// stage the files to AWS S3 bucket provides by MAPBOX while the upload is
		// processed and before upload to MAPBOX account. (stage=archiver)
		stageFile(credentialsObject, file_path);
		/**
		 * download files from AWS to MAPBOX account.
		 * 
		 * @param credentialsObject, tilesetId, tilesetName.
		 */
		String uploadStatusString = uploadFileToMapbox(credentialsObject, tilesetId, tilesetName);
		/**
		 * parse uploadStatus in UploadStatus object.
		 * 
		 * @param uploadStatusString
		 */
		UploadStatus uploadStatusObject = parseUploadStatus(uploadStatusString);

		// @return credentials's object in a String.
		return uploadStatusObject;
	}

	/**
	 * retrieve upload status files of MAPBOX we download.
	 * 
	 *@param uploadId a String
	 *
	 *@return uploadStatusObject
	 *
	 *@throws IOException if an error occurs
	 */
	public UploadStatus getUploadStatus (String uploadId) throws IOException {

		// Retrieve upload status of file we download to MAPBOX
		String uploadStatusString = uploadStatus(uploadId);

		// parse uploadStatus in UploadStatus object
		UploadStatus uploadStatusObject = parseUploadStatus(uploadStatusString);

		// @return uploadSatus's object in a String.
		return uploadStatusObject;

	}

	// retrieve credentials
	private String getCredentials() throws IOException {
		/**
		 * instantiation of URL object
		 * 
		 * @param GLOBAL_URL, CREDENTIALS, ACCESS_TOKEN "?"= prepared statement ("?"=
		 *        instruction SQL déjà compilée).
		 */
		URL obj = new URL(GLOBAL_URL + CREDENTIALS + "?" + ACCESS_TOKEN);
		/**
		 * opened connection.
		 * 
		 * A URLConnection with support for HTTP-specific features.
		 * 
		 * @return a URLConnection linking to the URL.
		 */
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// HTTP's request via POST method
		con.setRequestMethod("POST");

		// @param do output the new value to send a body request.
		con.setDoOutput(true);
		/**
		 * Gets the status code from an HTTP response message.
		 * 
		 * @return the HTTP Status.
		 */
		int responseCode = con.getResponseCode();
		/**
		 * A URLConnection with support for HTTP-specific features.
		 * 
		 * success. HTTP Status-Code 200: OK.
		 */
		if (responseCode == HttpURLConnection.HTTP_OK) {
			/**
			 * instantiation of BufferedReader(mettre les données traitées dans un tampon
			 * pour les traiter par lots)
			 * 
			 * instantiation of InputStreamReader(flux d'entrée de caractères)
			 */
			BufferedReader in = new BufferedReader(new InputStreamReader(

					// @return an input stream that reads from this open connection.
					con.getInputStream()));
			// To complete the execution
			String inputLine;
			/**
			 * instantiation of StringBuffer (when it have to read multi-threads).
			 * 
			 * @param a new string buffer.
			 */
			StringBuffer response = new StringBuffer();
			/**
			 * Reads a line of text which is not null.
			 * 
			 * @return A String containing the contents of the line.
			 */
			while ((inputLine = in.readLine()) != null) {
				/**
				 * Appends the specified string to this character sequence.
				 * 
				 * @return a reference to this object.
				 */
				response.append(inputLine);
			}
			// Closes the stream and releases any system resources associated with it.
			in.close();
			/**
			 * @return a string consisting of exactly this sequence of characters.
			 */
			return response.toString();
		} else {
			System.out.println("POST request not worked");

			return "Error";
		}

	}

	// parse credentials in credentials object
	private Credentials parseCredentials(String credentialsString) throws JSONException {

		/**
		 *  instantiation of credentials.
		 *  
		 *  @param cred an object
		 */
		Credentials cred = new Credentials();
		/**
		 * instantiation of a JSONObject(JavaScript Object Notation) is a lightweight,
		 * text-based, language-independent data exchange format that is easy for humans
		 * and machines to read and write.
		 * 
		 * @param credentialsString a String
		 */
		JSONObject obj = new JSONObject(credentialsString);

		// Get the string associated with a credential and the object.
		cred.bucket = obj.getString("bucket");
		cred.key = obj.getString("key");
		cred.accessKeyId = obj.getString("accessKeyId");
		cred.secretAccessKey = obj.getString("secretAccessKey");
		cred.sessionToken = obj.getString("sessionToken");
		cred.url = obj.getString("url");

		return cred;
	}

	/**
	 * stage files
	 * 
	 *@param cred an object
	 * 
	 *@param file_path a String
	 *
	 *@throws IOException if an error occurs
	 */
	public void stageFile (Credentials cred, String file_path) throws IOException {
		/**
		 * System.out.format (= printf) Writes a formatted string to this output stream
		 * can take many arguments.
		 * 
		 *@param file_path and cred.getBucket and what to what (%s = type of variable =  string).
		 *                 
		 */
		System.out.format ("Uploading %s to S3 bucket %s...\n", file_path, cred.getBucket());

		// region where's the AWS S3 bucket.
		Regions clientRegion = Regions.US_EAST_1;
		// parse cred.bucket in a String
		String bucketName = cred.bucket;
		String stringObjKeyName = cred.key;
		String fileName = file_path;

		try {
			/**
			 * Provides access to the AWS credentials used for accessing AWS services: AWS
			 * access key ID and secret access key. These credentials are used to secure
			 * requests to AWS services.
			 * 
			 * @param new BasicSessionCredentials, simple session credentials with keys and
			 *        session token.
			 */
			AWSCredentials s3Credentials = new BasicSessionCredentials(cred.getAccessKeyId(), cred.getSecretAccessKey(),
					cred.getSessionToken());
			// Fluent builder for AWS S3 bucket Capable of building synchronous and
			// asynchronous clients.
			AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withRegion(clientRegion)
					/**
					 * Sets the AWSCredentialsProvider used by the client.
					 * 
					 * @param credentialsProvider New AWSCredentialsProvider to use.
					 * 
					 * @return This object for method chaining.
					 */
					.withCredentials(new AWSStaticCredentialsProvider(s3Credentials)).build();
			// Upload the specified file to AWS S3 under the specified bucket and key name.
			s3Client.putObject(bucketName, stringObjKeyName, new File(fileName));
			/**
			 * Instantiate a new PutObjectRequest object to upload a file to the specified
			 * bucket and key.
			 * 
			 * @param bucketName The name of an existing bucket to which the new object will
			 *                   be uploaded. key The key under which to store the new
			 *                   object. file The path of the file to upload to Amazon S3.
			 */
			PutObjectRequest request = new PutObjectRequest(bucketName, stringObjKeyName, new File(fileName));

			// Instantiation of objectMetadata which is data object that provides
			// information about other data.
			ObjectMetadata metadata = new ObjectMetadata();
			/**
			 * set(modifier valeur des variables) HTTP is dealing with a content type which
			 * is an application/JSON.
			 * 
			 * @param application/JSON
			 */
			metadata.setContentType("application/json");
			/**
			 * metadata adding by the user
			 * 
			 * @param key The key for the custom user metadata entry. value The value for
			 *            the custom user-metadata entry.
			 */
			metadata.addUserMetadata("x-amz-meta-title", "someTitle");
			/**
			 * request to set metadata
			 * 
			 * @param metadata
			 */
			request.setMetadata(metadata);
			/**
			 * Uploads a new object to the specified Amazon S3 bucket.
			 * 
			 * @param putObjectRequest The request object containing all the parameters to
			 *                         upload a new object to Amazon S3.
			 * 
			 * @return A PutObjectResult object containing the information returned by
			 *         Amazon S3 for the newly created object.
			 */
			s3Client.putObject(request);
		}

		catch (AmazonServiceException e) {

			e.printStackTrace();
		}

		catch (SdkClientException e) {
			// Prints this throwable and its back trace to the standard error stream.
			e.printStackTrace();
		}

	}

	// upload files from AWS S3 bucket to MAPBOX.
	private String uploadFileToMapbox(Credentials cred, String tilesetId, String tilesetName) throws IOException {
		/**
		 * Writes a formatted string to this output stream using the specified format
		 * string and arguments.
		 * 
		 * @return This output stream.
		 */
		System.out.format("Uploading %s to mapbox tileset %s...\n", cred.getUrl(), tilesetId, tilesetName);

		String payload = "{" + // message voulu, charge utile
				"\"url\": \"" + cred.getUrl() + "\", " + // code donné par MAPBOX
				"\"tileset\": \"" + tilesetId + "\"," + "\"name\": \"" + tilesetName + "\"" + "}";

		System.out.println("payload : " + payload);
		// instantiation of StringEntity = object where it placed the pay load to set
		// the body of the request.
		StringEntity entity = new StringEntity(payload,
				// the object contents an JSON application.
				ContentType.APPLICATION_JSON);

		HttpClient httpClient = HttpClientBuilder.create().build();

		HttpPost request = new HttpPost(GLOBAL_URL + "?" + ACCESS_TOKEN);

		request.setEntity(entity);

		request.addHeader("Cache-Control", "no-cache");

		HttpResponse response = httpClient.execute(request);

		BufferedReader in = new BufferedReader(new InputStreamReader(

				response.getEntity().getContent()));
		String inputLine;

		StringBuffer data = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {

			data.append(inputLine);
		}

		in.close();

		return data.toString();

	}

	@SuppressWarnings("unused")
	private static final String REVERSE = "reverse=true";

	private static final String LIMIT = "limit=2";

	// retrieve at most 10 recent upload statuses from AWS to MAPBOX, in
	// chronological order.
	public String uploadStatuses() throws IOException {

		@SuppressWarnings("unused")
		Object urlRecentUpload;

		URL obj = new URL(GLOBAL_URL + "?" + ACCESS_TOKEN + "&" + LIMIT);
		// opened connection
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		// HTTP's request via get method
		con.setRequestMethod("GET");

		con.setDoOutput(true);
		// Gets the status code from an HTTP response message.
		int responseCode = con.getResponseCode();
		// code 200: success
		if (responseCode == HttpURLConnection.HTTP_OK) {

			BufferedReader in = new BufferedReader(new InputStreamReader(

					con.getInputStream()));

			String inputLine;

			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {

				response.append(inputLine);
			}
			// Closes the stream and releases any system resources associated with it.
			in.close();
			// @return a string with the upload statuses.
			return response.toString();
		} else {
			System.out.println("Upload status request not worked");

			return "Error";
		}

	}

	/**
	 * retrieve upload status of file we download to MAPBOX.
	 * 
	 * @param uploadId a String
	 * 
	 * @return error
	 * 
	 * @throws IOException if an error occurs
	 */
	public String uploadStatus (String uploadId) throws IOException {

		@SuppressWarnings("unused")
		Object urlRecentUpload;
		/**
		 * instantiation of URL object.
		 * 
		 * @param GLOBAL_URL, uploadId, ACCESS_TOKEN
		 * 
		 */
		URL obj = new URL(GLOBAL_URL + "/" + uploadId + "?" + ACCESS_TOKEN);

		System.out.println("URL : " + GLOBAL_URL + "/" + uploadId + "?" + ACCESS_TOKEN);
		// opened connection
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		// HTTP's request via GET method
		con.setRequestMethod("GET");
		// @param do output the new value.
		con.setDoOutput(true);
		// Gets the status code from an HTTP response message.
		int responseCode = con.getResponseCode();
		// the status response message 200 is ok.
		if (responseCode == HttpURLConnection.HTTP_OK) {
			// It buffers the characters in order to enable efficient reading of text data.
			// (buffered = mise en tampon, zone de memoire vive pour entreposer
			// temporairement des données)
			BufferedReader in = new BufferedReader(new InputStreamReader(
					// Returns an input stream that reads from this open connection.
					con.getInputStream()));

			String inputLine;

			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				// @param inputLine append to the response.
				response.append(inputLine);
			}
			// Closes the stream and releases any system resources associated with it.
			in.close();

			return response.toString();
		} else {
			System.out.println("Upload status request not worked");

			return "Error";
		}

	}

	/**
	 * parseUploadStatus to a String .
	 * 
	 * @param uploadStatusString a String
	 * 
	 * @return uploadStatus
	 */
	private UploadStatus parseUploadStatus(String uploadStatusString) {

		UploadStatus US = new UploadStatus();

		System.out.println("upload status string : " + uploadStatusString);
		// instantiate a JSON object
		JSONObject obj = new JSONObject(uploadStatusString);
		// get the boolean associated with a variable of upload status and an object
		US.iscomplete = obj.getBoolean("complete");
		US.tilesetId = obj.getString("tileset");
		// if error=null
		try {
			US.error = obj.getString("error");
		} catch (JSONException e) {
			US.error = "no error";
		}
		US.setUploadId(obj.getString("id"));
		US.tilesetName = obj.getString("name");

		@SuppressWarnings("unused")
		SimpleDateFormat dateFormatMapBox = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

		US.DateModified = fromISO8601UTC(obj.getString("modified"));
		US.DateCreated = fromISO8601UTC(obj.getString("created"));

		US.ownerId = obj.getString("owner");
		US.progress = obj.getFloat("progress");
		// @return uploadStatus.
		return US;
	}

	// boolean to know when the upload is terminated.
	public boolean UploadIsFinished(String uploadId) {

		UploadStatus usNew = new UploadStatus();

		do {
			try {
				/**
				 * method can be used to pause the execution of current thread for specified
				 *
				 * time in milliseconds
				 */
				Thread.sleep(3000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				// get method to read the value of variable uploadId of upload status.
				usNew = getUploadStatus(uploadId);

			} catch (IOException e) {
				
				e.printStackTrace();
				return false;
			}
			// Carry on the loop until usNew = is complete (true).
		} while (!usNew.iscomplete);

		return true;
	}

	//////////////////////////
	/// A U X I L I A R Y ///
	////////////////////////

	// convert Date String to/from ISO 8601 respecting UTC in JAVA
	private static String toISO8601UTC(Date date) {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df.setTimeZone(tz);
		return df.format(date);
	}

	private static Date fromISO8601UTC(String dateStr) {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		df.setTimeZone(tz);

		try {
			return df.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

}
