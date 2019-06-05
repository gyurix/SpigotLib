package gyurix.datareader;

import gyurix.protocol.event.PacketInType;
import gyurix.protocol.wrappers.inpackets.PacketPlayInChat;
import gyurix.spigotlib.ChatAPI;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

import static gyurix.spigotlib.Main.pl;
import static gyurix.spigotlib.SU.tp;

public class ChatGUI extends DataReader<String> {
  public ChatGUI(Player player, String msg, Consumer<String> onResult) {
    super(player, onResult);
    if (msg != null)
      ChatAPI.sendJsonMsg(ChatAPI.ChatMessageType.SYSTEM, msg, player);
    tp.registerIncomingListener(pl, this, PacketInType.Chat);
  }

  @Override
  protected boolean onPacket(Object packet) {
    PacketPlayInChat p = new PacketPlayInChat();
    p.loadVanillaPacket(packet);
    onResult(p.message);
    return true;
  }
}
