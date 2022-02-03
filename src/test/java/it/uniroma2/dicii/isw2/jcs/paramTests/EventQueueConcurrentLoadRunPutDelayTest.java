package it.uniroma2.dicii.isw2.jcs.paramTests;

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.CacheEventQueue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.apache.jcs.engine.behavior.ICacheListener;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.*;



@RunWith(Parameterized.class)
public class EventQueueConcurrentLoadRunPutDelayTest {

    private CacheEventQueue queue;
    private ICacheListener listen;

    private int maxFailure;
    private int waitBeforeRetry;
    private int idleTime;
    private int end, expectedPutCount, putCount;
    private int numberOfThreads;



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
            return Arrays.asList(new Object[][] {
                    {200, 200}, {1200, 1200}, {5200, 5200}
            });
        }

        public EventQueueConcurrentLoadRunPutDelayTest(int end, int expectedPutCount) {
            this.end = end;
            this.expectedPutCount = expectedPutCount;
        }


        public void runPutDelayTest(int end)
                throws Exception
        {
            while ( !queue.isEmpty() )
            {
                synchronized ( this )
                {
                    System.out.println( "queue is busy, waiting 250 millis to begin" );
                    this.wait( 250 );
                }
            }
            System.out.println( "queue is empty, begin" );

            // get it going
            CacheElement elem = new CacheElement( "testCache1", "a:key", "adata" );
            queue.addPutEvent( elem );

            for ( int i = 0; i <= end; i++ )
            {
                synchronized ( this )
                {
                    if ( i % 2 == 0 )
                    {
                        this.wait( idleTime );
                    }
                    else
                    {
                        this.wait( idleTime / 2 );
                    }
                }
                CacheElement elem2 = new CacheElement( "testCache1", i + ":key", i + "data" );
                queue.addPutEvent( elem2 );
            }

            while ( !queue.isEmpty() )
            {
                synchronized ( this )
                {
                    System.out.println( "queue is still busy, waiting 250 millis" );
                    this.wait( 250 );
                }
            }
            System.out.println( "queue is empty, comparing putCount" );

            Thread.sleep( 1000 );

            // this becomes less accurate with each test. It should never fail. If
            // it does things are very off.
//            Assert.assertTrue( "The put count [" + putCount + "] is below the expected minimum threshold ["
//                    + expectedPutCount + "]", putCount >= ( expectedPutCount - 1 ) );
        }

        @Test
        public void runPutDelayTest() throws Exception {
            runPutDelayTest(end);
            verify(listen, atLeast(expectedPutCount)).handlePut(anyObject());
        }
    }
