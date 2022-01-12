/*The mobility module manages the location of edge devices and clients. 
 * Since CloudSim focuses on the conventional cloud computing principles, 
 * the mobility is not considered in the framework. In our design, each 
 * mobile device has x and y coordinates which are updated according to 
 * the dynamically managed hash table. By default, we provide a nomadic 
 * mobility model, but different mobility models can be implemented by 
 * extending abstract MobilityModel class.*/

package com.boun.edgecloudsim.ul.avnet;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.boun.edgecloudsim.core.SimSettings;
import edu.boun.edgecloudsim.mobility.MobilityModel;
import edu.boun.edgecloudsim.utils.Location;

import edu.boun.edgecloudsim.utils.SimUtils;

public class AvnetMobility extends MobilityModel{
	private List<TreeMap<Double, Location>> treeMapArray;

	public AvnetMobility(int _numberOfMobileDevices, double _simulationTime) {
		super(_numberOfMobileDevices, _simulationTime);
	}
	@Override
	public void initialize() {
		treeMapArray = new ArrayList<TreeMap<Double, Location>>();
		ExponentialDistribution[] expRngList = new ExponentialDistribution[SimSettings.getInstance().getNumOfEdgeDatacenters()];
		//create random number generator for each place
		Document doc = SimSettings.getInstance().getEdgeDevicesDocument();
		NodeList datacenterList = doc.getElementsByTagName("datacenter");
		for (int i = 0; i < datacenterList.getLength(); i++) {
			Node datacenterNode = datacenterList.item(i);
			Element datacenterElement = (Element) datacenterNode;
			Element location = (Element)datacenterElement.getElementsByTagName("location").item(0);
			String attractiveness = location.getElementsByTagName("attractiveness").item(0).getTextContent();
			int placeTypeIndex = Integer.parseInt(attractiveness);

			expRngList[i] = new ExponentialDistribution(SimSettings.getInstance().getMobilityLookUpTable()[placeTypeIndex]);
		}
		//initialize tree maps and position of mobile devices
		for(int i=0; i<numberOfMobileDevices; i++) {
			treeMapArray.add(i, new TreeMap<Double, Location>());

			int randDatacenterId = SimUtils.getRandomNumber(0, SimSettings.getInstance().getNumOfEdgeDatacenters()-1);
			Node datacenterNode = datacenterList.item(randDatacenterId);
			Element datacenterElement = (Element) datacenterNode;
			Element location = (Element)datacenterElement.getElementsByTagName("location").item(0);
			String attractiveness = location.getElementsByTagName("attractiveness").item(0).getTextContent();
			int placeTypeIndex = Integer.parseInt(attractiveness);
			int wlan_id = Integer.parseInt(location.getElementsByTagName("wlan_id").item(0).getTextContent());
			int x_pos = Integer.parseInt(location.getElementsByTagName("x_pos").item(0).getTextContent());
			int y_pos = Integer.parseInt(location.getElementsByTagName("y_pos").item(0).getTextContent());

			//start locating user shortly after the simulation started (e.g. 10 seconds)
			treeMapArray.get(i).put(SimSettings.CLIENT_ACTIVITY_START_TIME, new Location(placeTypeIndex, wlan_id, x_pos, y_pos));
		}

		for(int i=0; i<numberOfMobileDevices; i++) {
			TreeMap<Double, Location> treeMap = treeMapArray.get(i);

			while(treeMap.lastKey() < SimSettings.getInstance().getSimulationTime()) {				
				boolean placeFound = false;
				int currentLocationId = treeMap.lastEntry().getValue().getServingWlanId();
				double waitingTime = expRngList[currentLocationId].sample();

				while(placeFound == false){
					int newDatacenterId = SimUtils.getRandomNumber(0,SimSettings.getInstance().getNumOfEdgeDatacenters()-1);
					if(newDatacenterId != currentLocationId){
						placeFound = true;
						Node datacenterNode = datacenterList.item(newDatacenterId);
						Element datacenterElement = (Element) datacenterNode;
						Element location = (Element)datacenterElement.getElementsByTagName("location").item(0);
						String attractiveness = location.getElementsByTagName("attractiveness").item(0).getTextContent();
						int placeTypeIndex = Integer.parseInt(attractiveness);
						int wlan_id = Integer.parseInt(location.getElementsByTagName("wlan_id").item(0).getTextContent());
						int x_pos = Integer.parseInt(location.getElementsByTagName("x_pos").item(0).getTextContent());
						int y_pos = Integer.parseInt(location.getElementsByTagName("y_pos").item(0).getTextContent());

						treeMap.put(treeMap.lastKey()+waitingTime, new Location(placeTypeIndex, wlan_id, x_pos, y_pos));
					}
				}
				if(!placeFound){
					AvnetSimLogger.printLine("impossible is occurred! location cannot be assigned to the device!");
					System.exit(1);
				}
			}
		}

	}

	@Override
	public Location getLocation(int deviceId, double time) {
		TreeMap<Double, Location> treeMap = treeMapArray.get(deviceId);

		Entry<Double, Location> e = treeMap.floorEntry(time);

		if(e == null){
			AvnetSimLogger.printLine("impossible is occurred! no location is found for the device '" + deviceId + "' at " + time);
			System.exit(1);
		}

		return e.getValue();
	}

}
