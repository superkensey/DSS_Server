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
	
	

	//--------------------------------------------------------------------------
	public JsonNode modeloutcome(Selection selection, String Output_Folder, int[][] RotationT)
	{
		
		float Ethanol_T = 0;
		float Net_Income_T = 0;
		float Net_Energy_T = 0;
		float Habitat_Index_T = 0;
		float Phosphorus_T = 0;
		float Nitrogen_T = 0;
		float Pest_T = 0;
		float Pollinator_T = 0;
	
		Layer_Base layer;
		int width, height;
		int NO_DATA = -9999;
		float Total_Cells = selection.countSelectedPixels();
		float Total_Bins_Num1 = 0; 
		float Total_Bins_Num2 = 0; 
		//Logger.info("Total_Cells:" + Float.toString(Total_Cells));
		
		// Ton/ha
		//float Min_Corn_Y = 3.08f - 0.11f * 70;
		float Min_Corn_Y = 3.08f - 0.11f * 70;
		float Max_Corn_Y = 3.08f + 0.02f * 210 + 0.10f * 75 + 0.04f * 200;
		// Tons per pixel
		//float Min_Corn_P = 0.0001f * 900 * Min_Corn_Y;
		float Min_Corn_P = 0.0001f * 900 * Min_Corn_Y;
		float Max_Corn_P = 0.0001f * 900 * Max_Corn_Y;
		//Logger.info("Min_CP:" + Float.toString(Min_Corn_P));
		//Logger.info("Max_CP:" + Float.toString(Max_Corn_P));
		// Ton/ha
		//float Min_Grass_Y = 2.20f - 0.07f * 70;
		float Min_Grass_Y = 2.20f - 0.07f * 70;
		float Max_Grass_Y = 2.20f + 0.02f * 210 + 0.07f * 75 + 0.03f * 200;
		// Tons per pixel
		//float Min_Grass_P = 0.0001f * 900 * Min_Grass_Y;
		float Min_Grass_P = 0.0001f * 900 * Min_Grass_Y;
		float Max_Grass_P = 0.0001f * 900 * Max_Grass_Y;
		//Logger.info("Min_GP:" + Float.toString(Min_Grass_P));
		//Logger.info("Max_GP:" + Float.toString(Max_Grass_P));
		// Ethanol
		// Ethonal Calculation 
		// Lit per pixel
		float C_E_Min = Min_Corn_P * 0.5f * 0.4f * 1000 + Min_Corn_P * 0.25f * 0.38f * 1000;
		float C_E_Max = Max_Corn_P * 0.5f * 0.4f * 1000 + Max_Corn_P * 0.25f * 0.38f * 1000;
		//Logger.info("C_Max:" + Float.toString(C_Max));
		//float C_Max = (3.08f - 0.11f * 0 + 0.02f * 201 + 0.10f * 75 + 0.04f * 190) * 0.5f * 0.4f + (3.08f - 0.11f * 0 + 0.02f * 201 + 0.10f * 75 + 0.04f * 190) * 0.25f * 0.38f;
		//float C_Max = (0.0001f * 900 * (3.08f + 0.02f * 201 + 0.10f * 75 + 0.04f * 190)) * 0.5f * 0.4f / 1000 + (0.0001f * 900 * (3.08f + 0.02f * 201 + 0.10f * 75 + 0.04f * 190)) * 0.25f * 0.38f / 1000;
		//Logger.info("C_Max:" + Float.toString(C_Max));
		//float C_Min = (3.08f - 0.11f * 100 + 0.02f * 0 + 0.10f * 0 + 0.04f * 0) * 0.5f * 0.4f + (3.08f - 0.11f * 100 + 0.02f * 0 + 0.10f * 0 + 0.04f * 0) * 0.25f * 0.38f;
		float G_E_Min = Min_Grass_P * 0.38f * 1000;
		float G_E_Max = Max_Grass_P * 0.38f * 1000;
		float E_Min = 0;
		float E_Max = 0;
		// Min in E
		if (G_E_Min <= C_E_Min)
		{
			E_Min = G_E_Min;
		}
		else 
		{
			E_Min = C_E_Min;
		}
		// Max in E
		if (G_E_Max <= C_E_Max)
		{
			E_Max = C_E_Max;
		}
		else 
		{
			E_Max = G_E_Max;
		}
		float E_Min_T = 1000;
		float E_Max_T = -1000;
		//float G_Max = (2.20f - 0.07f * 0 + 0.02f * 201 + 0.07f * 75 + 0.03f * 190) * 0.38f;
		//float G_Max = 0.0001f * 900 * (2.20f + 0.02f * 201 + 0.07f * 75 + 0.03f * 190) * 0.38f / 1000;
		//Logger.info("G_Max:" + Float.toString(G_Max));
		//float G_Min = (2.20f - 0.07f * 100 + 0.02f * 0 + 0.07f * 0 + 0.03f * 0) * 0.38f;
		//Logger.info(C_Max + C_Min + G_Max + G_Min);
		//float Min_E_C = C_Min;
		//float Max_E_C = C_Max;
		//float Min_E_G = G_Min;
		//float Max_E_G = G_Max;
		//Logger.info("Max_E:" + Float.toString(Max_E));
		//Logger.info("E_MAX:" + Float.toString(Max_E));
		
		// HI
		float Min_H = 0;
		float Max_H = 1;
		float Min_H_T = 0;
		float Max_H_T = 1;
		
		// Nitrogen
		float Min_N = (float)Math.pow(10, 1.13f * 0 - 0.23f);
		float Max_N = (float)Math.pow(10, 1.13f * 1 - 0.23f);
		//Logger.info("Min_N:" + Float.toString(Min_N));
		//Logger.info("Max_N:" + Float.toString(Max_N));
		float Min_N_T = 10000;
		float Max_N_T = -10000;
		
		// Phosphrous
		float Min_P = (float)Math.pow(10, 0.79f * 0 - 1.44f);
		float Max_P = (float)Math.pow(10, 0.79f * 1 - 1.44f);
		//Logger.info("Min_Ph:" + Float.toString(Min_P));
		//Logger.info("Max_Ph:" + Float.toString(Max_P));
		float Min_P_T = 10000;
		float Max_P_T = -10000;
		
		// Pest
		float Min_Pest = (float)(0.25 + 0.19f * 0 + 0.62f * 0);
		float Max_Pest = (float)(0.25 + 0.19f * 1 + 0.62f * 1);
		//Logger.info("Min_Pest:" + Float.toString(Min_Pest));
		//Logger.info("Max_Pest:" + Float.toString(Max_Pest));
		float Min_Pest_T = 10000;
		float Max_Pest_T = -10000;
		
		// Pollinator
		float Min_Poll = (float)Math.pow(0.6617f + 2.98 * 0 + 1.83 * 0, 2);
		float Max_Poll = (float)Math.pow(0.6617f + 2.98 * 1 + 1.83 * 1, 2);
		//Logger.info("Min_Poll:" + Float.toString(Min_Poll));
		//Logger.info("Max_Poll:" + Float.toString(Max_Poll));
		float Min_Poll_T = 10000;
		float Max_Poll_T = -10000;
		
		// Net Income
		// $ per pixel cost for Corn
		float PC_Cost = 1124 * 0.0001f * 900;
		// $ per pixel cost for Grass
		float PG_Cost = 412  * 0.0001f * 900;
		// Price per pixel
		float P_Per_Corn = 300;
		float P_Per_Stover = 100;
		float P_Per_Grass = 100;
		// Calculation
		float Min_NI_C = P_Per_Corn * 0.5f * Min_Corn_P + P_Per_Stover * 0.25f * Min_Corn_P - PC_Cost;
		float Max_NI_C = P_Per_Corn * 0.5f * Max_Corn_P + P_Per_Stover * 0.25f * Max_Corn_P - PC_Cost;
		//Logger.info("Max_NI_C:" + Float.toString(Max_NI_C));
		float Min_NI_G = P_Per_Grass * Min_Grass_P - PG_Cost;
		float Max_NI_G = P_Per_Grass * Max_Grass_P - PG_Cost;
		// Net Income
		float NI_Min = 0;
		float NI_Max = 0;
		// Min in Net Income
		if (Min_NI_C <= Min_NI_G)
		{
			NI_Min = Min_NI_C;
		}
		else 
		{
			NI_Min = Min_NI_G;
		}
		// Max in Net Income
		if (Max_NI_C <= Max_NI_G)
		{
			NI_Max = Max_NI_G;
		}
		else 
		{
			NI_Max = Max_NI_C;
		}
		float NI_Min_T =  10000;
		float NI_Max_T = -10000;
		//Logger.info("Max_NI_G:" + Float.toString(Max_NI_G));
		//float Min_NI_CG = Min_NI_C + Min_NI_G;
		//float Max_NI_CG = Max_NI_C + Max_NI_G;
		//Logger.info("Max_NI_CG:" + Float.toString(Max_NI_CG));
		//Logger.info("Max_NI:" + Float.toString(Max_NI));	
		
		// Net Energy
		//float Max_NE_C = (Max_Corn_P * 0.5f * 0.00040f * 21.20f + Max_Corn_P * 0.25f * 0.00038f * 21.20f) - (18.92f / 10000 * 900 + 7.41f / 10000 * 900 + 15.25f * Min_Corn_P * 0.5f * 0.4f * 1000 + 1.71f * Min_Corn_P * 0.25f * 0.38f * 1000);
		//float Max_NE_C = (Max_Corn_P * 0.5f * 0.40f * 21.20f + Max_Corn_P * 0.25f * 0.38f * 21.20f) - (18.92f / 10000 * 900 + 7.41f / 10000 * 900 + 15.25f * Min_Corn_P * 0.5f * 0.4f * 1000 + 1.71f * Min_Corn_P * 0.25f * 0.38f * 1000);
		// float Max_NE_C = (Max_Corn_P * 0.5f * 0.00040f * 21.20f + Max_Corn_P * 0.25f * 0.00038f * 21.20f) - (18.92f / 10000 * 900 + 7.41f / 10000 * 900 + 15.25f * Min_Corn_P * 0.5f * 0.0004f + 1.71f * Min_Corn_P * 0.25f * 0.00038f);
		float Min_NE_C = (Min_Corn_P * 0.5f * 0.4f * 1000 * 21.20f + Min_Corn_P * 0.25f * 0.38f * 1000 * 21.20f) - (18.92f / 10000 * 900 + 7.41f / 10000 * 900 + 15.25f * Max_Corn_P * 0.5f * 0.4f * 1000 + 1.71f * Max_Corn_P * 0.25f * 0.38f * 1000);
		float Max_NE_C = (Max_Corn_P * 0.5f * 0.4f * 1000 * 21.20f + Max_Corn_P * 0.25f * 0.38f * 1000 * 21.20f) - (18.92f / 10000 * 900 + 7.41f / 10000 * 900 + 15.25f * Min_Corn_P * 0.5f * 0.4f * 1000 + 1.71f * Min_Corn_P * 0.25f * 0.38f * 1000);
		//Logger.info("Max_NE_C:" + Float.toString(Max_NE_C));
		//float Max_NE_G = (Max_Grass_P * 0.00038f * 21.20f) - (7.41f / 10000 * 900 + 1.71f * Min_Grass_P * 0.38f * 1000);
		//float Max_NE_G = (Max_Grass_P * 0.38f * 21.20f) - (7.41f / 10000 * 900 + 1.71f * Min_Grass_P * 0.38f * 1000);
		//float Max_NE_G = (Max_Grass_P * 0.00038f * 21.20f) - (7.41f / 10000 * 900 + 1.71f * Min_Grass_P * 0.00038f);
		float Min_NE_G = (Min_Grass_P * 0.38f * 1000 * 21.20f) - (7.41f / 10000 * 900 + 1.71f * Max_Grass_P * 0.38f * 1000);
		float Max_NE_G = (Max_Grass_P * 0.38f * 1000 * 21.20f) - (7.41f / 10000 * 900 + 1.71f * Min_Grass_P * 0.38f * 1000);
		// Net Energy
		float NE_Min = 0;
		float NE_Max = 0;
		// Min in Net Income
		if (Min_NE_C <= Min_NE_G)
		{
			NE_Min = Min_NE_C;
		}
		else 
		{
			NE_Min = Min_NI_G;
		}
		// Max in Net Income
		if (Max_NE_G <= Max_NE_C)
		{
			NE_Max = Max_NE_C;
		}
		else 
		{
			NE_Max = Max_NE_G;
		}
		float NE_Min_T =  10000;
		float NE_Max_T = -10000;
		//Logger.info("Max_NE_G:" + Float.toString(Max_NE_G));
		//float Min_NE = Min_NE_C + Min_NE_G;
		//float Max_NE = Max_NE_C + Max_NE_G;
		//Logger.info("Max_NE:" + Float.toString(Max_NE));

		// Number of Bins
		//int Bin = 10;
		int Bin = 50;
		int Bin_T = 10;
		
		// Bins
		int[] CountBin_E = new int [Bin];
		int[] CountBin_H = new int [Bin];
		int[] CountBin_N = new int [Bin];
		int[] CountBin_P = new int [Bin];
		int[] CountBin_Pest = new int [Bin];
		int[] CountBin_Poll = new int [Bin];
		int[] CountBin_NI = new int [Bin];
		int[] CountBin_NE = new int [Bin];
		
		// Bins
		int[] CountBin_E_T = new int [Bin_T];
		int[] CountBin_H_T = new int [Bin_T];
		int[] CountBin_N_T = new int [Bin_T];
		int[] CountBin_P_T = new int [Bin_T];
		int[] CountBin_Pest_T = new int [Bin_T];
		int[] CountBin_Poll_T = new int [Bin_T];
		int[] CountBin_NI_T = new int [Bin_T];
		int[] CountBin_NE_T = new int [Bin_T];
		
		// Rotation
		if (RotationT == null)
		{
			Logger.info("Fail Rotation");
			layer = new Layer_Integer("Rotation"); layer.init();
			RotationT = Layer_Base.getLayer("Rotation").getIntData();
		}
		layer = Layer_Base.getLayer("Rotation");
		width = layer.getWidth();
		height = layer.getHeight();

		try 
		{
			Logger.info("Opening the files to write");
			
			// Raad Input Files
			BufferedReader br1 = HeaderRead("slope-soil", width, height, "Inputs");
			BufferedReader br2 = HeaderRead("depth", width, height, "Inputs");
			BufferedReader br3 = HeaderRead("silt", width, height, "Inputs");
			BufferedReader br4 = HeaderRead("cec", width, height, "Inputs");
			
			// Generate output file to write the model outcome
		
			// Ethanol Production
			//PrintWriter out1 = HeaderWrite("Ethanol", width, height, Output_Folder);
			// PrintWriter out1 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Ethanol.asc")));
			
			// Net Income
			//PrintWriter out2 = HeaderWrite("Net_Income", width, height, Output_Folder);
			//PrintWriter out2 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Net_Income.asc")));
			
			// Net Energy
			//PrintWriter out3 = HeaderWrite("Net_Energy", width, height, Output_Folder);
			//PrintWriter out3 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Net_Energy.asc")));
			
			// Bird Index
			//PrintWriter out4 = HeaderWrite("Bird_Index", width, height, Output_Folder);
			//PrintWriter out4 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Bird_Index.asc")));
			
			// Nitrogen
			//PrintWriter out5 = HeaderWrite("Nitrogen", width, height, Output_Folder);
			//PrintWriter out5 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Nitrogen.asc")));
			
			// Phosphorus
			//PrintWriter out6 = HeaderWrite("Phosphorus", width, height, Output_Folder);
			//PrintWriter out6 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Phosphorus.asc")));
			
			// Pest
			//PrintWriter out7 = HeaderWrite("Pest", width, height, Output_Folder);
			//PrintWriter out7 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Pest.asc")));
			
			// Pollinator 
			//PrintWriter out8 = HeaderWrite("Pollinator", width, height, Output_Folder);
			//PrintWriter out8 = new PrintWriter(new BufferedWriter(new FileWriter("./layerData/Pollinator.asc")));
			
			//Logger.info("Writing array to the file");

			// Size of Window
			//int Window_Size = 0;
			//int Count_C = 0;
			//int Count_G = 0;
			
			int Buffer = 390; // In Meter
			int Window_Size = Buffer / 30; // Number of Cells in Raster Map
			int Ag_Mask = 1 + 2 + 4 + 8 + 16 + 32 + 64 + 512; // 1, 2, 3, 4, 5, 6, 7, 10
			int Forest_Mask = 1024; // 11
			int Grass_Mask = 128 + 256; // 8 and 9
			int Corn_Mask = 1; // 1
									
			// Precompute this so we don't do it on every cell
			String stringNoData = Integer.toString(NO_DATA);
			
			for (int y = 0; y < height; y++) 
			{
				// Inputs
				String line1 = br1.readLine();
				String slope[] = line1.split("\\s+");
				String line2 = br2.readLine();
				String soil[] = line2.split("\\s+");
				String line3 = br3.readLine();
				String silt[] = line3.split("\\s+");
				String line4 = br4.readLine();
				String cec[] = line4.split("\\s+");
				//Logger.info(Float.toString(0) + " " + Float.toString(0) + " " + Float.toString(Float.parseFloat(slope[0])) + " " + Float.toString(Float.parseFloat(soil[0])) + " " + Float.toString(Float.parseFloat(silt[0])) + " " + Float.toString(Float.parseFloat(cec[0])));
							
				// Outputs
				//StringBuffer sb1 = new StringBuffer();
				//StringBuffer sb2 = new StringBuffer();
				//StringBuffer sb3 = new StringBuffer();
				//StringBuffer sb4 = new StringBuffer();
				//StringBuffer sb5 = new StringBuffer();
				//StringBuffer sb6 = new StringBuffer();
				//StringBuffer sb7 = new StringBuffer();
				//StringBuffer sb8 = new StringBuffer();
				
				for (int x = 0; x < width; x++) 
				{				
					//if (RotationT[y][x] == 0 || selection.mSelection[y][x] == 0) 
					//{
						// Check for No-Data Value
						//sb1.append(stringNoData);
						//sb2.append(stringNoData);
						//sb3.append(stringNoData);
						//sb4.append(stringNoData);
						//sb5.append(stringNoData);
						//sb6.append(stringNoData);
						//sb7.append(stringNoData);
						//sb8.append(stringNoData);
					//}
					//else 
					if (selection.mSelection[y][x] == 1)
					{
						// Formula to Compute Model Outcome
						// Initial Settings
						// The Sie of Moving Window Size to Calculate Bird Habitat
						//int Buffer = 150; // In Meter
						
						float Prop_Ag = 0;
						int Count_Ag = 0;
						float Prop_Forest = 0;
						int Count_Forest = 0;
						float Prop_Grass = 0;
						int Count_Grass = 0;
						

						
						// Calculate number of cells inside Bins
						int Value_E = 0;
						int Value_H = 0;
						int Value_N = 0;
						int Value_P = 0;
						int Value_Pest = 0;
						int Value_Poll = 0;
						int Value_NI = 0;
						int Value_NE = 0;
						
						
						
						// Ethanol Production
						float Corn_Y = 0;
						float Corn_P = 0;
						float Grass_Y = 0;
						float Grass_P = 0;
						float Ethanol = 0;
						//float Ethanol_C = 0;
						//float Ethanol_G = 0;
						//float Ethanol_T = 0;
						//float Ethanol_TT = 0;
						
						
						// Calculate prodcution and gross cost 
						// $ per pixel for Corn
						//float PC_Cost = 1124 * 0.0001f * 900;
						// $ per pixel for Grass
						//float PG_Cost = 412 * 0.0001f * 900;
						float Return = 0;
						//float GC_Return = 0;
						//float GG_Return = 0;
						float Net_Income = 0;
						
						// Energy
						//float NetC_Energy = 0;
						//float NetG_Energy = 0;
						float Net_Energy = 0;
						
						
						
						if ((RotationT[y][x] & Corn_Mask) > 0)
						{
							// Tonnes per Ha
							Corn_Y = 3.08f - 0.11f * Float.parseFloat(slope[x]) + 0.02f * Float.parseFloat(soil[x]) + 0.10f * Float.parseFloat(silt[x]) + 0.04f * Float.parseFloat(cec[x]);
							Corn_P = 0.0001f * 900 * Corn_Y;
							// Ethonal Calculation
							// Tonnes per pixel
							Ethanol = Corn_P * 0.5f * 0.4f * 1000 + Corn_P * 0.25f * 0.38f * 1000;
							E_Min_T = Min(E_Min_T, Ethanol);
							E_Max_T = Max(E_Max_T, Ethanol);
							//Ethanol_T = Ethanol_C / C_E_Max;
							//Ethanol_T = Ethanol_C;
							Value_E = (int)((Ethanol - E_Min)/(E_Max - E_Min) * (Bin - 1));
							CountBin_E[Value_E]++;
							//Ethanol_C = (Corn_Y * 0.5f * 0.4f + Corn_Y * 0.25f * 0.38f)/C_Max;
							//Ethanol_G = 0;
							//Count_C++;
							// Net_Income Calculation
							Return = P_Per_Corn * 0.5f * Corn_P + P_Per_Stover * 0.25f * Corn_P;
							//Net_Income = (GC_Return  - PC_Cost) / Max_NI_C;
							Net_Income = Return  - PC_Cost;
							NI_Min_T = Min(NI_Min_T, Net_Income);
							NI_Max_T = Max(NI_Max_T, Net_Income);
							Value_NI = (int)((Net_Income - NI_Min)/(NI_Max - NI_Min) * (Bin - 1));
							CountBin_NI[Value_NI]++;
							// Net_Energy Calculation (Mega Jul)
							Net_Energy = (Corn_P * 0.5f * 0.4f * 1000 * 21.20f + Corn_P * 0.25f * 0.38f * 1000 * 21.20f) - (18.92f / 10000 * 900 + 7.41f / 10000 * 900 + 15.25f * Corn_P * 0.5f * 0.4f * 1000 + 1.71f * Corn_P * 0.25f * 0.38f * 1000);
							NE_Min_T = Min(NE_Min_T, Net_Energy);
							NE_Max_T = Max(NE_Max_T, Net_Energy);
							//Net_Energy = NetC_Energy / Max_NE_C;
							//Net_Energy = NetC_Energy;
							Value_NE = (int)((Net_Energy - NE_Min)/(NE_Max - NE_Min) * (Bin - 1));
							CountBin_NE[Value_NE]++;
							//NetC_Energy = (Corn_P * 0.5f * 0.00040f * 21.20f + Corn_P * 0.25f * 0.00038f * 21.20f) - (18.92f / 10000 * 900 + 7.41f / 10000 * 900 + 15.25f * Corn_P * 0.5f * 0.0004f + 1.71f * Corn_P * 0.25f * 0.00038f);
							//NetC_Energy = (Corn_P * 0.5f * 0.40f * 21.20f + Corn_P * 0.25f * 0.38f * 21.20f) - (18.92f / 10000 * 900 + 7.41f / 10000 * 900 + 15.25f * Corn_P * 0.5f * 0.4f * 1000 + 1.71f * Corn_P * 0.25f * 0.38f * 1000);
						}
						else if ((RotationT[y][x] & Grass_Mask) > 0)
						{
							Grass_Y = 2.20f - 0.07f * Float.parseFloat(slope[x]) + 0.02f * Float.parseFloat(soil[x]) + 0.07f * Float.parseFloat(silt[x]) + 0.03f * Float.parseFloat(cec[x]);
							Grass_P = 0.0001f * 900 * Grass_Y;
							// Ethonal Calculation
							// Tonnes per pixel
							Ethanol = Grass_P * 0.38f * 1000;
							E_Min_T = Min(E_Min_T, Ethanol);
							E_Max_T = Max(E_Max_T, Ethanol);
							//Ethanol_T = Ethanol_G / G_E_Max;
							//Ethanol_T = Ethanol_G;
							Value_E = (int)((Ethanol - E_Min)/(E_Max - E_Min) * (Bin - 1));
							CountBin_E[Value_E]++;
							//Ethanol_G = (Grass_Y * 0.38f)/G_Max;
							//Ethanol_C = 0;
							//Count_G++;
							// Net_Income Calculation
							Return = P_Per_Grass * Grass_P;
							//Net_Income = (GG_Return - PG_Cost) / Max_NI_G;
							Net_Income = Return - PG_Cost;
							NI_Min_T = Min(NI_Min_T, Net_Income);
							NI_Max_T = Max(NI_Max_T, Net_Income);
							Value_NI = (int)((Net_Income - NI_Min)/(NI_Max - NI_Min) * (Bin - 1));
							CountBin_NI[Value_NI]++;
							// Net_Energy Calculation (Mega Jul)
							Net_Energy = (Grass_P * 0.38f * 1000 * 21.20f) - (7.41f / 10000 * 900 + 1.71f * Grass_P * 0.38f * 1000);
							NE_Min_T = Min(NE_Min_T, Net_Energy);
							NE_Max_T = Max(NE_Max_T, Net_Energy);
							//Net_Energy = NetC_Energy / Max_NE_G;
							//Net_Energy = NetC_Energy;
							Value_NE = (int)((Net_Energy - NE_Min)/(NE_Max - NE_Min) * (Bin - 1));
							CountBin_NE[Value_NE]++;
							//NetG_Energy = (Grass_P * 0.38f * 21.20f) - (7.41f / 10000 * 900 + 1.71f * Grass_P * 0.38f * 1000);
						}
						else 
						{
							E_Min_T = Min(E_Min_T, E_Min);
							E_Max_T = Max(E_Max_T, E_Max);
							NI_Min_T = Min(NI_Min_T, NI_Min);
							NI_Max_T = Max(NI_Max_T, NI_Max);
							NE_Min_T = Min(NE_Min_T, NE_Min);
							NE_Max_T = Max(NE_Max_T, NE_Max);
						}
						
						
						
						// Ethonal Calculation
						Ethanol_T += Ethanol;
						//if (Ethanol < 0 || Ethanol > 1)
						//{
						//	Logger.info("Out of range E: " + Float.toString(RotationT[y][x]) + " " + Float.toString(y) + " " + Float.toString(x) + " " + Float.toString(Float.parseFloat(slope[x])) + " " + Float.toString(Float.parseFloat(soil[x])) + " " + Float.toString(Float.parseFloat(silt[x])) + " " + Float.toString(Float.parseFloat(cec[x])));
						//	Logger.info("Out of range E: " + Float.toString(Ethanol));
						//}
						//Value_E = (int)((Ethanol_C + Ethanol_G - Min_E)/(Max_E - Min_E)*(Bin-1));
						//Value_E = (int)(Ethanol_T * (Bin - 1));
						//if (Value_E < 0 || Value_E >= Bin)
						//{
						//	Logger.info("Out of range E: " + Float.toString(RotationT[y][x]) + " " + Float.toString(y) + " " + Float.toString(x) + " " + Float.toString(Float.parseFloat(slope[x])) + " " + Float.toString(Float.parseFloat(soil[x])) + " " + Float.toString(Float.parseFloat(silt[x])) + " " + Float.toString(Float.parseFloat(cec[x])));
						//	Logger.info("Out of range E: " + Float.toString(Ethanol) + " " + Integer.toString(Value_E));
						//}
						//CountBin_E[Value_E]++;
						// Summary of Habitat Index
						// Write Habitat Index to The File
						//sb1.append(String.format("%.4f", Ethanol_T));
						
						
						
						// Net Income
						Net_Income_T += Net_Income;
						// Write Net Income to The File
						//if (Net_Income < 0 || Net_Income > 1)
						//{
						//	Logger.info("Out of range GC_Return: " + Float.toString(Return) + " " + "Out of range GG_Return: " + Float.toString(Return));
						//	Logger.info("Out of range PC_Cost: " + Float.toString(PC_Cost) + " " + "Out of range PG_Cost: " + Float.toString(PG_Cost));
						//	Logger.info("Out of range NI: " + Float.toString(Net_Income));
						//}
						//if (Value_NI < 0 || Value_NI >= Bin)
						//{
						//	Logger.info("Out of range NI:" + Float.toString(Net_Income) + " " + Integer.toString(Value_NI));
						//}
						//}
						//sb2.append(String.format("%.4f", Net_Income));
						
						
						
						// Net Energy
						Net_Energy_T += Net_Energy;
						// Write Net Energy to The File
						//if (Net_Energy < 0 || Net_Energy > 1)
						//{
						//	Logger.info("Out of range NE_C:" + Float.toString(NetC_Energy) + " " + "Out of range NE_G:" + Float.toString(NetG_Energy));
						//	Logger.info("Out of range NE:" + Float.toString(RotationT[y][x]) + " " + Float.toString(y) + " " + Float.toString(x) + " " + Float.toString(Float.parseFloat(slope[x])) + " " + Float.toString(Float.parseFloat(soil[x])) + " " + Float.toString(Float.parseFloat(silt[x])) + " " + Float.toString(Float.parseFloat(cec[x])));
						//	Logger.info("Out of range NE:" + Float.toString(Net_Energy));
						//}
						//if (Value_NE < 0 || Value_NE >= Bin)
						//{
						//	Logger.info("Out of range NE:" + Float.toString(Net_Energy) + " " + Integer.toString(Value_NE));
						//}
						//}
						//sb3.append(Float.toString(Net_Energy));
		
						
						
						// Calculate the Boundary for Moving Window
						Moving_Window mWin = new Moving_Window(x, y, Window_Size, width, height);
						
						// I to Width and J to Height
						for (int j = mWin.ULY; j <= mWin.LRY; j++) 
						{
							for (int i = mWin.ULX; i <= mWin.LRX; i++) 
							{
								if (RotationT[j][i] != 0)
								{
									mWin.Total++;
									if ((RotationT[j][i] & Ag_Mask) > 0)
									{
										Count_Ag = Count_Ag + 1;	
									}
									else if ((RotationT[j][i] & Forest_Mask) > 0)
									{
										Count_Forest = Count_Forest + 1;
									}
									else if ((RotationT[j][i] & Grass_Mask) > 0)
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
						//if (Prop_Ag < 0 || Prop_Ag > 1 || Prop_Forest < 0 || Prop_Forest > 1 || Prop_Grass < 0 || Prop_Grass > 1)
						//{
						//	Logger.info("Out of range:" + Float.toString(Prop_Ag) + " " + Float.toString(Prop_Forest) + " " + Float.toString(Prop_Grass));
						//}
						
						
						
						// Bird Habitat
						// Lambda
						float Lambda = -4.47f + 2.95f * Prop_Ag + 5.17f * Prop_Grass; 
						// Habitat Index
						float Habitat_Index = (float)((1 / ( 1 / Math.exp(Lambda) + 1 ) ) / 0.67f);
						Habitat_Index_T = Habitat_Index + Habitat_Index_T;
						//Value_H = (int)((Habitat_Index - Min_H)/(Max_H - Min_H)*(Bin-1));
						Value_H = (int)(Habitat_Index * (Bin - 1));
						//if (Value_H < 0 || Value_H >= Bin)
						//{
						//	Logger.info("Out of range HI:" + Float.toString(Habitat_Index) + " " + Integer.toString(Value_H));
						//}
						CountBin_H[Value_H]++;
						// Summary of Habitat Index
						// Write Habitat Index to The File
						//sb4.append(String.format("%.4f", Habitat_Index));
						
						
						
						// Nitrogen
						//float Nitrogen = (float)Math.pow(10, 1.13f * Prop_Ag - 0.23f);
						float Nitrogen = (float)Math.pow(10, 1.13f * Prop_Ag - 0.23f);
						Min_N_T = Min(Min_N_T, Nitrogen);
						Max_N_T = Max(Max_N_T, Nitrogen);
						//float Nitrogen = (float)Math.pow(10, 1.13f * Prop_Ag - 0.23f) / Max_N;
						Nitrogen_T = Nitrogen + Nitrogen_T;
						// Summary of Nitrogen
						//Value_N = (int)((Nitrogen - Min_N)/(Max_N - Min_N)*(Bin-1));
						Value_N = (int)((Nitrogen - Min_N) / (Max_N - Min_N) * (Bin - 1));
						//if (Value_N < 0 || Value_N >= Bin)
						//{
						//	Logger.info("Out of range N:" + Float.toString(Nitrogen) + " " + Integer.toString(Value_N));
						//}
						CountBin_N[Value_N]++;
						// Write Nitrogen to The File
						//sb5.append(String.format("%.3f", Nitrogen));
						
						
						
						// Phosphorus 
						//float Phosphorus = (float)Math.pow(10, 0.79f * Prop_Ag - 1.44f);
						float Phosphorus = (float)Math.pow(10, 0.79f * Prop_Ag - 1.44f);
						Min_P_T = Min(Min_P_T, Phosphorus);
						Max_P_T = Max(Max_P_T, Phosphorus);
						//float Phosphorus = (float)Math.pow(10, 0.79f * Prop_Ag - 1.44f) / Max_P;
						Phosphorus_T = Phosphorus + Phosphorus_T;
						// Summary of Phosphorus
						//Value_P = (int)((Phosphorus - Min_P)/(Max_P - Min_P)*(Bin-1));
						Value_P = (int)((Phosphorus - Min_P)/(Max_P - Min_P) * (Bin - 1));
						//if (Value_P < 0 || Value_P >= Bin)
						//{
						//	Logger.info("Out of range Pho:" + Float.toString(Phosphorus) + " " + Integer.toString(Value_P));
						//}
						CountBin_P[Value_P]++;
						// Write Phosphorus to The File
						//sb6.append(String.format("%.3f", Phosphorus));
						
						
						
						// Pest 
						int Crop_Type = 0;
						if ((RotationT[y][x] & Ag_Mask) > 0)
						{
							Crop_Type = 0;	
						}
						else if ((RotationT[y][x] & Grass_Mask) > 0)
						{
							Crop_Type = 1;
						}
						// Maximum is not really 1
						//Max_Pest = (float)(0.25 + 0.19f * 1 + 0.62f * 1);
						//Min_Pest = (float)(0.25 + 0.19f * 0 + 0.62f * 0);
						// Normalize using Max
						float Pest = (float)(0.25f + 0.19f * Crop_Type + 0.62f * Prop_Grass);
						Min_Pest_T = Min(Min_Pest_T, Pest);
						Max_Pest_T = Max(Max_Pest_T, Pest);
						//float Pest = (float)(0.25 + 0.19f * Crop_Type + 0.62f * Prop_Forest) / Max_Pest;
						Pest_T = Pest + Pest_T;
						// Summary of Pest
						Value_Pest = (int)((Pest - Min_Pest)/(Max_Pest - Min_Pest)* (Bin - 1));
						//if (Value_Pest < 0 || Value_Pest >= Bin)
						//{
						//	Logger.info("Out of range Pest:" + Float.toString(Pest) + " " + Integer.toString(Value_Pest));
						//}
						CountBin_Pest[Value_Pest]++;
						// Write Pest to The File
						//sb7.append(String.format("%.3f", Pest));
						
						
						
						// Pollinator
						//Max_Poll = (float)Math.pow(0.6617f + 2.98 * 1 + 1.83 * 1, 2);
						//Min_Poll = (float)Math.pow(0.6617f + 2.98 * 0 + 1.83 * 0, 2);
						// Normalize using Max
						float Poll = (float)Math.pow(0.6617f + 2.98f * Prop_Forest + 1.83f * Prop_Grass, 2);
						Min_Poll_T = Min(Min_Poll_T, Poll);
						Max_Poll_T = Max(Max_Poll_T, Poll);
						//float Poll = (float)Math.pow(0.6617f + 2.98 * Prop_Forest + 1.83 * Prop_Grass, 2) / Max_Poll;
						Pollinator_T = Poll + Pollinator_T;
						// Summary of Pollinator
						Value_Poll = (int)((Poll - Min_Poll)/(Max_Poll - Min_Poll)* (Bin - 1));
						//if (Value_Poll < 0 || Value_Poll >= Bin)
						//{
						//	Logger.info("Out of range Poll:" + Float.toString(Poll) + " " + Integer.toString(Value_Poll));
						//}
						CountBin_Poll[Value_Poll]++;
						// Write Pollinator to The File
						//sb8.append(String.format("%.3f", Poll));
					}
					//if (x != width - 1) 
					//{
					//	sb1.append(" ");
					//	sb2.append(" ");
					//	sb3.append(" ");
					//	sb4.append(" ");
					//	sb5.append(" ");
					//	sb6.append(" ");
					//	sb7.append(" ");
					//	sb8.append(" ");
					//}
				}
				//out1.println(sb1.toString());
				//out2.println(sb2.toString());
				//out3.println(sb3.toString());
				//out4.println(sb4.toString());
				//out5.println(sb5.toString());
				//out6.println(sb6.toString());
				//out7.println(sb5.toString());
				//out8.println(sb6.toString());
			}
			//Logger.info("Count C and G:" + Integer.toString(Count_C) + " " + Integer.toString(Count_G));
			//Logger.info("Closing the Files");
			// Close input files
			br1.close();
			br2.close();
			br3.close();
			br4.close();
			// Close output files
			//out1.close();
			//out2.close();
			//out3.close();
			//out4.close();
			//out5.close();
			//out6.close();
			//out7.close();
			//out8.close();
			//Logger.info("Writting to the Files has finished");
			
			// Print Min and Max
			// Ethanol
			Logger.info("Ethanol Min1:" + Float.toString(E_Min));
			Logger.info("Ethanol Min2:" + Float.toString(E_Min_T));
			Logger.info("Ethanol Max1:" + Float.toString(E_Max));
			Logger.info("Ethanol Max2:" + Float.toString(E_Max_T));
			// Habitat Index
			Logger.info("Habitat Index Min1:" + Float.toString(Min_H));
			Logger.info("Habitat Index Min2:" + Float.toString(Min_H_T));
			Logger.info("Habitat Index Max1:" + Float.toString(Max_H));
			Logger.info("Habitat Index Max2:" + Float.toString(Max_H_T));
			// Nitrogen
			Logger.info("Nitrogen Min1:" + Float.toString(Min_N));
			Logger.info("Nitrogen Min2:" + Float.toString(Min_N_T));
			Logger.info("Nitrogen Max1:" + Float.toString(Max_N));
			Logger.info("Nitrogen Max2:" + Float.toString(Max_N_T));
			// Phosphorus
			Logger.info("Phosphorus Min1:" + Float.toString(Min_P));
			Logger.info("Phosphorus Min2:" + Float.toString(Min_P_T));
			Logger.info("Phosphorus Max1:" + Float.toString(Max_P));
			Logger.info("Phosphorus Max2:" + Float.toString(Max_P_T));
			// Pest
			Logger.info("Pest Min1:" + Float.toString(Min_Pest));
			Logger.info("Pest Min2:" + Float.toString(Min_Pest_T));
			Logger.info("Pest Max1:" + Float.toString(Max_Pest));
			Logger.info("Pest Max2:" + Float.toString(Max_Pest_T));
			// Pollinator
			Logger.info("Pollinator Min1:" + Float.toString(Min_Poll));
			Logger.info("Pollinator Min2:" + Float.toString(Min_Poll_T));
			Logger.info("Pollinator Max1:" + Float.toString(Max_Poll));
			Logger.info("Pollinator Max2:" + Float.toString(Max_Poll_T));
			// Net Income
			Logger.info("Net Income Min1:" + Float.toString(NI_Min));
			Logger.info("Net Income Min2:" + Float.toString(NI_Min_T));
			Logger.info("Net Income Max1:" + Float.toString(NI_Max));
			Logger.info("Net Income Max2:" + Float.toString(NI_Max_T));
			// Net Energy
			Logger.info("Net Energy Min1:" + Float.toString(NE_Min));
			Logger.info("Net Energy Min2:" + Float.toString(NE_Min_T));
			Logger.info("Net Energy Max1:" + Float.toString(NE_Max));
			Logger.info("Net Energy Max2:" + Float.toString(NE_Max_T));
		}
		catch(Exception err) 
		{
			Logger.info(err.toString());
			Logger.info("Oops, something went wrong with writing to the files!");
		}
		
		
		// Data to return to the client		
		ObjectNode obj = JsonNodeFactory.instance.objectNode();
		ObjectNode E_C_G = JsonNodeFactory.instance.objectNode();
		ObjectNode H_I = JsonNodeFactory.instance.objectNode();
		ObjectNode Nitr = JsonNodeFactory.instance.objectNode();
		ObjectNode Phos = JsonNodeFactory.instance.objectNode();
		ObjectNode Pes = JsonNodeFactory.instance.objectNode();
		ObjectNode Pol = JsonNodeFactory.instance.objectNode();
		ObjectNode N_I = JsonNodeFactory.instance.objectNode();
		ObjectNode N_E = JsonNodeFactory.instance.objectNode();
		
		// 50 Bins
		Total_Bins_Num1 = 0; 
		// 10 Bins
		Total_Bins_Num2 = 0;
		
		// Ethonal
		for (int i = 0; i < CountBin_E.length; i++) 
		{
			if (CountBin_E[i] > 0)
			{
				Total_Bins_Num1 = CountBin_E[i] + Total_Bins_Num1;
				float Ethanol = E_Min + i * (E_Max - E_Min) / (Bin - 1);
				int Value_E = (int)((Ethanol - E_Min_T)/(E_Max_T - E_Min_T) * (Bin_T - 1));
				CountBin_E_T[Value_E] += CountBin_E[i];
			}
		}
		// Ethonal
		ArrayNode E = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < CountBin_E_T.length; i++) 
		{
			Total_Bins_Num2 = CountBin_E_T[i] + Total_Bins_Num2; 
			E.add(CountBin_E_T[i]);
		}
		// Average of Ethanol per pixel
		float E_Per_Cell = Ethanol_T / Total_Cells;
		Logger.info("Total_Cells_E1:" + Float.toString(Total_Bins_Num1));
		Logger.info("Total_Cells_E2:" + Float.toString(Total_Bins_Num2));
		
		
		Total_Bins_Num1 = 0; 
		Total_Bins_Num2 = 0;
		// Habitat Index
		for (int i = 0; i < CountBin_H.length; i++) 
		{
			if (CountBin_H[i] > 0)
			{
				Total_Bins_Num1 = CountBin_H[i] + Total_Bins_Num1;
				float Habitat_Index = (float)i / (Bin - 1);
				int Value_H = (int)(Habitat_Index * (Bin_T - 1));
				CountBin_H_T[Value_H] += CountBin_H[i];
			}
		}
		// Habitat Index
		ArrayNode HI = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < CountBin_H_T.length; i++) 
		{
			Total_Bins_Num2 = CountBin_H_T[i] + Total_Bins_Num2; 
			HI.add(CountBin_H_T[i]);
		}
		// Average of Habitat_Index per pixel
		float HI_Per_Cell = Habitat_Index_T / Total_Cells;
		Logger.info("Total_Cells_HI1:" + Float.toString(Total_Bins_Num1));
		Logger.info("Total_Cells_HI2:" + Float.toString(Total_Bins_Num2));
		
		
		Total_Bins_Num1 = 0; 
		Total_Bins_Num2 = 0;
		// Nitrogen
		for (int i = 0; i < CountBin_N.length; i++) 
		{
			if (CountBin_N[i] > 0)
			{
				Total_Bins_Num1 = CountBin_N[i] + Total_Bins_Num1;
				float Nitrogen = Min_N + i * (Max_N - Min_N) / (Bin - 1);
				int Value_N = (int)((Nitrogen - Min_N_T)/(Max_N_T - Min_N_T) * (Bin_T - 1));
				CountBin_N_T[Value_N] += CountBin_N[i];
			}
		}
		// Nitrogen
		ArrayNode N = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < CountBin_N_T.length; i++) 
		{
			Total_Bins_Num2 = CountBin_N_T[i] + Total_Bins_Num2; 
			N.add(CountBin_N_T[i]);
		}
		// Average of Nitrogen per pixel
		float N_Per_Cell = Nitrogen_T / Total_Cells;
		Logger.info("Total_Cells_N1:" + Float.toString(Total_Bins_Num1));
		Logger.info("Total_Cells_N2:" + Float.toString(Total_Bins_Num2));
		
		
		Total_Bins_Num1 = 0; 
		Total_Bins_Num2 = 0;
		// Phosphorus
		for (int i = 0; i < CountBin_P.length; i++) 
		{
			if (CountBin_P[i] > 0)
			{
				Total_Bins_Num1 = CountBin_P[i] + Total_Bins_Num1;
				float Phosphrous = Min_P + i * (Max_P - Min_P) / (Bin - 1);
				int Value_P = (int)((Phosphrous - Min_P_T)/(Max_P_T - Min_P_T) * (Bin_T - 1));
				CountBin_P_T[Value_P] += CountBin_P[i];
			}
		}
		// Phosphorus
		ArrayNode P = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < CountBin_P_T.length; i++) 
		{
			Total_Bins_Num2 = CountBin_P_T[i] + Total_Bins_Num2; 
			P.add(CountBin_P_T[i]);
		}
		// Average of Phosphorus per pixel
		float Phos_Per_Cell = Phosphorus_T / Total_Cells;
		Logger.info("Total_Cells_P1:" + Float.toString(Total_Bins_Num1));
		Logger.info("Total_Cells_P2:" + Float.toString(Total_Bins_Num2));
		
		
		Total_Bins_Num1 = 0; 
		Total_Bins_Num2 = 0;
		// Pest
		for (int i = 0; i < CountBin_Pest.length; i++) 
		{
			if (CountBin_Pest[i] > 0)
			{
				Total_Bins_Num1 = CountBin_Pest[i] + Total_Bins_Num1;
				float Pest = Min_Pest + i * (Max_Pest - Min_Pest) / (Bin - 1);
				int Value_Pest = (int)((Pest - Min_Pest_T)/(Max_Pest_T - Min_Pest_T) * (Bin_T - 1));
				CountBin_Pest_T[Value_Pest] += CountBin_Pest[i];
			}
		}
		// Pest
		ArrayNode PestS = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < CountBin_Pest_T.length; i++) 
		{
			Total_Bins_Num2 = CountBin_Pest_T[i] + Total_Bins_Num2;
			PestS.add(CountBin_Pest_T[i]);
		}
		// Average of Pest per pixel
		float Pest_Per_Cell = Pest_T / Total_Cells;
		Logger.info("Total_Cells_Pest1:" + Float.toString(Total_Bins_Num1));
		Logger.info("Total_Cells_Pest2:" + Float.toString(Total_Bins_Num2));
		
		
		Total_Bins_Num1 = 0; 
		Total_Bins_Num2 = 0;
		// Pollinator
		for (int i = 0; i < CountBin_Poll.length; i++) 
		{
			if (CountBin_Poll[i] > 0)
			{
				Total_Bins_Num1 = CountBin_Poll[i] + Total_Bins_Num1;
				float Poll = Min_Poll + i * (Max_Poll - Min_Poll) / (Bin - 1);
				int Value_Poll = (int)((Poll - Min_Poll_T)/(Max_Poll_T - Min_Poll_T) * (Bin_T - 1));
				CountBin_Poll_T[Value_Poll] += CountBin_Poll[i];
			}
		}
		// Pollinator
		ArrayNode Pollin = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < CountBin_Poll_T.length; i++) 
		{
			Total_Bins_Num2 = CountBin_Poll_T[i] + Total_Bins_Num2;
			Pollin.add(CountBin_Poll_T[i]);
		}
		// Average of Pollinator per pixel
		float Pollinator_Per_Cell = Pollinator_T / Total_Cells;
		Logger.info("Total_Cells_Pest1:" + Float.toString(Total_Bins_Num1));
		Logger.info("Total_Cells_Pest2:" + Float.toString(Total_Bins_Num2));
		
		
		Total_Bins_Num1 = 0; 
		Total_Bins_Num2 = 0;
		// Net_Income
		for (int i = 0; i < CountBin_NI.length; i++) 
		{
			if (CountBin_NI[i] > 0)
			{
				Total_Bins_Num1 = CountBin_NI[i] + Total_Bins_Num1;
				float Net_Income = NI_Min + i * (NI_Max - NI_Min) / (Bin - 1);
				int Value_NI = (int)((Net_Income - NI_Min_T)/(NI_Max_T - NI_Min_T) * (Bin_T - 1));
				CountBin_NI_T[Value_NI] += CountBin_NI[i];
			}
		}
		// Net_Income
		ArrayNode NI = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < CountBin_NI_T.length; i++) 
		{
			Total_Bins_Num2 = CountBin_NI_T[i] + Total_Bins_Num2;
			NI.add(CountBin_NI_T[i]);
		}
		// Average of Net_Income per pixel
		float Net_Income_Per_Cell = Net_Income_T / Total_Cells;
		Logger.info("Total_Cells_Pest1:" + Float.toString(Total_Bins_Num1));
		Logger.info("Total_Cells_Pest2:" + Float.toString(Total_Bins_Num2));
		
		
		Total_Bins_Num1 = 0; 
		Total_Bins_Num2 = 0;
		// Net Energy
		for (int i = 0; i < CountBin_NE.length; i++) 
		{
			if (CountBin_NE[i] > 0)
			{
				Total_Bins_Num1 = CountBin_NE[i] + Total_Bins_Num1;
				float Net_Energy = NE_Min + i * (NE_Max - NE_Min) / (Bin - 1);
				int Value_NE = (int)((Net_Energy - NE_Min_T)/(NE_Max_T - NE_Min_T) * (Bin_T - 1));
				CountBin_NE_T[Value_NE] += CountBin_NE[i];
			}
		}
		// Net Energy
		ArrayNode NE = JsonNodeFactory.instance.arrayNode();
		for (int i = 0; i < CountBin_NE_T.length; i++) 
		{
			Total_Bins_Num2 = CountBin_NE_T[i] + Total_Bins_Num2;
			NE.add(CountBin_NE_T[i]);
		}
		// Average of Net_Energy per pixel
		float Net_Energy_Per_Cell = Net_Energy_T / Total_Cells;
		Logger.info("Total_Cells_Pest1:" + Float.toString(Total_Bins_Num1));
		Logger.info("Total_Cells_Pest2:" + Float.toString(Total_Bins_Num2));
		
		
		// Ethonal
		E_C_G.put("Result", E);
		E_C_G.put("Min", E_Min_T);
		E_C_G.put("Max", E_Max_T);
		//E_C_G.put("Ethanol", Ethanol_T / 1000);
		E_C_G.put("Ethanol", E_Per_Cell);
		
		// Habitat_Index
		H_I.put("Result", HI);
		H_I.put("Min", Min_H_T);
		H_I.put("Max", Max_H_T);
		H_I.put("Average_HI", HI_Per_Cell);
		
		// Nitrogen
		Nitr.put("Result", N);
		Nitr.put("Min", Min_N_T);
		Nitr.put("Max", Max_N_T);
		//Nitr.put("Nitrogen", Nitrogen_T * 900 / 1000000);
		Nitr.put("Nitrogen", N_Per_Cell);
		
		// Phosphorus
		Phos.put("Result", P);
		Phos.put("Min", Min_P_T);
		Phos.put("Max", Max_P_T);
		//Phos.put("Phosphorus", Phosphorus_T * 900 / 1000000);
		Phos.put("Phosphorus", Phos_Per_Cell);
		
		// Pest
		Pes.put("Result", PestS);
		Pes.put("Min", Min_Pest_T);
		Pes.put("Max", Max_Pest_T);
		Pes.put("Pest", Pest_Per_Cell);
		
		// Pollinator
		Pol.put("Result", Pollin);
		Pol.put("Min", Min_Poll_T);
		Pol.put("Max", Max_Poll_T);
		Pol.put("Pollinator", Pollinator_Per_Cell);
		
		// Net_Income
		N_I.put("Result", NI);
		N_I.put("Min", NI_Min_T);
		N_I.put("Max", NI_Max_T);
		//N_I.put("Min", NI_Min);
		//N_I.put("Max", NI_Max);
		N_I.put("Net_Income", Net_Income_Per_Cell);
		
		// Net_Energy
		N_E.put("Result", NE);
		N_E.put("Min", NE_Min_T);
		N_E.put("Max", NE_Max_T);
		//N_E.put("Min", NE_Min);
		//N_E.put("Max", NE_Max);
		N_E.put("Net_Energy", Net_Energy_Per_Cell);
		
		
		
		// Add branches to JSON Node 
		obj.put("Ethanol", E_C_G);
		obj.put("Habitat_Index", H_I);
		obj.put("Nitrogen", Nitr);
		obj.put("Phosphorus", Phos);
		obj.put("Pest", Pes);
		obj.put("Pollinator", Pol);
		obj.put("Net_Income", N_I);
		obj.put("Net_Energy", N_E);
		

		
		Logger.info(obj.toString());
		return obj;
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
	
	// Min
	public float Min(float Min, float Num)
	{ 
		// Min
		if (Num < Min)
		{
			Min = Num;
		}
		
		return Min;
	}
	
	// Max
	public float Max(float Max, float Num)
	{

		// Max
		if (Num > Max)
		{
			Max = Num;
		}
		
		return Max;
	}
}
