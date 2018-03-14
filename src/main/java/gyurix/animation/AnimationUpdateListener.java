package gyurix.animation;

/**
 * Interface represanting the listener for the frames of a running animation
 */
public interface AnimationUpdateListener {
    /**
     * @param ar   - The running AnimationRunnable
     * @param text - The next of the current frame of the animation
     * @return true if the animation can continue running, false otherwise
     */
    boolean onUpdate(AnimationRunnable ar, String text);
}
