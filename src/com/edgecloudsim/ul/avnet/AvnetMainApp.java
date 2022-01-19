package com.edgecloudsim.ul.avnet;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;

import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.core.SimSettings;

import edu.boun.edgecloudsim.utils.SimUtils;

public class AvnetMainApp {

	/**
	 * Creates main() to run this example
	 */
	public static void main(String[] args) {
		//disable console output of cloudsim library
		Log.disable();
		
		//enable console output and file output of this application
		AvnetSimLogger.enablePrintLog();
		
		int iterationNumber = 1;
		String configFile = "";
		String outputFolder = "";
		String edgeDevicesFile = "";
		String applicationsFile = "";
		if (args.length == 5){
			configFile = args[0];
			edgeDevicesFile = args[1];
			applicationsFile = args[2];
			outputFolder = args[3];
			iterationNumber = Integer.parseInt(args[4]);
		}
		else{
			AvnetSimLogger.printLine("Simulation setting file, output folder and iteration number are not provided! Using default ones...");
			configFile = "scripts/avnet/config/default_config.properties";
			applicationsFile = "scripts/avnet/config/applications.xml";
			edgeDevicesFile = "scripts/avnet/config/edge_devices.xml";
			outputFolder = "sim_results/avnet" + iterationNumber;
		}		
		
		//load settings from configuration file
		SimSettings SS = SimSettings.getInstance();
		if(SS.initialize(configFile, edgeDevicesFile, applicationsFile) == false){
			AvnetSimLogger.printLine("cannot initialize simulation settings!");
			System.exit(0);
		}
		
		if(SS.getFileLoggingEnabled()){
			AvnetSimLogger.enableFileLog();
			SimUtils.cleanOutputFolder(outputFolder);
		}
		
		/**
		 * Creation of trace files @author fsmiguel
		 * */
		
		TraceFile.initialize();
		
		//traceFile.insertLatencyData("Latency.csv", "Bravoooooooo");
		
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date simulationStartDate = Calendar.getInstance().getTime();
		String now = df.format(simulationStartDate);
		AvnetSimLogger.printLine("Simulation started at " + now);
		AvnetSimLogger.printLine("----------------------------------------------------------------------");
		
		for(int j=SS.getMinNumOfMobileDev();j<=SS.getMaxNumOfMobileDev();j+=SS.getMobileDevCounterSize()) {
			for(int k=0;k<SS.getSimulationScenarios().length;k++) {
				for(int i=0;i<SS.getOrchestratorPolicies().length;i++) {
					String simScenario=SS.getSimulationScenarios()[k];
					String orchestratorPolicy=SS.getOrchestratorPolicies()[i];
					Date scenarioStartDate=Calendar.getInstance().getTime();
					now=df.format(scenarioStartDate);
					
					AvnetSimLogger.printLine("Avnet Scenario started at "+now);
					AvnetSimLogger.printLine("Avnet Scenario: "+ simScenario+" -Policy: "+orchestratorPolicy+" - #iteration: "+iterationNumber);
					AvnetSimLogger.printLine("Avnet Duration: "+SS.getSimulationTime()/3600+" hour(s) - Poison: "+SS.getTaskLookUpTable()[0][2]+" - #devices: "+j);
					AvnetSimLogger.getInstance().simStarted(outputFolder,"AVNET_SIMRESULT_"+simScenario+"_"+orchestratorPolicy+"_"+j+"DEVICES");
					
					try {
						//First step: Initialize the CloudSim package. It should be called before creating any entities
						int num_user=2; //Number of grid users
						Calendar calendar=Calendar.getInstance();
						
						boolean trace_flag=false;
						//Initialize the cloudSim library
						CloudSim.init(num_user, calendar, trace_flag,0.01);
						
						//Generate EdgeCloudSim Scenario Factory
						
						ScenarioFactory avnetScenarioFactory=new AvnetScenarioFactory(j, SS.getSimulationTime(), orchestratorPolicy, simScenario);
						
						//generate EdgeCloudSim Simulation Manager
						AvnetCoreSimulation manager=new AvnetCoreSimulation(avnetScenarioFactory, j, simScenario, orchestratorPolicy);
												
						//Start simulation
						manager.startSimulation();
//						AvnetSimLogger.printLine("Avnet Testing");
					}catch(Exception e) {
						AvnetSimLogger.printLine("The Avnet simulation has been terminated due to an unexpected error");
						e.printStackTrace();
						System.exit(0);
					}
					Date scenarioEndDate=Calendar.getInstance().getTime();
					now=df.format(scenarioEndDate);
					AvnetSimLogger.printLine("Avnet Scenario finished at "+now+". It took "+SimUtils.getTimeDifference(scenarioStartDate, scenarioEndDate));
					AvnetSimLogger.printLine("____________________________________________________________________________________________________");
				}//End of Orchestration Loop
			}//End of Scenarios Loop
		}//End of mobile devices Loop
		Date simulationEndDate=Calendar.getInstance().getTime();
		now=df.format(simulationEndDate);
		AvnetSimLogger.printLine("Avnet Simulation finishet at "+now+". It took "+SimUtils.getTimeDifference(simulationStartDate, simulationEndDate));
	}
}
