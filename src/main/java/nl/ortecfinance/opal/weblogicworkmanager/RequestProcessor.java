package nl.ortecfinance.opal.weblogicworkmanager;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@RequestScoped
@Path("wm")
public class RequestProcessor {

    TaskProcessor taskProcessor;
    TwmTaskProcessor twmTaskProcessor;

    @PostConstruct
    void init() {

        twmTaskProcessor = new TwmTaskProcessor();
        taskProcessor = new TaskProcessor();
        System.out.println("TwmTaskProcessors created");
    }

    @GET
    @Path("deadlock")
    public Response goIntoDeadlock() {
        TaskProcessor.deadLock = true;
        return Response.ok("deadlockig now").build();
    }

    @GET
    @Path("no_twm")
    public Response submitTask() {

        taskProcessor.doTask();
        return Response.ok().build();
    }

    @GET
    @Path("twm")
    public Response submitTwmTask() {

        try {

            twmTaskProcessor.doTask();
        } catch (InterruptedException ex) {
            return Response.serverError().entity(ex).build();
        }
        return Response.ok().build();
    }

    @GET
    @Path("status")
    public Response status() {

        return Response.ok(TwmTaskProcessor.statusToCountMap).build();
    }

    @GET
    @Path("statusHist")
    public Response statusHist(@QueryParam("id") Integer id) {

        return Response.ok("" + TwmTaskProcessor.workItemToStatusHistoryMap.get(id)).build();
    }
}
