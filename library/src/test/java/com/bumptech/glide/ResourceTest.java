package com.bumptech.glide;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class ResourceTest {
    private MockResource resource;

    @Before
    public void setUp() {
        resource = new MockResource();
    }

    @Test
    public void testCanAcquireAndRelease() {
        resource.acquire(1);
        resource.release();

        assertEquals(1, resource.recycled);
    }

    @Test
    public void testCanAcquireMultipleTimesAndRelease() {
        resource.acquire(2);
        resource.release();
        resource.release();

        assertEquals(1, resource.recycled);
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsIfRecycledTwice() {
        resource.recycle();
        resource.recycle();
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsIfReleasedBeforeAcquired() {
        resource.release();

        assertEquals(1, resource.recycled);
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsIfRecycledWhileAcquired() {
        resource.acquire(1);
        resource.recycle();
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsIfAcquiredAfterRecycled() {
        resource.recycle();
        resource.acquire(1);
    }

    @Test
    public void testThrowsIfAcquiredOnBackgroundThread() throws InterruptedException {
        Thread otherThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    resource.acquire(1);
                } catch (IllegalThreadStateException e) {
                    return;
                }
                Assert.fail("Failed to receive expected IllegalThreadStateException");
            }
        });
        otherThread.start();
        otherThread.join();
    }

    @Test
    public void testThrowsIfReleasedOnBackgroundThread() throws InterruptedException {
        resource.acquire(1);
        Thread otherThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                     resource.release();
                } catch (IllegalThreadStateException e) {
                    return;
                }
                Assert.fail("Failed to receive expected IllegalThreadStateException");
            }
        });
        otherThread.start();
        otherThread.join();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsIfAcquiredWithTimesEqualToZero() {
        resource.acquire(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsIfAcquiredWithTimesLessThanToZero() {
        resource.acquire(-1);
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowsIfReleasedMoreThanAcquired() {
        resource.acquire(1);
        resource.release();
        resource.release();
    }

    private static class MockResource extends Resource<Object> {
        int recycled = 0;
        @Override
        public Object get() {
            return null;
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        protected void recycleInternal() {
            recycled++;
        }
    }

}