package gyurix.test;

import gyurix.configfile.ConfigSerialization.ConfigOptions;

/**
 * Created by GyuriX on 2016. 07. 31..
 */
@ConfigOptions(compress = true)
public class CompressedData {
    int hi = 5;
    int ho = 6;
    String str;

    public CompressedData(int a, int b, String c) {
        hi = a;
        ho = b;
        str = c;
    }

    @Override
    public String toString() {
        return hi + " " + ho + " " + str;
    }
}
