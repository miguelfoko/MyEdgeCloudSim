package com.edgecloudsim.ul.avnet;

import edu.boun.edgecloudsim.cloud_server.CloudServerManager;
import edu.boun.edgecloudsim.core.ScenarioFactory;
import edu.boun.edgecloudsim.edge_client.MobileDeviceManager;
import edu.boun.edgecloudsim.edge_client.mobile_processing_unit.MobileServerManager;
import edu.boun.edgecloudsim.edge_orchestrator.EdgeOrchestrator;
import edu.boun.edgecloudsim.edge_server.EdgeServerManager;
import edu.boun.edgecloudsim.mobility.MobilityModel;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.task_generator.LoadGeneratorModel;


public class AvnetScenarioFactory implements ScenarioFactory{
	private int numOfMobileDevice;
	private double simulationTime;
	private String orchestratorPolicy;
	private String simScenario;
	
	public AvnetScenarioFactory(int _numOfMobileDevice, double _simulationTime, String _orchestratorPolicy, String _simScenario) {
		AvnetSimLogger.printLine("Creating AvnetScenarioFactory......");
		orchestratorPolicy = _orchestratorPolicy;
		numOfMobileDevice = _numOfMobileDevice;
		simulationTime = _simulationTime;
		simScenario = _simScenario;
		AvnetSimLogger.printLine("AvnetScenarioFactory created successfully......");
	}

	@Override
	public LoadGeneratorModel getLoadGeneratorModel() {
		return new AvnetLoadGenerator(numOfMobileDevice, simulationTime, simScenario);
	}

	@Override
	public EdgeOrchestrator getEdgeOrchestrator() {
		return new AvnetEdgeOrchestrator(orchestratorPolicy, simScenario);
	}

	@Override
	public MobilityModel getMobilityModel() {
		return new AvnetMobility(numOfMobileDevice,simulationTime);
	}

	@Override
	public NetworkModel getNetworkModel() {
		return new AvnetNetwork(numOfMobileDevice, simScenario);
	}

	@Override
	public EdgeServerManager getEdgeServerManager() {
		return new AvnetEdgeServerManager();
	}

	@Override
	public CloudServerManager getCloudServerManager() {
		return new AvnetCloudServerManager();
	}
	
	@Override
	public MobileDeviceManager getMobileDeviceManager() throws Exception {
		return new AvnetMobileDeviceManager();
	}

	@Override
	public MobileServerManager getMobileServerManager() {
		return new AvnetMobileServerManager();
	}

}
