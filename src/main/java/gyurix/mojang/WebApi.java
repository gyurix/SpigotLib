package gyurix.mojang;

import gyurix.spigotlib.SU;
import org.apache.logging.log4j.core.util.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import static gyurix.spigotlib.Config.debug;

/**
 * Created by GyuriX on 2016. 06. 10..
 */
public class WebApi {
  /**
   * @param urlString - The URL to the file
   * @param filename  - The path to the downloaded file
   * @return True if successful otherwise false
   */
  public static boolean download(String urlString, String filename) {
    try {
      URL url = new URL(urlString);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      File f = new File(filename).getAbsoluteFile();
      f.getParentFile().mkdirs();
      FileOutputStream fos = new FileOutputStream(filename);
      SU.transloadStream(con.getInputStream(), fos);
      fos.close();
      return true;
    } catch (Throwable e) {
      SU.error(SU.cs, e, "SpigotLib", "gyurix");
      return false;
    }
  }

  public static String get(String urlString) {
    try {
      URL url = new URL(urlString);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      return IOUtils.toString(new InputStreamReader(con.getInputStream()));
    } catch (Throwable e) {
      debug.msg("Web", e);
      return null;
    }
  }

  public static String post(String urlString, String req) {
    try {
      URL url = new URL(urlString);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setDoOutput(true);
      con.setRequestMethod("POST");
      con.getOutputStream().write(req.getBytes(Charset.forName("UTF-8")));
      return IOUtils.toString(new InputStreamReader(con.getInputStream()));
    } catch (Throwable e) {
      debug.msg("Web", e);
      return null;
    }
  }

  public static String postWithHeader(String urlString, String headerKey, String headerValue, String req) {
    try {
      URL url = new URL(urlString);
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setDoOutput(true);
      con.setRequestMethod("POST");
      con.setRequestProperty("Content-Length", String.valueOf(req.length()));
      con.setRequestProperty(headerKey, headerValue);
      con.getOutputStream().write(req.getBytes(Charset.forName("UTF-8")));
      return IOUtils.toString(new InputStreamReader(con.getInputStream()));
    } catch (Throwable e) {
      debug.msg("Web", e);
      return null;
    }
  }
}
