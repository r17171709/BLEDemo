package com.renyu.bledemo.utils;

import com.renyu.bledemo.params.AddBRequestBean;
import com.renyu.bledemo.params.AddQRequestBean;
import com.renyu.bledemo.params.AddRequestBean;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Created by renyu on 2017/2/9.
 */

public class ExcelUtils {

    public static boolean writeExcelB(String filePath, AddBRequestBean bean) {
        boolean isSave=false;
        File file=new File(filePath);
        int rowNumbers=getRowNumbers(filePath);
        if (rowNumbers==0) {
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        WritableWorkbook workbook=null;
        WritableSheet writableSheet;
        try {
            if (rowNumbers==0) {
                OutputStream os=new FileOutputStream(file);
                workbook= Workbook.createWorkbook(os);
                writableSheet=workbook.createSheet("半制测试结果", 0);
                String[] title={"SN", "RSSI", "SN写入情况", "MAGIC写入情况", "测试结果", "测试时间"};
                for (int i = 0; i < title.length; i++) {
                    Label label=new Label(i, 0, title[i]);
                    writableSheet.addCell(label);
                }
                rowNumbers=1;
            }
            else {
                workbook= Workbook.createWorkbook(file, Workbook.getWorkbook(file));
                writableSheet=workbook.getSheet(0);
            }
            Label label0=new Label(0, rowNumbers, bean.getSn());
            Label label1=new Label(1, rowNumbers, bean.getRssi());
            Label label2=new Label(2, rowNumbers, bean.getSn_state());
            Label label3=new Label(3, rowNumbers, bean.getMagic());
            Label label4=new Label(4, rowNumbers, bean.getTestResult());
            Label label5=new Label(5, rowNumbers, bean.getTestDate());
            writableSheet.addCell(label0);
            writableSheet.addCell(label1);
            writableSheet.addCell(label2);
            writableSheet.addCell(label3);
            writableSheet.addCell(label4);
            writableSheet.addCell(label5);
            workbook.write();
            isSave=true;
        } catch (Exception e) {
            isSave=false;
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
        return isSave;
    }

    public static boolean writeExcelQ(String filePath, AddQRequestBean bean) {
        boolean isSave=false;
        File file=new File(filePath);
        int rowNumbers=getRowNumbers(filePath);
        if (rowNumbers==0) {
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        WritableWorkbook workbook=null;
        WritableSheet writableSheet;
        try {
            if (rowNumbers==0) {
                OutputStream os=new FileOutputStream(file);
                workbook= Workbook.createWorkbook(os);
                writableSheet=workbook.createSheet("全制测试结果", 0);
                String[] title={"SN", "测试负载", "测试结果", "测试时间"};
                for (int i = 0; i < title.length; i++) {
                    Label label=new Label(i, 0, title[i]);
                    writableSheet.addCell(label);
                }
                rowNumbers=1;
            }
            else {
                workbook= Workbook.createWorkbook(file, Workbook.getWorkbook(file));
                writableSheet=workbook.getSheet(0);
            }
            Label label0=new Label(0, rowNumbers, bean.getSn());
            Label label1=new Label(1, rowNumbers, bean.getCurrent());
            Label label2=new Label(2, rowNumbers, bean.getTestResult());
            Label label3=new Label(3, rowNumbers, bean.getTestDate());
            writableSheet.addCell(label0);
            writableSheet.addCell(label1);
            writableSheet.addCell(label2);
            writableSheet.addCell(label3);
            workbook.write();
            isSave=true;
        } catch (Exception e) {
            isSave=false;
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
        return isSave;
    }

    public static boolean writeSExcel(String filePath, AddRequestBean bean) {
        boolean isSave=false;
        File file=new File(filePath);
        int rowNumbers=getRowNumbers(filePath);
        if (rowNumbers==0) {
            if (file.exists()) {
                file.delete();
            }
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        WritableWorkbook workbook=null;
        WritableSheet writableSheet;
        try {
            if (rowNumbers==0) {
                OutputStream os=new FileOutputStream(file);
                workbook= Workbook.createWorkbook(os);
                writableSheet=workbook.createSheet("施工测试结果", 0);
                String[] title={"SN", "deviceId", "读取阈值", "写阈值", "测试结果", "测试时间"};
                for (int i = 0; i < title.length; i++) {
                    Label label=new Label(i, 0, title[i]);
                    writableSheet.addCell(label);
                }
                rowNumbers=1;
            }
            else {
                workbook= Workbook.createWorkbook(file, Workbook.getWorkbook(file));
                writableSheet=workbook.getSheet(0);
            }
            Label label0=new Label(0, rowNumbers, bean.getSn());
            Label label1=new Label(1, rowNumbers, bean.getDeviceID());
            Label label2=new Label(2, rowNumbers, bean.getCurrent());
            Label label3=new Label(3, rowNumbers, bean.getCurrentThresHold());
            Label label4=new Label(4, rowNumbers, bean.getTestResult());
            Label label5=new Label(5, rowNumbers, bean.getTestDate());
            writableSheet.addCell(label0);
            writableSheet.addCell(label1);
            writableSheet.addCell(label2);
            writableSheet.addCell(label3);
            writableSheet.addCell(label4);
            writableSheet.addCell(label5);
            workbook.write();
            isSave=true;
        } catch (Exception e) {
            isSave=false;
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
        return isSave;
    }

    public static ArrayList<AddRequestBean> readExcel(String filePath) {
        ArrayList<AddRequestBean> beans=new ArrayList<>();
        File file=new File(filePath);
        if (!file.exists()) {
            return beans;
        }
        try {
            InputStream is=new FileInputStream(filePath);
            Workbook workbook=Workbook.getWorkbook(is);
            Sheet sheet=workbook.getSheet(0);
            int rows=sheet.getRows();
            for (int i=0;i<rows;i++) {
                // 去除第一行标题名称
                if (i==0) {
                    continue;
                }
                AddRequestBean bean=new AddRequestBean();
                bean.setDeviceID(sheet.getCell(1, i).getContents());
                bean.setMachineName(sheet.getCell(2, i).getContents());
                bean.setMachineId(sheet.getCell(3, i).getContents());
                beans.add(bean);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return beans;
    }

    private static int getRowNumbers(String filePath) {
        File file=new File(filePath);
        if (!file.exists()) {
            return 0;
        }
        else {
            int rows=0;
            try {
                InputStream is = new FileInputStream(filePath);
                Workbook workbook=Workbook.getWorkbook(is);
                Sheet sheet=workbook.getSheet(0);
                rows=sheet.getRows();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (BiffException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                return rows;
            }
        }
    }
}
