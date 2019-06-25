package com.skyconseil.mapboxupdater;

import java.awt.geom.Point2D;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages the classes to parse JSON file in jSON object
 * 
 * @author Tessa
 * 
 * @version 1.2
 *
 */
public class ProductParser {

	private JSONArray _airportsFeatures;
	private JSONArray _waypointsFeatures;
	private JSONArray _routesFeatures;

	private String _airportsLayerString;
	private String _waypointsLayerString;
	private String _routesLayerString;

	private Map<String, JSONObject> _dictWaypoints = new HashMap<>();

	JSONObject airports;
	JSONObject waypoints;
	JSONObject routes;

	// get the content of a JSON file
	private String getFileContent(String filePath) {

		try {
			String text = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
			return text;
		} catch (Exception ex) {
			return "-1";
		}
	}

	// parse file content in JSONObject
	public void parseFile(String filePath) {

		// get the file content
		String fileContent = getFileContent(filePath);

		JSONObject flightEnvObj = new JSONObject(fileContent);

		airports = flightEnvObj.getJSONObject("airports");
		@SuppressWarnings("unused")
		JSONObject bufferGeometry = flightEnvObj.getJSONObject("bufferGeometry");
		waypoints = flightEnvObj.getJSONObject("pathPoints");
		routes = flightEnvObj.getJSONObject("paths");

		_airportsFeatures = airports.getJSONArray("features");
		_waypointsFeatures = waypoints.getJSONArray("features");
		_routesFeatures = routes.getJSONArray("features");

		// call private method
		generateLayerAirports();
		generateLayerWaypoints();
		generateLayerRoutes();

	}

	private void generateLayerAirports() {

		// Generate GeoJson object containing all airports and then generate its layer
		JSONObject geoJsonAirports = new JSONObject();

		geoJsonAirports.put("type", "FeatureCollection");

		// complete geoJsonAirports
		geoJsonAirports.put("features", _airportsFeatures);

		// generate String from geoJsonAirportsObject
		_airportsLayerString = geoJsonAirports.toString();
	}

	private void generateLayerWaypoints() {

		// Generate GeoJson object containing all way points
		JSONObject geoJsonWaypoints = new JSONObject();

		geoJsonWaypoints.put("type", "FeatureCollection");
		// complete geoJsonWayPoints
		geoJsonWaypoints.put("features", _waypointsFeatures);

		// generate private String from geoJsonAirportsObject
		_waypointsLayerString = geoJsonWaypoints.toString();

		// implementation of a Map interface.

		for (int i = 0; i < _waypointsFeatures.length(); i++) {
			// Retrieve the id of point
			JSONObject featureObj = _waypointsFeatures.optJSONObject(i);
			String id;
			try {
				id = featureObj.getString("id");
			} catch (Exception e) {
				System.out.println("Y'a pas d'id !");
				continue;
			}

			// Retrieve coordinates of point
			JSONObject geometryObj = featureObj.getJSONObject("geometry");
			JSONArray coordinatesArray = geometryObj.getJSONArray("coordinates");
			float lon = coordinatesArray.getFloat(0);
			float lat = coordinatesArray.getFloat(1);

			// Retrieve type of point
			JSONObject geometryO = featureObj.getJSONObject("geometry");
			@SuppressWarnings("unused")
			String typePoint = geometryO.getString("type");
			// to check if the data retrieve is correct
			// System.out.println("TOTO : " + id + ", " + longitude + ", " + latitude);

			// Create a Point2D to add it to the dictionary
			@SuppressWarnings("unused")
			Point2D pt = new Point2D.Float(lon, lat);

			// add points to dictionary ( id is the "key", latitude and longitude are
			// the"value")
			_dictWaypoints.put(id, featureObj);

		}

	}

	private void generateLayerRoutes() {

		// initialise routesGeoFeatures
		JSONArray routesGeoFeatures = new JSONArray();

		// to browse pathsFeatures array
		for (int i = 0; i < _routesFeatures.length(); i++) {

			// create a paths object
			JSONObject thisRoutesObject = _routesFeatures.optJSONObject(i);
			// retrieve String "properties" of paths object
			JSONObject properties = thisRoutesObject.getJSONObject("properties");
			// Retrieve String designator
			@SuppressWarnings("unused")
			String designator = properties.getString("designator");
			// retrieve array of lines
			JSONArray lines = properties.getJSONArray("lines");

			// to browse lines array
			for (int j = 0; j < lines.length(); j++) {

				// ** Retrieve data which will be recorded in the JSON object ** //

				JSONObject thisLine = lines.optJSONObject(j);

				String endId = thisLine.getString("endId");

				String startId = thisLine.getString("startId");

				int upperFL = thisLine.getInt("upperFL");

				int lowerFL = thisLine.getInt("lowerFL");

				String lineDesignator = properties.getString("designator");

				// retrieve feature of the first way point
				JSONObject featureObjS = _dictWaypoints.get(startId);
				JSONObject startProperties = featureObjS.getJSONObject("properties");
				String startDesignator = startProperties.getString("designator");

				JSONObject featureObjE = _dictWaypoints.get(endId);
				JSONObject endProperties = featureObjE.getJSONObject("properties");
				String endDesignator = endProperties.getString("designator");

				String routDesignator = lineDesignator + "," + endDesignator + "," + startDesignator;

				JSONObject geometryObjS = featureObjS.getJSONObject("geometry");
				JSONArray startCoordinatesArray = geometryObjS.getJSONArray("coordinates");

				JSONObject geometryObjE = featureObjE.getJSONObject("geometry");
				JSONArray endCoordinatesArray = geometryObjE.getJSONArray("coordinates");

				JSONArray thisCoordinates = new JSONArray();

				thisCoordinates.put(startCoordinatesArray);

				thisCoordinates.put(endCoordinatesArray);

				// create empty feature it should be a geoJsonFeature
				JSONObject thisGeoJsonFeature = new JSONObject();
				thisGeoJsonFeature.put("type", "Feature");

				// set geometry
				JSONObject thisGeometry = new JSONObject();

				thisGeometry.put("type", "LineString");
				thisGeometry.put("coordinates", thisCoordinates);

				// set properties
				JSONObject thisProperties = new JSONObject();
				thisProperties.put("lowerFL", lowerFL);
				thisProperties.put("upperFL", upperFL);
				thisProperties.put("designator", routDesignator);

				// complete thisGeoJsonFeature
				thisGeoJsonFeature.put("geometry", thisGeometry);
				thisGeoJsonFeature.put("properties", thisProperties);

				// put thisGeoJsonFeature in routesGeoFeatures
				routesGeoFeatures.put(thisGeoJsonFeature);
			}

		}

		// Generate GeoJson object containing all routes
		JSONObject geoJsonRoutes = new JSONObject();

		geoJsonRoutes.put("type", "FeatureCollection");
		// complete geoJsonRoutes
		geoJsonRoutes.put("features", routesGeoFeatures);

		// generate String from geoJsonAirportsObject
		_routesLayerString = geoJsonRoutes.toString();
	}

	public String getAirportsLayerString() {
		return _airportsLayerString;
	}

	public String getWaypointsLayerString() {
		return _waypointsLayerString;
	}

	public String getRoutesLayerString() {
		return _routesLayerString;
	}

}
