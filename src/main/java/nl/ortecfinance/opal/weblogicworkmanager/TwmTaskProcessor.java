package nl.ortecfinance.opal.weblogicworkmanager;

import commonj.work.Work;
import commonj.work.WorkEvent;
import commonj.work.WorkException;
import commonj.work.WorkItem;
import commonj.work.WorkListener;
import commonj.work.WorkManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class TwmTaskProcessor {

    static WorkManager workManager;

    static Map<Integer, Integer> statusToCountMap = new HashMap<>();
    static Map<Integer, List<Integer>> workItemToStatusHistoryMap = new TreeMap<>();
    static AtomicInteger index = new AtomicInteger();

    TwmTaskProcessor() {
        try {
            if (workManager == null) {
                workManager = (WorkManager) new InitialContext().lookup("java:comp/env/wm/myWm");
                System.out.println("new WM looked up");
            } else {
                System.out.println("reusing wm");
            }
            System.out.println("****  Started ******");
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void doTask() throws InterruptedException {

        int block = 0;

        List<WorkItem> workItems = new ArrayList<>();
        try {
            for (int i = 0; i < 200; i++) {
                final int id = index.incrementAndGet();
                WorkItem workItem = workManager.schedule(new BatchSlice(id), new MyWorkListener(id, workItemToStatusHistoryMap));

                workItems.add(workItem);

                if (i % 1000 == 0) {
                    printMap(block++);
                }

            }
            printMap(block);
            //  System.out.println("TWM work done");
            System.out.println("wait for all");
            workManager.waitForAll(workItems, Long.MAX_VALUE);
            System.out.println("wait done");
            //     Thread.sleep(2000);
            printStatusHistoryMap();

        } catch (WorkException | IllegalArgumentException ex) {
            System.out.println("Failed to process BatchSlice " + ex);
        }
    }

    private void printMap(int n) {
        System.out.println("------ top " + n + " -------");
        for (Entry<Integer, Integer> entry : statusToCountMap.entrySet()) {
            System.out.println(printKey(entry.getKey()) + ": " + entry.getValue());
        }
        System.out.println("------ bottom -------");
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

    private void printStatusHistoryMap() {

        for (Entry<Integer, List<Integer>> entrySet : workItemToStatusHistoryMap.entrySet()) {
            Integer key = entrySet.getKey();
            List<Integer> value = entrySet.getValue();

            System.out.println("workItem " + key + " statusHistory=" + value);

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
//            if (id == 0) {
//                Thread.sleep(5000);
//            } else {
//                Thread.sleep(3000);
//            }
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            System.out.println("Sleep failed");
        }
        //  System.out.println("TWM : Doing task " + id + " @" + new Date());
    }

}

class MyWorkListener implements WorkListener {

    int id;
    Map<Integer, List<Integer>> workItemToStatusHistoryMap;

    public MyWorkListener(int id) {
        this.id = id;
    }

    MyWorkListener(int i, Map<Integer, List<Integer>> workItemToStatusHistoryMap) {
        this.id = i;
        this.workItemToStatusHistoryMap = workItemToStatusHistoryMap;
        List<Integer> myStatuses = Collections.synchronizedList(new ArrayList<Integer>());
        myStatuses.add(-1);
        workItemToStatusHistoryMap.put(i, myStatuses);
    }

    @Override
    public void workAccepted(WorkEvent we) {
        workItemToStatusHistoryMap.get(id).add(WorkEvent.WORK_ACCEPTED);
        //      System.out.println("workAccepted for " + id +", history="+workItemToStatusHistoryMap.get(id));
        //     System.out.println("workAccepted for " + id);
    }

    @Override
    public void workRejected(WorkEvent we) {
        workItemToStatusHistoryMap.get(id).add(WorkEvent.WORK_REJECTED);
        //       System.out.println("workRejected for " + id+", history="+workItemToStatusHistoryMap.get(id));
        //     System.out.println("workRejected for " + id);
    }

    @Override
    public void workStarted(WorkEvent we) {
        workItemToStatusHistoryMap.get(id).add(WorkEvent.WORK_STARTED);
        //   System.out.println("workStarted for " + id+", history="+workItemToStatusHistoryMap.get(id));
        //      System.out.println("workStarted for " + id);
    }

    @Override
    public void workCompleted(WorkEvent we) {
        workItemToStatusHistoryMap.get(id).add(WorkEvent.WORK_COMPLETED);
        //   System.out.println("workCompleted for " + id+", history="+workItemToStatusHistoryMap.get(id));
        //  System.out.println("workCompleted for " + id);
    }

}
