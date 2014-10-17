package demo.humantalk.rxjava;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import rx.Observable;
import rx.schedulers.TestScheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SchedulersTest {

    @Test
    public void should_test_the_test_schedulers() {
        TestScheduler scheduler = new TestScheduler();
        final List<Long> result = new ArrayList<>();
        Observable.interval(1, TimeUnit.SECONDS, scheduler).take(5).subscribe(result::add);
        assertTrue(result.isEmpty());
        scheduler.advanceTimeBy(2, TimeUnit.SECONDS);
        assertEquals(2, result.size());
        scheduler.advanceTimeBy(10, TimeUnit.SECONDS);
        assertEquals(5, result.size());
    }

}