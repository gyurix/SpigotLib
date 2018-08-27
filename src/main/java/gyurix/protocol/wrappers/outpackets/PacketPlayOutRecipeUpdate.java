package gyurix.protocol.wrappers.outpackets;

import gyurix.protocol.event.PacketOutType;
import gyurix.protocol.wrappers.WrappedPacket;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PacketPlayOutRecipeUpdate extends WrappedPacket {
    public List<Recipe> recipes = new ArrayList<>();

    public PacketPlayOutRecipeUpdate() {
    }

    public PacketPlayOutRecipeUpdate(Recipe... recipes) {
        this.recipes.addAll(Arrays.asList(recipes));
    }

    public static Recipe fromNMSRecipe(Object r) {
        //TODO
        return null;
    }

    public static List<Recipe> fromNMSRecipeList(Iterable<Object> recipes) {
        List<Recipe> out = new ArrayList<>();
        recipes.forEach(r -> out.add(fromNMSRecipe(r)));
        return out;
    }

    public static Object toNMSRecipe(Recipe r) {
        //TODO
        return null;
    }

    public static List<Object> toNMSRecipeList(Iterable<Recipe> recipes) {
        List<Object> out = new ArrayList<>();
        recipes.forEach(r -> out.add(toNMSRecipe(r)));
        return out;
    }

    @Override
    public Object getVanillaPacket() {
        return PacketOutType.RecipeUpdate.newPacket(toNMSRecipeList(recipes));
    }

    @Override
    public void loadVanillaPacket(Object obj) {
        Object[] data = PacketOutType.RecipeUpdate.getPacketData(obj);
        recipes = fromNMSRecipeList((Iterable<Object>) data[0]);
    }
}
