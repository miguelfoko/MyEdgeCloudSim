package com.edgecloudsim.ul.avnet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import edu.boun.edgecloudsim.core.SimSettings;

public class TraceFile {
	public static String traceDirectory=SimSettings.getInstance().getTraceDirectory();
	/**
	 * Method to create trace files
	 * @param String type// must specify the trace type withing latency, computing, storing, bandwidth, etc.
	 * */
	@SuppressWarnings("finally")
	public static boolean createTrace(String type) {
		boolean valRet=false;
		String fileName=null;
		if(type.equals("latency"))
			fileName="Latency.csv";
		else
			if(type.equals("computing"))
				fileName="Computing.csv";
			else
				if(type.equals("storing"))
					fileName="Storing.csv";
				else
					if(type.equals("ram"))
						fileName="Ram.csv";
					else
						if(type.equals("bandwidth"))//It is Bandwidth
							fileName="Bandwidth.csv";
						else
							fileName="Task.csv";
		try {
			File myObj = new File(traceDirectory,fileName);
			if (myObj.createNewFile()) {
				
					//AvnetSimLogger.printLine("\n\n&&&&&&&&&&&&&&&&&&&&&&& After file creation.");
				valRet= true;
			} else {
				//AvnetSimLogger.printLine("\n\n&&&&&&&&&&&&&&&&&&&&&&&File already exists: " + myObj.getName()+"   $$$$$$$$$$$$$$$$$$$$$");
				valRet= false;
			}
		} catch (IOException e) {
			//System.out.println("\n\n&&&&&&&&&&&&&&&&&&&&&&&An error occurred."+"   $$$$$$$$$$$$$$$$$$$$$");
			e.printStackTrace();
		}finally {
			return valRet;
		}
	}

	@SuppressWarnings("finally")
	public static boolean insertData(String fileName,String data) {
		boolean valRet=false;
		try {
			File file=new File(traceDirectory,fileName);
			FileWriter myWriter = new FileWriter(file,true);
			BufferedWriter fileBW=new BufferedWriter(myWriter);
			fileBW.write(data);
			fileBW.newLine();
			//myWriter.write(data);
			//myWriter.close();
			fileBW.close();
			valRet=true;
			AvnetSimLogger.printLine("\n\n&&&&&&&&&&&&&&&&&&&&&&& Successfully wrote to the file.");
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}finally {
			return valRet;
		}
	}
	
	public static void initialize() {
		TraceFile.createTrace("latency");
		TraceFile.createTrace("computing");
		TraceFile.createTrace("storing");
		TraceFile.createTrace("ram");
		TraceFile.createTrace("bandwidth");
		TraceFile.createTrace("task");
		
		/**
		 * Initialization of the trace file for tasks creation/processing
		 * */
		String data="NumberOfTasks,NumberOfLocalTask,NumberOfAwayTaskDueToPosition"
				+ ",NumberOfAwayTaskDueToCapacity,NumberOfTaskProcessed";			
		TraceFile.insertData("Task.csv", data);
		
		/**
		 * Initialization of the trace file for latency
		 * */
		data="taskDelay,wanDelay,manDelay,wlanDelay,lanDelay";
		TraceFile.insertData("Latency.csv", data);
		/**
		 * Initialization of the trace file for Computing resource
		 * */
		
		/**
		 * Initialization of the trace file for Storing resource
		 * */
		
		/**
		 * Initialization of the trace file for Ram resource
		 * */
		
		/**
		 * Initialization of the trace file for Bandwidth resource
		 * */
	}
}
