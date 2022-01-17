/*The networking module particularly handles the transmission delay in the 
 * WLAN and WAN by considering both upload and download data. The default 
 * implementation of the networking module is based on a single server queue
 *  model. Users of EdgeCloudSim can incorporate their own network behavior
 *   models by extending abstract NetworkModel class.*/

package com.edgecloudsim.ul.avnet;

import java.text.DecimalFormat;

import org.cloudbus.cloudsim.core.CloudSim;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.edge_client.Task;
import edu.boun.edgecloudsim.edge_server.EdgeHost;
import edu.boun.edgecloudsim.network.NetworkModel;
import edu.boun.edgecloudsim.utils.Location;
import edu.boun.edgecloudsim.utils.SimUtils;

public class AvnetNetwork extends NetworkModel {
	private double WlanPoissonMean; //seconds
	private double WanPoissonMean; //seconds
	private double avgTaskInputSize; //bytes
	private double avgTaskOutputSize; //bytes
	private int maxNumOfClientsInPlace;
	private double beta;
	public AvnetNetwork(int _numberOfMobileDevices, String _simScenario) {
		super(_numberOfMobileDevices, _simScenario);
	}

	@Override
	public void initialize() {
		WlanPoissonMean=0;
		WanPoissonMean=0;
		avgTaskInputSize=0;
		avgTaskOutputSize=0;
		maxNumOfClientsInPlace=0;
		beta=new Double(new DecimalFormat("##,###").format(SimUtils.getRandomDoubleNumber(0, 1)));
		//Calculate interarrival time and task sizes
		double numOfTaskType = 0;
		SimSettings SS = SimSettings.getInstance();
		for (int i=0; i<SimSettings.getInstance().getTaskLookUpTable().length; i++) {
			double weight = SS.getTaskLookUpTable()[i][0]/(double)100;
			if(weight != 0) {
				WlanPoissonMean += (SS.getTaskLookUpTable()[i][2])*weight;

				double percentageOfCloudCommunication = SS.getTaskLookUpTable()[i][1];
				WanPoissonMean += (WlanPoissonMean)*((double)100/percentageOfCloudCommunication)*weight;

				avgTaskInputSize += SS.getTaskLookUpTable()[i][5]*weight;

				avgTaskOutputSize += SS.getTaskLookUpTable()[i][6]*weight;

				numOfTaskType++;
			}
		}

		WlanPoissonMean = WlanPoissonMean/numOfTaskType;
		avgTaskInputSize = avgTaskInputSize/numOfTaskType;
		avgTaskOutputSize = avgTaskOutputSize/numOfTaskType;
		
		/**
		 * Creation of MEC servers and cloud server for our own implementation
		 * */
		AvnetEdgeServer mecServer=new AvnetEdgeServer();
		AvnetSimLogger.printLine("MEC server created++++++++++++++++++++++++++++++++++++++");

	}

	/**
	 * source device may be mobile device(AVs) or MEC servers  in our simulation scenarios!
	 */
	@Override
	public double getUploadDelay(int sourceDeviceId, int destDeviceId, Task task) {
		double delay = 0;
		Location accessPointLocation = AvnetCoreSimulation.getInstance().getMobilityModel().getLocation(sourceDeviceId,CloudSim.clock());

		//Edge Servr to cloud server (WAN)
		if(destDeviceId == SimSettings.CLOUD_DATACENTER_ID){
			delay = getWanUploadDelay(accessPointLocation, CloudSim.clock());
		}
		//Edge Server to Edge Server(MAN)
		else if(destDeviceId == SimSettings.EDGE_ORCHESTRATOR_ID){
			delay = getManUploadDelay(accessPointLocation, CloudSim.clock());
		}
		//mobile device to edge device (wifi access point)	(WLAN)
		else if (destDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID) {
			delay = getWlanUploadDelay(accessPointLocation, CloudSim.clock());
		}

		return delay;
	}

	/**
	 * destination device may be Mobile device (AVs) or MEC servers in our simulation scenarios!
	 */
	@Override
	public double getDownloadDelay(int sourceDeviceId, int destDeviceId, Task task) {
		//Special Case -> edge orchestrator to edge device (that is MEC server to MEC server) or vice-versa
		if((sourceDeviceId == SimSettings.EDGE_ORCHESTRATOR_ID &&
				destDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID)|| (sourceDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID &&
				destDeviceId == SimSettings.EDGE_ORCHESTRATOR_ID)){
			return SimSettings.getInstance().getMAN_PROPAGATION_DELAY();
		}
		//Cloud Server to MEC server
		if(sourceDeviceId == SimSettings.CLOUD_DATACENTER_ID &&
				destDeviceId == SimSettings.EDGE_ORCHESTRATOR_ID){
			return SimSettings.getInstance().getWAN_PROPAGATION_DELAY();
		}

		double delay = 0;
		Location accessPointLocation = AvnetCoreSimulation.getInstance().getMobilityModel().getLocation(destDeviceId,CloudSim.clock());

		//				//edge device (server) to edge orchestrator
		//				if(sourceDeviceId == SimSettings.GENERIC_EDGE_DEVICE_ID){
		//					double wlanDelay = getWlanDownloadDelay(accessPointLocation, CloudSim.clock());
		//					double wanDelay = getWanDownloadDelay(accessPointLocation, CloudSim.clock() + wlanDelay);
		//					if(wlanDelay > 0 && wanDelay >0)
		//						delay = wlanDelay + wanDelay;
		//				}
		//edge device (wifi access point) to mobile device
		//				else{
		delay = getWlanDownloadDelay(accessPointLocation, CloudSim.clock());

		EdgeHost host = (EdgeHost)(AvnetCoreSimulation.
				getInstance().
				getEdgeServerManager().
				getDatacenterList().get(sourceDeviceId).
				getHostList().get(0));

		//if source device id is the edge server which is located in another location, add internal lan delay
		//in our scenario, serving wlan ID is equal to the host id, because there is only one host in one place
		if(host.getLocation().getServingWlanId() != accessPointLocation.getServingWlanId())
			delay += (SimSettings.getInstance().getInternalLanDelay() * 2);
		//				}

		return delay;
	}

	public int getMaxNumOfClientsInPlace(){
		return maxNumOfClientsInPlace;
	}

	private int getDeviceCount(Location deviceLocation, double time){
		int deviceCount = 0;

		for(int i=0; i<numberOfMobileDevices; i++) {
			Location location = AvnetCoreSimulation.getInstance().getMobilityModel().getLocation(i,time);
			if(location.equals(deviceLocation))
				deviceCount++;
		}

		//record max number of client just for debugging
		if(maxNumOfClientsInPlace<deviceCount)
			maxNumOfClientsInPlace = deviceCount;

		return deviceCount;
	}

	private double calculateMM1(double propagationDelay, int bandwidth /*Kbps*/, double PoissonMean, double avgTaskSize /*KB*/, int deviceCount){
		double Bps=0, mu=0, lamda=0;

		avgTaskSize = avgTaskSize * (double)1000; //convert from KB to Byte

		Bps = bandwidth * (double)1000 / (double)8; //convert from Kbps to Byte per seconds
		lamda = ((double)1/(double)PoissonMean); //task per seconds
		mu = Bps / avgTaskSize ; //task per seconds
		double result = (double)1 / (mu-lamda*(double)deviceCount);

		result += propagationDelay;

		return (result > 5) ? -1 : result;
	}

	//Computation of T_Hand(Response time)
	public double meanHandlingLatency(double mu) {
		double ret=1/(mu*(1-beta));
		//return new Double(new DecimalFormat("##.####").format(ret));
		return ret;
	}

	//Computation of T_Tail (Waiting time)
	public double tailLatency(double mu) {	
		double ret=beta/(mu*(1-beta));
		//		return new Double(new DecimalFormat("##.####").format(ret));
		return ret;
	}

	/*We compute the propagation delay (T_Total_FW) with the formula of Eq.29 (SliceCal)
	 * serviceLatency is the WAN  propagation delay while internalLatency is the WLAN propagation delay
	 * numOfResourceBlock is the number of VM available for an Edge server
	 */

	private double computeTotalLatency(double serviceLatency, double internalLatency, double numOfResourceBlock) {
		double t_hand=meanHandlingLatency(numOfResourceBlock);
		double t_tail=tailLatency(numOfResourceBlock);
		double tTotalFw=serviceLatency+internalLatency+t_hand+t_tail;
		//		AvnetSimLogger.printLine("  T_Total_FW= "+tTotalFw+"; T_Hand= "+t_hand
		//				+ "; T_Tail= "+t_tail+"; CB_i= "+numOfResourceBlock+" Beta= "+beta);
		//		return new Double(new DecimalFormat("##.####").format(tTotalFw)); 
		return tTotalFw;
	}
	private double getWlanDownloadDelay(Location accessPointLocation, double time) {
		double propagationDelay=0;
		return calculateMM1(
				propagationDelay,
				SimSettings.getInstance().getWlanBandwidth(),
				WlanPoissonMean,
				avgTaskOutputSize,
				getDeviceCount(accessPointLocation, time));
	}

	private double getWlanUploadDelay(Location accessPointLocation, double time) {

		double propagationDelay=0;
		return calculateMM1(propagationDelay,
				SimSettings.getInstance().getWlanBandwidth(),
				WlanPoissonMean,
				avgTaskInputSize,
				getDeviceCount(accessPointLocation, time));
	}

	private double getWanDownloadDelay(Location accessPointLocation, double time) {
		double propagationDelay=SimSettings.getInstance().getWanPropagationDelay();
		return calculateMM1(propagationDelay,
				SimSettings.getInstance().getWanBandwidth(),
				WanPoissonMean,
				avgTaskOutputSize,
				getDeviceCount(accessPointLocation, time));
	}

	private double getWanUploadDelay(Location accessPointLocation, double time) {
		double propagationDelay=SimSettings.getInstance().getWanPropagationDelay();
		return calculateMM1(propagationDelay,
				SimSettings.getInstance().getWanBandwidth(),
				WanPoissonMean,
				avgTaskInputSize,
				getDeviceCount(accessPointLocation, time));
	}

	private double getManUploadDelay(Location accessPointLocation, double time) {
		double propagationDelay=SimSettings.getInstance().getMAN_PROPAGATION_DELAY();
		return calculateMM1(propagationDelay,
				SimSettings.getInstance().getManBandwidth(),
				WanPoissonMean,
				avgTaskInputSize,
				getDeviceCount(accessPointLocation, time));
	}

	@Override
	public void uploadStarted(Location accessPointLocation, int destDeviceId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void uploadFinished(Location accessPointLocation, int destDeviceId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void downloadStarted(Location accessPointLocation, int sourceDeviceId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void downloadFinished(Location accessPointLocation, int sourceDeviceId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void uploadStarted(Location accessPointLocation, int destDeviceId, Task task) {
		// TODO Auto-generated method stub
		/**
		 * Call the AvnetedgeServer here
		 * */
		
	}

	@Override
	public void uploadFinished(Location accessPointLocation, int destDeviceId, Task task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void downloadStarted(Location accessPointLocation, int sourceDeviceId, Task task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void downloadFinished(Location accessPointLocation, int sourceDeviceId, Task task) {
		// TODO Auto-generated method stub
		
	}

	
}
