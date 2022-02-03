package it.uniroma2.dicii.isw2.jcs.paramTests;

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.CacheEventQueue;
import org.apache.jcs.engine.behavior.ICacheListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class EventQueueConcurrentLoadRunPutTest {
    private  CacheEventQueue queue;
    private  ICacheListener listen;

    private  int maxFailure;
    private  int waitBeforeRetry;
    private  int idleTime;
    private int end, expectedPutCount, putCount;

    @Before
    public void configureEnvironment() {
        maxFailure = 3;
        waitBeforeRetry = 100;
        // very small idle time
        idleTime = 2;
        listen = mock(ICacheListener.class);
        queue = new CacheEventQueue( listen, 1L, "testCache1", maxFailure, waitBeforeRetry );
        queue.setWaitToDieMillis( idleTime );
    }



    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {200, 200}, {1200, 1200}, {5200, 5200}
        });
    }

    public EventQueueConcurrentLoadRunPutTest(int end, int expectedPutCount) {
        this.end = end;
        this.expectedPutCount = expectedPutCount;
        this.putCount = 0;

    }

    public void runPutTest(int end, int expectedPutCount)
            throws Exception {

        for (int i = 0; i <= end; i++) {
            CacheElement elem = new CacheElement("testCache1", i + ":key", i + "data");
            this.queue.addPutEvent(elem);
        }

        while (!this.queue.isEmpty()) {
            synchronized (this) {
                System.out.println("queue is still busy, waiting 250 millis");
                this.wait(250);
            }
        }
        System.out.println("queue is empty, comparing putCount");

        // this becomes less accurate with each test. It should never fail. If
        // it does things are very off.
        verify(this.listen, atLeast(expectedPutCount)).handlePut(anyObject());

        for (int i = 0; i <= end; i++) {
            this.queue.addRemoveEvent(i + ":key");
        }
    }

    @Test
    public void runPutTest() throws Exception {
        runPutTest(end, expectedPutCount);
    }

}
