package cn.edu.ruc.iir.rainbow.daemon;

import java.util.concurrent.TimeUnit;

public class WorkloadServer implements Server
{
    private boolean shutdown = true;
    @Override
    public boolean isRunning()
    {
        return Thread.currentThread().isAlive();
    }

    @Override
    public void shutdown()
    {
        this.shutdown = true;
    }

    @Override
    public void run()
    {
        this.shutdown = false;
        while (shutdown == false)
        {
            System.out.println("workload is running...");
            try
            {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
