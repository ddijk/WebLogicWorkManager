package nl.ortecfinance.opal.weblogicworkmanager;

import java.util.Date;
import javax.ejb.Stateless;

@Stateless
public class TaskProcessor {

    public void doTask() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ex) {
            System.out.println("Sleep failed");
        }
        System.out.println("Doing task @" + new Date());
    }

}
