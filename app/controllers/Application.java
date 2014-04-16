package controllers;

import util.*;

import java.util.*;
import java.io.*;
import java.net.*;

import play.*;
import play.mvc.*;
import play.Logger;
import play.cache.*;

import views.html.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

import org.apache.commons.io.FileUtils; 
import org.apache.commons.io.filefilter.*; 

//import org.codehaus.jackson.*;
//import org.codehaus.jackson.node.*;
import javax.xml.bind.DatatypeConverter;

//------------------------------------------------------------------------------
public class Application extends Controller 
{
	static int mHeatCount = 0;
	
	//--------------------------------------------------------------------------
	public static Result index() 
	{
		return ok(index.render());
	}
	
	//--------------------------------------------------------------------------
	public static Result query() throws Exception 
	{
		Query query = new Query();
		JsonNode result = query.selection(request().body().asJson());
		return ok(result);
	}

	//--------------------------------------------------------------------------
	public static Result layerParmRequest() throws Exception 
	{
		JsonNode request = request().body().asJson();

		JsonNode ret = Layer_Base.getParameter(request);
		if (ret != null) 
		{
			return ok(ret);
		}
		
		return badRequest(); // TODO: add return errors if needed...
	}
	
	//----------------------------------------------------------------------
	public static Result wmsRequest() 
	{
		// Open up request from client...
		JsonNode request = request().body().asJson();

		Logger.info(request.toString());
		// e.g., 'Vector:Watersheds-C'
		String layerName = request.get("layer").textValue();
		int x = request.get("x").intValue(); // 585
		int y = request.get("y").intValue(); // 273
		int width = request.get("width").intValue();
		int height = request.get("height").intValue();
		String bbox = request.get("bbox").textValue();

		BufferedReader rd = null;
		OutputStreamWriter wr = null;
		
		try 
		{
			URL url = new URL("http://pgis.glbrc.org:8080/geoserver/Vector/wms" + 
				"?LAYERS=" + layerName + "&QUERY_LAYERS=" + layerName + 
				"&STYLES=&SERVICE=WMS&VERSION=1.1.1&SRS=EPSG:900913" +
				"&REQUEST=GetFeatureInfo&FEATURE_COUNT=10&INFO_FORMAT=application/vnd.ogc.gml/3.1.1" +
				"&BBOX=" + bbox +
				"&HEIGHT=" + Integer.toString(height) + 
				"&WIDTH=" + Integer.toString(width) +
				"&X=" + Integer.toString(x) + 
				"&Y=" + Integer.toString(y));
		
			Logger.info("------------------------------");
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			
			wr = new OutputStreamWriter(conn.getOutputStream());
			wr.flush();
			
			// get the response
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			
			String line = rd.readLine();
			if (line != null) {
//				Logger.info(line);
				wr.close();
				rd.close();
				return ok(line);
/*				String line1 = rd.readLine();
				if (line1 != null) {
					wr.close();
					rd.close();
					return ok(line1);
				}*/
			}
			
			wr.close();
			rd.close();
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
		
		Logger.info("WMS request failed");
		return badRequest(); // TODO: add return errors if needed...
	}

	//----------------------------------------------------------------------
	public static Result getAssumptions() throws Exception 
	{
		// Get the ArrayNodes of all assumption defaults so the client knows...
		ObjectNode sendback = JsonNodeFactory.instance.objectNode();
		sendback.put("Assumptions", GlobalAssumptions.getAssumptionDefaultsForClient());
		
		return ok(sendback);
	}

	// TODO: should ultimately have accounts....
	//----------------------------------------------------------------------
	public static Result getClientID() throws Exception 
	{
		ObjectNode sendback = JsonNodeFactory.instance.objectNode();
		// FIXME: should ultimately have accounts with log in?
		sendback.put("DSS_clientID", RandomString.get(6));
		
		return ok(sendback);
	}
	
	//----------------------------------------------------------------------
	public static Result setUpScenario() throws Exception
	{
		Logger.info("----- Initializing scenario ----");
		// Create a new scenario and get a transformed crop rotation layer from it...
		JsonNode request = request().body().asJson();

		// TODO: validate that this can't contain anything that could be used as an attack?
		String clientID = request.get("clientID").textValue();
//		String folder = "client_" + clientID;

		int saveID = request.get("saveID").asInt();
		if (saveID < 0) saveID = 0;
		else if (saveID > 9) {
			saveID = 9;
		}
		
		String folder = "client_" + clientID + "/" + Integer.toString(saveID);
		
		Scenario scenario = new Scenario();
		scenario.setAssumptions(request);
		scenario.getTransformedRotation(request);
		scenario.mOutputDir = folder;
		
		String cacheID = Scenario.cacheScenario(scenario, clientID);

		ObjectNode sendback = JsonNodeFactory.instance.objectNode();
		sendback.put("scenarioID", cacheID);
		
		QueuedWriter.queueResults(new ScenarioSetupResult(folder, scenario.mNewRotation,
			scenario.mSelection.mRasterData, scenario.mSelection.mWidth, scenario.mSelection.mHeight));

		return ok(sendback);
	}
	
	//----------------------------------------------------------------------
	public static Result getHeatmap() throws Exception 
	{
		// Open up request from client...
		JsonNode request = request().body().asJson();
		Logger.info("---- getHeatmap request ----");
		Logger.info(request.toString());
		
		// model can be: (TODO: verify list)
		//	habitat_index, soc, nitrogen, phosphorus, pest, pollinator(s?), net_energy,
		//		net_income, ethanol, nitrous_oxide
		String model = request.get("model").textValue();

		if (model == null) {
			Logger.info("Tried to find a model data file but none was passed. Aborting heatmap.");
			return badRequest(); // TODO: add return errors if needed...
		}

		// type can be: 
		//	delta - shows change between file1 and file2
		//	file1 - shows file1 as an absolute map
		//	file2 - shows file2 as an absolute map		
		String type = request.get("type").textValue();
		// subtype can be:
		//	equal - equal interval map
		//	quantile - quantiled...
		String subtype = request.get("subtype").textValue();

		if (type == null) {
			Logger.info("Tried to find a heatmap 'type' key but didn't. Assuming 'delta'");
			type = "delta";
		}
		
		String clientID = request.get("clientID").textValue();
		String folder = "client_" + clientID;

		int compare1ID = request.get("compare1ID").asInt(); // -1 is default, otherwise 0-9, validated below
		int compare2ID = request.get("compare2ID").asInt(); // -1 is default, otherwise 0-9, validated below
	
		String path1 = "./layerData/";
		if (compare1ID == -1) {
			path1 += "default/";
		}
		else if (compare1ID < 0 || compare1ID > 9) {
			return badRequest(); // TODO: add return errors if needed...
		}
		else {
			path1 += folder + "/" + Integer.toString(compare1ID) + "/";
		}
		path1 += model + ".dss";
		
		String path2 = "./layerData/";
		if (compare2ID == -1) {
			path2 += "default/";
		}
		else if (compare2ID < 0 || compare2ID > 9) {
			return badRequest(); // TODO: add return errors if needed...
		}
		else {
			path2 += folder + "/" + Integer.toString(compare2ID) + "/";
		}
		path2 += model + ".dss";
		
		// BLURF, crutching up for SOC layer being handled/stored kind of differently...

		Logger.info("Going to heatmap files:");
		Logger.info("  " + path1);
		Logger.info("  " + path2);
		
		File file1 = new File(path1);
		File file2 = new File(path2);
		
		if (type.equals("delta")) {
			// for 'delta' type, both files must exist!
			if (!file1.exists() || !file2.exists()) {
				Logger.info("Wanted to open files for 'delta' heatmap but one of the files did not exist");
				return badRequest(); // TODO: add return errors if needed...
			}
		}
		else if (type.equals("file1")) {
			if (!file1.exists()) {
				Logger.info("Wanted to open file for 'file1' heatmap but that file did not exist");
				return badRequest(); // TODO: add return errors if needed...
			}
		}
		else if (type.equals("file2")) {
			if (!file2.exists()) {
				Logger.info("Wanted to open file for 'file2' heatmap but that file did not exist");
				return badRequest(); // TODO: add return errors if needed...
			}
		}
		else {
			Logger.info("Error, unknown heatmap type: <" + type + ">");
			return badRequest(); // TODO: add return errors if needed...
		}

		String outputPath = "/public/dynamicFiles/heat_max_" + model + "_" + Integer.toString(mHeatCount++) + ".png";
		
		// FIXME: not sure why play doesn't hand me back the expected directory path in production?
		if (Play.isProd()) {
			// FIXME: blugh, like this won't be totally fragile? :)
//			outputPath = "/target/scala-2.10/classes" + outputPath;
		}
		outputPath = "." + outputPath;
		
		File outputFile = new File(outputPath);
		ObjectNode sendBack = null;
		
		if (type.equals("delta")) { // TWO file heatmap
			if (subtype.equals("equal")) {
				sendBack = Analyzer_Heatmap.runEqualInterval(file1, file2, 
							outputFile, 
							10);
			}
			else {
				sendBack = Analyzer_Heatmap.runQuantile(file1, file2, 
							outputFile, 
							10);
			}
		}
		else
		{
			File fileToMap = file1;
			
			if (type.equals("file2")) { // ONE file absolute map
				fileToMap = file2;
			}
			
			if (subtype.equals("equal")) {
				sendBack = Analyzer_Heatmap.runAbsolute(fileToMap, 
								outputFile, 
								10);
			}
			else {
				sendBack = Analyzer_Heatmap.runAbsoluteQuantiled(fileToMap, 
								outputFile, 
								10);
			}
		}

		if (sendBack != null) {
			sendBack.put("heatFile", "/files/" + outputFile.getName());
		}
		return ok(sendBack);
	}
	
	// TODO: safety fallbacks (ie, restore files to original dir) if moves or whatnots fail?
/*	//----------------------------------------------------------------------
	public static Result saveScenario() throws Exception
	{
		Logger.info("----- Save Scenario Request ----");

		JsonNode request = request().body().asJson();
		
		String clientID = request.get("clientID").textValue();
	
		Logger.info("Information from client: " + clientID);		
		String baseFolder = "client_" + clientID;
		String srcPath = "./layerData/" + baseFolder;

		// find highest currently used slot number, default to zero so an increment later will start
		//	us at slot 1;
		File srcDir = new File(srcPath);
		String[] subDirList = srcDir.list(DirectoryFileFilter.DIRECTORY);
		int slot = 0;
		int lowestSlot = Integer.MAX_VALUE;
		
		for (int i = 0; i < subDirList.length; i++) {
			int res = Integer.parseInt(subDirList[i]);
			if (res > slot) {
				slot = res;
			}
			if (res < lowestSlot) {
				lowestSlot = res;
			}
		}
		
		slot++;

		ObjectNode sendBack = JsonNodeFactory.instance.objectNode();
	
		// Move the files to the new slot...if they exist...
		// FIXME: probably shouldn't be able to even push the save button if these files don't exist?
		Collection<File> files = FileUtils.listFiles(srcDir, null, false);
		
		if (files.size() > 0) {
			String destPath = "./layerData/" + baseFolder + "/" + Integer.toString(slot);
			for (File aFile : FileUtils.listFiles(srcDir, null, false)) {
					FileUtils.moveFileToDirectory(aFile, new File(destPath), true);
			}

			sendBack.put("saveSlot", slot);
		}

		// Oldest directory (lowest slot...which we'll delete if we have more than 10 slots active)
		if (subDirList.length >= 10) {		
			String deletePath = "./layerData/" + baseFolder + "/" + Integer.toString(lowestSlot);
			FileUtils.deleteDirectory(new File(deletePath));
			
			sendBack.put("removedSlot", lowestSlot);
		}

		return ok(sendBack);
	}
*/
 // NEW STUFFS -----------------------------------------------------------------
 
	//----------------------------------------------------------------------
	public static Result runModelCluster() throws Exception 
	{
		Logger.info("----- Model Cluster Process Started ----");

		JsonNode request = request().body().asJson();
		
/*		// NOTE: Seems to not be needed? TODO: verify...

		// Rotation
		Layer_Base layer = Layer_Base.getLayer("cdl_2012");
		int[][] defaultRotation = layer.getIntData();
		int width = layer.getWidth(), height = layer.getHeight();
*/		
		Scenario scenario = Scenario.getCachedScenario(request.get("scenarioID").textValue());
		
		if (scenario == null) {
			return badRequest();
		}
		
		String modelType = request.get("modelType").textValue();
		List<ModelResult> results = null;
		
		if (modelType.equals("yield")) {
			Model_EthanolNetEnergyIncome ethanolEnergyIncome = new Model_EthanolNetEnergyIncome();
			results = ethanolEnergyIncome.run(scenario);
		}
		else if (modelType.equals("epic_phosphorus")) {
			Model_P_LossEpic ple = new Model_P_LossEpic();
			results = ple.run(scenario);
		}
		else if (modelType.equals("water_quality")) {
			Model_WaterQuality wq = new Model_WaterQuality();
			results = wq.run(scenario);
		}
		else if (modelType.equals("soc")) {
			Model_SoilCarbon soc = new Model_SoilCarbon();
			results = soc.run(scenario);
		}
		else if (modelType.equals("pest_pol")) {
			Model_PollinatorPestSuppression pp = new Model_PollinatorPestSuppression();
			results = pp.run(scenario);
		}
		else if (modelType.equals("nitrous")) {
			Model_NitrousOxideEmissions n20 = new Model_NitrousOxideEmissions();
			results = n20.run(scenario);
		}
		else if (modelType.equals("soil_loss")) {
			Model_Soil_Loss sl = new Model_Soil_Loss();
			results = sl.run(scenario);
		}
		else {//(modelType.equals("habitat_index")) {
			Model_HabitatIndex hi = new Model_HabitatIndex();
			results = hi.run(scenario);
		}
		
		// SendBack to Client
		ObjectNode sendBack  = JsonNodeFactory.instance.objectNode();
		
		if (results != null) {
			Analyzer_HistogramNew histogram = new Analyzer_HistogramNew();
			
			// Try to do an in-memory compare of (usually) default...
			//	if layer is not in memory, try doin a file-based compare
			for (int i = 0; i < results.size(); i++) {
				
				ModelResult res = results.get(i);
				Logger.info("Procesing results for " + res.mName);
				
				String clientID = request.get("clientID").textValue();
				String clientFolder = "client_" + clientID + "/";
				int compare1ID = request.get("compare1ID").asInt(); // -1 is default
				String runFolder = Integer.toString(compare1ID) + "/";
			
				String path1 = "";
				// Asking to compare against DEFAULT?
				if (compare1ID == -1) {
					path1 = "default/" + res.mName;
					
					// See if the layer is in memory (it usually will be unless the server was started
					//	with the DEFAULTS NOT loaded...)
					Layer_Base layer = Layer_Base.getLayer(path1);
					if (layer != null) {
						// other layer is in memory so compare with that.
						float[][] data1 = layer.getFloatData();
						if (data1 == null) {
							Logger.info("could not get layer in runModelCluster");
						}
						else {
							sendBack.put(res.mName, 
								histogram.run(res.mWidth, res.mHeight, data1, scenario.mSelection,
												res.mRasterData, scenario.mSelection));
						}
						continue; // process next result...
					}
				}
				else {
					path1 = clientFolder + runFolder + res.mName;
				}
				
				// Compare to file was not in memory, set up the real path and we'll try to load it for
				//	comparison (which is slower...booo)
				path1 = "./layerData/" + path1 + ".dss";
				sendBack.put(res.mName, 
						histogram.run(new File(path1), scenario.mSelection,
										res.mWidth, res.mHeight, res.mRasterData, scenario.mSelection));
			}
		}
		Logger.info("Done processing list of results, queuing results for file writer");
		QueuedWriter.queueResults(results);

		Logger.info(sendBack.toString());
		return ok(sendBack);
	}
 
	//----------------------------------------------------------------------
	public static Result initComparison() throws Exception {
		
		Logger.info("----- Initializing for comparison ----");
		JsonNode request = request().body().asJson();

		// TODO: validate that this can't contain anything that could be used as an attack?
		String clientID = request.get("clientID").textValue();
		String folder = "client_" + clientID;

		int compare1ID = request.get("compare1ID").asInt(); // -1 is default
		int compare2ID = request.get("compare2ID").asInt(); // -1 is default

		String path1 = "./layerData/";
		if (compare1ID == -1) {
			path1 += "default/";
		}
		else if (compare1ID < 0 || compare1ID > 9) {
			return badRequest(); // TODO: add return errors if needed...
		}
		else {
			path1 += folder + "/" + Integer.toString(compare1ID) + "/";
		}
		String basePath1 = path1;
		path1 += "selection.sel";
		
		String path2 = "./layerData/";
		if (compare2ID == -1) {
			path2 += "default/";
		}
		else if (compare2ID < 0 || compare2ID > 9) {
			return badRequest(); // TODO: add return errors if needed...
		}
		else {
			path2 += folder + "/" + Integer.toString(compare2ID) + "/";
		}
		String basePath2 = path2;
		path2 += "selection.sel";

		Logger.info(" ... Going to custom compare files:");
		Logger.info("  " + path1);
		Logger.info("  " + path2);
		
		File file1 = new File(path1);
		File file2 = new File(path2);
		
		boolean failed = false;
		
		if (!file1.exists()) {
			Logger.info(" Error! - file <" + file1.toString() + 
				"> does not exist");
			failed = true;
		}
		if (!file2.exists()) {
			Logger.info(" Error! - file <" + file2.toString() + 
				"> does not exist");
			failed = true;
		}

		if (failed) {
			Logger.info(" Error! - custom comparison aborting.");
			return badRequest(); // TODO: add return errors if needed...
		}
		
		Selection sel1 = new Selection(file1);
		if (!sel1.isValid) {
			Logger.info(" Error! - load of selection from file <" + file1.toString() + 
				"> failed! Custom comparison aborting.");
			return badRequest(); // TODO: add return errors if needed...
		}
		Selection sel2 = new Selection(file2);
		if (!sel2.isValid) {
			Logger.info(" Error! - load of selection from file <" + file2.toString() + 
				"> failed! Custom comparison aborting.");
			return badRequest(); // TODO: add return errors if needed...
		}
		
		CustomComparison customCompare = new CustomComparison(basePath1, sel1, basePath2, sel2);
		
		String cacheID = CustomComparison.cacheCustomComparions(customCompare, clientID);

		ObjectNode sendback = JsonNodeFactory.instance.objectNode();
		sendback.put("customCompareID", cacheID);
		
		return ok(sendback);
	}

	//----------------------------------------------------------------------
	public static Result runComparison() throws Exception {
		
		return ok();
	}
}

/*
//------------------
	// TODO: set up the analysis tools
	//----------------------------------------------------------------------
	public static Result runAnalysisCluster() throws Exception 
	{
		Logger.info("----- Custom Comparison Analysis Cluster Process Started ----");

		JsonNode request = request().body().asJson();
		
		String compareID = request.get("customComparisonID").textValue()
		CustomComparison comparison = CustomComparison.getCachedComparison(compareID);
		
		if (comparison == null) {
			return badRequest(); // TODO: return error if needed...
		}
		
		String modelType = request.get("modelType").textValue();
		List<ModelResult> results = null;
		
		if (modelType.equals("yield")) {
			Model_EthanolNetEnergyInTcome ethanolEnergyIncome = new Model_EthanolNetEnergyIncome();
			results = ethanolEnergyIncome.run(scenario);
		}
		else if (modelType.equals("n_p")) {
			Model_NitrogenPhosphorus np = new Model_NitrogenPhosphorus();
			results = np.run(scenario);
		}
		else if (modelType.equals("soc")) {
			Model_SoilCarbon soc = new Model_SoilCarbon();
			results = soc.run(scenario);
		}
		else if (modelType.equals("pest_pol")) {
			Model_PollinatorPestSuppression pp = new Model_PollinatorPestSuppression();
			results = pp.run(scenario);
		}
		else if (modelType.equals("nitrous")) {
			Model_NitrousOxideEmissions n20 = new Model_NitrousOxideEmissions();
			results = n20.run(scenario);
		}
		else if (modelType.equals("water_quality")) {
			Model_Water_Quality wq = new Model_Water_Quality();
			results = wq.run(scenario);
		}
		else {//(modelType.equals("habitat_index")) {
			Model_HabitatIndex hi = new Model_HabitatIndex();
			results = hi.run(scenario);
		}
		
		// SendBack to Client
		ObjectNode sendBack  = JsonNodeFactory.instance.objectNode();
		
		if (results != null) {
			Analyzer_HistogramNew histogram = new Analyzer_HistogramNew();
			
			// Try to do an in-memory compare of (usually) default...
			//	if layer is not in memory, try doing a file-based compare
			for (int i = 0; i < results.size(); i++) {
				
				ModelResult res = results.get(i);
				Logger.info("Procesing results for " + res.mName);
				
				String clientID = request.get("clientID").textValue();
				String clientFolder = "client_" + clientID + "/";
				int compare1ID = request.get("compare1ID").asInt(); // -1 is default
				String runFolder = Integer.toString(compare1ID) + "/";
			
				String path1 = "";
				// Asking to compare against DEFAULT?
				if (compare1ID == -1) {
					// YES, but SOC is not actually in DEFAULT so redirect to its real location
					if (res.mName.equals("soc")) {
						path1 = "soc";
					}
					else {
						// YES, so set up the path to the default folder
						path1 = "default/" + res.mName;
					}
					
					// See if the layer is in memory (it usually will be unless the server was started
					//	with the DEFAULTS NOT loaded...)
					layer = Layer_Base.getLayer(path1);
					if (layer != null) {
						// other layer is in memory so compare with that.
						float[][] data1 = layer.getFloatData();
						if (data1 == null) {
							Logger.info("could not get layer in runModelCluster");
						}
						else {
							sendBack.put(res.mName, 
								histogram.run(res.mWidth, res.mHeight, data1, scenario.mSelection,
												res.mRasterData, scenario.mSelection));
						}
						continue; // process next result...
					}
				}
				else {
					path1 = clientFolder + runFolder + res.mName;
				}
				
				// Compare to file was not in memory, set up the real path and we'll try to load it for
				//	comparison (which is slower...booo)
				path1 = "./layerData/" + path1 + ".dss";
				sendBack.put(res.mName, 
						histogram.run(new File(path1), scenario.mSelection,
										res.mWidth, res.mHeight, res.mRasterData, scenario.mSelection));
			}
		}

		Logger.info(sendBack.toString());
		return ok(sendBack);
	}
*/

