package io.antmedia.app;

import org.bytedeco.ffmpeg.avutil.AVFrame;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.PriorityQueue;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.antmedia.plugin.api.IFrameListener;
import io.antmedia.plugin.api.StreamParametersInfo;

import javax.imageio.ImageIO;

import static io.antmedia.app.Utils.convertAVFrameToByteArray;

public class ScreenshotPluginFrameListener implements IFrameListener{
	
	protected static Logger logger = LoggerFactory.getLogger(ScreenshotPluginFrameListener.class);

	private final Queue<String> queue = new PriorityQueue<>();

	@Override
	public AVFrame onAudioFrame(String streamId, AVFrame audioFrame) {
		return audioFrame;
	}

	@Override
	public AVFrame onVideoFrame(String streamId, AVFrame videoFrame) {

		if (queue.contains(streamId)) {

            try {
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String outputFilePath = "output" + streamId + timestamp.getTime() + ".png";

                saveFrameAsPNG(videoFrame, outputFilePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

			queue.remove(streamId);
		}

		return videoFrame;
	}

	@Override
	public void writeTrailer(String streamId) {
		logger.info("ScreenshotPluginFrameListener.writeTrailer() for streamId:{}", streamId);
	}

	@Override
	public void setVideoStreamInfo(String streamId, StreamParametersInfo videoStreamInfo) {
		logger.info("ScreenshotPluginFrameListener.setVideoStreamInfo() for streamId:{}", streamId);		
	}

	@Override
	public void setAudioStreamInfo(String streamId, StreamParametersInfo audioStreamInfo) {
		logger.info("ScreenshotPluginFrameListener.setAudioStreamInfo() for streamId:{}", streamId);		
	}

	@Override
	public void start() {
		logger.info("ScreenshotPluginFrameListener.start()");		
	}

	public boolean addIntoScreenshotQueue(String streamId) {
		if(!queue.contains(streamId)) {
			queue.add(streamId);
			return true;
		} else {
			return false;
		}
	}

    private static void saveFrameAsPNG(AVFrame frame, String outputFilePath) throws IOException {
        AVFrame rgbFrame = Utils.toRGB(frame);
        byte[] frameData = convertAVFrameToByteArray(rgbFrame);

        DataBuffer buffer = new DataBufferByte(frameData, frameData.length);

        //3 bytes per pixel: red, green, blue
        WritableRaster raster = Raster.createInterleavedRaster(buffer, rgbFrame.width(), rgbFrame.height(), 3 * rgbFrame.width(), 3, new int[] {0, 1, 2}, (Point)null);
        ColorModel cm = new ComponentColorModel(ColorModel.getRGBdefault().getColorSpace(), false, true, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
        BufferedImage image = new BufferedImage(cm, raster, true, null);

        ImageIO.write(image, "png", new File(outputFilePath));

    }

}
