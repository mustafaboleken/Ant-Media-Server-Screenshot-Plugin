package io.antmedia.app;

import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.antmedia.plugin.api.IPacketListener;
import io.antmedia.plugin.api.StreamParametersInfo;

public class ScreenshotPluginPacketListener implements IPacketListener{

	private int packetCount = 0;
	
	protected static Logger logger = LoggerFactory.getLogger(ScreenshotPluginPacketListener.class);

	@Override
	public void writeTrailer(String streamId) {
		System.out.println("ScreenshotPluginPacketListener.writeTrailer()");
		
	}

	@Override
	public AVPacket onVideoPacket(String streamId, AVPacket packet) {
		packetCount++;
		return packet;
	}
	
	@Override
	public AVPacket onAudioPacket(String streamId, AVPacket packet) {
		packetCount++;
		return packet;
	}

	@Override
	public void setVideoStreamInfo(String streamId, StreamParametersInfo videoStreamInfo) {
		logger.info("ScreenshotPluginPacketListener.setVideoStreamInfo() for streamId:{}", streamId);		
	}

	@Override
	public void setAudioStreamInfo(String streamId, StreamParametersInfo audioStreamInfo) {
		logger.info("ScreenshotPluginPacketListener.setAudioStreamInfo() for streamId:{}", streamId);		
	}

}
