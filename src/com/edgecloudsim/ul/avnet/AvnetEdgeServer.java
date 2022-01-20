package com.edgecloudsim.ul.avnet;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.core.CloudSim;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.core.SimSettings.AV_DIRECTION;
import edu.boun.edgecloudsim.core.SimSettings.NETWORK_DELAY_TYPES;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.utils.SimUtils;

public class AvnetEdgeServer {
	
	private static final int BASE = 100000; //start from base in order not to conflict cloudsim tag!
	
	
	private List<Task> listOfTasks;
	private int usedComputingResources;
	private int usedStoringResources;
	private int usedRamResources;
	private int theta;//computing ressource needed my the mec server for internal processing
	private int lambda;//computing ressource needed my the mec server for internal processing
	private AV_DIRECTION orientation;//This parameter will help to direct AVs according to their direction
	private static int[] distance;//Virtual distance between MEC severs and AVs
	private int radius;
	public static int numberOfTasks, numOfTaskProcessedInternaly,numOfTaskProcessedAwayDueToCapacity,numOfTaskProcessedAwayDueToAvPosition
	,numOfTaskAlreadyProcessed;
	public static double wlanDelay,lanDelay,wanDelay,manDelay;



	public AvnetEdgeServer() {
		this.listOfTasks=new ArrayList<Task>();
		this.theta=SimSettings.getInstance().getTHETA();
		this.lambda=SimSettings.getInstance().getLAMBDA();
		this.radius=SimSettings.getInstance().getMEC_SERVER_RADIUS_COVERAGE();
		this.usedComputingResources=0;//That is CR_i in the manuscript
		this.usedStoringResources=0;//That is SR_i in the manuscript
		this.usedRamResources=0;//That is a complement to CR_i in the manuscript
	}



	public AvnetEdgeServer(List<Task> listOfTasks, int computingResources, int storingResources, int ramResources,
			int theta, int lambda, AV_DIRECTION orientation, int radius) {
		super();
		this.listOfTasks = listOfTasks;
		this.theta = theta;
		this.lambda = lambda;
		this.orientation = orientation;
		this.radius = radius;
	}


	/**
	 * The MEC SDN controller that also acts as the SFC controller 
	 * ****This function should return the delay used for task computing****
	 * */
	public double mecSDNController(Task incomingTask) {
		//AvnetSimLogger.printLine("***************************************INSIDE MEC SDN CONTROLLER*****************************");
		numberOfTasks++;
		double returnValue=0;
		this.listOfTasks.add(incomingTask);
		this.usedComputingResources+=incomingTask.getNeededCPU();
		this.usedStoringResources+=incomingTask.getNeededStorage();
		this.usedRamResources+=incomingTask.getNeededRam();
		boolean position=checkPosition(incomingTask);
		if(position) {
			
			if(mecVNFChecker(incomingTask)) {
				/**
				 * Task can be computed in the actual MEC server
				 * The propagation delay considers both the time for the request to reach the MEC server and time for the response
				 * to reach the requesting AV
				 */
				numOfTaskProcessedInternaly++;
				mecVNFProcessor(incomingTask);
				mecVNFSender(incomingTask);
				mecAvResult(incomingTask);
				returnValue=2*SimSettings.getInstance().getWLAN_PROPAGATION_DELAY()
						+4*SimSettings.getInstance().getInternalLanDelay();
			}else {
				AvnetSimLogger.printLine("**********************INSIDE MEC SDN CONTROLLER: VNF Checker Failed" + "***********************");
				mecVNFSender(incomingTask);
				if(incomingTask.isProcess()) {
					/**
					 * Task is processed and result is sent to the cloud SDN controller for saving
					 * */
					numOfTaskAlreadyProcessed++;
					mecCloudResult(incomingTask);
					returnValue=2*(SimSettings.getInstance().getWAN_PROPAGATION_DELAY()+
							SimSettings.getInstance().getMAN_PROPAGATION_DELAY()+
							SimSettings.getInstance().getWAN_PROPAGATION_DELAY());
				}else {
					/**
					 * Task is sent to the next MEC server of to the cloud server for processing
					 * */
					numOfTaskProcessedAwayDueToCapacity++;
					mecVNFReceiver(incomingTask);
				}
				/**
				 *The task should be computed to the Cloud server due to a capacity problem in the actual MEC sever 
				 *To obtain the numbers, we referred to Fig2.4: MEC server internal architecture of our scheme
				 */
				returnValue=5*SimSettings.getInstance().getInternalLanDelay()
						+2*SimSettings.getInstance().getWAN_PROPAGATION_DELAY()
						+2*SimSettings.getInstance().getMAN_PROPAGATION_DELAY()
						+2*SimSettings.getInstance().getWLAN_PROPAGATION_DELAY();
				//Find a way to materialized tasks that belong to Actual MEC server that are not yet proceeded
			}

		}else {
			/** 
			 * In our simulations, we consider that the changing of MEC server for computing is done once, but we can
			 * still consider other cases by playing with the MEC server diameters>>>>>
			 * 
			 * Here we forward the task to the cloud server because the requesting AV will no more be in the coverage area 
			 * of the actual MEC server at the end of it computations if it does it
			 * We use the internalLANDelay to materialize the changing of the MEC server (it is done by the cloud server)
			 */
			numOfTaskProcessedAwayDueToAvPosition++;
			mecCloudResult(incomingTask);
			returnValue=2*SimSettings.getInstance().getWLAN_PROPAGATION_DELAY()+
					2*SimSettings.getInstance().getMAN_PROPAGATION_DELAY()+
					2*SimSettings.getInstance().getWAN_PROPAGATION_DELAY()
					+4*SimSettings.getInstance().getInternalLanDelay();

		}
		this.usedComputingResources-=incomingTask.getNeededCPU();
		this.usedStoringResources-=incomingTask.getNeededStorage();
		this.usedRamResources-=incomingTask.getNeededRam();
		this.listOfTasks.remove(incomingTask);
		return returnValue;
	}

	/**
	 * Function that helps to check if the requesting AV will still be in the 
	 * coverage area of the actual MEC server at the end of computations
	 * */
	private boolean checkPosition(Task incomingTask) {
		//AvnetSimLogger.printLine("**********************INSIDE MEC SDN CONTROLLER: Checking AV's position" + "***********************");
		if(incomingTask.getAvDistanceTpMecServer()<=this.radius)
			return true;
		return false;
	}

	/*The use of the following function will consider little variation on the delay, bandwidth, computing and storing resources*/

	private void mecSFCClasifier() {

	}

	private void mecVNFReceiver(Task incomingTask) {
		//AvnetSimLogger.printLine("**********************INSIDE MEC SDN CONTROLLER: NFV Receiver" + "***********************");
		
		/**
		 * Delay Management
		 * Communication between two VMs(Here VM4 and VM1) in the same MEC server is materialized by the InternalLanDelay
		 * from VM4 to VM1
		 * */
		lanDelay+=SimSettings.getInstance().getInternalLanDelay();
		
		
		if(checkPosition(incomingTask)) {
			incomingTask.setAvDistanceTpMecServer(SimUtils.getRandomNumber(SimSettings.getInstance().getMIN_AV_DISTANCE_TO_MEC_SERVER(),SimSettings.getInstance().getMAX_AV_DISTANCE_TO_MEC_SERVER()));
			mecSDNController(incomingTask);
		}else {
			mecVNFReceiver(incomingTask);
		}
	}

	private void mecVNFSender(Task incomingTask) {
		//AvnetSimLogger.printLine("**********************INSIDE MEC SDN CONTROLLER: NFV Sender" + "***********************");
		/**
		 * Delay Management
		 * Communication between two VMs(Here VM1 and VM4 & VM4 and Cloud server) in the same MEC server is materialized by the InternalLanDelay
		 * from VM1 to VM4 and from VM4 to CDC
		 * */
		lanDelay+=SimSettings.getInstance().getInternalLanDelay();
		manDelay+=SimSettings.getInstance().getMAN_PROPAGATION_DELAY();
		wanDelay+=SimSettings.getInstance().getWAN_PROPAGATION_DELAY();
	}

	private boolean mecVNFChecker(Task incomingTask) {
		int computingResources=SimSettings.getInstance().getMEC_COMPUTING_RESOURCES();
		int storingResources=SimSettings.getInstance().getMEC_STORING_RESOURCES();
		int ramResources=SimSettings.getInstance().getMEC_RAM_RESOURCES();
		
		/**
		 * Delay Management
		 * Communication between two VMs(Here VM1 and VM2) in the same MEC server is materialized by the InternalLanDelay
		 * 2 beacause the task arived to VM3 and go back to VM1 after processing
		 * */
		lanDelay+=2*SimSettings.getInstance().getInternalLanDelay();
		//wlanDelay+=2*SimSettings.getInstance().getWLAN_PROPAGATION_DELAY();
		incomingTask.setProcess(true);
		
		
		
		/**
		 * We should update the used Computing/Storing and Ram resources in order to evaluate their cost at the end of our proposal
		 * */
		/**
		 * Check if the total needed resources to compute the incoming task are available in the actual MEC server
		 * */
		int comp=computingResources-this.theta*computingResources/100;
		int stor=storingResources-this.lambda*storingResources/100;
		int ram=ramResources-this.theta*computingResources/100;
//		AvnetSimLogger.printLine("UsedComputingResources: "+usedComputingResources+"; comp= "+comp+"; UsedStoringesources: "+
//		 usedStoringResources+"; Storage: "+stor+"; UsedRam: "+usedRamResources+";Ram= "+ram);
		if((usedComputingResources<=comp && usedRamResources<=ram) && usedStoringResources<=stor)
			return true;
		return false;

	}

	private void mecVNFProcessor(Task incomingTask) {
		/**
		 * Delay Management
		 * Communication between two VMs(Here VM1 and VM3) in the same MEC server is materialized by the InternalLanDelay
		 * 2 beacause the task arived to VM3 and go back to VM1 after processing
		 * */
		lanDelay+=2*SimSettings.getInstance().getInternalLanDelay();
		//wlanDelay+=2*SimSettings.getInstance().getWLAN_PROPAGATION_DELAY();
		incomingTask.setProcess(true);
		//AvnetSimLogger.printLine("**********************INSIDE MEC SDN CONTROLLER: Task Processing" + "***********************");
	}

	private void mecAvResult(Task incomingTask) {
		/**
		 * Delay Management
		 * Communication between two VMs(Here VM1 and AV) in the same MEC server is materialized by the InternalLanDelay
		 * from VM1 to VM4 and from VM4 to CDC
		 * */
		wlanDelay+=2*SimSettings.getInstance().getWLAN_PROPAGATION_DELAY();
	}

	private void mecCloudResult(Task incomingTask) {
		if(incomingTask.isFinished()) {
			/**
			 * Task is sent to cloud for storing
			 * */
//			numOfTaskAlreadyProcessed++;
		}
		else {
			/** Task is sent to cloud for processing*/
			if(checkPosition(incomingTask)) {
//				numOfTaskProcessedAwayDueToAvPosition++;
			}
			else {
//				numOfTaskProcessedAwayDueToCapacity++;
			}
			
		}
		//AvnetCoreSimulation.getInstance().getMobileDeviceManager().sendTaskToCloud(incomingTask);		
	}



}
