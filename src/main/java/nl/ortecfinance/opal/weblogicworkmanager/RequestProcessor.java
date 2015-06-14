package nl.ortecfinance.opal.weblogicworkmanager;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@RequestScoped
@Path("wm")
public class RequestProcessor {
    
    @PostConstruct
    void init() {
        
        twmTaskProcessor = new TwmTaskProcessor();
        System.out.println("TwmTaskProcessor created");
    }
    
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
        
        return Response.ok(twmTaskProcessor.statusToCountMap).build();
    }
    
    @GET
    @Path("statusHist")
    public Response statusHist(@QueryParam("id") Integer id) {
        
        return Response.ok(""+twmTaskProcessor.workItemToStatusHistoryMap.get(id)).build();
    }
}
