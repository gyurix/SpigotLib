package gyurix.test;

import java.util.HashMap;

/**
 * Created by GyuriX on 2016. 07. 31..
 */
public class TestObj {
    HashMap<String, CompressedData> fullCompression = new HashMap<>();
    String name;
    HashMap<String, PartialCompress> partialCompress = new HashMap<>();

    public TestObj() {
        fullCompression.put("alma", new CompressedData(1, 233, "Hello"));
        fullCompression.put("k█rte", new CompressedData(150, 23, "◄██►"));
        partialCompress.put("--", new PartialCompress(45, "This data is compressed"));
        partialCompress.put("-HIHO-", new PartialCompress(99, "This data IS NOW compressed"));
    }
}
