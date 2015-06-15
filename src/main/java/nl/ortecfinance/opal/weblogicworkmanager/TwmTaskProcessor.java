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
import org.apache.log4j.Logger;

public class TwmTaskProcessor {

    static WorkManager workManager;
    private static final Logger LOGGER = Logger.getLogger(TwmTaskProcessor.class);

    static Map<Integer, Integer> statusToCountMap = Collections.synchronizedMap(new HashMap<Integer, Integer>());
    static Map<Integer, List<Integer>> workItemToStatusHistoryMap = Collections.synchronizedMap(new TreeMap<Integer, List<Integer>>());
    static AtomicInteger index = new AtomicInteger();

    static int usersCounter;
    final int USER_COUNT = 3;
    final int LOOP_COUNT = 10;

    TwmTaskProcessor() {
        try {
            if (workManager == null) {
                workManager = (WorkManager) new InitialContext().lookup("java:comp/env/wm/myWm");
                LOGGER.info("new WM looked up");
            } else {
                LOGGER.info("reusing wm");
            }
            LOGGER.info("****  Started ******");
        } catch (NamingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void doTask() throws InterruptedException {

        int block = 0;

        List<WorkItem> workItems = new ArrayList<>();
        try {
            for (int i = 0; i < LOOP_COUNT; i++) {
                final int id = index.incrementAndGet();
                WorkItem workItem = workManager.schedule(new BatchSlice(id), new MyWorkListener(id, workItemToStatusHistoryMap));

                workItems.add(workItem);

                if (i % LOOP_COUNT - 1 == 0) {
                    printMap(block++);
                }

            }
            printMap(block);
            //  LOGGER.info("TWM work done");
            LOGGER.info("wait for all");
            workManager.waitForAll(workItems, Long.MAX_VALUE);
            LOGGER.info("wait done");
            //     Thread.sleep(2000);
            //  printStatusHistoryMap();
            printStatusHistoryForWorkItems(workItems);
            LOGGER.info("Index is " + index.get());

        } catch (WorkException | IllegalArgumentException ex) {
            LOGGER.info("Failed to process BatchSlice " + ex);
        } catch (RuntimeException ex) {
            LOGGER.info("Hmmm." + ex, ex);
        }
    }

    private void printMap(int n) {
        LOGGER.info("------ top " + n + " -------");
        for (Entry<Integer, Integer> entry : statusToCountMap.entrySet()) {
            LOGGER.info(printKey(entry.getKey()) + ": " + entry.getValue());
        }
        LOGGER.info("------ bottom -------");
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
        usersCounter++;
        if (usersCounter < USER_COUNT) {
            return;
        }
        for (Entry<Integer, List<Integer>> entrySet : workItemToStatusHistoryMap.entrySet()) {
            Integer key = entrySet.getKey();
            List<Integer> value = entrySet.getValue();

            LOGGER.info("workItem " + key + " statusHistory=" + value);

        }
    }

    private void printStatusHistoryForWorkItems(List<WorkItem> workItems) {
        for (WorkItem wi : workItems) {
            BatchSlice bs = null;

            try {
                bs = (BatchSlice) wi.getResult();
                LOGGER.info("id=" + bs.id + ", status=" + wi.getStatus());
                // LOGGER.info("status " + wi.getStatus());
            } catch (WorkException ex) {
                LOGGER.error("Querying of workitem failed. ", ex);
            }

        }

    }
}

class BatchSlice implements Work {

    int id;
    private static final Logger LOGGER = Logger.getLogger(BatchSlice.class);

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

            LOGGER.info("Starting " + id);
            Thread.sleep(10000000);
        } catch (InterruptedException ex) {
            LOGGER.info("Sleep failed");
        }
        //  LOGGER.info("TWM : Doing task " + id + " @" + new Date());
    }

}

class MyWorkListener implements WorkListener {

    int id;
    Map<Integer, List<Integer>> workItemToStatusHistoryMap;
    private static final Logger LOGGER = Logger.getLogger(MyWorkListener.class);

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
        //      LOGGER.info("workAccepted for " + id +", history="+workItemToStatusHistoryMap.get(id));
        LOGGER.info("workAccepted for " + id);
    }

    @Override
    public void workRejected(WorkEvent we) {
        workItemToStatusHistoryMap.get(id).add(WorkEvent.WORK_REJECTED);
        //       LOGGER.info("workRejected for " + id+", history="+workItemToStatusHistoryMap.get(id));
        LOGGER.info("workRejected for " + id);
    }

    @Override
    public void workStarted(WorkEvent we) {
        workItemToStatusHistoryMap.get(id).add(WorkEvent.WORK_STARTED);
        //   LOGGER.info("workStarted for " + id+", history="+workItemToStatusHistoryMap.get(id));
        LOGGER.info("workStarted for " + id);
    }

    @Override
    public void workCompleted(WorkEvent we) {
        workItemToStatusHistoryMap.get(id).add(WorkEvent.WORK_COMPLETED);
        //   LOGGER.info("workCompleted for " + id+", history="+workItemToStatusHistoryMap.get(id));
        LOGGER.info("workCompleted for " + id);
    }

}
