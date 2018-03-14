package gyurix.test;

import gyurix.configfile.ConfigSerialization.ConfigOptions;

/**
 * Created by GyuriX on 2016. 07. 31..
 */
public class PartialCompress {
    @ConfigOptions(compress = true)
    String compressed;
    int id;

    public PartialCompress(int id, String s) {
        this.id = id;
        compressed = s;
    }
}
