package gyurix.inventory.trade;

import gyurix.chat.ChatTag;
import gyurix.protocol.Reflection;
import gyurix.spigotlib.SU;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Hook, which implements IMerchant interface
 */
class IMerchantHook implements InvocationHandler {
    private static final Class mrlC = Reflection.getNMSClass("MerchantRecipeList");
    private final String displayName;
    public ArrayList<Object> offers;
    public Object player;

    IMerchantHook(Object player, String title) {
        this.player = player;
        displayName = title;
        try {
            offers = (ArrayList<Object>) mrlC.newInstance();
        } catch (Throwable e) {
            SU.error(SU.cs, e, "SpigotLib", "gyurix");
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        switch (method.getName()) {
            case "t_":
                return player;
            case "setTradingPlayer":
                player = args[0];
                return null;
            case "getScoreboardDisplayName":
                return new ChatTag(displayName).toICBC();
            case "getOffers":
                return offers;
        }
        return null;
    }
}
