package passwordProtector.dataProcessing;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** Created by shams@StackOverFlow.com*/
public class EventExecutorService implements Executor {

    private final Executor executor;
    // the field which keeps track of the latest available event to process
    private final AtomicReference<Runnable> latestEventReference = new AtomicReference<>();
    private final AtomicInteger activeTaskCount = new AtomicInteger(0);

    public EventExecutorService(final Executor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(final Runnable eventTask) {
        // update the latest event
        latestEventReference.set(eventTask);
        // read count _after_ updating event
        final int activeTasks = activeTaskCount.get();

        if (activeTasks == 0) {
            // there is definitely no other task to process this event, create a new task
            final Runnable customTask = () -> {
                // decrement the count for available tasks _before_ reading event
                activeTaskCount.decrementAndGet();
                // find the latest available event to process
                final Runnable currentTask = latestEventReference.getAndSet(null);
                if (currentTask != null) {
                    // if such an event exists, process it
                    currentTask.run();
                } else {
                    // somebody stole away the latest event. Do nothing.
                }
            };
            // increment tasks count _before_ submitting task
            activeTaskCount.incrementAndGet();
            // submit the new task to the queue for processing
            executor.execute(customTask);
        }
    }
}