package nl.ortecfinance.opal.weblogicworkmanager;

import java.util.Date;
import javax.ejb.Stateless;
import org.apache.log4j.Logger;

@Stateless
public class TaskProcessor {

    private static final Logger LOGGER = Logger.getLogger(TaskProcessor.class);
    static boolean deadLock;

    static final byte[] lock = new byte[0];
    static final byte[] logLock = new byte[0];

    static volatile int counter;
    private static final int INITIAL_VALUE = -10;
    static volatile int counterWhenDeadlocked = INITIAL_VALUE;

    boolean deadlockInstance;

    public void doTask() {
        counter++;
        try {
            synchronized (lock) {
                if (deadLock || counter <= counterWhenDeadlocked + 2) {
                    deadLock = false;
                    LOGGER.info("entering deadlock for counter=" + counter);
                    if (counterWhenDeadlocked == INITIAL_VALUE) {
                        LOGGER.info("Initiating  deadlock");
                        counterWhenDeadlocked = counter;
                    }

                    deadlockInstance = true;
                }
            }

            if (deadlockInstance) {
                synchronized (logLock) {
                    LOGGER.info("Locked on counter=" + counter);

                    deadlockInstance = true;
                    logLock.wait();
                }
            }

            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            LOGGER.info("Sleep failed");
        }
        LOGGER.info("Doing task @" + new Date() + ", counter=" + counter);
    }

}
