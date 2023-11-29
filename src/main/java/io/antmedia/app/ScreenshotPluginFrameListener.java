package io.antmedia.app;

import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVIOContext;
import org.bytedeco.ffmpeg.avformat.AVOutputFormat;
import org.bytedeco.ffmpeg.avformat.AVStream;
import org.bytedeco.ffmpeg.avutil.AVDictionary;
import org.bytedeco.ffmpeg.avutil.AVFrame;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avformat;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.Pointer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.antmedia.plugin.ScreenshotPlugin;
import io.antmedia.plugin.api.IFrameListener;
import io.antmedia.plugin.api.StreamParametersInfo;

public class ScreenshotPluginFrameListener implements IFrameListener{
	
	protected static Logger logger = LoggerFactory.getLogger(ScreenshotPluginFrameListener.class);

	private int audioFrameCount = 0;
	private int videoFrameCount = 0;

	private Queue<String> queue = new PriorityQueue<Obj> ();

	@Override
	public AVFrame onAudioFrame(String streamId, AVFrame audioFrame) {
		audioFrameCount ++;
		return audioFrame;
	}

	@Override
	public AVFrame onVideoFrame(String streamId, AVFrame videoFrame) {
		videoFrameCount ++;

		if (queue.contains(streamId)) {
			AVFrame rgbFrame = convertToRGB(inputFrame);

			saveFrameAsPNG(rgbFrame, outputFilePath);

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

	public String getStats() {
		return "audio frames:"+audioFrameCount+"\t"+"video frames:"+videoFrameCount;
	}

	public void addIntoScreenshotQueue(String streamId) {
		queue.add(streamId);
	}

	private static AVFrame convertToRGB(AVFrame inputFrame) {
        AVFrame rgbFrame = avutil.av_frame_alloc();

        if (rgbFrame == null) {
            throw new RuntimeException("Failed to allocate RGB frame");
        }

        // Allocate buffer for RGB data
        int bufferSize = avutil.av_image_get_buffer_size(avutil.AV_PIX_FMT_RGB24, inputFrame.width(), inputFrame.height(), 1);
        BytePointer buffer = new BytePointer(avutil.av_malloc(bufferSize));

        // Assign buffer to the RGB frame
        avutil.av_image_fill_arrays(rgbFrame.data(), rgbFrame.linesize(), buffer, avutil.AV_PIX_FMT_RGB24,
                inputFrame.width(), inputFrame.height(), 1);

        // Set properties for the RGB frame
        rgbFrame.width(inputFrame.width());
        rgbFrame.height(inputFrame.height());
        rgbFrame.format(avutil.AV_PIX_FMT_RGB24);

        // Create SwsContext for color conversion
        SwsContext swsContext = swscale.sws_getContext(inputFrame.width(), inputFrame.height(), inputFrame.format(),
                inputFrame.width(), inputFrame.height(), avutil.AV_PIX_FMT_RGB24, swscale.SWS_BICUBIC, null, null, (DoublePointer) null);

        // Convert the input frame to RGB
        swscale.sws_scale(swsContext, inputFrame.data(), inputFrame.linesize(), 0, inputFrame.height(),
                rgbFrame.data(), rgbFrame.linesize());

        // Free the SwsContext
        swscale.sws_freeContext(swsContext);

        return rgbFrame;
    }

    private static void saveFrameAsPNG(AVFrame frame, String outputFilePath) {
        AVOutputFormat format = avformat.av_guess_format("image2", outputFilePath, null);
        if (format == null) {
            throw new RuntimeException("Could not determine image format");
        }

        AVFormatContext formatContext = avformat.avformat_alloc_context();
        if (formatContext == null) {
            throw new RuntimeException("Could not allocate format context");
        }

        formatContext.oformat(format);

        AVStream stream = avformat.avformat_new_stream(formatContext, null);
        if (stream == null) {
            throw new RuntimeException("Could not allocate stream");
        }

        AVCodecContext codecContext = stream.codec();

        codecContext.codec_id(format.video_codec());
        codecContext.codec_type(avutil.AVMEDIA_TYPE_VIDEO);

        codecContext.width(frame.width());
        codecContext.height(frame.height());
        codecContext.pix_fmt(avutil.AV_PIX_FMT_RGB24);

        if (avformat.avio_open(formatContext.pb(), outputFilePath, AVIOContext.AVIO_FLAG_WRITE) < 0) {
            throw new RuntimeException("Could not open output file");
        }

        if (avformat.avformat_write_header(formatContext, new AVDictionary(null)) < 0) {
            throw new RuntimeException("Error writing header");
        }

        AVPacket packet = new AVPacket();
        avcodec.av_init_packet(packet);

        int[] gotPacket = {0};
        if (avcodec.avcodec_encode_video2(codecContext, packet, frame, gotPacket) < 0) {
            throw new RuntimeException("Error encoding video");
        }

        if (gotPacket[0] != 0) {
            if (avformat.av_write_frame(formatContext, packet) < 0) {
                throw new RuntimeException("Error writing frame");
            }
        }

        if (avformat.av_write_trailer(formatContext) < 0) {
            throw new RuntimeException("Error writing trailer");
        }

        avformat.avcodec_close(codecContext);
        avformat.avio_close(formatContext.pb());

        avformat.avformat_free_context(formatContext);
    }

}
