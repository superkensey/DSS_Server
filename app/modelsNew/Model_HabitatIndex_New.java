package util;

import play.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import java.nio.channels.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
public class Model_HabitatIndex_New extends Model_Base
{
	private static int mWindowSizeMeters = 390;
	private static int mWindowSizeInCells;
	private static String mModelFile = "habitat_index";
	
	//--------------------------------------------------------------------------
	public Model_HabitatIndex_New() {
		
		mWindowSizeInCells = mWindowSizeMeters / 30; // Number of Cells in Raster Map
	}
	
	// Define habitat index function
	//--------------------------------------------------------------------------
	public List<ModelResult> run(int[][] rotationData, int width, int height, String destFolder) {
		
Logger.info(">>> Computing Model Habitat Index");
long timeStart = System.currentTimeMillis();
		
		float [][] habitatData = new float[height][width];
Logger.info("  > Allocated memory for Habitat Index");
		
		// --- Model specific code starts here
		Moving_Z_Window zWin = new Moving_Z_Window(mWindowSizeInCells, rotationData, width, height);
		
		boolean moreCells = true;
		while (moreCells) {
			Moving_Z_Window.Z_WindowPoint point = zWin.getPoint();
			
			if (zWin.canGetProportions()) {
				float proportionAg = zWin.getProportionAg();
				float proportionGrass = zWin.getProportionGrass();
				
				// Habitat Index
				float lambda = -4.47f + (2.95f * proportionAg) + (5.17f * proportionGrass); 
				float habitatIndex = (float)((1.0f / (1.0f / Math.exp(lambda) + 1.0f )) / 0.67f);
	
				habitatData[point.mY][point.mX] = habitatIndex;
			}
			else {
				habitatData[point.mY][point.mX] = -9999.0f; // NO DATA
			}
			
			moreCells = zWin.advance();
		}		
		
		List<ModelResult> results = new ArrayList<ModelResult>();
		results.add(new ModelResult("habitat_index", destFolder, habitatData, width, height));
		
long timeEnd = System.currentTimeMillis();
float timeSec = (timeEnd - timeStart) / 1000.0f;
Logger.info(">>> Model_Habitat_Index is finished - timing: " + Float.toString(timeSec));

		return results;
	}	
}