package io.antmedia.app;

import org.bytedeco.ffmpeg.avutil.AVFrame;

import java.nio.file.Paths;
import java.nio.file.Files;
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
				String savedFolder = "./screenshots";
				Files.createDirectories(Paths.get(savedFolder));
				Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                String outputFilePath = savedFolder + "/" + "output" + streamId + timestamp.getTime() + ".png";

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

		BufferedImage image = new BufferedImage(rgbFrame.width(), rgbFrame.height(), BufferedImage.TYPE_INT_ARGB);

		// Iterate over array and set image pixel-by-pixel
		for (int i = 0; i < frameData.length; i += 4) {
			int row = (i / 4) / rgbFrame.width();
			int column = (i / 4) % rgbFrame.width();

			int red = frameData[i];
			int green = frameData[i + 1];
			int blue = frameData[i + 2];
			int alpha = frameData[i + 3];

			// rgb value is 8 + 8 + 8 + 8 bits, each for r/g/b/a number
			int rgb = (alpha << 24) | (red << 16) | (green << 8) | blue;

			image.setRGB(column, row, rgb);
		}

		ImageIO.write(image, "png", new File(outputFilePath));

    }

}
