package gyurix.mojang;

import gyurix.json.JsonAPI;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import static gyurix.mojang.WebApi.post;
import static gyurix.mojang.WebApi.postWithHeader;

/**
 * Created by GyuriX on 2016. 06. 10..
 */
public class ClientSession {
    public String accessToken, clientToken, username, password;
    public ArrayList<ClientProfile> availableProfiles;
    public ClientProfile selectedProfile;

    public void changeSkin(String newSkinURL, boolean slimModel) {
        try {
            postWithHeader("https://api.mojang.com/user/profile/" + selectedProfile.id + "/skin", "Authorization", "Bearer " + accessToken,
                    "model=" + (slimModel ? "slim&url=" : "&url=") + URLEncoder.encode(newSkinURL, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public boolean invalidate() {
        try {
            post("https://authserver.mojang.com/invalidate",
                    "{\"accessToken\": \"" + accessToken + "\"," +
                            "\"clientToken\": \"" + clientToken + "\"}");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public boolean refresh() {
        try {
            String s = post("https://authserver.mojang.com/refresh",
                    "{\"accessToken\": \"" + accessToken + "\"," +
                            "\"clientToken\": \"" + clientToken + "\"}");
            ClientSession newClient = JsonAPI.deserialize(s, ClientSession.class);
            accessToken = newClient.accessToken;
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public boolean signOut() {
        try {
            post("https://authserver.mojang.com/signout",
                    "{\"username\": \"" + username + "\"," +
                            "\"password\": \"" + password + "\"}");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public boolean uploadSkin(File f, boolean slimModel) {
        try {
            MultipartUtility mu = new MultipartUtility("https://api.mojang.com/user/profile/" + selectedProfile.id + "/skin", "PUT");
            mu.addHeaderField("Authorization", "Bearer " + accessToken);
            mu.addFormField("model", slimModel ? "slim" : "alex");
            mu.addFilePart("file", f);
            mu.finish();
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean validate() {
        try {
            post("https://authserver.mojang.com/validate",
                    "{\"accessToken\": \"" + accessToken + "\"," +
                            "\"clientToken\": \"" + clientToken + "\"}");
            return true;
        } catch (Throwable e) {
            return false;
        }
    }
}
