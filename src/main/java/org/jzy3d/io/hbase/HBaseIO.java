package org.jzy3d.io.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.KeyValue;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.util.Bytes;
import org.jzy3d.demos.vbo.barmodel.model.KeyVal;

public class HBaseIO {

    private static Configuration conf = null;
    /**
     * Initialization
     */
    static {
        conf = HBaseConfiguration.create();
    }

    HBaseAdmin admin = null;

    public HBaseAdmin getAdmin() throws MasterNotRunningException, ZooKeeperConnectionException, IOException {
        if (admin == null)
            admin = new HBaseAdmin(conf);
        return admin;
    }

    public HTable getTable(String tableName) throws IOException {
        HTable table = new HTable(conf, tableName);
        return table;
    }

    /* TABLES */

    /**
     * Create a table
     */
    public void tableCreate(String tableName, String[] familys) throws Exception {
        HBaseAdmin admin = getAdmin();
        if (admin.tableExists(tableName)) {
            System.out.println("table already exists!");
        } else {
            HTableDescriptor tableDesc = new HTableDescriptor(tableName);
            for (int i = 0; i < familys.length; i++) {
                tableDesc.addFamily(new HColumnDescriptor(familys[i]));
            }
            admin.createTable(tableDesc);
            System.out.println("create table " + tableName + " ok.");
        }
    }

    /**
     * Delete a table
     */
    public void tableDelete(String tableName) throws Exception {
        try {
            HBaseAdmin admin = getAdmin();
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
            System.out.println("delete table " + tableName + " ok.");
        } catch (MasterNotRunningException e) {
            e.printStackTrace();
        } catch (ZooKeeperConnectionException e) {
            e.printStackTrace();
        }
    }

    /* ROWS */

    /**
     * Put (or insert) a row
     */
    public void put(String tableName, String rowKey, String family, String qualifier, String value) throws Exception {
        try {
            HTable table = getTable(tableName);
            put(table, rowKey, family, qualifier, value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void put(HTable table, String rowKey, String family, String qualifier, String value) throws Exception {
        try {
            Put put = new Put(Bytes.toBytes(rowKey));
            put.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes.toBytes(value));
            table.put(put);
            // System.out.println("insert recored " + rowKey + " to table ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void put(HTable table, String rowKey, String family, List<KeyVal<String, Float>> keyvals) throws Exception {
        try {
            Put put = new Put(Bytes.toBytes(rowKey));
            
            for(KeyVal<String, Float> keyval: keyvals){
                put.addColumn(Bytes.toBytes(family), Bytes.toBytes(keyval.key.toString()), Bytes.toBytes(keyval.key.toString()));               
            }
            table.put(put);
            // System.out.println("insert recored " + rowKey + " to table ok.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    public void putAll(List<List<KeyVal<String, Float>>> rows, String tableName, String family) throws Exception {
    }

    public void putAll(List<List<KeyVal<String, Float>>> rows, String tableName, String family, Progress progress) throws Exception {
        HTable table = getTable(tableName);

        int r = 0;
        int n = 0;
        for (List<KeyVal<String, Float>> row : rows) {
            put(table, r + "", family, row);

            /*for (KeyVal<?, ?> column : row) {
                put(table, r + "", family, column.key, column.val + "");
            }*/
            if (progress != null)
                progress.progress(r++);
        }
    }

    /**
     * Delete a row
     */
    public void delete(String tableName, String rowKey) throws IOException {
        HTable table = getTable(tableName);
        List<Delete> list = new ArrayList<Delete>();
        Delete del = new Delete(rowKey.getBytes());
        list.add(del);
        table.delete(list);
        System.out.println("del recored " + rowKey + " ok.");
    }

    /**
     * Get a row
     */
    public void get(String tableName, String rowKey) throws IOException {
        HTable table = getTable(tableName);
        Get get = new Get(rowKey.getBytes());
        Result rs = table.get(get);
        for (KeyValue kv : rs.raw()) {
            printKv(kv);
        }
    }

    /**
     * Scan (or list) a table
     */
    public void scanPrint(String tableName) {
        try {
            HTable table = getTable(tableName);
            Scan s = new Scan();
            ResultScanner ss = table.getScanner(s);
            for (Result r : ss) {
                for (KeyValue kv : r.raw()) {
                    printKv(kv);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* LOGS */

    private void printKv(KeyValue kv) {
        System.out.print(new String(kv.getRow()) + " ");
        System.out.print(new String(kv.getFamily()) + ":");
        System.out.print(new String(kv.getQualifier()) + " ");
        System.out.print(kv.getTimestamp() + " ");
        System.out.println(new String(kv.getValue()));
    }

}