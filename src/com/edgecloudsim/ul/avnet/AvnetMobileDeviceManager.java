package com.edgecloudsim.ul.avnet;

import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;


import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.core.SimSettings.AV_DIRECTION;
import edu.boun.edgecloudsim.core.SimSettings.NETWORK_DELAY_TYPES;
import edu.boun.edgecloudsim.edge_client.CpuUtilizationModel_Custom;
import edu.boun.edgecloudsim.edge_client.MobileDeviceManager;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.TaskProperty;

public class AvnetMobileDeviceManager extends MobileDeviceManager {
	private static final int BASE = 100000; //start from base in order not to conflict cloudsim tag!
	private static final int REQUEST_RECEIVED_BY_CLOUD = BASE + 1;
	private static final int REQUEST_RECEIVED_BY_EDGE_DEVICE = BASE + 2;
	private static final int RESPONSE_RECEIVED_BY_MOBILE_DEVICE = BASE + 3;
	private int taskIdCounter=0;

	public AvnetMobileDeviceManager() throws Exception{
	}

	@Override
	public void initialize() {
	}

	@Override
	public UtilizationModel getCpuUtilizationModel() {
		return new CpuUtilizationModel_Custom();
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlets() {
		//do nothing!
	}

	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		NetworkModel networkModel = AvnetCoreSimulation.getInstance().getNetworkModel();
		Task task = (Task) ev.getData();

		AvnetSimLogger.getInstance().taskExecuted(task.getCloudletId());

		if(task.getAssociatedDatacenterId() == SimSettings.CLOUD_DATACENTER_ID){
			//SimLogger.printLine(CloudSim.clock() + ": " + getName() + ": task #" + task.getCloudletId() + " received from cloud");
			double WanDelay = networkModel.getDownloadDelay(SimSettings.CLOUD_DATACENTER_ID, task.getMobileDeviceId(), task);
			if(WanDelay > 0)
			{
				Location currentLocation = AvnetCoreSimulation.getInstance().getMobilityModel().getLocation(task.getMobileDeviceId(),CloudSim.clock()+WanDelay);
				if(task.getSubmittedLocation().getServingWlanId() == currentLocation.getServingWlanId())
				{
					networkModel.downloadStarted(task.getSubmittedLocation(), SimSettings.CLOUD_DATACENTER_ID);
					AvnetSimLogger.getInstance().setDownloadDelay(task.getCloudletId(), WanDelay, NETWORK_DELAY_TYPES.WAN_DELAY);
					schedule(getId(), WanDelay, RESPONSE_RECEIVED_BY_MOBILE_DEVICE, task);
				}
				else
				{
					AvnetSimLogger.getInstance().failedDueToMobility(task.getCloudletId(), CloudSim.clock());
				}
			}
			else
			{
				AvnetSimLogger.getInstance().failedDueToBandwidth(task.getCloudletId(), CloudSim.clock(), NETWORK_DELAY_TYPES.WAN_DELAY);
			}
		}
		else{
			//SimLogger.printLine(CloudSim.clock() + ": " + getName() + ": task #" + task.getCloudletId() + " received from edge");
			double WlanDelay = networkModel.getDownloadDelay(task.getAssociatedHostId(), task.getMobileDeviceId(), task);
			if(WlanDelay > 0)
			{
				Location currentLocation = AvnetCoreSimulation.getInstance().getMobilityModel().getLocation(task.getMobileDeviceId(),CloudSim.clock()+WlanDelay);
				if(task.getSubmittedLocation().getServingWlanId() == currentLocation.getServingWlanId())
				{
					networkModel.downloadStarted(currentLocation, SimSettings.GENERIC_EDGE_DEVICE_ID);
					AvnetSimLogger.getInstance().setDownloadDelay(task.getCloudletId(), WlanDelay, NETWORK_DELAY_TYPES.WLAN_DELAY);
					schedule(getId(), WlanDelay, RESPONSE_RECEIVED_BY_MOBILE_DEVICE, task);
				}
				else
				{
					AvnetSimLogger.getInstance().failedDueToMobility(task.getCloudletId(), CloudSim.clock());
				}
			}
			else
			{
				AvnetSimLogger.getInstance().failedDueToBandwidth(task.getCloudletId(), CloudSim.clock(), NETWORK_DELAY_TYPES.WLAN_DELAY);
			}
		}
	}

	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			AvnetSimLogger.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null! Terminating simulation...");
			System.exit(1);
			return;
		}

		NetworkModel networkModel = AvnetCoreSimulation.getInstance().getNetworkModel();

		switch (ev.getTag()) {
		case REQUEST_RECEIVED_BY_CLOUD:
		{
			Task task = (Task) ev.getData();
//			AvnetSimLogger.printLine(task+"  in AvnetMobileDeviceManager processOtherEvent method........... ");
			networkModel.uploadFinished(task.getSubmittedLocation(), SimSettings.CLOUD_DATACENTER_ID);

			submitTaskToVm(task,0,SimSettings.CLOUD_DATACENTER_ID);

			break;
		}
		case REQUEST_RECEIVED_BY_EDGE_DEVICE:
		{
			Task task = (Task) ev.getData();

			networkModel.uploadFinished(task.getSubmittedLocation(), SimSettings.GENERIC_EDGE_DEVICE_ID);

			submitTaskToVm(task, 0, SimSettings.GENERIC_EDGE_DEVICE_ID);

			break;
		}
		case RESPONSE_RECEIVED_BY_MOBILE_DEVICE:
		{
			Task task = (Task) ev.getData();

			if(task.getAssociatedDatacenterId() == SimSettings.CLOUD_DATACENTER_ID)
				networkModel.downloadFinished(task.getSubmittedLocation(), SimSettings.CLOUD_DATACENTER_ID);
			else if(task.getAssociatedDatacenterId() != SimSettings.MOBILE_DATACENTER_ID)
				networkModel.downloadFinished(task.getSubmittedLocation(), SimSettings.GENERIC_EDGE_DEVICE_ID);

			AvnetSimLogger.getInstance().taskEnded(task.getCloudletId(), CloudSim.clock());
			break;
		}
		default:
			AvnetSimLogger.printLine(getName() + ".processOtherEvent(): " + "Error - event unknown by this DatacenterBroker. Terminating simulation...");
			System.exit(1);
			break;
		}
	}

	@Override
	public void submitTask(TaskProperty edgeTask) {
		NetworkModel networkModel = AvnetCoreSimulation.getInstance().getNetworkModel();

		//create a task
		Task task = createTask(edgeTask);

		Location currentLocation = AvnetCoreSimulation.getInstance().getMobilityModel().
				getLocation(task.getMobileDeviceId(),CloudSim.clock());

		//set location of the mobile device which generates this task
		task.setSubmittedLocation(currentLocation);

		//add related task to log list
		AvnetSimLogger.getInstance().addLog(task.getMobileDeviceId(),
				task.getCloudletId(),
				task.getTaskType(),
				(int)task.getCloudletLength(),
				(int)task.getCloudletFileSize(),
				(int)task.getCloudletOutputSize());

		//The tasks submitted by a mobile device (AV) should exclusively be received by the Edge Server
		int nextHopId = SimSettings.GENERIC_EDGE_DEVICE_ID;
		
		double WlanDelay = networkModel.getUploadDelay(task.getMobileDeviceId(), nextHopId, task);

		if(WlanDelay > 0){
			networkModel.uploadStarted(currentLocation, nextHopId, task);//Manage that method in AvnetModel
			schedule(getId(), WlanDelay, REQUEST_RECEIVED_BY_EDGE_DEVICE, task);
			AvnetSimLogger.getInstance().taskStarted(task.getCloudletId(), CloudSim.clock());
			AvnetSimLogger.getInstance().setUploadDelay(task.getCloudletId(), WlanDelay, NETWORK_DELAY_TYPES.WLAN_DELAY);
			//AvnetSimLogger.printLine("Tast submited to edgeServer");
		}
		else {
			AvnetSimLogger.getInstance().rejectedDueToBandwidth(
					task.getCloudletId(),
					CloudSim.clock(),
					SimSettings.VM_TYPES.EDGE_VM.ordinal(),
					NETWORK_DELAY_TYPES.WLAN_DELAY);
			//AvnetSimLogger.printLine("No Tast submited to edgeServer");
		}
	/*
		int nextHopId = AvnetCoreSimulation.getInstance().getEdgeOrchestrator().getDeviceToOffload(task);

		if(nextHopId == SimSettings.CLOUD_DATACENTER_ID){
			double WanDelay = networkModel.getUploadDelay(task.getMobileDeviceId(), nextHopId, task);

			if(WanDelay>0){
				networkModel.uploadStarted(currentLocation, nextHopId);
				AvnetSimLogger.getInstance().taskStarted(task.getCloudletId(), CloudSim.clock());
				AvnetSimLogger.getInstance().setUploadDelay(task.getCloudletId(), WanDelay, NETWORK_DELAY_TYPES.WAN_DELAY);
				schedule(getId(), WanDelay, REQUEST_RECEIVED_BY_CLOUD, task);
			}
			else
			{
				//SimLogger.printLine("Task #" + task.getCloudletId() + " cannot assign to any VM");
				AvnetSimLogger.getInstance().rejectedDueToBandwidth(
						task.getCloudletId(),
						CloudSim.clock(),
						SimSettings.VM_TYPES.CLOUD_VM.ordinal(),
						NETWORK_DELAY_TYPES.WAN_DELAY);
			}
		}
		else if(nextHopId == SimSettings.GENERIC_EDGE_DEVICE_ID) {
			double WlanDelay = networkModel.getUploadDelay(task.getMobileDeviceId(), nextHopId, task);

			if(WlanDelay > 0){
				networkModel.uploadStarted(currentLocation, nextHopId);
				schedule(getId(), WlanDelay, REQUEST_RECEIVED_BY_EDGE_DEVICE, task);
				AvnetSimLogger.getInstance().taskStarted(task.getCloudletId(), CloudSim.clock());
				AvnetSimLogger.getInstance().setUploadDelay(task.getCloudletId(), WlanDelay, NETWORK_DELAY_TYPES.WLAN_DELAY);
			}
			else {
				AvnetSimLogger.getInstance().rejectedDueToBandwidth(
						task.getCloudletId(),
						CloudSim.clock(),
						SimSettings.VM_TYPES.EDGE_VM.ordinal(),
						NETWORK_DELAY_TYPES.WLAN_DELAY);
			}
		}
		else {
			AvnetSimLogger.printLine("Unknown nextHopId! Terminating simulation...");
			System.exit(1);
		}
	*/
	}

	private void submitTaskToVm(Task task, double delay, int datacenterId) {
		//select a VM
				Vm selectedVM = AvnetCoreSimulation.getInstance().getEdgeOrchestrator().getVmToOffload(task, datacenterId);
				
				int vmType = 0;
				if(datacenterId == SimSettings.CLOUD_DATACENTER_ID)
					vmType = SimSettings.VM_TYPES.CLOUD_VM.ordinal();
				else
					vmType = SimSettings.VM_TYPES.EDGE_VM.ordinal();
				
				if(selectedVM != null){
					if(datacenterId == SimSettings.CLOUD_DATACENTER_ID)
						task.setAssociatedDatacenterId(SimSettings.CLOUD_DATACENTER_ID);
					else
						task.setAssociatedDatacenterId(selectedVM.getHost().getDatacenter().getId());

					//save related host id
					task.setAssociatedHostId(selectedVM.getHost().getId());
					
					//set related vm id
					task.setAssociatedVmId(selectedVM.getId());
					
					//bind task to related VM
					getCloudletList().add(task);
					bindCloudletToVm(task.getCloudletId(),selectedVM.getId());
					
					//SimLogger.printLine(CloudSim.clock() + ": Cloudlet#" + task.getCloudletId() + " is submitted to VM#" + task.getVmId());
					schedule(getVmsToDatacentersMap().get(task.getVmId()), delay, CloudSimTags.CLOUDLET_SUBMIT, task);

					AvnetSimLogger.getInstance().taskAssigned(task.getCloudletId(),
							selectedVM.getHost().getDatacenter().getId(),
							selectedVM.getHost().getId(),
							selectedVM.getId(),
							vmType);
				}
				else{
					//SimLogger.printLine("Task #" + task.getCloudletId() + " cannot assign to any VM");
					AvnetSimLogger.getInstance().rejectedDueToVMCapacity(task.getCloudletId(), CloudSim.clock(), vmType);
				}
	}

	private Task createTask(TaskProperty edgeTask){
		UtilizationModel utilizationModel = new UtilizationModelFull(); /*UtilizationModelStochastic*/
		UtilizationModel utilizationModelCPU = getCpuUtilizationModel();

		Task task = new Task(edgeTask.getMobileDeviceId(), ++taskIdCounter,
				edgeTask.getLength(), edgeTask.getPesNumber(),
				edgeTask.getInputFileSize(), edgeTask.getOutputFileSize(),
				utilizationModelCPU, utilizationModel, utilizationModel,edgeTask.getDirection(),edgeTask.getVelocity(),
				edgeTask.getNeededBandwidth(),edgeTask.getNeededCPU(),edgeTask.getNeededRam(),edgeTask.getNeededStorage(),edgeTask.getAvDistanceToMecServer());
		//set the owner of this task
		task.setUserId(this.getId());
		task.setTaskType(edgeTask.getTaskType());
		
		if (utilizationModelCPU instanceof CpuUtilizationModel_Custom) {
			((CpuUtilizationModel_Custom)utilizationModelCPU).setTask(task);
		}
		
		return task;
	}


}
