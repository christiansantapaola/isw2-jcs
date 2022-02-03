package it.uniroma2.dicii.isw2.jcs.paramTests;

import org.apache.jcs.engine.CacheElement;
import org.apache.jcs.engine.behavior.ICacheElement;
import org.apache.jcs.engine.behavior.ICompositeCacheAttributes;
import org.apache.jcs.engine.control.CompositeCache;
import org.apache.jcs.engine.control.CompositeCacheManager;
import org.apache.jcs.engine.memory.lru.LRUMemoryCache;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

@RunWith(Parameterized.class)
public class LRUMemoryCacheConcurrentUnitTest {

    private  CompositeCacheManager cacheMgr;
    private  CompositeCache cache;
    private  LRUMemoryCache lru;
    private  String region;
    private  int maxObject;
    private  int items;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {  200 },
                { 102 },
                { 100 },
                { 98 },
                { 50 },
                { 0 },
                { 400 }
        });
    }

    public LRUMemoryCacheConcurrentUnitTest(int items) {
        this.items = items;
    }

    @Before
    public void configure()  {
        cacheMgr = CompositeCacheManager.getUnconfiguredInstance();
        cacheMgr.configure( "/TestDiskCache.ccf" );
        region = "indexedRegion1";
        cache = cacheMgr.getCache( region );
        lru = new LRUMemoryCache();
        lru.initialize( cache );
        maxObject = lru.getCacheAttributes().getMaxObjects() - 2;
        // Add items to cache

        for ( int i = 0; i < items; i++ )
        {
            try {
                ICacheElement ice = new CacheElement(cache.getCacheName(), i + ":key", region + " data " + i);
                ice.setElementAttributes(cache.getElementAttributes());
                lru.update(ice);
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }
    }



    @Test
    public void runTestForRegion()
            throws Exception
    {
        System.err.printf("items %d, maxObject: %d\n", items, maxObject);
        if (items < maxObject) {
            // Test that the items are in cache
            for ( int i = 0; i < items; i++ )
            {
                try {
                    String value = (String) lru.get(i + ":key").getVal();
                    Assert.assertEquals(region + " data " + i, value);
                } catch (NullPointerException e) {
                    System.err.println(i);
                }
            }

        } else {
            // Test that initial items have been purged
            for ( int i = 0; i < items - maxObject ; i++ )
            {
                Assert.assertNull( lru.get( i + ":key" ) );
            }


            // Test that last items are in cache

            for ( int i = items - maxObject; i < items; i++ )
            {
                try {
                    String value = (String) lru.get(i + ":key").getVal();
                    Assert.assertEquals(region + " data " + i, value);
                } catch (NullPointerException e) {
                    System.err.println(i);
                }
            }


        }
        // Remove all the items

        for ( int i = 0; i < items; i++ )
        {
            lru.remove( i + ":key" );
        }

        // Verify removal

        for ( int i = 0; i < items; i++ )
        {
            Assert.assertNull( "Removed key should be null: " + i + ":key", lru.get( i + ":key" ) );
        }

    }


}
