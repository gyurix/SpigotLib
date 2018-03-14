package gyurix.animation;

public interface CustomEffect {
    CustomEffect clone();

    String getText();

    void setText(String var1);

    String next(String var1);
}

