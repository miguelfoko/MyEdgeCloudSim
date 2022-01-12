/*
 * Title:        EdgeCloudSim - EdgeTask
 * 
 * Description: 
 * A custom class used in Load Generator Model to store tasks information
 * 
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 * Copyright (c) 2017, Bogazici University, Istanbul, Turkey
 */

package edu.boun.edgecloudsim.utils;

import org.apache.commons.math3.distribution.ExponentialDistribution;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.core.SimSettings.AV_DIRECTION;

public class TaskProperty {
	private double startTime;
	private long length, inputFileSize, outputFileSize;
	private int taskType;
	private int pesNumber;
	private int mobileDeviceId;
	
	//For our implementation
	
	private int vmUtilizationOnEdge;
	private int vmUtilizationOnCloud;
	private double delaySensitivity;
	private int maxDelayRequirement;
	private AV_DIRECTION direction;
	private double velocity;
	private int neededBandwidth;
	private int neededCPU;
	private int neededRam;
	private int neededStorage;

	//Starting Our Constructors
	public TaskProperty(double _startTime, int _mobileDeviceId, int _taskType, int _pesNumber, long _length, long _inputFileSize, 
			long _outputFileSize, int vmUtilizationOnEdge1, int vmUtilizationOnCloud1,double delaySensitivity1,
			int maxDelayRequirement1,AV_DIRECTION direction1,double velocity1,int neededBandwidth1,int neededCPU1,
			int neededRam1,int neededStorage1) {
		startTime=_startTime;
		mobileDeviceId=_mobileDeviceId;
		taskType=_taskType;
		pesNumber = _pesNumber;
		length = _length;
		outputFileSize = _inputFileSize;
		inputFileSize = _outputFileSize;
		vmUtilizationOnCloud=vmUtilizationOnCloud1;
		vmUtilizationOnEdge=vmUtilizationOnEdge1;
		delaySensitivity=delaySensitivity1;
		maxDelayRequirement=maxDelayRequirement1;
		direction=direction1;
		velocity=velocity1;
		neededBandwidth=neededBandwidth1;
		neededCPU=neededCPU1;
		neededRam=neededRam1;
		neededStorage=neededStorage1;
	}
	
	public TaskProperty(int _mobileDeviceId, int _taskType, double _startTime, ExponentialDistribution[][] expRngList) {
		mobileDeviceId=_mobileDeviceId;
		startTime=_startTime;
		taskType=_taskType;

		inputFileSize = (long)expRngList[_taskType][0].sample();
		outputFileSize =(long)expRngList[_taskType][1].sample();
		length = (long)expRngList[_taskType][2].sample();

		pesNumber = (int)SimSettings.getInstance().getTaskLookUpTable()[_taskType][8];
		vmUtilizationOnCloud=(int)SimSettings.getInstance().getTaskLookUpTable()[_taskType][10];
		vmUtilizationOnEdge=(int)SimSettings.getInstance().getTaskLookUpTable()[_taskType][11];
		delaySensitivity=(double)SimSettings.getInstance().getTaskLookUpTable()[_taskType][12];
		maxDelayRequirement=(int)SimSettings.getInstance().getTaskLookUpTable()[_taskType][13];
		direction=SimSettings.AV_DIRECTION.getRandomDirection();
		velocity=SimUtils.getRandomDoubleNumber(SimSettings.getInstance().getMIN_TASK_VELOCITY(), SimSettings.getInstance().getMAX_TASK_VELOCITY());
		neededBandwidth=SimUtils.getRandomNumber(SimSettings.getInstance().getMIN_TASK_BANDWIDTH_NEEDED(), SimSettings.getInstance().getMAX_TASK_BANDWIDTH_NEEDED());
		neededCPU=SimUtils.getRandomNumber(SimSettings.getInstance().getMIN_TASK_CPU_NEEDED(), SimSettings.getInstance().getMAX_TASK_CPU_NEEDED());;
		neededRam=SimUtils.getRandomNumber(SimSettings.getInstance().getMIN_TASK_RAM_NEEDED(), SimSettings.getInstance().getMAX_TASK_RAM_NEEDED());;;
		neededStorage=SimUtils.getRandomNumber(SimSettings.getInstance().getMIN_TASK_STORAGE_NEEDED(), SimSettings.getInstance().getMAX_TASK_STORAGE_NEEDED());;;
	}
	
	//Ending our constructors
	
	public TaskProperty(double _startTime, int _mobileDeviceId, int _taskType, int _pesNumber, long _length, long _inputFileSize, long _outputFileSize) {
		startTime=_startTime;
		mobileDeviceId=_mobileDeviceId;
		taskType=_taskType;
		pesNumber = _pesNumber;
		length = _length;
		outputFileSize = _inputFileSize;
		inputFileSize = _outputFileSize;
	}


	public TaskProperty(int mobileDeviceId, double startTime, ExponentialDistribution[] expRngList) {
		this.mobileDeviceId = mobileDeviceId;
		this.startTime = startTime;
		taskType = 0;
		inputFileSize = (long)expRngList[0].sample();
		outputFileSize = (long)expRngList[1].sample();
		length = (long) expRngList[2].sample();
		pesNumber = (int)SimSettings.getInstance().getTaskLookUpTable()[0][8];
	}

	public double getStartTime(){
		return startTime;
	}

	public long getLength(){
		return length;
	}

	public long getInputFileSize(){
		return inputFileSize;
	}

	public long getOutputFileSize(){
		return outputFileSize;
	}

	public int getTaskType(){
		return taskType;
	}

	public int getPesNumber(){
		return pesNumber;
	}


	public int getVmUtilizationOnEdge() {
		return vmUtilizationOnEdge;
	}

	public int getVmUtilizationOnCloud() {
		return vmUtilizationOnCloud;
	}

	public int getMobileDeviceId() {
		return mobileDeviceId;
	}

	@Override
	public String toString() {
		return "TaskProperty [mobileDeviceId=" + mobileDeviceId + ", direction=" + direction + ", velocity=" + velocity
				+ ", neededBandwidth=" + neededBandwidth + ", neededCPU=" + neededCPU + ", neededRam=" + neededRam
				+ ", neededStorage=" + neededStorage + "]";
	}

	
	

	
	
	
	
	
}
