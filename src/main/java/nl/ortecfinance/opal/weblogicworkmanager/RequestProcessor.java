package nl.ortecfinance.opal.weblogicworkmanager;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@RequestScoped
@Path("wm")
public class RequestProcessor {

    @Inject
    TaskProcessor taskProcessor;

    //  @Inject
    TwmTaskProcessor twmTaskProcessor;

    @GET
    @Path("no_twm")
    public Response submitTask() {
        taskProcessor.doTask();
        return Response.ok().build();
    }

    @GET
    @Path("twm")
    public Response submitTwmTask() {

        twmTaskProcessor = new TwmTaskProcessor();
        twmTaskProcessor.doTask();
        return Response.ok().build();
    }
}
