package com.renyu.bledemo.utils;

import android.content.Context;

import com.renyu.bledemo.params.AddRequestBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * Created by renyu on 2017/2/9.
 */

public class ExcelUtils {

    public static void writeExcel(String filePath, List<AddRequestBean> beanList) {
        File fileDir=new File(filePath);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        File file=new File(fileDir+"/test.xls");
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        WritableWorkbook workbook=null;
        try {
            OutputStream os=new FileOutputStream(file);
            workbook= Workbook.createWorkbook(os);
            WritableSheet writableSheet=workbook.createSheet("测试结果", 0);
            String[] title={"SN", "测试结果", "测试时间"};
            for (int i = 0; i < title.length; i++) {
                Label label=new Label(i, 0, title[i]);
                writableSheet.addCell(label);
            }
            for (int i = 0; i < beanList.size(); i++) {
                AddRequestBean bean=beanList.get(i);
                Label label0=new Label(0, i+1, bean.getSn());
                Label label1=new Label(1, i+1, bean.getTestResult());
                Label label2=new Label(2, i+1, bean.getTestDate());
                writableSheet.addCell(label0);
                writableSheet.addCell(label1);
                writableSheet.addCell(label2);
            }
            workbook.write();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RowsExceededException e) {
            e.printStackTrace();
        } catch (WriteException e) {
            e.printStackTrace();
        } finally {
            if (workbook!=null) {
                try {
                    workbook.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (WriteException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
