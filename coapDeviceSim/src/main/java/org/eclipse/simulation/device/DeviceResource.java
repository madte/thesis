/*******************************************************************************
 * Copyright (c) {DATE} {INITIAL COPYRIGHT OWNER} {OTHER COPYRIGHT OWNERS}.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Martin Kessel - initial implementation
 *******************************************************************************/

package org.eclipse.simulation.device;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP.Type;
import org.eclipse.californium.core.server.resources.CoapExchange;

/**
 * @author Madte
 *
 */
public class DeviceResource extends CoapResource {

    private String state = new String();
    private String type = new String();

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     */
    public DeviceResource(String _id, String _state, String _type) {

        super(_id);
        state = _state;
        type = _type;

        // make resource observable
        setObservable(true);
        setObserveType(Type.CON); // configure the notification type to CONs
        getAttributes().setObservable(); // mark observable in the Link-Format

        // schedule a periodic update task, otherwise let events call changed()
        Timer timer = new Timer();
        timer.schedule(new UpdateTask(), 0, 60000);

        // set display name
        getAttributes().setTitle(_id);
        // TODO Auto-generated constructor stub

    }

    private class UpdateTask extends TimerTask {
        @Override
        public void run() {
            // .. periodic update of the resource
            changed(); // notify all observers
        }
    }

    @Override
    public void handleGET(CoapExchange exchange) {
        // the Max-Age value should match the update interval
        // exchange.setMaxAge(1);
        // respond to the request
        exchange.respond(getState());
    }

    @Override
    public void handlePUT(CoapExchange exchange) {

        setState(exchange.getRequestText());
        // respond to the request
        exchange.respond(getState());
        changed(); // notify all observers
    }

}

/*
 * Definition of the Hello-World Resource
 */
class Switch extends DeviceResource {

    public Switch(String _name, String _state) {

        // set resource identifier
        super(_name, _state, "switch");
    }

}

class StringR extends DeviceResource {

    public StringR(String _name, String _state) {

        // set resource identifier
        super(_name, _state, "string");
    }

}