package retrofit;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

/**
 * @说明：文件管理
 */

/**
 * 文件管理类    在以后的开发中也可以使用这个工具类，提高代码的利用性
 * 只要是对SD卡的操作
 * 1、在SD卡上根据传入的目录名创建目录  createSDDir
 * 2、在创建上目录后可以在该目录上创建文件    createSDFile
 * 3、SD卡上删除文件夹    deleteSDDir
 * 4、SD卡上删除文件     deleteSDFile
 * 5、检测文件是否存在  isFileExist
 * 6、将一个InputStream写入到SD卡中   write2SDFromInput
 * 7、将一个字符流写入到SD卡 write2SDFromWrite
 * 注：如果要写入SD卡，只要调用write2SDFromInput函数即可
 * @author Administrator
 *
 */
public class FileUtils {
    final static String TAG = "FileUitls";
    private static String mSDCardFolderPath;
    private static String mImgFolderPath;
    private static String mApkFolderPath;
    private static String mCacheFolderPath;
    private static String mLogFolderPath;

    public static FileUtils getInstance() {

        return FileUtilsHolder.instance;
    }

    public void init() {
        mImgFolderPath = getImgFolderPath();
        mApkFolderPath = getApkFolderPath();
        mCacheFolderPath = getCacheFolderPath();
        mLogFolderPath = getLogFolderPath();

        mkdirs(mImgFolderPath);
        mkdirs(mApkFolderPath);
        mkdirs(mCacheFolderPath);
        mkdirs(mLogFolderPath);
    }

    private String getSDCardFolderPath() {
        if (TextUtils.isEmpty(mSDCardFolderPath)) {
            mSDCardFolderPath = Environment.getExternalStorageDirectory().getPath() + "/CacheDemo";
        }

        return mSDCardFolderPath;
    }

    public File mkdirs(@NonNull String path) {
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }

        return file;
    }

    public String getCacheFolderPath() {
        if (TextUtils.isEmpty(mCacheFolderPath)) {
            mCacheFolderPath = getSDCardFolderPath() + "/Cache/";
        }
        return mCacheFolderPath;
    }

    public String getImgFolderPath() {
        if (TextUtils.isEmpty(mImgFolderPath)) {
            mImgFolderPath = getSDCardFolderPath() + "/Img/";
        }
        return mImgFolderPath;
    }

    public String getApkFolderPath() {
        if (TextUtils.isEmpty(mApkFolderPath)) {
            mApkFolderPath = getSDCardFolderPath() + "/Apk/";
        }
        return mApkFolderPath;
    }

    public String getLogFolderPath() {
        if (TextUtils.isEmpty(mLogFolderPath)) {
            mLogFolderPath = getSDCardFolderPath() + "/Log/";
        }
        return mLogFolderPath;
    }

    public File getCacheFolder(){
       return mkdirs(getCacheFolderPath());
    }

    private static class FileUtilsHolder {
        private static FileUtils instance = new FileUtils();
    }




    /**
     * SD卡上创建目录
     */
    public static File createSDDir(String dirName){
        File dir = new File(dirName);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("createSDDir " + dirName);
        }
        return dir;
    }
    /**
     * SD卡上创建文件
     */
    public static File createSDFile(String fileName)throws IOException {
        File file = new File(fileName);
//        System.out.println("createSDFile " + fileName);
        file.createNewFile();
        return file;
    }

    /**
     * SD卡上删除文件
     */
    public static File deleteSDFile(String fileName)throws IOException{
        File file = new File(fileName);
//        System.out.println("deleteSDFile " + fileName);
        file.delete();
        return file;
    }
    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     * @param dir 将要删除的文件目录
     * @return boolean
     */
    public static boolean deleteDir(File dir) {
        System.out.println("####deleteDir#cache#######"+dir.getPath());
        if (dir.isDirectory()) {
            String[] children = dir.list();
            //递归删除目录中的子目录下
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return true;
        }
        //目录此时为空，可以删除
        return dir.delete();
    }


    /**
     * 判断SD卡上的文件是否存在
     */
    public static boolean isFileExist(String fileName){
        File file = new File(fileName);
        return file.exists();
    }
    /**
     * 将一个InputStream字节流写入到SD卡中
     */
    public static File write2SDFromInput(String Path, String FileName, InputStream input){
        File file = null;
        OutputStream output = null; //创建一个写入字节流对象
        try{
            createSDDir(Path);  //根据传入的路径创建目录
            file = createSDFile(Path + FileName); //根据传入的文件名创建
            output = new FileOutputStream(file);
            byte buffer[] = new byte[4 * 1024]; //每次读取4K
            int num = 0;    //需要根据读取的字节大小写入文件
            while((num = (input.read(buffer))) != -1){
                output.write(buffer, 0, num);
            }
            output.flush();//清空缓存
        }catch (Exception e) {
            e.printStackTrace();
            Log.v(TAG, "#######" + FileName + "##write2SDFromInput失败###");
        }
        finally{
            try{
                output.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 把传入的字符流写入到SD卡中
     * @param Path
     * @param FileName
     * @param input
     * @return
     */
    public static File write2SDFromWrite(String Path, String FileName, BufferedReader input){
        File file = null;
        FileWriter output = null; //创建一个写入字符流对象
        BufferedWriter bufw = null;
        try{
            createSDDir(Path);  //根据传入的路径创建目录
            file = createSDFile(Path + FileName); //根据传入的文件名创建
            output = new FileWriter(file);
            bufw = new BufferedWriter(output);
            String line = null;
            while((line = (input.readLine())) != null){
                System.out.println("line = " + line);
                bufw.write(line);
                bufw.newLine();
            }
            bufw.flush();//清空缓存
        }catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            try{
                bufw.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static boolean checkNeedDowloadFile(String fileName, long time) {
        File file = new File(fileName);
        if (!file.exists()) {
            return false;
        }
        long lastModifiedTime = file.lastModified();
        long nowTime = new Date().getTime();
        return (nowTime - lastModifiedTime < time);
    }
}
