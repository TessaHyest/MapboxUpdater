package com.skyconseil.mapboxupdater;

import java.io.IOException;
import com.skyconseil.mapboxupdater.ProductParser;
import com.skyconseil.mapboxupdater.MapboxManager.UploadStatus;

/**
 * Main class MainUpdateTilesets manages all the classes of the project
 * MapboxUpdater.
 * 
 * @author Tessa
 * 
 * @version 1.0
 *
 */
public class MainUpdateTilesets {

	public static void main(String[] args) throws IOException {

		/*
		 * // file path of the files we want to copy in the s3 bucket String file_path =
		 * "C:\\Users\\Stage\\Documents\\MapboxFiles\\myFile.json"; String tilesetId =
		 * "skyconseil.aacdr86o"; String tilesetName = "toto";
		 * 
		 * MapboxManager m = new MapboxManager(); UploadStatus us =
		 * m.uploadFile(file_path, tilesetId, tilesetName);
		 * 
		 * UploadStatus usNew = m.getUploadStatus(us.getUploadId());
		 * System.out.println("Upload Status : " + usNew.toString());
		 * 
		 * boolean isFinished = m.UploadIsFinished(us.getUploadId()); if (isFinished) {
		 * System.out.println("Upload finished"); }
		 * 
		 * MclickAeroManager mc = new MclickAeroManager();
		 * System.out.println("connection ok");
		 */

		ProductParser total = new ProductParser();

		total.parseFile("C:/Users/tessa/Documents/Java EE/FlightEnv.json");

		System.out.println("AirportsLayerString: ");
		System.out.println(total.getAirportsLayerString());
		System.out.println("WaypointsLayerString: ");
		System.out.println(total.getWaypointsLayerString());
		System.out.println("RoutesLayerString: ");
		System.out.println(total.getRoutesLayerString());

	}
}
