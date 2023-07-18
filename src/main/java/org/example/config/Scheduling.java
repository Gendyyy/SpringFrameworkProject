package org.example.config;

import org.example.config.annotations.Scheduled;
import org.example.config.annotations.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class Scheduling {

    private ExecutorService executor = Executors.newCachedThreadPool();

    public void MyScheduler(ArrayList<Object> beansIsntances) {

        for (Object bean : beansIsntances) {
            for (Method method : bean.getClass().getMethods()) {
                if (method.isAnnotationPresent(Scheduled.class)) {
                    Scheduled myScheduled = method.getAnnotation(Scheduled.class);
                    long delay = myScheduled.fixedDelay();
                    Runnable task = () -> {
                        while (!Thread.currentThread().isInterrupted()) {
                            try {
                                method.invoke(bean);
                                Thread.sleep(delay);
                            } catch (InterruptedException | IllegalAccessException | InvocationTargetException e) {
                                Thread.currentThread().interrupt();
                                e.printStackTrace();
                            }
                        }
                    };
                    executor.submit(task);
                }
            }
        }
    }

}
