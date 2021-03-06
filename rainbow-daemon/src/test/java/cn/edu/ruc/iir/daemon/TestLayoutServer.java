package cn.edu.ruc.iir.daemon;

import cn.edu.ruc.iir.pixels.common.metadata.domain.Column;
import cn.edu.ruc.iir.pixels.common.metadata.domain.Layout;
import cn.edu.ruc.iir.pixels.common.metadata.domain.Schema;
import cn.edu.ruc.iir.pixels.common.metadata.domain.Table;
import cn.edu.ruc.iir.pixels.common.utils.ConfigFactory;
import cn.edu.ruc.iir.pixels.daemon.metadata.dao.ColumnDao;
import cn.edu.ruc.iir.pixels.daemon.metadata.dao.LayoutDao;
import cn.edu.ruc.iir.pixels.daemon.metadata.dao.SchemaDao;
import cn.edu.ruc.iir.pixels.daemon.metadata.dao.TableDao;
import cn.edu.ruc.iir.rainbow.common.exception.MetadataException;
import cn.edu.ruc.iir.rainbow.common.metadata.PixelsMetadataStat;
import cn.edu.ruc.iir.rainbow.daemon.layout.LayoutServer;
import cn.edu.ruc.iir.rainbow.daemon.workload.WorkloadQueue;
import cn.edu.ruc.iir.rainbow.workload.cache.AccessPattern;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestLayoutServer
{
    @Test
    public void testUpdateColumnsFromSchemaFile ()
    {
        SchemaDao schemaModel = new SchemaDao();
        TableDao tableModel = new TableDao();
        ColumnDao columnModel = new ColumnDao();
        Schema schema = schemaModel.getByName("pixels");
        Table table = tableModel.getByNameAndSchema("testnull_pixels", schema);
        List<Column> columns = columnModel.getByTable(table);
        Map<String, Column> nameToColumnMap = new HashMap<>();

        for (Column column : columns)
        {
            nameToColumnMap.put(column.getName(), column);
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(
                "/home/hank/dev/idea-projects/rainbow/rainbow-layout/src/test/resources/105_schema.txt")))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                String[] splits = line.split("\t");
                String name = splits[0];
                nameToColumnMap.get(name).setSize(Double.parseDouble(splits[2]));
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        for (Column column : columns)
        {
            columnModel.update(column);
        }
    }

    @Test
    public void testUpdateColumnsFromPixels () throws IOException, MetadataException
    {
        SchemaDao schemaModel = new SchemaDao();
        TableDao tableModel = new TableDao();
        ColumnDao columnModel = new ColumnDao();
        Schema schema = schemaModel.getByName("pixels");
        Table table = tableModel.getByNameAndSchema("test_105_perf", schema);
        List<Column> columns = columnModel.getByTable(table);
        Map<String, Column> nameToColumnMap = new HashMap<>();

        for (Column column : columns)
        {
            nameToColumnMap.put(column.getName(), column);
        }

        System.out.println(columns.size());

        PixelsMetadataStat stat = new PixelsMetadataStat("dbiir01", 9000,"/pixels/pixels/test_105/v_0_order/");
        double[] columnSizes = stat.getAvgColumnChunkSize();
        List<String> columnNames = stat.getFieldNames();
        for (int i = 0;i < columnNames.size(); ++i)
        {
            String name = columnNames.get(i);
            nameToColumnMap.get(name).setSize(columnSizes[i]);
        }

        for (Column column : columns)
        {
            columnModel.update(column);
        }
    }

    @Test
    public void testGetLayout ()
    {
        SchemaDao schemaModel = new SchemaDao();
        TableDao tableModel = new TableDao();
        ColumnDao columnModel = new ColumnDao();
        LayoutDao layoutModel = new LayoutDao();
        Schema schema = schemaModel.getByName("pixels");
        Table table = tableModel.getByNameAndSchema("test_1187", schema);
        Layout layout = layoutModel.getByTable(table).get(0);
        System.out.println(layout.getCompact());
    }

    @Test
    public void testUpdateRGSize ()
    {
        ConfigFactory configFactory = ConfigFactory.Instance();
        configFactory.addProperty("metadata.db.password", "pixels16");
        configFactory.addProperty("metadata.db.url", "jdbc:mysql://dbiir10:3306/pixels_metadata?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull");
        SchemaDao schemaModel = new SchemaDao();
        TableDao tableModel = new TableDao();
        ColumnDao columnModel = new ColumnDao();
        LayoutDao layoutModel = new LayoutDao();
        Schema schema = schemaModel.getByName("pixels");
        Table table = tableModel.getByNameAndSchema("test_1187", schema);
        List<Column> columns = columnModel.getByTable(table);
        for (Column column : columns)
        {
            column.setSize(column.getSize()*36/25);
            columnModel.update(column);
        }
    }

    @Test
    public void testGetRGSize ()
    {
        ConfigFactory configFactory = ConfigFactory.Instance();
        configFactory.addProperty("metadata.db.password", "pixels16");
        configFactory.addProperty("metadata.db.url", "jdbc:mysql://dbiir10:3306/pixels_metadata?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull");
        SchemaDao schemaModel = new SchemaDao();
        TableDao tableModel = new TableDao();
        ColumnDao columnModel = new ColumnDao();
        LayoutDao layoutModel = new LayoutDao();
        Schema schema = schemaModel.getByName("pixels");
        Table table = tableModel.getByNameAndSchema("test_105", schema);
        List<Column> columns = columnModel.getByTable(table);
        double size = 0;
        for (Column column : columns)
        {
            size += column.getSize();
        }
        System.out.println(size);
    }

    @Test
    public void testServer ()
    {
        ConfigFactory configFactory = ConfigFactory.Instance();
        configFactory.addProperty("metadata.db.password", "pixels16");
        configFactory.addProperty("metadata.db.url", "jdbc:mysql://dbiir10:3306/pixels_metadata?useUnicode=true&characterEncoding=UTF-8&zeroDateTimeBehavior=convertToNull");
        WorkloadQueue workloadQueue = new WorkloadQueue();
        LayoutServer layoutServer = new LayoutServer("pixels", "test_1187",
                workloadQueue);
        Thread thread = new Thread(layoutServer);
        thread.start();

        List<AccessPattern> workload = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(
                "/home/hank/dev/idea-projects/rainbow/rainbow-layout/src/test/resources/1187_workload.txt")))
        {
            String line;
            while ((line = reader.readLine()) != null)
            {
                String[] splits = line.split("\t");
                AccessPattern accessPattern = new AccessPattern(splits[0], Double.parseDouble(splits[1]));
                String[] columns = splits[2].split(",");
                for (String column : columns)
                {
                    accessPattern.addColumn(column);
                }
                workload.add(accessPattern);
            }
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }

        try
        {
            workloadQueue.push(workload);
            thread.join();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
