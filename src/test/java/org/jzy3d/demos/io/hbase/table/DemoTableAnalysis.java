package org.jzy3d.demos.io.hbase.table;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jzy3d.demos.BigPicture;
import org.jzy3d.demos.vbo.barmodel.builder.VBOBuilderTableColumnsScatter3d;
import org.jzy3d.demos.vbo.barmodel.generators.GeneratorKeyValue;
import org.jzy3d.io.DefaultTableScanScheduler;
import org.jzy3d.io.KeyVal;
import org.jzy3d.io.Progress;
import org.jzy3d.io.hbase.HBaseIO;
import org.jzy3d.plot3d.primitives.vbo.drawable.DrawableVBO;
import org.jzy3d.utils.LoggerUtils;

/**
 * Draws key-values of each row, where each column name is mapped to Y and a
 * color, and each column value is mapped to Z.
 * 
 * @author martin
 *
 */
public class DemoTableAnalysis {
    public static int MILION = 1000000;
    public static String TABLE = DemoTableAnalysis.class.getSimpleName();
    public static String FAMILY = "demo";
    static {
        LoggerUtils.minimal();
    }

    public static void main(String[] args) throws Exception {
        int nRaws = MILION / 1000;
        int nPivotTheme = 10;
        int nPivotCol = 15 * nPivotTheme;
        int nCpCcCat = 10;
        int nCpCcCol = 5;

        // Generate table data
        GeneratorKeyValue generator = new GeneratorKeyValue();
        DefaultTableScanScheduler scheduler = new DefaultTableScanScheduler(generator.vip(nRaws, nPivotCol, nCpCcCat, nCpCcCol));
        
        report(scheduler, new File("data/screenshots/tableanalysis/"));
        draw(scheduler.getTable());
        //hbaseDump(rows);
    }
    
    public static void report(DefaultTableScanScheduler scheduler, File output) throws IOException{
        TableAnalysis analysis = new TableAnalysis(scheduler);
        analysis.report(output);
    }
    

    public static void draw(final List<List<KeyVal<String, Float>>> rows) {
        DrawableVBO drawable = new DrawableVBO(new VBOBuilderTableColumnsScatter3d(rows));
        BigPicture.chart(drawable, BigPicture.Type.dd).black();
    }

    public static void hbaseDump(final List<List<KeyVal<String, Float>>> rows) throws Exception {
        // dump in HBase table
        String[] families = { FAMILY };
        HBaseIO hbase = new HBaseIO();
        //hbase.tableDelete(TABLE);
        hbase.tableCreate(TABLE, families);
        hbase.putAll(rows, TABLE, FAMILY, progress(1000));
    }

    private static Progress progress(final int interval) {
        Progress progress = new Progress(){
            public void progress(int value) {
                if(value%interval==0)
                    System.out.println(value + " inserted");
            }
        };
        return progress;
    }
}
