package com.boun.edgecloudsim.ul.avnet;

import java.text.DecimalFormat;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.utils.SimUtils;

public class AvnetHelper {
	/*In this implementation,we assume that all the base stations (Edge Servers in our case) have the same 
	 * characteristics. They have the same number of blocks CB_I, then we fixed
	 * it as the ratio of the available Wlan Bandwidth over the number of virtual
	 * machines, to ensure that all the VMs have the same bandwidth resources
	 */
	public static int NUMBER_LOCAL_RESOURCE_BLOCK;
	/*We assume that the propagation latencies of all autonomous slices (T_Prop) 
	are equal for all slicies and corresponds to wan_propagation_delay of the 
	simulator*/
	public static double  PROPAGATION_LATENCY_OF_AUTONOMOUS_SEVICES;
	/*We assume that the propagation latency between two adjacents 
	 * BS(P_Prop_i^j) is the same for all adjacents BS and equal to the 
	 * lan_internal_delay of the simulator
	 * second 
	 */
	public static double PROPAGATION_LATENCY_ADJACENT_BS;

	public static double beta;
	public AvnetHelper() {
		NUMBER_LOCAL_RESOURCE_BLOCK=SimSettings.getInstance().getNumOfEdgeVMs();
		PROPAGATION_LATENCY_OF_AUTONOMOUS_SEVICES=SimSettings.getInstance().getWanPropagationDelay();
		PROPAGATION_LATENCY_ADJACENT_BS=SimSettings.getInstance().getInternalLanDelay();
		beta=new Double(new DecimalFormat("##,###").format(SimUtils.getRandomDoubleNumber(0, 1)));
	}

	public double sliceScal(double latencyRequirement) {
		int NUMBER_LOCAL_RESOURCE_BLOCK_PRIME=0;

		int NUMBER_LOCAL_RESOURCE_BLOCK_SECOND=0;
		//We compute T_Total_FW
		double PROPAGATION_LATENCY_OF_SEVICES=PROPAGATION_LATENCY_OF_AUTONOMOUS_SEVICES+PROPAGATION_LATENCY_ADJACENT_BS
				+ meanHandlingLatency(NUMBER_LOCAL_RESOURCE_BLOCK)+tailLatency(NUMBER_LOCAL_RESOURCE_BLOCK);
		PROPAGATION_LATENCY_OF_SEVICES=new Double(new DecimalFormat("##.###").format(PROPAGATION_LATENCY_OF_SEVICES));
		NUMBER_LOCAL_RESOURCE_BLOCK_PRIME=NUMBER_LOCAL_RESOURCE_BLOCK;
		if(PROPAGATION_LATENCY_OF_SEVICES==latencyRequirement) {
			NUMBER_LOCAL_RESOURCE_BLOCK_PRIME=NUMBER_LOCAL_RESOURCE_BLOCK;//Condition to check because there is no specification about that in the paper
		}else {
			if (PROPAGATION_LATENCY_OF_SEVICES<latencyRequirement) {
				while(PROPAGATION_LATENCY_OF_SEVICES<latencyRequirement) {
					NUMBER_LOCAL_RESOURCE_BLOCK_PRIME--;//To check+++++++++++++++++++++++++++++++++++++
					//NUMBER_LOCAL_RESOURCE_BLOCK_SECOND=NUMBER_LOCAL_RESOURCE_BLOCK-NUMBER_LOCAL_RESOURCE_BLOCK_PRIME;
					double t_hand=meanHandlingLatency(NUMBER_LOCAL_RESOURCE_BLOCK_PRIME);
					double t_tail=tailLatency(NUMBER_LOCAL_RESOURCE_BLOCK_PRIME);
					PROPAGATION_LATENCY_OF_SEVICES=PROPAGATION_LATENCY_OF_AUTONOMOUS_SEVICES+PROPAGATION_LATENCY_ADJACENT_BS
							+ t_hand+t_tail;
					PROPAGATION_LATENCY_OF_SEVICES=new Double(new DecimalFormat("##.###").format(PROPAGATION_LATENCY_OF_SEVICES));
//					AvnetSimLogger.printLine("First:  T_Total_FW= "+PROPAGATION_LATENCY_OF_SEVICES+"; T_req= "+latencyRequirement+"; T_Hand= "+t_hand
//							+ "; T_Tail= "+t_tail+"; CB_i= "+NUMBER_LOCAL_RESOURCE_BLOCK+" Beta= "+beta+" CB_PRIME= "
//							+NUMBER_LOCAL_RESOURCE_BLOCK_PRIME);
				}
			}else {
				//if(PROPAGATION_LATENCY_OF_SEVICES>latencyRequirement) {
				while(PROPAGATION_LATENCY_OF_SEVICES>latencyRequirement) {
					NUMBER_LOCAL_RESOURCE_BLOCK_PRIME++;//To check also+++++++++++++++++++++++++++++++++++++
					//NUMBER_LOCAL_RESOURCE_BLOCK_SECOND=NUMBER_LOCAL_RESOURCE_BLOCK_PRIME-NUMBER_LOCAL_RESOURCE_BLOCK;
					double t_hand=meanHandlingLatency(NUMBER_LOCAL_RESOURCE_BLOCK_PRIME);
					double t_tail=tailLatency(NUMBER_LOCAL_RESOURCE_BLOCK_PRIME);
					PROPAGATION_LATENCY_OF_SEVICES=PROPAGATION_LATENCY_OF_AUTONOMOUS_SEVICES+PROPAGATION_LATENCY_ADJACENT_BS
							+ t_hand+t_tail;
					PROPAGATION_LATENCY_OF_SEVICES=new Double(new DecimalFormat("##.###").format(PROPAGATION_LATENCY_OF_SEVICES));
//					AvnetSimLogger.printLine("Second:  T_Total_FW= "+PROPAGATION_LATENCY_OF_SEVICES+"; T_req= "+latencyRequirement+"; T_Hand= "+t_hand
//							+ "; T_Tail= "+t_tail+"; CB_i= "+NUMBER_LOCAL_RESOURCE_BLOCK+" Beta= "+beta+" CB_PRIME= "
//							+NUMBER_LOCAL_RESOURCE_BLOCK_PRIME);
				}
				//}
			}
		}


		return NUMBER_LOCAL_RESOURCE_BLOCK_PRIME;
	}

	//Computation of T_Hand(Response time)
	public double meanHandlingLatency(double mu) {
		double ret=1/(mu*(1-beta));
		return new Double(new DecimalFormat("##.####").format(ret)); 
	}

	//Computation of T_Tail (Waiting time)
	public double tailLatency(double mu) {
		double ret=beta/(mu*(1-beta));
		return new Double(new DecimalFormat("##.####").format(ret)); 
	}
}
