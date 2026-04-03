package com.ahao.wifichat.net.file;

public class PigeonPacket {

	String version;
	String packetNumber;
	String username;
	String hostname;
	int command;
	String message;

	public static PigeonPacket parse(String bin) {
		PigeonPacket packet = new PigeonPacket();
		String[] segments = bin.split(":", 6);
		packet.version = segments[0];
		packet.packetNumber = segments[1];
		packet.username = segments[2];
		packet.hostname = segments[3];
		packet.command = Integer.parseInt(segments[4]);
		packet.message = segments.length > 5 ? segments[5] : null;
		return packet;
	}

	public String getVersion() {
		return version;
	}

	public String getUsername() {
		return username;
	}

	public String getHostname() {
		return hostname;
	}

	public String getPacketNumber() {
		return packetNumber;
	}

	public int getCommand() {
		return command;
	}

	public String getMessage() {
		return message;
	}

}
