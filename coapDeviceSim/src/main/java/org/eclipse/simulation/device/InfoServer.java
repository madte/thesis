/**
 *
 */
package org.eclipse.simulation.device;

import java.net.InetSocketAddress;

import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.californium.core.network.interceptors.MessageTracer;

import com.google.gson.Gson;

/**
 * @author Madte
 *
 */
public class InfoServer extends CoapServer {

    class DeviceInfo {

        public String id;
        public String type;

    }

    private static final int COAP_INFO_PORT = 5682; // NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);

    private DeviceInfo deviceInfo = new DeviceInfo();

    /**
     *
     */
    public InfoServer(String _ipAddr, String _id, String _type) {
        // TODO Auto-generated constructor stub

        Gson gson = new Gson();
        deviceInfo.id = _id;
        deviceInfo.type = _type;
        String jsonDeviceInfo = gson.toJson(deviceInfo);

        this.add(new StringR("info", jsonDeviceInfo)); // TODO make extra resource that only supports GET

        // add endpoints on all IP addresses
        // this.addEndpoints();
        this.addEndpoint(new CoapEndpoint(new InetSocketAddress(_ipAddr, COAP_INFO_PORT)));
        this.start();

        // add special interceptor for message traces
        for (Endpoint ep : this.getEndpoints()) {
            ep.addInterceptor(new MessageTracer());
        }

        System.out.println(" InfoServer is listening on port " + COAP_INFO_PORT);
    }

}
