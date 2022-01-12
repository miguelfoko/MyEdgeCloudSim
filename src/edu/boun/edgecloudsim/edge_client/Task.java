/*
 * Title:        EdgeCloudSim - Task
 * 
 * Description: 
 * Task adds app type, task submission location, mobile device id and host id
 * information to CloudSim's Cloudlet class.
 *               
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.edge_client;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.core.CloudSim;

import edu.boun.edgecloudsim.core.SimSettings.AV_DIRECTION;
import edu.boun.edgecloudsim.utils.Location;

public class Task extends Cloudlet {
	private Location submittedLocation;
	private double creationTime;
	private int type;
	private int mobileDeviceId;
	private int hostIndex;
	private int vmIndex;
	private int datacenterId;
	
	//For our proper use
	private AV_DIRECTION direction;//NORTH,SOUTH,WST,EAST
	private double velocity;
	private int neededBandwidth;
	private int neededCPU;
	private int neededRam;
	private int neededStorage;
	
	public Task(int _mobileDeviceId, int cloudletId, long cloudletLength, int pesNumber,
			long cloudletFileSize, long cloudletOutputSize,
			UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw, AV_DIRECTION direction1, double velocity1,int neededBandwidth1,
			int neededCPU1,int neededRam1,int neededStorage1) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize,
				cloudletOutputSize, utilizationModelCpu, utilizationModelRam,
				utilizationModelBw);
		
		mobileDeviceId = _mobileDeviceId;
		direction=direction1;
		velocity=velocity1;
		neededCPU=neededCPU1;
		neededRam=neededRam1;
		neededStorage=neededStorage1;
		neededBandwidth=neededBandwidth1;
		creationTime = CloudSim.clock();
	}
	
	//End of our implementation

	public Task(int _mobileDeviceId, int cloudletId, long cloudletLength, int pesNumber,
			long cloudletFileSize, long cloudletOutputSize,
			UtilizationModel utilizationModelCpu,
			UtilizationModel utilizationModelRam,
			UtilizationModel utilizationModelBw) {
		super(cloudletId, cloudletLength, pesNumber, cloudletFileSize,
				cloudletOutputSize, utilizationModelCpu, utilizationModelRam,
				utilizationModelBw);
		
		mobileDeviceId = _mobileDeviceId;
		creationTime = CloudSim.clock();
	}

	
	public void setSubmittedLocation(Location _submittedLocation){
		submittedLocation =_submittedLocation;
	}

	public void setAssociatedDatacenterId(int _datacenterId){
		datacenterId=_datacenterId;
	}
	
	public void setAssociatedHostId(int _hostIndex){
		hostIndex=_hostIndex;
	}

	public void setAssociatedVmId(int _vmIndex){
		vmIndex=_vmIndex;
	}
	
	public void setTaskType(int _type){
		type=_type;
	}

	public int getMobileDeviceId(){
		return mobileDeviceId;
	}
	
	public Location getSubmittedLocation(){
		return submittedLocation;
	}
	
	public int getAssociatedDatacenterId(){
		return datacenterId;
	}
	
	public int getAssociatedHostId(){
		return hostIndex;
	}

	public int getAssociatedVmId(){
		return vmIndex;
	}
	
	public int getTaskType(){
		return type;
	}
	
	public double getCreationTime() {
		return creationTime;
	}

	public AV_DIRECTION getDirection() {
		return direction;
	}

	public double getVelocity() {
		return velocity;
	}

	public int getNeededBandwidth() {
		return neededBandwidth;
	}

	public int getNeededCPU() {
		return neededCPU;
	}

	public int getNeededRam() {
		return neededRam;
	}

	public int getNeededStorage() {
		return neededStorage;
	}
	
	
}
