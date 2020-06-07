package utils;

import org.apache.commons.lang3.StringUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public  class UploadUtil {

    public static String uploadImage(MultipartFile file) {
        String imgUrl = "http://10.0.1.10";
        if (file != null) {
            System.out.println("multipartFile = " + file.getName() + "|" + file.getSize());
            //会获取调用者(springboot web项目)的路径下的配置文件，而不是本模块下面的配置文件
            String configFile = UploadUtil.class.getResource("/application.properties").getPath();
            try {
                ClientGlobal.init(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = null;
            try {
                //todo:上传一次文件就会创建一次连接，需要改善
                trackerServer = trackerClient.getConnection();
            } catch (Exception e) {
                e.printStackTrace();
            }
            StorageClient storageClient = new StorageClient(trackerServer, null);
            String filename = file.getOriginalFilename();
            String extName = StringUtils.substringAfterLast(filename, ".");

            String[] upload_file = new String[0];
            try {
                upload_file = storageClient.upload_file(file.getBytes(), extName, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int i = 0; i < upload_file.length; i++) {
                String path = upload_file[i];
                imgUrl += "/" + path;
            }

        }
        return imgUrl;
    }


}
