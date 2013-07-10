package util;

import play.*;
import java.util.*;
import java.io.*;

import org.codehaus.jackson.*;
import org.codehaus.jackson.node.*;

//------------------------------------------------------------------------------
// Modeling Process
//
// This program uses crop rotation layers to assess the impact of crop rotation on other things
//
//------------------------------------------------------------------------------
public class Models
{
	
	static int Window_Size;
	static int mWidth, mHeight;
	static int Bin;
	static float Habitat_Index_T;
	static float Phosphorus_T;
	static float Nitrogen_T;
	static float Pest_T;
	static float Pollinator_T;
	
	//--------------------------------------------------------------------------
	public JsonNode modeloutcome(JsonNode requestBody, Selection selection, String Output_Folder, int[][] RotationT)
	{
		int NO_DATA = -9999;
		// Return the Selected Array From Query
		Logger.info("Gathering Data:");
		
		Layer_Base layer;
		int width, height;
		
		// HI
		float Max_H = 1;
		float Min_H = 0;
		// Nitrogen
		float Max_N = 0;
		float Min_N = 0;
		// Phosphrous
		float Max_P = 0;
		float Min_P = 0;
		// Pest
		float Max_Pest = (float)(0.25 + 0.19f * 1 + 0.62f * 1);
		float Min_Pest = (float)(0.25 + 0.19f * 0 + 0.62f * 0);
		// Pollinator
		float Max_Poll = (float)Math.pow(0.6617f + 2.98 * 1 + 1.83 * 1, 2);
		float Min_Poll = (float)Math.pow(0.6617f + 2.98 * 0 + 1.83 * 0, 2);
		int Bin = 10;
		
		// Before Transformation
		int[] CountBin_H = new int [Bin];
		int[] CountBin_N = new int [Bin];
		int[] CountBin_P = new int [Bin];
		int[] CountBin_Pest = new int [Bin];
		int[] CountBin_Poll = new int [Bin];

		// Rotation
		int[][] Rotation = Layer_Base.getLayer("Rotation").getIntData();
		if (Rotation == null){
			Logger.info("Fail Rotation");
			layer = new Layer_Raw("Rotation"); layer.init();
			Rotation = Layer_Base.getLayer("Rotation").getIntData();
		}
			layer = Layer_Base.getLayer("Rotation");
			width = layer.getWidth();
			height = layer.getHeight();

		// DEM 
		// int[][] DEM = Layer_Base.getLayer("DEM").getIntData();
		// if (DEM == null){
			// Logger.info("Fail DEM");
			// layer = new Layer_Raw("DEM"); layer.init();
			// DEM = Layer_Base.getLayer("DEM").getIntData();
		// }
		
		// Slope
		/* int[][] Slope = Layer_Base.getLayer("Slope").getIntData();
		if (Slope == null){
			Logger.info("Fail Slope");
			layer = new Layer_Raw("Slope"); layer.init();
			Slope = Layer_Base.getLayer("Slope").getIntData();
		}
		
		// LCC 
		int[][] LCC = Layer_Base.getLayer("LCC").getIntData();
		if (LCC == null){
			Logger.info("Fail LCC");
			layer = new Layer_Raw("LCC"); layer.init();
			LCC = Layer_Base.getLayer("LCC").getIntData();
		}
		
		// LCS
		int[][] LCS = Layer_Base.getLayer("LCS").getIntData();
		if (LCS == null){
			Logger.info("Fail LCS");
			layer = new Layer_Raw("LCS"); layer.init();
			LCS = Layer_Base.getLayer("LCS").getIntData();
		}
		
		// Rivers 
		int[][] Rivers = Layer_Base.getLayer("Rivers").getIntData();
		if (Rivers == null){
			Logger.info("Fail Rivers");
			layer = new Layer_Raw("Rivers"); layer.init();
			Rivers = Layer_Base.getLayer("Rivers").getIntData();
		}
		
		// Roads
		int[][] Roads = Layer_Base.getLayer("Roads").getIntData();
		if (Roads == null){
			Logger.info("Fail Roads");
			layer = new Layer_Raw("Roads"); layer.init();
			Roads = Layer_Base.getLayer("Roads").getIntData();
		}
		
		// SOC 
		int[][] SOC = Layer_Base.getLayer("SOC").getIntData();
		if (SOC == null){
			Logger.info("Fail SOC");
			layer = new Layer_Raw("SOC"); layer.init();
			SOC = Layer_Base.getLayer("SOC").getIntData();
		}
		
		// Watersheds
		int[][] Watersheds = Layer_Base.getLayer("Watersheds").getIntData();
		if (Watersheds == null){
			Logger.info("Fail Watersheds");
			layer = new Layer_Raw("Watersheds"); layer.init();
			Watersheds = Layer_Base.getLayer("Watersheds").getIntData();
		} */
		
		Logger.info("About to output the model outcomes");
		try {
			Logger.info("Opening the files to write");
			// Generate output file to write the model outcome
			
			// Ethanol Production
			//PrintWriter out1 = HeaderWrite("Ethanol", width, height);
			// PrintWriter out1 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Ethanol.asc")));
			
			// Net Income
			//PrintWriter out2 = HeaderWrite("Net_Income", width, height);
			//PrintWriter out2 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Net_Income.asc")));
			
			// Soil Carbon
			//PrintWriter out3 = HeaderWrite("Soil_Carbon", width, height);
			//PrintWriter out3 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Soil_Carbon.asc")));
			
			// Bird Index
			PrintWriter out4 = HeaderWrite("Bird_Index", width, height, Output_Folder);
			//PrintWriter out4 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Bird_Index.asc")));
			
			// Nitrogen
			PrintWriter out5 = HeaderWrite("Nitrogen", width, height, Output_Folder);
			//PrintWriter out5 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Nitrogen.asc")));
			
			// Phosphorus
			PrintWriter out6 = HeaderWrite("Phosphorus", width, height, Output_Folder);
			//PrintWriter out6 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Phosphorus.asc")));
			
			// Pest
			PrintWriter out7 = HeaderWrite("Pest", width, height, Output_Folder);
			//PrintWriter out7 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Pest.asc")));
			
			// Pollinator 
			PrintWriter out8 = HeaderWrite("Pollinator", width, height, Output_Folder);
			//PrintWriter out8 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Pollinator.asc")));
			
			Logger.info("Writing array to the file");
			
			// Size of Window
			int Window_Size = 0;
			
			for (int y = 0; y < height; y++) {
				// StringBuffer sb1 = new StringBuffer();
				// StringBuffer sb2 = new StringBuffer();
				// StringBuffer sb3 = new StringBuffer();
				StringBuffer sb4 = new StringBuffer();
				StringBuffer sb5 = new StringBuffer();
				StringBuffer sb6 = new StringBuffer();
				StringBuffer sb7 = new StringBuffer();
				StringBuffer sb8 = new StringBuffer();
				
				for (int x = 0; x < width; x++) {
					if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					{
						// Check for No-Data Value
						// sb1.append(Integer.toString(NO_DATA));
						// sb2.append(Integer.toString(NO_DATA));
						// sb3.append(Integer.toString(NO_DATA));
						sb4.append(Integer.toString(NO_DATA));
						sb5.append(Integer.toString(NO_DATA));
						sb6.append(Integer.toString(NO_DATA));
						sb7.append(Integer.toString(NO_DATA));
						sb8.append(Integer.toString(NO_DATA));
					}
					else if (selection.mSelection[y][x] == 1)
					{
						// Formula to Compute Model Outcome
						// Initial Settings
						// The Sie of Moving Window Size to Calculate Bird Habitat
						int Buffer = 390; // In Meter
						Window_Size = Buffer/30; // Number of Cells in Raster Map
						float Prop_Ag = 0;
						int Count_Ag = 0;
						int Ag_Mask = 1 + 2 + 4 + 8 + 16 + 32 + 64 + 512; // 1, 2, 3, 4, 5, 6, 7, 10
						float Prop_Forest = 0;
						int Count_Forest = 0;
						int Forest_Mask = 1024; // 11
						float Prop_Grass = 0;
						int Count_Grass = 0;
						int Grass_Mask = 128 + 256; // 8 and 9
						// Calculate number of cells inside Bins
						int Value_H = 0;
						int Value_N = 0;
						int Value_P = 0;
						int Value_Pest = 0;
						int Value_Poll = 0;
						
						
						
						// Ethonal Production
						
						// Write Ethonal Production to The File
						//sb1.append(Float.toString(NO_DATA));
						
						
						
						// Net Income
						
						// Write Net Income to The File
						//sb2.append(Float.toString(NO_DATA));
						
						
						
						// Soil Carbon
						
						// Write Soil Carbon to The File
						//sb3.append(Float.toString(NO_DATA));
						

						
						// Calculate the Boundary for Moving Window
						Moving_Window mWin = new Moving_Window(x, y, Window_Size, width, height);
						
						// I to Width and J to Height
						for (int i = mWin.ULX; i <= mWin.LRX; i++) {
						for (int j = mWin.ULY; j <= mWin.LRY; j++) {
							if (RotationT[j][i] != 0)
							{
								mWin.Total++;
							if ((RotationT[j][i] & Ag_Mask) > 0)
							{
								Count_Ag = Count_Ag + 1;	
							}
							else if ((RotationT[j][i] & Forest_Mask) > 0 )
							{
								Count_Forest = Count_Forest + 1;
							}
							else if ((RotationT[j][i] & Grass_Mask) > 0 )
							{
								Count_Grass = Count_Grass + 1;
							}
						}
						}
						}
						
						
						
						// Compute Ag, forest and grass proportion
						// Agriculture Proportion
						Prop_Ag = (float)Count_Ag / mWin.Total;
						// Forest Proportion
						Prop_Forest = (float)Count_Forest / mWin.Total;
						// Grass Proportion
						Prop_Grass = (float)Count_Grass / mWin.Total;
						if (Prop_Ag < 0 || Prop_Ag > 1 || Prop_Forest < 0 || Prop_Forest > 1 || Prop_Grass < 0 || Prop_Grass > 1)
						{
							Logger.info("Out of range:" + Float.toString(Prop_Ag) + " " + Float.toString(Prop_Forest) + " " + Float.toString(Prop_Grass));
						}
						
						
						
						// Bird Habitat
						float Lambda = 0;
						float Habitat_Index = 0;
						// Lambda
						Lambda = -4.47f + 2.95f * Prop_Ag + 5.17f * Prop_Forest; 
						// Habitat Index
						Habitat_Index = (float)((1 / ( 1 / Math.exp(Lambda) + 1 ) ) / 0.67f);
						
						Habitat_Index_T = Habitat_Index + Habitat_Index_T;
						Value_H = (int)((Habitat_Index - Min_H)/(Max_H - Min_H)*(Bin-1));
						if (Value_H < 0 || Value_H >= Bin)
						{
							Logger.info("Out of range:" + Float.toString(Habitat_Index) + " " + Integer.toString(Value_H));
						}
						CountBin_H[Value_H]++;
						// Summary of Habitat Index
						// Write Habitat Index to The File
						sb4.append(String.format("%.4f", Habitat_Index));
						
						
						
						// Nitrogen
						float Nitrogen = (float)Math.pow(10, 1.13f * Prop_Ag - 0.23f);
						Min_N = (float)Math.pow(10, 1.13f * 0 - 0.23f);
						Max_N = (float)Math.pow(10, 1.13f * 1 - 0.23f);
						Nitrogen_T = Nitrogen + Nitrogen_T;
						// Summary of Nitrogen
						Value_N = (int)((Nitrogen - Min_N)/(Max_N - Min_N)*(Bin-1));
						if (Value_N < 0 || Value_N >= Bin)
						{
							Logger.info("Out of range:" + Float.toString(Nitrogen) + " " + Integer.toString(Value_N));
						}
						CountBin_N[Value_N]++;
						// Write Nitrogen to The File
						sb5.append(String.format("%.3f", Nitrogen));
						
						
						
						// Phosphorus 
						float Phosphorus = (float)Math.pow(10, 0.79f * Prop_Ag - 1.44f);
						Phosphorus_T = Phosphorus + Phosphorus_T;
						Min_P = (float)Math.pow(10, 0.79f * 0 - 1.44f);
						Max_P = (float)Math.pow(10, 0.79f * 1 - 1.44f);
						// Summary of Phosphorus
						Value_P = (int)((Phosphorus - Min_P)/(Max_P - Min_P)*(Bin-1));
						if (Value_P < 0 || Value_P >= Bin)
						{
							Logger.info("Out of range:" + Float.toString(Phosphorus) + " " + Integer.toString(Value_P));
						}
						CountBin_P[Value_P]++;
						// Write Phosphorus to The File
						sb6.append(String.format("%.3f", Phosphorus));
						
						
						
						// Pest 
						int Crop_Type = 0;
						if ((RotationT[y][x] & Ag_Mask) > 0)
						{
							Crop_Type = 0;	
						}
						else if ((RotationT[y][x] & Grass_Mask) > 0 )
						{
							Crop_Type = 1;
						}
						// Maximum is not really 1
						//Max_Pest = (float)(0.25 + 0.19f * 1 + 0.62f * 1);
						//Min_Pest = (float)(0.25 + 0.19f * 0 + 0.62f * 0);
						// Normalize using Max
						float Pest = (float)(0.25 + 0.19f * Crop_Type + 0.62f * Prop_Forest) / Max_Pest;
						Pest_T = Pest + Pest_T;
						// Summary of Pest
						Value_Pest = (int)(Pest*(Bin-1));
						if (Value_Pest < 0 || Value_Pest >= Bin)
						{
							Logger.info("Out of range:" + Float.toString(Pest) + " " + Integer.toString(Value_Pest));
						}
						CountBin_Pest[Value_Pest]++;
						// Write Pest to The File
						sb7.append(String.format("%.3f", Pest));
						
						
						
						// Pollinator
						//Max_Poll = (float)Math.pow(0.6617f + 2.98 * 1 + 1.83 * 1, 2);
						//Min_Poll = (float)Math.pow(0.6617f + 2.98 * 0 + 1.83 * 0, 2);
						// Normalize using Max
						float Poll = (float)Math.pow(0.6617f + 2.98 * Prop_Forest + 1.83 * Prop_Grass, 2) / Max_Poll;
						Pollinator_T = Poll / Max_Poll + Pollinator_T;
						// Summary of Pollinator
						Value_Poll = (int)(Poll * (Bin-1));
						if (Value_Poll < 0 || Value_Poll >= Bin)
						{
							Logger.info("Out of range:" + Float.toString(Poll) + " " + Integer.toString(Value_Poll));
						}
						CountBin_Poll[Value_Poll]++;
						// Write Pollinator to The File
						sb8.append(String.format("%.3f", Poll));
					}
					if (x != width - 1) 
					{
						// sb1.append(" ");
						// sb2.append(" ");
						// sb3.append(" ");
						sb4.append(" ");
						sb5.append(" ");
						sb6.append(" ");
						sb7.append(" ");
						sb8.append(" ");
					}
				}
				// out1.println(sb1.toString());
				// out2.println(sb2.toString());
				// out3.println(sb3.toString());
				out4.println(sb4.toString());
				out5.println(sb5.toString());
				out6.println(sb6.toString());
				out7.println(sb5.toString());
				out8.println(sb6.toString());
			}
			Logger.info("Closing the Files");
			// out1.close();
			// out2.close();
			// out3.close();
			out4.close();
			out5.close();
			out6.close();
			out7.close();
			out8.close();
			Logger.info("Writting to the Files has finished");
		}
		catch(Exception err) {
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with writing to the files!");
		}

		// Data to return to the client		
		ObjectNode obj = JsonNodeFactory.instance.objectNode();
		ObjectNode H_I = JsonNodeFactory.instance.objectNode();
		ObjectNode Nitr = JsonNodeFactory.instance.objectNode();
		ObjectNode Phos = JsonNodeFactory.instance.objectNode();
		ObjectNode Pest = JsonNodeFactory.instance.objectNode();
		ObjectNode Poll = JsonNodeFactory.instance.objectNode();
		
		// Habitat Index
		ArrayNode HI = JsonNodeFactory.instance.arrayNode();
		float Total_Cells = 0;
		float HI_Per_Cell = 0;
		for (int i = 0; i < CountBin_H.length; i++) {
			Total_Cells = CountBin_H[i] + Total_Cells;
			HI.add(CountBin_H[i]);
		}
		// Average of Habitat_Index per pixel
		HI_Per_Cell = Habitat_Index_T / Total_Cells;
		
		// Nitrogen
		ArrayNode N = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < CountBin_N.length; i++) {
			N.add(CountBin_N[i]);
		}
		
		// Phosphorus
		ArrayNode P = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < CountBin_P.length; i++) {
			P.add(CountBin_P[i]);
		}
		
		// Pest
		float Pest_Per_Cell = 0;
		ArrayNode PestS = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < CountBin_Pest.length; i++) {
			PestS.add(CountBin_Pest[i]);
		}
		// Average of Pest per pixel
		Pest_Per_Cell = Pest_T / Total_Cells;
		
		// Pollinator
		float Pollinator_Per_Cell = 0;
		ArrayNode Pollin = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < CountBin_Poll.length; i++) {
			Pollin.add(CountBin_Poll[i]);
		}
		// Average of Pollinator per pixel
		Pollinator_Per_Cell = Pollinator_T / Total_Cells;
		
		// Habitat_Index
		H_I.put("Result", HI);
		H_I.put("Min", Min_H);
		H_I.put("Max", Max_H);
		H_I.put("Average_HI", HI_Per_Cell);
		
		// Nitrogen
		Nitr.put("Result", N);
		Nitr.put("Min", Min_N);
		Nitr.put("Max", Max_N);
		Nitr.put("Nitrogen", Nitrogen_T * 900 / 1000000);
		
		// Phosphorus
		Phos.put("Result", P);
		Phos.put("Min", Min_P);
		Phos.put("Max", Max_P);
		Phos.put("Phosphorus", Phosphorus_T * 900 / 1000000);
		
		// Pest
		Pest.put("Result", PestS);
		Pest.put("Min", 0);
		Pest.put("Max", 1);
		Pest.put("Pest", Pest_Per_Cell);
		
		// Pollinator
		Poll.put("Result", Pollin);
		Poll.put("Min", 0);
		Poll.put("Max", 1);
		Poll.put("Pollinator", Pollinator_Per_Cell);
		
		// Add branches to JSON Node 
		obj.put("Habitat_Index", H_I);
		obj.put("Nitrogen", Nitr);
		obj.put("Phosphorus", Phos);
		obj.put("Pest", Pest);
		obj.put("Pollinator", Poll);
		
                Logger.info(obj.toString());
		return obj;
		
		// GetIt.put("URL", urlPath);
		// GetIt.put("Pixels", count);
		// GetIt.put("Total", mHeight * mWidth);
		// return GetIt;
	}	

	// Moving Window Function
	// X Location, Y Location and Window Size
	// Window Size should be odd
	public class Moving_Window
	{
		public int ULX, ULY, LRX, LRY, Total;
		public Moving_Window(int x, int y, int wsz, int w, int h)
		{
			ULX = x - wsz/2;
			ULY = y - wsz/2;
			LRX = x + wsz/2;
			LRY = y + wsz/2;
			
			// Left
			if (ULX < 0)
			{
				ULX = 0;
			}
			// Up
			if (ULY < 0)
			{
				ULY = 0;
			}
			// Right
			if (LRX > w - 1)
			{
				LRX = w - 1;
			}
			// Low
			if (LRY > h - 1)
			{
				LRY = h - 1;
			}

			Total = 0;	
		}
	}
	
	
	
	// Write Header To The File
	public PrintWriter HeaderWrite(String name, int W, int H, String Output_Folder) 
	{
		PrintWriter out = null;
		
		try 
		{
			out = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/" + Output_Folder + "/" + name + ".asc")));
		} 
		catch (Exception e) 
		{
			Logger.info(e.toString());
		}
		
		out.println("ncols         " + Integer.toString(W));
		out.println("nrows         " + Integer.toString(H));
		out.println("xllcorner     -10062652.65061");
		out.println("yllcorner     5249032.6922889");
		out.println("cellsize      30");
		out.println("NODATA_value  -9999");
		
		return out;
	}
	
	// Read The Header of The File
	public BufferedReader HeaderRead(String name, int W, int H, String Input_Folder) 
	{
		BufferedReader br = null;
		
		try 
		{
			br = new BufferedReader(new FileReader("./layerData/" + Input_Folder + "/" + name + ".asc"));
			String line = br.readLine();
			line = br.readLine();
			line = br.readLine();
			line = br.readLine();
			line = br.readLine();
			line = br.readLine();
		} 
		catch (Exception e) 
		{
			Logger.info(e.toString());
		}
		
		return br;
	}
	
}
