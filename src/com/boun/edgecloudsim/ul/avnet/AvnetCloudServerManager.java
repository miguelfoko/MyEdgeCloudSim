package com.boun.edgecloudsim.ul.avnet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
/*Class that helps to manage the architecture Cloud Servers*/
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.VmSchedulerSpaceShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import edu.boun.edgecloudsim.cloud_server.CloudServerManager;
import edu.boun.edgecloudsim.cloud_server.CloudVM;
import edu.boun.edgecloudsim.cloud_server.CloudVmAllocationPolicy_Custom;

import edu.boun.edgecloudsim.core.SimSettings;


public class AvnetCloudServerManager extends CloudServerManager{

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public VmAllocationPolicy getVmAllocationPolicy(List<? extends Host> hostList, int dataCenterIndex) {
		return new CloudVmAllocationPolicy_Custom(hostList,dataCenterIndex);
	}

	@Override
	public void startDatacenters() throws Exception {
//		AvnetSimLogger.printLine("******//////////////////////////////////////Avnet Starting Cloud datacenters******//////////////////////////////////////");
		localDatacenter = createDatacenter(SimSettings.CLOUD_DATACENTER_ID);
//		AvnetSimLogger.printLine("******//////////////////////////////////////Avnet Cloud datacenters started******//////////////////////////////////////");

	}

	@Override
	public void terminateDatacenters() {
		localDatacenter.shutdownEntity();
	}

	@Override
	public void createVmList(int brokerId) {
//		AvnetSimLogger.printLine("******//////////////////////////////////////Avnet Creating Cloud VMs******//////////////////////////////////////");
		//VMs should have unique IDs, so create Cloud VMs after Edge VMs
		int vmCounter=SimSettings.getInstance().getNumOfEdgeVMs();

		//Create VMs for each hosts
		for (int i = 0; i < SimSettings.getInstance().getNumOfCloudHost(); i++) {
			vmList.add(i, new ArrayList<CloudVM>());
			for(int j = 0; j < SimSettings.getInstance().getNumOfCloudVMsPerHost(); j++){
				String vmm = "Xen";
				int numOfCores = SimSettings.getInstance().getCoreForCloudVM();
				double mips = SimSettings.getInstance().getMipsForCloudVM();
				int ram = SimSettings.getInstance().getRamForCloudVM();
				long storage = SimSettings.getInstance().getStorageForCloudVM();
				long bandwidth = 0;

				//VM Parameters		
				CloudVM vm = new CloudVM(vmCounter, brokerId, mips, numOfCores, ram, bandwidth, storage, vmm, new CloudletSchedulerTimeShared());
				vmList.get(i).add(vm);
				vmCounter++;
			}
		}
//		AvnetSimLogger.printLine("******//////////////////////////////////////Avnet Cloud VMs Created******//////////////////////////////////////");
	}

	@Override
	public double getAvgUtilization() {
		double totalUtilization = 0;
		double vmCounter = 0;

		List<? extends Host> list = localDatacenter.getHostList();
		// for each host...
		for (int hostIndex=0; hostIndex < list.size(); hostIndex++) {
			List<CloudVM> vmArray = AvnetCoreSimulation.getInstance().getCloudServerManager().getVmList(hostIndex);
			//for each vm...
			for(int vmIndex=0; vmIndex<vmArray.size(); vmIndex++){
				totalUtilization += vmArray.get(vmIndex).getCloudletScheduler().getTotalUtilizationOfCpu(CloudSim.clock());
				vmCounter++;
			}
		}

		return totalUtilization / vmCounter;
	}


	private List<Host> createHosts(){
		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store one or more Machines
		List<Host> hostList = new ArrayList<Host>();

		for (int i = 0; i < SimSettings.getInstance().getNumOfCloudHost(); i++) {
			int numOfVMPerHost = SimSettings.getInstance().getNumOfCloudVMsPerHost();
			int numOfCores = SimSettings.getInstance().getCoreForCloudVM() * numOfVMPerHost;
			double mips = SimSettings.getInstance().getMipsForCloudVM() * numOfVMPerHost;
			int ram = SimSettings.getInstance().getRamForCloudVM() * numOfVMPerHost;
			long storage = SimSettings.getInstance().getStorageForCloudVM() * numOfVMPerHost;
			long bandwidth = 0;

			// 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
			//    create a list to store these PEs before creating
			//    a Machine.
			List<Pe> peList = new ArrayList<Pe>();

			// 3. Create PEs and add these into the list.
			//for a quad-core machine, a list of 4 PEs is required:
			for(int j=0; j<numOfCores; j++){
				peList.add(new Pe(j, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
			}

			//4. Create Hosts with its id and list of PEs and add them to the list of machines
			Host host = new Host(
					//Hosts should have unique IDs, so create Cloud Hosts after Edge Hosts
					i+SimSettings.getInstance().getNumOfEdgeHosts(),
					new RamProvisionerSimple(ram),
					new BwProvisionerSimple(bandwidth), //kbps
					storage,
					peList,
					new VmSchedulerSpaceShared(peList)
					);
			hostList.add(host);
		}

		return hostList;
	}

	private Datacenter createDatacenter(int index) {
		String arch="x86";
		String os="Linux";
		String vmm="Xen";
		double costPerBw=0;
		double costPerSec=0;
		double costPerMem=0;
		double costPerStorage=0;

		List<Host> hostList=createHosts();

		String name="CloudDatacenter_"+Integer.toString(index);
		double time_zone=3.0;
		LinkedList<Storage> storageList=new LinkedList<Storage>();
		DatacenterCharacteristics characteristics=new DatacenterCharacteristics(
				arch, os, vmm, hostList, time_zone, costPerSec, costPerMem, costPerStorage, costPerBw);
		Datacenter datacenter=null;
		VmAllocationPolicy vm_policy=getVmAllocationPolicy(hostList, index);
		try {
			datacenter=new Datacenter(name, characteristics, vm_policy, storageList, 0);
			return datacenter;
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			
			
			return null;
		}

	}
	
	

}
