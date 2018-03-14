package gyurix.configfile;

/**
 * The PostLoadable interface allows objects to have a postLoad method which will be executed after loading it from configuration.
 */
public interface PostLoadable {
    void postLoad();
}
