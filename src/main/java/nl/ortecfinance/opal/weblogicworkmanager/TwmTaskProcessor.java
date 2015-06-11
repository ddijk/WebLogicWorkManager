package nl.ortecfinance.opal.weblogicworkmanager;

import commonj.work.Work;
import commonj.work.WorkException;
import commonj.work.WorkItem;
import commonj.work.WorkManager;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@Stateless
public class TwmTaskProcessor {

    WorkManager workManager;

    byte[] lock;

    static int taskCounter;

    Map<Integer, Integer> statusMap = new HashMap<>();

    @PostConstruct
    void lookupWm() {
        try {
            workManager = (WorkManager) new InitialContext().lookup("java:comp/env/wm/myWm");
            System.out.println("***************");
            printMap();
            System.out.println("****  Started ******");
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void doTask() {

        try {
            for (int i = 0; i < 10000; i++) {
                WorkItem workItem = workManager.schedule(new BatchSlice(i));

                final int status = workItem.getStatus();
                updateMap(status);
//                if (status != WorkEvent.WORK_ACCEPTED) {
//                    System.out.println("WorkItem " + workItem.getStatus());
//                }

                if (i % 1000 == 0) {
                    printMap();
                }

//                while (workItem.getStatus() != WorkEvent.WORK_COMPLETED) {
//                    // System.out.println("Nog niet klaar");
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException ex) {
//                        System.out.println("Sleep failed. " + ex);
//                    }
//                }
            }

            //  System.out.println("TWM work done");
        } catch (WorkException ex) {
            System.out.println("Failed to process BatchSlice " + ex);
        } catch (IllegalArgumentException ex) {
            System.out.println("Failed to process BatchSlice " + ex);
        }
    }

    private void printMap() {
        for (Entry<Integer, Integer> entry : statusMap.entrySet()) {
            System.out.println(printKey(entry.getKey()) + ": " + entry.getValue());
        }
    }

    private void updateMap(int status) {
        Integer currentCount = statusMap.get(status);
        if (currentCount != null) {
            statusMap.put(status, ++currentCount);
        } else {
            statusMap.put(status, 1);
        }

    }

    /*

     public static final int WORK_ACCEPTED = 1;
     public static final int WORK_REJECTED = 2;
     public static final int WORK_STARTED = 3;
     public static final int WORK_COMPLETED = 4;
     */
    private String printKey(Integer key) {
        switch (key) {
            case 1:
                return "WORK_ACCEPTED";
            case 2:
                return "WORK_REJECTED";
            case 3:
                return "WORK_STARTED";
            case 4:
                return "WORK_COMPLETED";
            default:
                return "onbekende status";
        }
    }
}

class BatchSlice implements Work {

    int id;

    public BatchSlice(int j) {
        id = j;
    }

    @Override
    public void release() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isDaemon() {
        return false;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(30000);
        } catch (InterruptedException ex) {
            System.out.println("Sleep failed");
        }
        System.out.println("TWM : Doing task " + id + " @" + new Date());
    }

}
