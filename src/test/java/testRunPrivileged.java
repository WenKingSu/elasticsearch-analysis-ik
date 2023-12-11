import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.wltea.analyzer.dic.Dictionary;

import java.io.IOException;

public class testRunPrivileged {
    private static CloseableHttpClient httpclient = HttpClients.createDefault();
    /*
     * 上次更改時間
     */
    private static String last_modified;
    /*
     * 資源屬性
     */
    private static String eTags;

    /*
     * 請求地址
     */
    private static final String location = "http://localhost/rest/es/reload";

    public static void main(String[] args) {
        //超時設定
        RequestConfig rc = RequestConfig.custom().setConnectionRequestTimeout(10 * 1000)
                .setConnectTimeout(10 * 1000).setSocketTimeout(15 * 1000).build();

        HttpHead head = new HttpHead(location);
        head.setConfig(rc);

        //設定請求頭
        if (last_modified != null) {
            head.setHeader("If-Modified-Since", last_modified);
        }
        if (eTags != null) {
            head.setHeader("If-None-Match", eTags);
        }

        HttpGet get = new HttpGet(location);
        get.setConfig(rc);

        CloseableHttpResponse response = null;
        try {

//            response = httpclient.execute(head);
            response = httpclient.execute(get);

            //返回200 才做操作
            if (response.getStatusLine().getStatusCode() == 200) {

                if (((response.getLastHeader("Last-Modified") != null) && !response.getLastHeader("Last-Modified").getValue().equalsIgnoreCase(last_modified))
                        || ((response.getLastHeader("ETag") != null) && !response.getLastHeader("ETag").getValue().equalsIgnoreCase(eTags))) {

                    // 遠端詞庫有更新,需要重新載入詞典，並修改last_modified,eTags
                    Dictionary.getSingleton().reLoadMainDict();
                    last_modified = response.getLastHeader("Last-Modified") == null ? null : response.getLastHeader("Last-Modified").getValue();
                    eTags = response.getLastHeader("ETag") == null ? null : response.getLastHeader("ETag").getValue();
                }
            } else if (response.getStatusLine().getStatusCode() == 304) {
                //沒有修改，不做操作
                //noop
            } else {
                System.out.printf("remote_ext_dict %s return bad code %s\n", location, response.getStatusLine().getStatusCode());
            }

        } catch (Exception e) {
            System.out.printf("remote_ext_dict %s error!", e, location);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                System.out.println(e);
                System.out.println(e.getMessage());
            }
        }
    }
}
