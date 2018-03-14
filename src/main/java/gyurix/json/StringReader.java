package gyurix.json;

public class StringReader {
    public int id;
    public char[] str;

    public StringReader(String in) {
        str = in.toCharArray();
    }

    public boolean hasNext() {
        return id < str.length;
    }

    public char last() {
        return str[id - 1];
    }

    public char next() {
        return str[id++];
    }
}

