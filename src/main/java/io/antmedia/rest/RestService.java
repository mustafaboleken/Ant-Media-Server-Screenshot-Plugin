package io.antmedia.rest;

import jakarta.servlet.ServletContext;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.google.gson.Gson;

import io.antmedia.plugin.ScreenshotPlugin;

@Component
@Path("/screenshot-plugin")
public class RestService {

	@Context
	protected ServletContext servletContext;
	Gson gson = new Gson();
	
	@POST
	@Path("/register/{streamId}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register(@PathParam("streamId") String streamId) {
		ScreenshotPlugin app = getScreenshotPluginApp();
		app.register(streamId);

		return Response.status(Status.OK).entity("").build();
	}
	
	@GET
	@Path("/take-screenshot")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public String getStats() {
		ScreenshotPlugin app = getScreenshotPluginApp();
		return app.getStats();
	}
	
	private ScreenshotPlugin getScreenshotPluginApp() {
		ApplicationContext appCtx = (ApplicationContext) servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		return (ScreenshotPlugin) appCtx.getBean("plugin.screenshotplugin");
	}
}
