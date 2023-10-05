import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.introduction.downloader.Constants;
import com.jetswift.io.utils.FileUtils;

public class Main {
    String googleVideo = Constants.GOOGLE_VIDEO;

    public static void main(String[] args) {
        System.out.println("Hello, i'll download some images...");
        String[] pictureUrls = Constants.PICTURE_URLS;
        PicturesDownloader picturesDownloader = new PicturesDownloader(pictureUrls);
        System.out.println(picturesDownloader.downloadPictures());
    }

    private static class PicturesDownloader {
        private volatile String[] pictureUrls;

        private PicturesDownloader() {

        }

        public PicturesDownloader(final String[] pictureUrls) {
            this();
            if (pictureUrls != null) this.pictureUrls = pictureUrls;
        }

        public String downloadPictures() {
            var wrapper = new Object() {
                private volatile String response = "";
                private volatile URI uri = null;
                private volatile File childFile = null, parentFile = null;
            };
            try (ExecutorService executorService = Executors.newFixedThreadPool(1)) {
                executorService.execute(() -> {
                    for (String pictureUrl : pictureUrls) {
                        if (pictureUrl != null && pictureUrl.length() > 0) {
                            try {
                                wrapper.uri = new URI(pictureUrl);
                                URL url = wrapper.uri.toURL();
                                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                                connection.setRequestMethod("GET");
                                connection.setRequestProperty("Connection", "Keep-Alive");
                                connection.setRequestProperty("Keep-Alive", "header");
                                connection.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                                connection.setRequestProperty("Accept-Language", "en-US, en;q=0.9,mt;q=0.8");
                                connection.setRequestProperty("Accept-Encoding", "gzip,deflate,br");
                                connection.setRequestProperty("Host", FileUtils.getDomainName(pictureUrl));
                                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/117.0.0.0 Safari/537.36");
                                connection.setRequestProperty("http.agent", "Chrome/117.0.0.0 (Windows NT 10.0; Win64; x64)");
                                connection.setConnectTimeout(5000);
                                connection.setReadTimeout(30000);
                                connection.setDoOutput(true);
                                connection.connect();
                                InputStream inputStream = connection.getInputStream();
                                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream, 1024 * 5);
                                wrapper.parentFile = new File("D:\\Java\\projects\\introduction\\ImageDownloader\\assets\\images");
                                if (!wrapper.parentFile.exists()) wrapper.parentFile.mkdirs();
                                wrapper.childFile = new File(wrapper.parentFile, FileUtils.getFileNameWithExtension(pictureUrl));
                                if (!wrapper.childFile.exists()) {
                                    try (FileOutputStream fileOutputStream = new FileOutputStream(wrapper.childFile)) {
                                        byte[] buffer = new byte[5 * 1024];
                                        int length;
                                        while ((length = bufferedInputStream.read(buffer)) != -1) {
                                            fileOutputStream.write(buffer, 0, length);
                                        }
                                        fileOutputStream.flush();
                                        fileOutputStream.close();
                                        bufferedInputStream.close();
                                        inputStream.close();
                                    }
                                }
                            } catch (URISyntaxException | IOException e) {
                                e.printStackTrace();
                                wrapper.response = e.getMessage();
                                System.err.println(e.getMessage());
                                System.err.println("Error");
                            }
                        }
                    }
                });
                executorService.shutdown();
            }
            return wrapper.response;
        }
    }
}