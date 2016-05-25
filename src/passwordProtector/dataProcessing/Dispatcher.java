package passwordProtector.dataProcessing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Administrator on 19.05.2016.
 */
public class Dispatcher {
    private static Dispatcher instance;

    private final List<Callable<String>> callables = new ArrayList<Callable<String>>(2);
    private  ExecutorService threadPool = Executors.newFixedThreadPool(3);
    private final EventExecutorService eventExService = new EventExecutorService(threadPool);

    public static Dispatcher getInstance() {
        if (instance == null) {
            instance = new Dispatcher();
        }
        return instance;
    }

    public String[] perform(Callable... tasks){
        for(Callable<String> task: tasks) {
            if (task != null) {
                callables.add(task);
            }
        }
        String[] processed = new String[2];

        List<Future<String>> futures = null;
        try {
            futures = threadPool.invokeAll(callables);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (futures != null) {
            try {
                processed[0] = futures.get(0).get(); //password
                if (futures.size() > 1)
                    processed[1] = futures.get(1).get(); //data
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        callables.clear();
        return processed;
    }




    public List<Callable<String>> getCallables() {
        return callables;
    }

    public ExecutorService getExecutorService() {
        return threadPool;
    }

    public EventExecutorService getEventExService() {
        return eventExService;
    }

    public void restartThreadPool(){
        threadPool.shutdownNow();
        threadPool = Executors.newFixedThreadPool(3);
    }
}
