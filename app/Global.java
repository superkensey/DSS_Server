package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
public class Global extends GlobalSettings
{
	// If the play server is started in DEV mode, should we skip loading certain layers
	//	to get a faster server startup time and use less memory?
	private static final boolean LOAD_ALL_LAYERS_FOR_DEV = true;
	private static final boolean LOAD_DEFAULT_DATA = true;
	
	//--------------------------------------------------------------------------
	@Override
	public void onStart(play.Application app) 
	{
//		convertFolderBinariesToASC("./layerData/default/", "./layerData/defaultConverted/");
		
		systemReport("Application has started");
		
		// create any computed layers (currently don't have any in here?)
		computeLayers();
		
		cacheLayers();

		QueuedWriter.beginWatchWriteQueue();
		conditionalCreateDefaultModelOutputs();		
		cacheModelDefaults();
		
		// Create all of the assumptions the server knows about, these will be fed to clients
		GlobalAssumptions.initAssumptions();
		
		systemReport("Data Layers Cached");
	}

	//--------------------------------------------------------------------------
	@Override
	public void onStop(play.Application app) {
		
		Layer_Base.removeAllLayers();
		System.gc();
		systemReport("Application stopped, Garbage Collection call made");
	}
	
	//--------------------------------------------------------------------------
	private void systemReport(String customMessage) {
		
		float unitConversion = (1024.0f * 1024.0f); // bytes -> MB
		String unitName = "MB";
		
		Logger.info("+-------------------------------------------------------+");
		Logger.info("| " + customMessage);
		Logger.info("+-------------------------------------------------------+");
		Logger.info("  Available Processors: " + 
			Integer.toString(Runtime.getRuntime().availableProcessors()));
		Logger.info("  Total Free Memory: " + 
			String.format("%.2f", 
				(float)(Runtime.getRuntime().freeMemory() / unitConversion)) +
				unitName);
		Logger.info("  Current Total Memory in Use: " + 
			String.format("%.2f", 
				(float)(Runtime.getRuntime().totalMemory() / unitConversion)) +
				unitName);
		Logger.info("  Maximum Memory for Use: " + 
			String.format("%.2f", 
				(float)(Runtime.getRuntime().maxMemory() / unitConversion)) +
				unitName);
		Logger.info("+-------------------------------------------------------+");
	}
	
	//--------------------------------------------------------------------------
	private void computeLayers() {
		/* // Uncomment if need to recalculate and output slope
		CalculateSlope cs = new CalculateSlope();
		cs.computeSlope();
		*/

		/* // Uncomment if need to recalculate and output crop rotation
		CropRotation cr = new CropRotation();
		cr.computeRotation();
		*/
		
		/* new CalculateCornGrassProduction().run();
		*/
	}
	
	//--------------------------------------------------------------------------
	private void cacheLayers() 
	{
		Layer_Base layer;
		try {
			if (Play.isProd() || LOAD_ALL_LAYERS_FOR_DEV == true) {
				Logger.info("Loading all layers");
			}
			
			layer = new Layer_Integer("cdl_2012"); layer.init();
			layer = new Layer_Float("slope"); layer.init();
			layer = new Layer_Float("cec"); layer.init();
			layer = new Layer_Float("depth"); layer.init();
			layer = new Layer_Float("silt"); layer.init();
			layer = new Layer_Float("soc"); layer.init();
			layer = new Layer_Integer("watersheds", Layer_Integer.EType.EQueryShiftedIndex); layer.init();
			layer = new Layer_Float("texture"); layer.init();
			layer = new Layer_Float("om_soc"); layer.init();
			layer = new Layer_Float("drainage"); layer.init();
			layer = new Layer_Float("ph"); layer.init();
			
			// NOTE: am putting low-priority (rarely used) data layers here so that
			//	we can have them skip loading in DEVELOPMENT mode. Ie, faster loads
			//	and less memory usage...
			if (Play.isProd() || LOAD_ALL_LAYERS_FOR_DEV == true) {
				
				layer = new Layer_Float("rivers"); layer.init();
				layer = new Layer_Integer("lcc"); layer.init();
				layer = new Layer_Integer("lcs"); layer.init();
//				layer = new Layer_Continuous("roads"); layer.init();
			}
		}
		catch (Exception e) {
			Logger.info(e.toString());
		}
	}
	
	//--------------------------------------------------------------------------
	private void cacheModelDefaults() {
		
		if (LOAD_DEFAULT_DATA) {
			Logger.info(" --- LOADING MODEL DEFAULT RESULT FILES ---");
			Layer_Base layer;
			try {
				layer = new Layer_Float("default/net_income"); layer.init();
				layer = new Layer_Float("default/net_energy"); layer.init();
				layer = new Layer_Float("default/ethanol"); layer.init();
				layer = new Layer_Float("default/habitat_index"); layer.init();
				layer = new Layer_Float("default/nitrogen"); layer.init();
				layer = new Layer_Float("default/phosphorus"); layer.init();
				layer = new Layer_Float("default/pest"); layer.init();
				layer = new Layer_Float("default/pollinator"); layer.init();
				layer = new Layer_Float("default/nitrous_oxide"); layer.init();
			}
			catch (Exception e) {
				Logger.info(e.toString());
			}
		}
		else {
			Logger.info(" --- SKIPPING LOAD OF MODEL DEFAULT RESULT FILES ---");
		}
	}

	// TODO: potentially check for individual model files vs. just the directory?
	//--------------------------------------------------------------------------
	private void conditionalCreateDefaultModelOutputs() {
		
		// Check for Default Scenario files...to replace them, you need to delete the whole
		//	DEFAULT folder otherwise they will not be recalculated with how this is coded
		//	The default folder also cannot exist for first generation (even if empty...)
		File Output = new File("./layerData/default");
		if(!Output.exists()) {
			
			Logger.info("Default scenario folder does not exist, creating it and default model files!");
			// Rotation
			Layer_Base layer = Layer_Base.getLayer("cdl_2012");
			int width = layer.getWidth();
			int height = layer.getHeight();
			
			Scenario scenario = new Scenario();
			scenario.mNewRotation = layer.getIntData();
			scenario.mSelection = new Selection(width, height);
	// FIXME: these assumptions don't seem to be set up correctly?
			scenario.mAssumptions = new GlobalAssumptions();
			scenario.mOutputDir = "default";
			
			List<ModelResult> results;
			results = new Model_HabitatIndex_New().run(scenario);
			QueuedWriter.queueResults(results);

			results = new Model_EthanolNetEnergyIncome_New().run(scenario);
			QueuedWriter.queueResults(results);
			
			results = new Model_PollinatorPestSuppression_New().run(scenario);
			QueuedWriter.queueResults(results);
			
			results = new Model_NitrogenPhosphorus_New().run(scenario);
			QueuedWriter.queueResults(results);

			results = new Model_NitrousOxideEmissions_New().run(scenario);
			QueuedWriter.queueResults(results);

			// NOTE: SOC for the default is not in the model run because it is not a computed data layer like others...
			
			// wait for write queue to dump out the defaults...
			while(QueuedWriter.doesWriteQueueHaveFiles()) {
				Logger.info("Waiting for defaults to be written by the QueuedWriter");
				try {
					Thread.sleep(4000);
				}
				catch(Exception e) {
					// blah, java exception handling...
				}
			}
		}
	}
	
	// converts all .DSS files in a given folder to ASC. E.g. convert...("./layerData/default");
	//-----------------------------------------------------------
	private void convertFolderBinariesToASC(String folderPath, String destPath) {

		File folder = new File(folderPath);
		if (!folder.isDirectory()) {
			Logger.info(folderPath + " is not a directory!!");
			return;
		}
		
		// get all files in that directory...
		File[] listOfFiles = folder.listFiles();
		if (listOfFiles != null) {
			for (int i = 0; i < listOfFiles.length; i++) {
				
				File sourceBinary = listOfFiles[i];
				// extract off just the name of that file in the directory...
				String destFile = sourceBinary.getName();
				// make sure it has a .dss it in...
				if (destFile.indexOf(".dss") > 0) {
					// make dest dir if needed
					File destDir = new File(destPath);
					if (!destDir.exists()) {
						destDir.mkdirs();
					}
					// now create the new final path....with the new .asc file extension...
					destFile = destPath + destFile.replace(".dss", ".asc");
					
					Binary_Reader fileReader = new Binary_Reader(sourceBinary);
					if (fileReader.readHeader()) {
						
						PrintWriter ascOut = null;
						int width = fileReader.getWidth(), height = fileReader.getHeight();
						try 
						{
							ascOut = new PrintWriter(new BufferedWriter(new FileWriter(destFile)));
							ascOut.println("ncols         " + Integer.toString(width));
							ascOut.println("nrows         " + Integer.toString(height));
							ascOut.println("xllcorner     -10062652.65061");
							ascOut.println("yllcorner     5249032.6922889");
							ascOut.println("cellsize      30");
							ascOut.println("NODATA_value  -9999");
						} 
						catch (Exception err) 
						{
							Logger.info(err.toString());
						}
						
						if (ascOut != null) {	
							String stringNoData = Integer.toString(-9999);

							for (int y = 0; y < height; y++) {
								ByteBuffer buff = fileReader.readLine();
								StringBuffer ascLine = new StringBuffer();

								if (buff != null) {
									for (int x=0; x < width; x++) {
										float data = buff.getFloat(x * 4); // blah, 4 = size of float, ie 32bit
										if (data > -9999.0f) {
											ascLine.append(Float.toString(data));
										}
										else {
											ascLine.append(stringNoData);
										}
										if (x != width - 1) {
											ascLine.append(" ");
										}
									}
								}
								ascOut.println(ascLine.toString());
							}
							fileReader.close();
							try {
								ascOut.close();
							}
							catch (Exception err) {
								Logger.info(err.toString());
							}
						}
					}
					else {
						Logger.info("File read problem with file: " + sourceBinary.toString());
					}
				}
				Logger.info(destFile);
			}
		}
	}
}

