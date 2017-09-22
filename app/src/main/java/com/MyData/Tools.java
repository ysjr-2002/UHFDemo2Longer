package com.MyData;

import java.io.*;
import java.io.FileNotFoundException;

/**
 * Created by Shaojie on 2017/9/17.
 */

public class Tools {
    public static void copyFileUsingStream(File source, File dest)
            throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //�ļ�����ʱ
                InputStream inStream = new FileInputStream(oldPath); //����ԭ�ļ�
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024 * 10];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //�ֽ��� �ļ���С
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("���Ƶ����ļ���������");
            e.printStackTrace();
        }
    }

    public static void deleteFile(File file) {

        if (file.isDirectory()) {
            File[] listfiles = file.listFiles();
            for (File temp : listfiles) {
                deleteFile(temp);
            }
        }
        file.delete();
    }
}
