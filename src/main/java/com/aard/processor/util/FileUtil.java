package com.aard.processor.util;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.text.Collator;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文件操作工具类
 *
 * @author chengao
 * @date 2016年10月26日 下午6:01:24
 */

public class FileUtil {

    public final static Comparator<Object> CHINA_COMPARE = Collator.getInstance(Locale.CHINA);

    /**
     * 删除空的文件夹
     *
     * @param folderPath 文件夹
     * @author chengao chengao163postbox@163.com
     * @date 2020/8/22 15:15
     */
    public static void delEmptyFolder(String folderPath) {
        if (StringUtils.isBlank(folderPath)) {
            return;
        }
        delEmptyFolder(new File(folderPath));
    }

    /**
     * 删除空的文件夹
     *
     * @param folderPath 文件夹
     * @author chengao chengao163postbox@163.com
     * @date 2020/8/22 15:15
     */
    public static void delEmptyFolder(File folderPath) {
        if (!folderPath.exists()) {
            return;
        }
        if (folderPath.isFile()) {
            return;
        }
        File[] files = folderPath.listFiles();
        if (files == null || files.length == 0) {
            folderPath.delete();
            return;
        }
        for (File item : files) {
            delEmptyFolder(item);
        }
        files = folderPath.listFiles();
        if (files == null || files.length == 0) {
            folderPath.delete();
        }
    }

    /**
     * 删除文件夹
     *
     * @param folderPath 文件夹完整绝对路径
     * @author chengao
     */
    public static void delFolder(String folderPath) {
        if (folderPath == null || folderPath.trim().equals("")) {
            return;
        }
        try {
            // 删除完里面所有内容
            delAllFile(folderPath);
            String filePath = folderPath;
            File myFilePath = new File(filePath);
            // 删除空文件夹
            myFilePath.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除指定文件夹下所有文件,包括本身
     *
     * @param path 文件夹完整绝对路径
     * @author chengao
     */
    public static boolean delAllFile(String path) {
        boolean flag = false;
        File file = new File(path);
        if (!file.exists()) {
            return flag;
        }
        if (file.isFile()) {
            file.delete();
            return true;
        }
        String[] tempList = file.list();
        File temp = null;
        for (int i = 0; tempList != null && i < tempList.length; i++) {
            if (path.endsWith(File.separator)) {
                temp = new File(path + tempList[i]);
            } else {
                temp = new File(path + File.separator + tempList[i]);
            }
            if (temp.isFile()) {
                temp.delete();
            }
            if (temp.isDirectory()) {
                // 先删除文件夹里面的文件
                delAllFile(temp.getAbsolutePath());
                // 再删除空文件夹
                delFolder(temp.getAbsolutePath());
                flag = true;
            }
        }
        file.delete();
        return flag;
    }



    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     * @return boolean
     */
    public boolean copyFile(String oldPath, String newPath) {
        try (
                FileOutputStream fs = new FileOutputStream(newPath);
                InputStream inStream = new FileInputStream(oldPath);
        ) {
            File oldfile = new File(oldPath);
            if (!oldfile.exists()) {
                return false;
            }
            int byteread = 0;
            // 读入原文件
            byte[] buffer = new byte[1024];
            while ((byteread = inStream.read(buffer)) != -1) {
                // 字节数 文件大小
                fs.write(buffer, 0, byteread);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取临时路径
     *
     * @return String
     * @author chengao chengao163postbox@163.com
     * @date 2020/4/11 16:27
     */
    public static String getTmpDirPath() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * 获取子级同等路径（子级只有一个文件并且是文件夹，文件夹名称各当前文件名相同）时返回子级文件夹完整路径
     *
     * @param folder 文件夹
     * @return String
     * @author chengao chengao163postbox@163.com
     * @date 2020/11/11 16:02
     */
    public static String getChildrenEqualityPath(String folder) {
        File folderFile = new File(folder);
        if (!folderFile.exists()) {
            return folder;
        }
        File[] folderChildrenFiles = folderFile.listFiles();
        if (folderFile.isDirectory()
                && folderChildrenFiles != null
                && folderChildrenFiles.length == 1
                && folderChildrenFiles[0].isDirectory()
                && folderChildrenFiles[0].getName().equals(folderFile.getName())) {
            //子级只有一个文件夹并且名称相同时拿子级文件夹当解析文件
            return getChildrenEqualityPath(folderChildrenFiles[0].getAbsolutePath());
        }
        return folder;
    }

    /**
     * 指定后辍查找文件,只返回第一个
     *
     * @param file    文件对象
     * @param suffixs 后辍
     * @return {@link File}
     * @author chengao chengao163postbox@163.com
     * @date 2019/8/7 14:35
     */
    public static File findOne(File file, String... suffixs) {
        List<File> files = find(file, suffixs);
        if (files == null || files.isEmpty()) {
            return null;
        }
        return files.get(0);
    }

    /**
     * 文件集合转map
     *
     * @param currentAbsolutePath 当前相对的绝对路径
     * @param files               文件集合
     * @author chengao chengao163postbox@163.com
     * @date 2020/3/17 15:30
     */
    private static Map<String, File> files2RelativePathMap(String currentAbsolutePath, List<File> files) {
        if (files == null || files.isEmpty()) {
            return new LinkedHashMap<>();
        }
        files = files.stream().sorted((file1, file2) -> CHINA_COMPARE.compare(file1.getAbsolutePath(), file2.getAbsolutePath())).collect(Collectors.toList());
        Map<String, File> relativePathMap = new LinkedHashMap<>();
        for (File item : files) {
            String relativePath = StringUtils.substring(item.getAbsolutePath(), currentAbsolutePath.length());
            relativePathMap.put(relativePath.replace(File.separator, "/"), item);
        }
        return relativePathMap;
    }

    /**
     * 指定后辍查找文件
     *
     * @param file    文件对象
     * @param suffixs 后辍
     * @return {@link File}
     * @author chengao chengao163postbox@163.com
     * @date 2019/8/7 14:35
     */
    public static Map<String, File> find2RelativePathMap(File file, String... suffixs) {
        List<File> files = find(file, suffixs);
        return files2RelativePathMap(file.getAbsolutePath(), files);
    }

    /**
     * 指定后辍查找文件
     *
     * @param file    文件对象
     * @param suffixs 后辍
     * @return {@link List < File >}
     * @author chengao chengao163postbox@163.com
     * @date 2019/8/7 14:35
     */
    public static List<File> find(File file, String... suffixs) {
        if (file == null || !file.exists() || suffixs == null) {
            return new ArrayList<>();
        }
        Function<String, Boolean> isVailFun = fileName -> {
            if (fileName.indexOf(".") == 0) {
                // 以 . 开头的文件排除
                return false;
            }
            if (fileName.indexOf("~$") == 0) {
                // 以 ~$ 开头的文件排除
                return false;
            }
            if (suffixs == null || suffixs.length == 0) {
                return true;
            }
            String suff = StringUtils.substringAfterLast(fileName, ".");
            for (String suffix : suffixs) {
                if (StringUtils.equalsIgnoreCase(suff, suffix)) {
                    return true;
                }
            }
            return false;
        };
        List<File> files = new ArrayList<>();
        if (file.isFile()) {
            if (isVailFun.apply(file.getName())) {
                files.add(file);
            }
            return files;
        }
        File[] listFiles = file.listFiles();
        for (File child : listFiles) {
            files.addAll(find(child, suffixs));
        }
        return files;
    }

    /**
     * 指定后辍查找文件
     *
     * @param file    文件对象
     * @param suffixs 排除的后辍
     * @return {@link List < File >}
     * @author chengao chengao163postbox@163.com
     * @date 2019/8/7 14:35
     */
    public static List<File> findExclude(File file, String... suffixs) {
        if (file == null || !file.exists()) {
            return new ArrayList<>();
        }
        Function<String, Boolean> isVailFun = fileName -> {
            if (fileName.indexOf(".") == 0) {
                // 以 . 开头的文件排除
                return false;
            }
            if (suffixs == null || suffixs.length == 0) {
                return true;
            }
            String suff = StringUtils.substringAfterLast(fileName, ".");
            for (String suffix : suffixs) {
                if (StringUtils.equalsIgnoreCase(suff, suffix)) {
                    return false;
                }
            }
            return true;
        };
        List<File> files = new ArrayList<>();
        if (file.isFile()) {
            if (isVailFun.apply(file.getName())) {
                files.add(file);
            }
            return files;
        }
        File[] listFiles = file.listFiles();
        for (File child : listFiles) {
            files.addAll(findExclude(child, suffixs));
        }
        return files;
    }

    /**
     * 指定名称查找文件夹
     *
     * @param file  文件对象
     * @param names 名称
     * @return {@link File}
     * @author chengao chengao163postbox@163.com
     * @date 2019/8/7 14:35
     */
    public static Map<String, File> findFolder2RelativePathMap(File file, Collection<String> names) {
        List<File> files = findFolder(file, names);
        return files2RelativePathMap(file.getAbsolutePath(), files);
    }

    /**
     * 指定名称查找文件夹
     *
     * @param file  文件对象
     * @param names 名称
     * @return {@link List < File >}
     * @author chengao chengao163postbox@163.com
     * @date 2019/8/7 14:35
     */
    public static List<File> findFolder(File file, Collection<String> names) {
        if (file == null || !file.exists() || names == null) {
            return new ArrayList<>();
        }
        Function<String, Boolean> isVailFun = fileName -> {
            if (fileName.indexOf(".") == 0) {
                // 以 . 开头的文件排除
                return false;
            }
            if (names == null || names.isEmpty()) {
                return true;
            }
            for (String name : names) {
                if (StringUtils.equalsIgnoreCase(name, fileName)) {
                    return true;
                }
            }
            return false;
        };
        if (file.isFile()) {
            return new ArrayList<>();
        }
        List<File> files = new ArrayList<>();
        if (file.isDirectory()) {
            if (isVailFun.apply(file.getName())) {
                files.add(file);
                return files;
            }
        }
        File[] listFiles = file.listFiles();
        for (File child : listFiles) {
            files.addAll(findFolder(child, names));
        }
        return files;
    }

    /**
     * 截断文件，文件夹下只有一个文件时文件夹当文件
     *
     * @param file 文件
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2020/11/18 22:17
     */
    public static boolean truncationFile(File file) {
        return truncationFile(file, 0);
    }

    /**
     * 截断文件，文件夹下只有一个文件时文件夹当文件
     *
     * @param file            文件
     * @param startLevelIndex 开始截取层级索引
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2020/11/18 22:17
     */
    public static boolean truncationFile(File file, int startLevelIndex) {
        return truncationFile(file, startLevelIndex, 0);
    }

    /**
     * 截断文件，文件夹下只有一个文件时文件夹当文件
     *
     * @param file            文件
     * @param startLevelIndex 开始截取层级索引
     * @param currLevelIndex  当前层级索引
     * @return boolean
     * @author chengao chengao163postbox@163.com
     * @date 2020/11/18 22:17
     */
    private static boolean truncationFile(File file, int startLevelIndex, int currLevelIndex) {
        if (file == null) {
            return false;
        }
        if (!file.exists()) {
            return false;
        }
        if (file.isFile()) {
            return false;
        }
        File[] files = file.listFiles();
        if (files.length == 1 && files[0].isFile() && currLevelIndex >= startLevelIndex) {
            String name = files[0].getName();
            String suffix = StringUtils.substringAfterLast(name, ".");
            if (StringUtils.isBlank(suffix)) {
                return false;
            }
            File renameToFile = new File(file.getAbsolutePath() + "." + suffix);
            if (!renameToFile.exists()) {
                files[0].renameTo(renameToFile);
                file.delete();
                return true;
            }
        }
        boolean flag = false;
        for (File file1 : file.listFiles()) {
            boolean b = truncationFile(file1, startLevelIndex, currLevelIndex + 1);
            if (b) {
                flag = b;
            }
        }
        return flag;
    }

    /**
     * inputStream 数据流转 byte[] 字节数组
     *
     * @param inputStream 数据流
     * @return {@link byte[]}
     * @author chengao chengao163postbox@163.com
     * @date 2019/7/30 21:13
     */
    public static byte[] inputStream2Byte(InputStream inputStream) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(1024)) {
            byte[] bytes = new byte[1024 * 16];
            int n;
            while ((n = inputStream.read(bytes)) != -1) {
                out.write(bytes, 0, n);
            }
            // 此方法大文件OutOfMemory
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * byte[] 字节数组转inputStream 数据流
     *
     * @param bytes 字节数组
     * @return {@link InputStream}
     * @author chengao chengao163postbox@163.com
     * @date 2019/7/30 21:13
     */
    public static final InputStream byte2InputStream(byte[] bytes) {
        return new ByteArrayInputStream(bytes);
    }

    /**
     * 文件转 byte[] 字节数组
     *
     * @param filePath 文件路径
     * @return {@link byte[]}
     * @author chengao chengao163postbox@163.com
     * @date 2019/7/30 21:13
     */
    public static byte[] file2byte(String filePath) {
        File file = new File(filePath);
        return file2byte(file);
    }

    /**
     * 文件转 byte[] 字节数组
     *
     * @param file 文件
     * @return {@link byte[]}
     * @author chengao chengao163postbox@163.com
     * @date 2019/7/30 21:13
     */
    public static byte[] file2byte(File file) {
        if (!file.exists()) {
            return new byte[0];
        }
        try (FileInputStream stream = new FileInputStream(file)) {
            return inputStream2Byte(stream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    /**
     * inputStream 数据流转文件
     *
     * @param inputStream 数据流
     * @param filePath    文件
     * @return {@link boolean}
     * @author chengao chengao163postbox@163.com
     * @date 2019/7/30 21:13
     */
    public static boolean inputStream2File(InputStream inputStream, String filePath) {
        File dir = new File(filePath).getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (
                FileOutputStream fos = new FileOutputStream(filePath);
                InputStream ins = inputStream;
        ) {
            int data = 0;
            // 1024Byte=1KB，分配30KB的缓存---通过这个还可以限制上传大小
            byte[] bytes = new byte[30 * 1024];
            while ((data = ins.read(bytes)) != -1) {
                fos.write(bytes, 0, data);
            }
            fos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * byte[] 字节数组流转文件
     *
     * @param bytes    字节数组
     * @param filePath 文件
     * @return {@link boolean}
     * @author chengao chengao163postbox@163.com
     * @date 2019/7/30 21:13
     */
    public static boolean byte2File(byte[] bytes, String filePath) {
        File dir = new File(filePath).getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        try (
                FileOutputStream fos = new FileOutputStream(new File(filePath));
                BufferedOutputStream bos = new BufferedOutputStream(fos);
        ) {
            bos.write(bytes);
            bos.flush();
            fos.flush();
            fos.close();
            bos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 计算文件大小
     *
     * @param files 文件
     * @return long
     * @author chengao chengao163postbox@163.com
     * @date 2020/3/28 9:23
     */
    public static long calculateSize(Collection<File> files) {
        if (files == null || files.isEmpty()) {
            return 0L;
        }
        long size = 0L;
        for (File file : files) {
            size += file.length();
        }
        return size;
    }


    /**
     * 判断文件的编码格式
     *
     * @return 文件编码格式
     * @throws Exception 无法识别utf-8withoutBOM
     */
    public static String getFileCodeString(byte[] bytes) {
        try (BufferedInputStream bin = new BufferedInputStream(new ByteArrayInputStream(bytes))) {
            // 读取文件头前16位
            int p = (bin.read() << 8) + bin.read();
            switch (p) {
                case 0xefbb:
                    return "UTF-8";
                case 0xfffe:
                    return "UTF-16LE";
                case 0xfeff:
                    return "UTF-16BE";
                default:
                    if (isUTF8(bytes)) {
                        return "UTF-8";
                    } else {
                        return "GBK";
                    }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断文件是否是无BOM的UTF-8编码
     *
     * @param bytes 字节数组
     * @return boolean
     */
    public static boolean isUTF8(byte[] bytes) {
        boolean state = true;
        try (BufferedInputStream bin = new BufferedInputStream(new ByteArrayInputStream(bytes))) {
            // 设置判断的字节流的数量
            int count = 10;
            int firstByte = 0;
            // 根据字节流是否符合UTF-8的标准来判断
            while (true) {
                if (count-- < 0 || (firstByte = bin.read()) == -1) {
                    break;
                }
                // 判断字节流
                if ((firstByte & 0x80) == 0x00) {
                    // 字节流为0xxxxxxx
                    continue;
                } else if ((firstByte & 0xe0) == 0xc0) {
                    // 字节流为110xxxxx10xxxxxx
                    if ((bin.read() & 0xc0) == 0x80) {
                        continue;
                    }
                } else if ((firstByte & 0xf0) == 0xe0) {
                    // 字节流为1110xxxx10xxxxxx10xxxxxx
                    if ((bin.read() & 0xc0) == 0x80 && (bin.read() & 0xc0) == 0x80) {
                        continue;
                    }
                } else if ((firstByte & 0xf8) == 0xf0) {
                    // 字节流为11110xxx10xxxxxx10xxxxxx10xxxxxx
                    if ((bin.read() & 0xc0) == 0x80 && (bin.read() & 0xc0) == 0x80 && (bin.read() & 0xc0) == 0x80) {
                        continue;
                    }
                } else if ((firstByte & 0xfc) == 0xf8) {
                    // 字节流为111110xx10xxxxxx10xxxxxx10xxxxxx10xxxxxx
                    if ((bin.read() & 0xc0) == 0x80 && (bin.read() & 0xc0) == 0x80 && (bin.read() & 0xc0) == 0x80 && (bin.read() & 0xc0) == 0x80) {
                        continue;
                    }
                } else if ((firstByte & 0xfe) == 0xfc) {
                    // 字节流为1111110x10xxxxxx10xxxxxx10xxxxxx10xxxxxx10xxxxxx
                    if ((bin.read() & 0xc0) == 0x80 && (bin.read() & 0xc0) == 0x80 && (bin.read() & 0xc0) == 0x80 && (bin.read() & 0xc0) == 0x80 && (bin.read() & 0xc0) == 0x80) {
                        continue;
                    }
                }
                state = false;
                break;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODOAuto-generatedcatchblock
            state = false;
            e.printStackTrace();
        }
        return state;
    }
}

