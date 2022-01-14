package com.edgecloudsim.ul.avnet;

import java.util.ArrayList;
import java.util.List;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.core.SimSettings.AV_DIRECTION;
import edu.boun.edgecloudsim.edge_client.Task;

public class AvnetEdgeServer {
	private List<Task> listOfTasks;
	private int usedComputingResources;
	private int usedStoringResources;
	private int usedRamResources;
	private int theta;//computing ressource needed my the mec server for internal processing
	private int lambda;//computing ressource needed my the mec server for internal processing
	private AV_DIRECTION orientation;//This parameter will help to direct AVs according to their direction
	private static int[] distance;//Virtual distance between MEC severs and AVs
	private int radius;
	
	
	
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
	private double mecSDNController(Task incomingTask) {
		double returnValue=0;
		int computingResources=SimSettings.getInstance().getMEC_COMPUTING_RESOURCES();
		int storingResources=SimSettings.getInstance().getMEC_STORING_RESOURCES();
		int ramResources=SimSettings.getInstance().getMEC_RAM_RESOURCES();
		this.usedComputingResources+=incomingTask.getNeededCPU();
		this.usedStoringResources+=incomingTask.getNeededStorage();
		this.usedRamResources+=incomingTask.getNeededRam();
		boolean position=checkPosition(incomingTask);
		if(position) {
			/**
			 * Check if the total needed resources to compute the incoming task are available in the actual MEC server
			 * */
			int comp=computingResources-this.theta*computingResources;
			int stor=storingResources-this.lambda*storingResources;
			int ram=ramResources-this.theta*computingResources;
			if((usedComputingResources<=comp && usedRamResources<=ram) && usedStoringResources<=stor) {
				/**
				 * Task can be computed in the actual MEC server
				 * The propagation delay considers both the time for the request to reach the MEC server and time for the response
				 * to reach the requesting AV
				 */
				
				returnValue=2*SimSettings.getInstance().getWLAN_PROPAGATION_DELAY();
			}else {
				/**
				 *The task should be computed to the Cloud server due to a capacity problem in the actual MEC sever 
				 */
				returnValue=2*(SimSettings.getInstance().getWAN_PROPAGATION_DELAY()+
						SimSettings.getInstance().getMAN_PROPAGATION_DELAY()+
						SimSettings.getInstance().getWAN_PROPAGATION_DELAY());
				//Find a way to materialized tasks that belong to Actual MEC server that are not yet procedeed
			}
			
		}else {
			/** 
			 * In our simulations, we consider that the changing of MEC server for computing is donne once, but we can
			 * still consider other cases by playing with the MEC server diameters>>>>>
			 * 
			 * Here we forward the task to the cloud server because the requesting AV will no more be in the coverage area 
			 * of the actual MEC server at the end of it computations if it does it
			 * We use the internalLANDelay to materialise the changing of the MEC server (it is donne by the cloud server)
			 */
			returnValue=2*(SimSettings.getInstance().getWLAN_PROPAGATION_DELAY()+
					SimSettings.getInstance().getMAN_PROPAGATION_DELAY()+
					SimSettings.getInstance().getWAN_PROPAGATION_DELAY())
					+SimSettings.getInstance().getInternalLanDelay();
			
		}
		this.usedComputingResources-=incomingTask.getNeededCPU();
		this.usedStoringResources-=incomingTask.getNeededStorage();
		this.usedRamResources-=incomingTask.getNeededRam();
		return returnValue;
	}
	
	/**
	 * Function that helps to check if the requesting AV will still be in the 
	 * coverage area of the actual MEC server at the end of computations
	 * */
	private boolean checkPosition(Task incomingTask) {
		//to complete
		return false;
	}

	/*The use of the following function will consider little variation on the delay, bandwidth, computing and storing resources*/

	private void mecSFCClasifier() {
		
	}
	
	private void mecVNFReceiver() {
		
	}
	
	private void mecVNFSender() {
		
	}
	
	private void mecVNFChecker() {
		
	}
	
	private void mecVNFProcessor() {
		
	}

}
