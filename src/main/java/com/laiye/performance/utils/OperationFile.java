package com.laiye.performance.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

@Component
public class OperationFile {
    public String getFileString(String filename) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }
            br.close();
            return buffer.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteFile(String folder) {
        File path = new File(folder);
        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    file.delete();
                } else {
                    deleteFile(file.getAbsolutePath());
                    file.delete();
                }
            }
        }
    }

    public JSONObject getResourcesJosonconfig(String filename) {
        String fname = filename;
        try {
            ClassPathResource resource = new ClassPathResource(fname);
            BufferedReader br = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }
            br.close();
            JSONObject object = JSON.parseObject(String.valueOf(buffer));
            return object;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String zipString(String unzipString) {
        //?????????????????????????????????????????????????????????
        Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION, true);
//        Deflater deflater = new Deflater();
        //???????????????????????????
        deflater.setInput(unzipString.getBytes(StandardCharsets.UTF_8));
        //??????????????????????????????????????????????????????????????????????????????
        deflater.finish();

        final byte[] bytes = new byte[512];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(512);

        while (!deflater.finished()) {
            //???????????????????????????????????????????????????????????????
            int length = deflater.deflate(bytes);
            outputStream.write(bytes, 0, length);
        }
        //???????????????????????????????????????????????????
        deflater.end();
        return new String(outputStream.toByteArray());
    }

    public String unzipString(String zipString) {
        //??????????????????????????????  https://www.yiibai.com/javazip/javazip_inflater.html

        Inflater inflater = new Inflater(true);
        //?????????????????????????????????
        inflater.reset();
//        byte[] zipStringBytes = zipString.getBytes(StandardCharsets.UTF_8);
        byte[] zipStringBytes = zipString.getBytes();
        inflater.setInput(zipStringBytes);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(zipStringBytes.length);
        try {
            byte[] bytes = new byte[1024];
            //finished() ???????????????????????????????????????????????????true???
            while (!inflater.finished()) {
                //?????????????????????????????????????????????
                int length = inflater.inflate(bytes);
                outputStream.write(bytes, 0, length);
            }
        } catch (DataFormatException e) {
            e.printStackTrace();
            return null;
        } finally {
            //??????????????????????????????????????????????????????
            inflater.end();
        }
        try {
            return outputStream.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public  String uncompress(byte[] input) throws IOException {
        Inflater inflater = new Inflater(true);
        inflater.setInput(input);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
        try {
            byte[] buff = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buff);
                baos.write(buff, 0, count);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            baos.close();
        }
        inflater.end();
        byte[] output = baos.toByteArray();
        return new String(output, "UTF-8");
    }

    public  byte[] compress(byte[] data) throws IOException {
        byte[] output;
        Deflater compress = new Deflater(Deflater.BEST_COMPRESSION,true);

        compress.reset();
        compress.setInput(data);
        compress.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!compress.finished()) {
                int i = compress.deflate(buf);
                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            bos.close();
        }
        compress.end();
        return output;
    }

    public boolean isFileExist(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return true;
        }
        return false;
    }
    public static void main(String[] args) {
        String a = "aaa";
        OperationFile operationFile = new OperationFile();
        String zipString = operationFile.zipString(a);
        System.out.println(zipString);
        System.out.println("****");
        System.out.println(operationFile.unzipString(zipString));
    }
}
