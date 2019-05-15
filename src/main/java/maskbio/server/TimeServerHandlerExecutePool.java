package maskbio.server;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @ClassName TimeServerHandlerExecutePool
 * @Description //TODO
 * @Date 2019/5/15 13:34
 * @Author jszhang@wisedu
 * @Version 1.0
 **/
public class TimeServerHandlerExecutePool {
    private ExecutorService executor;
    public TimeServerHandlerExecutePool(int maxPoolSize,int queueSize){
        executor = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),maxPoolSize,120L, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(queueSize));

    }
    public void execute(Runnable runnable){
        executor.execute(runnable);
    }
}
