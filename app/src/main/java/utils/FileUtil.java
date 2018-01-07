package utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @Title: SopCastUtils
 * @Package com.laifeng.sopcastsdk.utils
 * @Description:
 * @Author Jim
 * @Date 2016/11/2
 * @Time 下午1:14
 * @Version
 */

public class FileUtil {
    private static final String APP_NAME = "laifeng";
    private static final String POST_VIDEO = ".mp4";
    private static final String POST_AUDIO = ".mp3";
    private static final String POST_IMAGE = ".jpg";
    private static final String LITE_VIDEO_PATH = "/" + APP_NAME + "/LiteVideo/";
    private static final String LITE_AUDIO_PATH = "/" + APP_NAME + "/LiteAudio/";
    private static final String LITE_IMAGE_PATH = "/" + APP_NAME + "/LiteImage/";
    private static final String SKILL_AUDIO_NAME = "facetime_skill_audio";
    private static final String SKILL_COVER_NAME = "facetime_skill_cover";
    private static final String REPORT_IMAGE_NAME = "facetime_report_image";
    private static final String UPLOAD_VIDEO_OSS_RECORD_PATH = "/" + APP_NAME + "/oss_record/";

    public static String getStringFromAssets(Context context, String fileName) {
        String fileContent = "";
        try {
            InputStream is = context.getAssets().open(fileName);
            fileContent = inputStream2String(is);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileContent;
    }

    public static String createOssRecoder(Context context){
        String state = Environment.getExternalStorageState();
        File rootDir = state.equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory() : context.getCacheDir();

        File folderDir = new File(rootDir.getAbsolutePath() + UPLOAD_VIDEO_OSS_RECORD_PATH);
        if (!folderDir.exists() && folderDir.mkdirs()) {

        }
        return  folderDir.getAbsolutePath();
    }

    public static String getFileMD5(String fileName) {
        RandomAccessFile raf;
        try {
            raf=new RandomAccessFile(fileName, "r");
            long le=raf.length();
            int sz=(int) (le/100);
            byte[] buffer = new byte[sz];
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            int numRead = 0;
            long size=0;
            while ((numRead = raf.read(buffer)) > 0) {
                md5.update(buffer, 0, numRead);
                size+=numRead;
            }
            raf.close();
            return bufferToHex(md5.digest());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String bufferToHex(byte[] bytes) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuffer stringbuffer = new StringBuffer(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            appendHexPair(bytes[l], stringbuffer);
        }
        return stringbuffer.toString();
    }

    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
        char c0 = md5Chars[(bt & 0xf0) >> 4];
        char c1 = md5Chars[bt & 0xf];
        stringbuffer.append(c0);
        stringbuffer.append(c1);
    }

    private static char md5Chars[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    public static byte[] getBytesFromAssets(Context context, String fileName) {
        byte[] buffer = null;
        try {
            InputStream is = context.getAssets().open(fileName);
            //获取文件的字节数
            int lenght = is.available();
            //创建byte数组
            buffer = new byte[lenght];
            //将文件中的数据读到byte数组中
            is.read(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return buffer;
    }

    public static String inputStream2String(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];
        for (int n; (n = in.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

    public static void copyFile(String source, String destination) throws IOException {
        File sourceFile = new File(source);
        File destFile = new File(destination);
        if(destFile.exists()) {
            destFile.delete();
        }

        FileChannel sourceChannel = null;
        FileChannel destChannel = null;

        try {
            sourceChannel = new FileInputStream(sourceFile).getChannel();
            destChannel = new FileOutputStream(destFile).getChannel();
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        }
        finally {
            if(sourceChannel != null) {
                sourceChannel.close();
            }
            if(destChannel != null) {
                destChannel.close();
            }
        }
    }

    public static Bitmap getImageFromAssetsFile(Context context, String fileName){
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try{
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return image;
    }

    public static void deleteFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File combineFile = new File(filePath);
        if (combineFile.exists()) {
            combineFile.delete();
        }
    }

    public static String createVideoFile(Context context) {
        String state = Environment.getExternalStorageState();
        File rootDir = state.equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory() : context.getCacheDir();
        File folderDir = new File(rootDir.getAbsolutePath() + LITE_VIDEO_PATH);
        if (!folderDir.exists() && folderDir.mkdirs()) {}
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        long currentMillis = System.currentTimeMillis() % 1000;
        timeStamp = timeStamp + "_" + currentMillis;
        String fileName = APP_NAME + "_" + timeStamp;
        File file = new File(folderDir, fileName + POST_VIDEO);
        return file.getAbsolutePath();
    }

    public static File createSkillAudioFile(Context context) {
        String state = Environment.getExternalStorageState();
        File rootDir = state.equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory() : context.getCacheDir();
        File folderDir = new File(rootDir.getAbsolutePath() + LITE_AUDIO_PATH);
        if (!folderDir.exists() && folderDir.mkdirs()) {}
        String fileName = APP_NAME + "_" + SKILL_AUDIO_NAME;
        return new File(folderDir, fileName + POST_AUDIO);
    }

    public static File createSkillCoverFile(Context context) {
        String state = Environment.getExternalStorageState();
        File rootDir = state.equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory() : context.getCacheDir();
        File folderDir = new File(rootDir.getAbsolutePath() + LITE_IMAGE_PATH);
        if (!folderDir.exists() && folderDir.mkdirs()) {}
        String fileName = APP_NAME + "_" + SKILL_COVER_NAME;
        return new File(folderDir, fileName + POST_IMAGE);
    }

    public static File createReportImageFile(Context context) {
        String state = Environment.getExternalStorageState();
        File rootDir = state.equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory() : context.getCacheDir();
        File folderDir = new File(rootDir.getAbsolutePath() + LITE_IMAGE_PATH);
        if (!folderDir.exists() && folderDir.mkdirs()) {}
        String fileName = APP_NAME + "_" + REPORT_IMAGE_NAME;
        return new File(folderDir, fileName + POST_IMAGE);
    }
}
