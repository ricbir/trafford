package uk.ac.manchester.trafford.pojo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.ac.manchester.trafford.Main;

public class NetworkPojo {
	private static final Logger LOGGER = LogManager.getLogger(Main.class);

	public List<EdgePojo> edgeList = new ArrayList<EdgePojo>();
	public List<TrafficLightPojo> trafficlightList = new ArrayList<TrafficLightPojo>();

	/**
	 * Serialize network info to the given file
	 * 
	 * @param file
	 */
	public static void Serialize(File file, NetworkPojo network) throws Exception {
		LOGGER.debug(">>> ENTER [Serialize]");
		ObjectMapper om = new ObjectMapper();
		om.writeValue(file, network);
		LOGGER.debug("<<< EXIT [Serialize]");
	}

	/**
	 * Deserialize network info from the given file
	 * 
	 * @param file
	 */
	public static NetworkPojo Deserialize(File file) throws Exception {
		LOGGER.debug(">>> ENTER [Deserialize]");
		ObjectMapper om = new ObjectMapper();
		NetworkPojo result = om.readValue(file, NetworkPojo.class);
		LOGGER.debug("<<< EXIT [Deserialize]");
		return result;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public EdgePojo getEdgeById(int id) {
		EdgePojo result = null;
		for (EdgePojo ep : edgeList) {
			if (ep.id == id) {
				result = ep;
				break;
			}
		}
		return result;
	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	public TrafficLightPojo getTrafficlightById(int id) {
		TrafficLightPojo result = null;
		for (TrafficLightPojo tl : trafficlightList) {
			if (tl.id == id) {
				result = tl;
				break;
			}
		}
		return result;
	}
}
