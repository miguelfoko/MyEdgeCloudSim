package com.edgecloudsim.ul.avnet;

import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.VmAllocationPolicy;

import edu.boun.edgecloudsim.edge_client.mobile_processing_unit.MobileServerManager;
import edu.boun.edgecloudsim.edge_client.mobile_processing_unit.MobileVmAllocationPolicy_Custom;

public class AvnetMobileServerManager extends MobileServerManager {

	@Override
	public void initialize() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public VmAllocationPolicy getVmAllocationPolicy(List<? extends Host> list, int dataCenterIndex) {
		return new MobileVmAllocationPolicy_Custom(list, dataCenterIndex);
	}

	@Override
	public void startDatacenters() throws Exception {
//		AvnetSimLogger.printLine("******//////////////////////////////////////Avnet Starting Mobile datacenters******//////////////////////////////////////");
//		AvnetSimLogger.printLine("******//////////////////////////////////////Avnet Mobile datacenters started******//////////////////////////////////////");
	}

	@Override
	public void terminateDatacenters() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void createVmList(int brokerId) {
//		AvnetSimLogger.printLine("******//////////////////////////////////////Avnet Creating Mobile VMs******//////////////////////////////////////");
//		AvnetSimLogger.printLine("******//////////////////////////////////////Avnet Mobile VMs Created******//////////////////////////////////////");
		
	}

	@Override
	public double getAvgUtilization() {
		// TODO Auto-generated method stub
		return 0;
	}

}
