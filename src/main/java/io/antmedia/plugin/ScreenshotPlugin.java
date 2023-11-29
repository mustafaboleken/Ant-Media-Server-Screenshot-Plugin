package io.antmedia.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import io.antmedia.AntMediaApplicationAdapter;
import io.antmedia.app.ScreenshotPluginFrameListener;
import io.antmedia.app.ScreenshotPluginPacketListener;
import io.antmedia.muxer.IAntMediaStreamHandler;
import io.antmedia.muxer.MuxAdaptor;
import io.antmedia.plugin.api.IFrameListener;
import io.antmedia.plugin.api.IStreamListener;
import io.vertx.core.Vertx;

@Component(value="plugin.screenshotplugin")
public class ScreenshotPlugin implements ApplicationContextAware, IStreamListener{

	public static final String BEAN_NAME = "web.handler";
	protected static Logger logger = LoggerFactory.getLogger(ScreenshotPlugin.class);
	
	private Vertx vertx;
	private ScreenshotPluginFrameListener frameListener = new ScreenshotPluginFrameListener();
	private ScreenshotPluginPacketListener packetListener = new ScreenshotPluginPacketListener();
	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		vertx = (Vertx) applicationContext.getBean("vertxCore");
		
		IAntMediaStreamHandler app = getApplication();
		app.addStreamListener(this);
	}
		
	public MuxAdaptor getMuxAdaptor(String streamId) 
	{
		IAntMediaStreamHandler application = getApplication();
		MuxAdaptor selectedMuxAdaptor = null;

		if(application != null)
		{
			selectedMuxAdaptor = application.getMuxAdaptor(streamId);
		}

		return selectedMuxAdaptor;
	}
	
	public void register(String streamId) {
		IAntMediaStreamHandler app = getApplication();
		app.addFrameListener(streamId, frameListener);		
		app.addPacketListener(streamId, packetListener);
	}
	
	public IAntMediaStreamHandler getApplication() {
		return (IAntMediaStreamHandler) applicationContext.getBean(AntMediaApplicationAdapter.BEAN_NAME);
	}
	
	public IFrameListener createCustomBroadcast(String streamId) {
		IAntMediaStreamHandler app = getApplication();
		return app.createCustomBroadcast(streamId);
	}

	public boolean addIntoScreenshotQueue(String streamId) {
		return frameListener.addIntoScreenshotQueue(streamId);
	}

	@Override
	public void streamStarted(String s) {

	}

	@Override
	public void streamFinished(String s) {

	}

	@Override
	public void joinedTheRoom(String s, String s1) {

	}

	@Override
	public void leftTheRoom(String s, String s1) {

	}
}
