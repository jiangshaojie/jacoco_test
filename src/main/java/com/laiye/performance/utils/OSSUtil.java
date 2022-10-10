package com.laiye.performance.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.*;
//import com.laiye.evaluation.enums.Constants1;
import com.laiye.performance.enity.JmxFileDescription;
import com.laiye.performance.enity.TestDataRecord;
import com.laiye.performance.enums.ConstantFileType;
import com.laiye.performance.enums.TestDataTypes;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class OSSUtil {
    private static final int BUFFER_SIZE = 2 * 1024;
    //    private final static Logger logger = Logger.getLogger(OSSUtil.class);
    private final static String endpoint = "https://oss-cn-beijing.aliyuncs.com";
    private final static String accessKeyId = "LTAI4GC5vTAdv8mq9zq39Ltc";
    private final static String accessKeySecret = "Va16tcbGH2NZ0VeIVvyhFvgRaUpc1k";
    private final static String bucketName = "laiye-backup";
    private final static String folderName = "performance-platform/";
//    private final static String ossStart = "oss://laiye-backup/performance-platform/";

    public static void uploadFile(String projectName, String fileType, MultipartFile file) {
        String destFile = getOssDestPath(projectName, fileType, file);
        ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
        conf.setSupportCname(true);
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret, conf);
        try {
            InputStream inputStream = file.getInputStream();
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, destFile, inputStream);
            ossClient.putObject(putObjectRequest);

            Date expiration = new Date(new Date().getTime() + 365 * 24 * 3600 * 1000);
            URL url = ossClient.generatePresignedUrl(bucketName, destFile, expiration);
            log.info("oss upload: {}:{}:{}", destFile, file.getName(), url);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ossClient.shutdown();
        }
    }

    public static void uploadFile(String destPath, File file) {
        if (file.isDirectory() || !file.exists()) {
            return;
        }
        ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
        conf.setSupportCname(true);
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret, conf);
        try {
            InputStream inputStream = new FileInputStream(file.getPath());
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, destPath, inputStream);
            ossClient.putObject(putObjectRequest);

            Date expiration = new Date(new Date().getTime() + 365 * 24 * 3600 * 1000);
            URL url = ossClient.generatePresignedUrl(bucketName, destPath, expiration);
            log.info("oss upload: {}:{}:{}", destPath, file.getName(), url);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ossClient.shutdown();
        }
    }

    public static void downloadFolder(String sourceDir, String destDir) {
        /*if (sourceDir.startsWith(Constants1.OSS_URL_PREFIX)) {
            sourceDir = sourceDir.replaceFirst(Constants1.OSS_URL_PREFIX + bucketName + "/", "");
        }*/
        sourceDir = folderName + sourceDir;
        ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
        conf.setSupportCname(true);
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret, conf);
        try {
            final int maxKeys = 1000;
            String nextMarker = null;
            ObjectListing objectListing;
//            ObjectListing objectListing = ossClient.listObjects(bucketName, sourceDir);
            do {
//                ossClient.
                objectListing = ossClient.listObjects(new ListObjectsRequest(bucketName).withPrefix(sourceDir).withMarker(nextMarker).withMaxKeys(maxKeys));
                List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
                for (OSSObjectSummary s : sums) {
                    String key = s.getKey();
                    if (key.endsWith("/")) {
                        continue;
                    }
//                    String dest = destDir + "/" + key.replaceFirst(sourceDir, "");
                    String dest = key.replaceFirst(folderName, "");
                    File destFile = new File(dest);

                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    ossClient.getObject(new GetObjectRequest(bucketName, s.getKey()), destFile);
                    log.info("oss download::{} {}", s.getKey(), destFile);

                }
                nextMarker = objectListing.getNextMarker();
            } while (objectListing.isTruncated());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ossClient.shutdown();
        }
    }

    /*public static void downloadFile(String sourceDir, String destDir) {
     *//*if (sourceDir.startsWith(Constants1.OSS_URL_PREFIX)) {
            sourceDir = sourceDir.replaceFirst(Constants1.OSS_URL_PREFIX + bucketName + "/", "");
        }*//*
        sourceDir = folderName + sourceDir;
        ClientBuilderConfiguration conf = new ClientBuilderConfiguration();
        conf.setSupportCname(true);
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret, conf);
        try {
            final int maxKeys = 1000;
            String nextMarker = null;
            ObjectListing objectListing;
//            ObjectListing objectListing = ossClient.listObjects(bucketName, sourceDir);
            do {
//                ossClient.
                objectListing = ossClient.listObjects(new ListObjectsRequest(bucketName).withPrefix(sourceDir).withMarker(nextMarker).withMaxKeys(maxKeys));
                List<OSSObjectSummary> sums = objectListing.getObjectSummaries();
                for (OSSObjectSummary s : sums) {
                    String key = s.getKey();
                    if (key.endsWith("/")) {
                        continue;
                    }
//                    String dest = destDir + "/" + key.replaceFirst(sourceDir, "");
//                    String dest = new String((destDir + "/" + key.replaceFirst(sourceDir, "")).getBytes(), "utf-8");
                    File destFile = new File(destDir);

                    if (!destFile.getParentFile().exists()) {
                        destFile.getParentFile().mkdirs();
                    }
                    ossClient.getObject(new GetObjectRequest(bucketName, s.getKey()), destFile);
                    log.info("oss download::{}:{}", s.getKey(), destFile);
                }
                nextMarker = objectListing.getNextMarker();
            } while (objectListing.isTruncated());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            ossClient.shutdown();
        }
    }*/

    public static void toZip(String srcDir, OutputStream out, boolean KeepDirStructure) throws RuntimeException {
        long start = System.currentTimeMillis();
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(out);
            File sourceFile = new File(srcDir);
            compress(sourceFile, zos, sourceFile.getName(), KeepDirStructure);
            long end = System.currentTimeMillis();
            log.info("压缩完成，耗时：{} ms", (end - start));
        } catch (Exception e) {
            throw new RuntimeException("zip error from ZipUtils", e);
        } finally {
            if (zos != null) {
                try {
                    zos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void compress(File sourceFile, ZipOutputStream zos, String name, boolean KeepDirStructure) throws Exception {
        byte[] buf = new byte[BUFFER_SIZE];
        if (sourceFile.isFile()) {
            zos.putNextEntry(new ZipEntry(name));
            int len;
            FileInputStream in = new FileInputStream(sourceFile);
            while ((len = in.read(buf)) != -1) {
                zos.write(buf, 0, len);
            }
            zos.closeEntry();
            in.close();
        } else {
            File[] listFiles = sourceFile.listFiles();
            if (listFiles == null || listFiles.length == 0) {
                if (KeepDirStructure) {
                    zos.putNextEntry(new ZipEntry(name + "/"));
                    zos.closeEntry();
                }
            } else {
                for (File file : listFiles) {
                    if (KeepDirStructure) {
                        compress(file, zos, name + "/" + file.getName(), KeepDirStructure);
                    } else {
                        compress(file, zos, file.getName(), KeepDirStructure);
                    }
                }
            }
        }
    }

   /* public static void upload2OSS(String producerName, String ocrType, String subType, String tag) {
        String subDir = getSubDir(producerName, ocrType);
        String basePath = OcrUtil.getOcrDataPathByType(ocrType);
        Map<String, String> subs = null;
        if (Constants1.OCR_TYPE_GENERAL_STRING.equals(ocrType) || Constants1.OCR_TYPE_GENERAL_STRING_STANDARD.equals(ocrType) || Constants1.OCR_TYPE_GENERAL_STRING_CPU.equals(ocrType)) {
            subs = OcrUtil.getGeneralSubCategories();
        } else {
            subs = Otils.getSubCategories(basePath);
        }

        for (String key : subs.keySet()) {
            if (subType != null && !key.contains(subType)) {
                continue;
            }
            String value = subs.get(key);
            if (!basePath.endsWith("/")) {
                basePath = basePath + "/";
            }
            String dataPath = basePath + value;
            String diffPath = dataPath.replaceFirst("data", "diff/" + subDir);
            logger.info(String.format("diff path:%s:%s:%s", subDir, diffPath, dataPath));

            File localPath = new File(diffPath);
            if (!localPath.exists()) {
                logger.info(String.format("data path not existed::%s:%s:%s", subDir, diffPath, dataPath));
                continue;
            }

            String zip = diffPath.replaceAll("\\s+", "") + ".zip";
            try {
                FileOutputStream fps1 = new FileOutputStream(zip);
                toZip(diffPath, fps1, true);
                uploadFile(ocrType, ocrType, tag, new File(zip));
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(String.format("upload failed::%s:%s:%s", subDir, diffPath, dataPath));
            }
        }
    }*/

    /*public static void uploadBadCases2OSS(String producerName, String ocrType, String tag, String dataPath) {
        File localPath = new File(dataPath);
        if (!localPath.exists()) {
            logger.info(String.format("data path not existed:%s:%s:%s", producerName, ocrType, dataPath));
            return;
        }
        if (dataPath.endsWith("/")) {
            dataPath = dataPath.substring(0, dataPath.length() - 1);
        }

        String zip = dataPath.replaceAll("\\s+", "") + ".zip";
        try {
            FileOutputStream fps1 = new FileOutputStream(zip);
            toZip(dataPath, fps1, true);
            uploadFile(producerName, ocrType, tag, new File(zip));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(String.format("upload failed:%s:%s:%s", producerName, ocrType, dataPath));
        }
    }*/

    /**
     * 获取文件数据oss路径
     *
     * @param projectName
     * @param fileType
     * @param file
     * @return
     */
    public static String getOssDestPath(String projectName, String fileType, MultipartFile file) {
       /* if (ConstantFileType.JMX_FILE_TYPE.getValue().equals(fileType)) {
            JmxFileDescription jmxFileDescription = (JmxFileDescription) fileInfo;
            return folderName + "data/" + jmxFileDescription.getProjectName() + "/" + file.getOriginalFilename();
        }
        if (ConstantFileType.TEST_DATA_TYPE.getValue().equals(fileType)) {
            TestDataRecord testDataRecord = (TestDataRecord) fileInfo;
            *//*if (testDataRecord.getType().equals(TestDataTypes.FOLDER.getValue())) {
                return folderName + "data/" + testDataRecord.getProjectName() + "/" + testDataRecord.getDataName() + "/" + file.getOriginalFilename();
            }
            if (testDataRecord.getType().equals(TestDataTypes.FILE.getValue())) {*//*
            return folderName + "data/" + testDataRecord.getProjectName() + "/" + file.getOriginalFilename();
//            }
        }*/
//        return null;
        return folderName + "data/" + projectName + "/" + file.getOriginalFilename();

    }

    private static String getSubDir(String producerName, String ocrType) {
        String subdir = "";
        if (producerName == null || "".equals(producerName)) {
            subdir = ocrType;
        } else {
            subdir = ocrType + "-" + producerName;
        }
        return subdir;
    }


    public static void main(String[] args) throws Exception {
//        upload2OSS(null, Constants1.OCR_TYPE_GENERAL_STRING_STANDARD, "公开数据");
//        upload2OSS("合合版", Constants1.OCR_TYPE_GENERAL_STRING, "公开数据");
//        upload2OSS(null, Constants1.OCR_TYPE_GENERAL_STRING_STANDARD, "紫金");
//        upload2OSS("合合版", Constants1.OCR_TYPE_GENERAL_STRING, "紫金");
//        upload2OSS(null, Constants1.OCR_TYPE_GENERAL_STRING_STANDARD, "门诊收据-外汇管理局-4");
//        upload2OSS("合合版", Constants1.OCR_TYPE_GENERAL_STRING, "门诊收据-外汇管理局-4");

//        upload2OSS("标准版", Constants1.OCR_TYPE_STAMP, "");

//        upload2OSS(null, Constants1.OCR_TYPE_GENERAL_STRING_STANDARD, "送检单");

        String sourceDir = "评测集/通用卡证识别/身份证评测集-200/身份证评测集-复印件-20（已标注）/1-20/027f80d63fefeb2455cf8e51ab1e1107_周磊.png";
        sourceDir = "oss://laiye-backup/评测集/通用卡证识别/身份证评测集-200/身份证评测集-复印件-20（已标注）/1-20/";
        String destDir = "data/";
//        downloadFile(sourceDir, destDir);

//        String type = Constants1.OCR_TYPE_GENERAL_STRING_STANDARD;
//        type = Constants1.OCR_TYPE_GENERAL_STRING;
//        List<String> producers = new ArrayList<>();
//        producers.add(null);
//        producers.add("合合版");
//        producers.add("第四范式");
//        producers.add("探智立方");
//        producers.add("汉王");
//        for (String producer : producers) {
//            upload2OSS(producer, type, "送检单");
//            upload2OSS(producer, type, "印尼身份证");
//            upload2OSS(producer, type, "事业单位法人证");
//            upload2OSS(producer, type, "票据-建筑行业投标人信息公示");
//
//            upload2OSS(producer, type, "运营商行业");
//            upload2OSS(producer, type, "银行流水");
//            upload2OSS(producer, type, "物流行业");
//            upload2OSS(producer, type, "销货清单");
//        }

//        upload2OSS("第四范式", Constants1.OCR_TYPE_GENERAL_STRING, "");
//        upload2OSS("合合", Constants1.OCR_TYPE_STAMP, "");
    }

    public static void uploadFile(String projectName, String value, MultipartFile[] folder) {
        for (MultipartFile file : folder) {
            uploadFile(projectName, value, file);
        }
    }
}

