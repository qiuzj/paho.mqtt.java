/*******************************************************************************
 * Copyright (c) 2016 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 * 	  Dave Locke   - Original MQTTv3 implementation
 *    James Sutton - Initial MQTTv5 implementation
 */
package org.eclipse.paho.mqttv5.common.packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.packet.MqttDisconnect;
import org.eclipse.paho.mqttv5.common.packet.MqttWireMessage;
import org.junit.Assert;
import org.junit.Test;

public class MqttDisconnectTest {
	private static final int returnCode = MqttReturnCode.RETURN_CODE_PROTOCOL_ERROR;
	private static final String reasonString = "Reason String 123.";
	private static final int sessionExpiryInterval = 60;
	private static final String serverReference = "127.0.0.1";
	private static final String userKey1 = "userKey1";
	private static final String userKey2 = "userKey2";
	private static final String userKey3 = "userKey3";
	private static final String userValue1 = "userValue1";
	private static final String userValue2 = "userValue2";
	private static final String userValue3 = "userValue3";
	
	@Test
	public void testEncodingMqttDisconnect() throws MqttException {
		MqttDisconnect mqttDisconnectPacket = generatemqttDisconnectPacket();
		mqttDisconnectPacket.getHeader();
		mqttDisconnectPacket.getPayload();
	}
	
	@Test
	public void testDecodingMqttDisconnect() throws MqttException, IOException {
		MqttDisconnect mqttDisconnectPacket = generatemqttDisconnectPacket();
		byte[] header = mqttDisconnectPacket.getHeader();
		byte[] payload = mqttDisconnectPacket.getPayload();
		
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		outputStream.write(header);
		outputStream.write(payload);
		//printByteArray(outputStream.toByteArray());
		
		MqttDisconnect decodedDisconnectPacket = (MqttDisconnect) MqttWireMessage.createWireMessage(outputStream.toByteArray());
		
		Assert.assertEquals(returnCode, decodedDisconnectPacket.getReturnCode());
		Assert.assertEquals(reasonString, decodedDisconnectPacket.getReasonString());
		Assert.assertEquals(sessionExpiryInterval, decodedDisconnectPacket.getSessionExpiryInterval());
		Assert.assertEquals(serverReference, decodedDisconnectPacket.getServerReference());
		Assert.assertEquals(3, decodedDisconnectPacket.getUserDefinedPairs().size());
		Assert.assertEquals(userValue1, decodedDisconnectPacket.getUserDefinedPairs().get(userKey1));
		Assert.assertEquals(userValue2, decodedDisconnectPacket.getUserDefinedPairs().get(userKey2));
		Assert.assertEquals(userValue3, decodedDisconnectPacket.getUserDefinedPairs().get(userKey3));
		
		
	}
	
	private MqttDisconnect generatemqttDisconnectPacket() throws MqttException{
		MqttDisconnect mqttDisconnectPacket = new MqttDisconnect(returnCode);
		mqttDisconnectPacket.setReasonString(reasonString);
		mqttDisconnectPacket.setSessionExpiryInterval(sessionExpiryInterval);
		mqttDisconnectPacket.setServerReference(serverReference);
		Map<String, String> userDefinedPairs = new HashMap<String,String>();
		userDefinedPairs.put(userKey1, userValue1);
		userDefinedPairs.put(userKey2, userValue2);
		userDefinedPairs.put(userKey3, userValue3);
		mqttDisconnectPacket.setUserDefinedPairs(userDefinedPairs);
		return mqttDisconnectPacket;
	}
	


}
