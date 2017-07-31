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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.packet.util.CountingInputStream;

public class MqttSubAck extends MqttAck {

	private static final int[] validReturnCodes = { MqttReturnCode.RETURN_CODE_MAX_QOS_0,
			MqttReturnCode.RETURN_CODE_MAX_QOS_1, MqttReturnCode.RETURN_CODE_MAX_QOS_2,
			MqttReturnCode.RETURN_CODE_UNSPECIFIED_ERROR, MqttReturnCode.RETURN_CODE_IMPLEMENTATION_SPECIFIC_ERROR,
			MqttReturnCode.RETURN_CODE_NOT_AUTHORIZED, MqttReturnCode.RETURN_CODE_TOPIC_FILTER_NOT_VALID,
			MqttReturnCode.RETURN_CODE_PACKET_ID_IN_USE, MqttReturnCode.RETURN_CODE_SHARED_SUB_NOT_SUPPORTED };

	// Fields
	private int[] returnCodes;
	private String reasonString;
	private Map<String, String> userDefinedPairs = new HashMap<>();

	public MqttSubAck(byte info, byte[] data) throws IOException, MqttException {
		super(MqttWireMessage.MESSAGE_TYPE_SUBACK);
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		CountingInputStream counter = new CountingInputStream(bais);
		DataInputStream inputStream = new DataInputStream(counter);
		msgId = inputStream.readUnsignedShort();

		parseIdentifierValueFields(inputStream);
		int remainingLength = data.length - counter.getCounter();
		returnCodes = new int[remainingLength];

		for(int i = 0; i < remainingLength; i++){
			int returnCode = inputStream.readUnsignedByte();
			validateReturnCode(returnCode, validReturnCodes);
			returnCodes[i] = returnCode;
		}


		inputStream.close();
	}

	public MqttSubAck(int[] returnCodes) throws MqttException {
		super(MqttWireMessage.MESSAGE_TYPE_SUBACK);
		for (int returnCode : returnCodes) {
			validateReturnCode(returnCode, validReturnCodes);
		}
		this.returnCodes = returnCodes;
	}

	@Override
	protected byte[] getVariableHeader() throws MqttException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream outputStream = new DataOutputStream(baos);

			// Encode the Message ID
			outputStream.writeShort(msgId);

			// Write Identifier / Value Fields
			byte[] identifierValueFieldsByteArray = getIdentifierValueFields();
			outputStream.write(encodeVariableByteInteger(identifierValueFieldsByteArray.length));
			outputStream.write(identifierValueFieldsByteArray);
			outputStream.flush();
			return baos.toByteArray();
		} catch (IOException ioe) {
			throw new MqttException(ioe);
		}
	}

	@Override
	public byte[] getPayload() throws MqttException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream outputStream = new DataOutputStream(baos);

			for (int returnCode : returnCodes) {
				outputStream.writeByte(returnCode);
			}

			outputStream.flush();
			return baos.toByteArray();
		} catch (IOException ioe) {
			throw new MqttException(ioe);
		}
	}

	private byte[] getIdentifierValueFields() throws MqttException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream outputStream = new DataOutputStream(baos);

			// If Present, encode the Reason String (3.9.2.1.2)
			if (reasonString != null) {
				outputStream.write(MqttPropertyIdentifiers.REASON_STRING_IDENTIFIER);
				encodeUTF8(outputStream, reasonString);
			}

			// If Present, encode the User Properties (3.9.2.1.3)
			if (userDefinedPairs.size() != 0) {
				for (Map.Entry<String, String> entry : userDefinedPairs.entrySet()) {
					outputStream.write(MqttPropertyIdentifiers.USER_DEFINED_PAIR_IDENTIFIER);
					encodeUTF8(outputStream, entry.getKey());
					encodeUTF8(outputStream, entry.getValue());
				}
			}

			outputStream.flush();
			return baos.toByteArray();
		} catch (IOException ioe) {
			throw new MqttException(ioe);
		}
	}

	private void parseIdentifierValueFields(DataInputStream dis) throws IOException, MqttException {
		// First get the length of the IV fields
		int length = readVariableByteInteger(dis).getValue();
		if (length > 0) {
			byte[] identifierValueByteArray = new byte[length];
			dis.read(identifierValueByteArray, 0, length);
			ByteArrayInputStream bais = new ByteArrayInputStream(identifierValueByteArray);
			DataInputStream inputStream = new DataInputStream(bais);
			while (inputStream.available() > 0) {
				// Get the first Byte
				byte identifier = inputStream.readByte();
				if (identifier == MqttPropertyIdentifiers.REASON_STRING_IDENTIFIER) {
					reasonString = decodeUTF8(inputStream);
				}  else if ( identifier == MqttPropertyIdentifiers.USER_DEFINED_PAIR_IDENTIFIER){
					String key = decodeUTF8(inputStream);
					String value = decodeUTF8(inputStream);
					userDefinedPairs.put(key, value);
				} else {
					// Unidentified Identifier
					throw new MqttException(MqttException.REASON_CODE_INVALID_IDENTIFIER);
				}
			}
		}
	}

	public int[] getReturnCodes() {
		return returnCodes;
	}

	public void setReturnCodes(int[] returnCodes) {
		this.returnCodes = returnCodes;
	}

	public String getReasonString() {
		return reasonString;
	}

	public void setReasonString(String reasonString) {
		this.reasonString = reasonString;
	}

	public Map<String, String> getUserDefinedPairs() {
		return userDefinedPairs;
	}

	public void setUserDefinedPairs(Map<String, String> userDefinedPairs) {
		this.userDefinedPairs = userDefinedPairs;
	}

}
