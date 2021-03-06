package cn.edu.ruc.iir.rainbow.layout;

import cn.edu.ruc.iir.rainbow.common.exception.*;
import cn.edu.ruc.iir.rainbow.layout.algorithm.AlgorithmFactory;
import cn.edu.ruc.iir.rainbow.layout.algorithm.ExecutorContainer;
import cn.edu.ruc.iir.rainbow.layout.algorithm.impl.ord.FastScoaPixels;
import cn.edu.ruc.iir.rainbow.layout.builder.ColumnOrderBuilder;
import cn.edu.ruc.iir.rainbow.layout.builder.WorkloadBuilder;
import cn.edu.ruc.iir.rainbow.layout.domian.Column;
import cn.edu.ruc.iir.rainbow.layout.domian.Query;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestFastScoaPixels
{
    @Test
    public void test () throws IOException, ColumnNotFoundException, AlgoException, ClassNotFoundException, InterruptedException
    {
        List<Column> initColumnOrder = ColumnOrderBuilder.build(new File(TestScoa.class.getResource("/scoa_ordered_schema.txt").getFile()));
        List<Query> workload = WorkloadBuilder.build(new File(TestScoa.class.getResource("/workload.txt").getFile()), initColumnOrder);
        System.out.println(workload.size());
        //SeekCost seekCostFunction = new PowerSeekCost();
        //RealSeekCostBuilder.build(new File("layout/resources/seek_cost.txt"));

        FastScoaPixels scoaPixels = (FastScoaPixels) AlgorithmFactory.Instance().getAlgorithm("scoa.pixels", 2, new ArrayList<>(initColumnOrder), workload);

        try
        {
            ExecutorContainer container = new ExecutorContainer(scoaPixels, 1);
            System.out.println("origin seek cost: " + scoaPixels.getOriginSeekCost());
            System.out.println("Init seek cost: " + scoaPixels.getSchemaSeekCost());
            System.out.println("Init cost: " + scoaPixels.getSchemaCost());
            container.waitForCompletion(1, percentage -> {
                System.out.println(percentage);
            });
        } catch (NotMultiThreadedException e)
        {
            ExceptionHandler.Instance().log(ExceptionType.ERROR, "thread number is " + 1, e);
        }

        System.out.println("Final seek cost: " + scoaPixels.getOrderedSeekCost());
        System.out.println("start cached cost: " + scoaPixels.getStartCachedCost());
        System.out.println("ordered cached cost: " + scoaPixels.getOrderedCachedCost());
        System.out.println(scoaPixels.getCurrentWorkloadSeekCost());
        List<Column> realColumnletOrder = scoaPixels.getRealColumnletOrder();
        System.out.println("column order cached cost: " + scoaPixels.getColumnOrderCachedCost(realColumnletOrder));
        ColumnOrderBuilder.saveAsSchemaFile(new File(TestScoa.class.getResource("/").getFile() + "scoa_pixels_ordered_schema_1000s.txt"), realColumnletOrder);
        System.out.println("ordered schema file: " + TestScoa.class.getResource("/").getFile() + "scoa_pixels_ordered_schema_1000s.txt");

    }
}
