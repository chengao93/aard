package com.aard.processor.util;

import org.apache.commons.lang3.StringUtils;

import java.io.*;


/**
 * Text文件操作工具类
 *
 * @author chengao
 * @Package com.concom.yunzq.util
 * @ClassName: FileUtil
 * @date 2016年10月26日 下午6:01:24
 * @Description
 */

public class TextUtil {

    /**
     * 读取Text里的数据
     *
     * @param path 文件路径
     * @return String
     * @throws Exception
     */
    public static String reader(String path, String encoding) throws Exception {
        if (StringUtils.isBlank(path)) {
            return null;
        }
        //判断文件是否存在
        if (!new File(path).exists()) {
            return null;
        }
        File file = new File(path);
        if (file.isFile()) {
            return reader(new FileInputStream(file), encoding);
        }
        return null;
    }

    /**
     * 读取Text里的数据
     *
     * @param in 文件 encoding
     * @return String
     * @throws Exception
     */
    public static String reader(InputStream in, String encoding) throws Exception {
        byte[] bytes = FileUtil.inputStream2Byte(in);
        if (StringUtils.isBlank(encoding)) {
            encoding = FileUtil.getFileCodeString(bytes);
        }
        if (StringUtils.isBlank(encoding)) {
            encoding = "UTF-8";
        }
        StringBuffer sb = new StringBuffer();
        InputStreamReader read = null;

        try {
            read = new InputStreamReader(FileUtil.byte2InputStream(bytes), encoding);//考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while ((lineTxt = bufferedReader.readLine()) != null) {
                sb.append(lineTxt).append("\n");
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (read != null) {
                read.close();
            }
            if (in != null) {
                in.close();
            }
        }
        return sb.toString();
    }

    /**
     * 写入Text
     *
     * @param path 文件路径
     * @return String
     * @throws Exception
     */
    public static boolean write(String path, String text, String encoding) {
        if (encoding == null || encoding.trim().equals("")) {
            encoding = "UTF-8";
        }
        File parentFile = new File(path).getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        if (!new File(path).exists()) {
            try {
                new File(path).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        boolean flag = false;
        try (
                FileOutputStream fileOutputStream = new FileOutputStream(path);
                OutputStreamWriter out = new OutputStreamWriter(fileOutputStream, encoding)) {
            out.write(text);
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

}
