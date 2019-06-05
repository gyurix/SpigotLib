package gyurix.protocol.manager;

import gyurix.protocol.Protocol;
import gyurix.protocol.Reflection;
import gyurix.protocol.event.PacketInEvent;
import gyurix.protocol.event.PacketInType;
import gyurix.protocol.event.PacketOutEvent;
import gyurix.protocol.wrappers.WrappedPacket;
import gyurix.spigotlib.SU;
import gyurix.spigotutils.EntityUtils;
import net.minecraft.util.com.google.common.collect.MapMaker;
import net.minecraft.util.com.mojang.authlib.GameProfile;
import net.minecraft.util.io.netty.channel.*;
import net.minecraft.util.org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static gyurix.protocol.Reflection.*;
import static gyurix.spigotlib.Main.pl;

public final class ProtocolLegacyImpl extends Protocol {
  static final Field getGameProfile = getFirstFieldOfType(getNMSClass("PacketLoginInStart"), GameProfile.class),
          playerConnectionF = getField(getNMSClass("EntityPlayer"), "playerConnection"),
          networkManagerF = getField(getNMSClass("PlayerConnection"), "networkManager"),
          channelF = getField(getNMSClass("NetworkManager"), "channel");
  private static final Map<String, Channel> channelLookup = new MapMaker().weakValues().makeMap();
  private static final Class minecraftServerClass = getNMSClass("MinecraftServer");
  private static final Class serverConnectionClass = getNMSClass("ServerConnection");
  private static Object oldH;
  private static Field oldHChildF;
  private ChannelFuture cf;

  public ProtocolLegacyImpl() {
  }

  @Override
  public Channel getChannel(Player plr) {
    Channel c = channelLookup.get(plr.getName());
    if (c == null)
      try {
        Object nmsPlayer = EntityUtils.getNMSEntity(plr);
        Object playerConnection = playerConnectionF.get(nmsPlayer);
        Object networkManager = networkManagerF.get(playerConnection);
        Channel channel = (Channel) channelF.get(networkManager);
        channelLookup.put(plr.getName(), c = channel);
      } catch (Throwable e) {
        SU.error(SU.cs, e, "SpigotLib", "gyurix");
      }
    return c;
  }

  @Override
  public Player getPlayer(Object channel) {
    ClientChannelHook ch = ((Channel) channel).pipeline().get(ClientChannelHook.class);
    if (ch == null)
      return null;
    return ch.player;
  }

  @Override
  public void init() throws Throwable {
    Object minecraftServer = getFirstFieldOfType(Reflection.getOBCClass("CraftServer"), minecraftServerClass).get(SU.srv);
    Object serverConnection = getFirstFieldOfType(minecraftServerClass, serverConnectionClass).get(minecraftServer);
    cf = (ChannelFuture) ((List) getFirstFieldOfType(serverConnectionClass, List.class).get(serverConnection)).iterator().next();
    registerServerChannelHook();
    SU.srv.getOnlinePlayers().forEach(this::injectPlayer);
  }

  @Override
  public void injectPlayer(final Player plr) {
    getChannel(plr).pipeline().get(ClientChannelHook.class).player = plr;
  }

  @Override
  public void printPipeline(Iterable<Map.Entry<String, ?>> pipeline) {
    ArrayList<String> list = new ArrayList<>();
    pipeline.forEach((e) -> list.add(e.getKey()));
    SU.cs.sendMessage("§ePipeline: §f" + StringUtils.join(list, ", "));
  }

  @Override
  public void receivePacket(Object channel, Object packet) {
    if (packet instanceof WrappedPacket)
      packet = ((WrappedPacket) packet).getVanillaPacket();
    ((Channel) channel).pipeline().context("encoder").fireChannelRead(packet);
  }

  @Override
  public void registerServerChannelHook() throws Throwable {
    Channel serverCh = cf.channel();
    oldH = serverCh.pipeline().get(Reflection.getUtilClass("io.netty.bootstrap.ServerBootstrap$ServerBootstrapAcceptor"));
    oldHChildF = Reflection.getField(oldH.getClass(), "childHandler");
    serverCh.pipeline().addFirst("SpigotLibServer", new ServerChannelHook((ChannelHandler) oldHChildF.get(oldH)));
  }

  @Override
  public void removeHandler(Object ch, String handler) {
    try {
      ((Channel) ch).pipeline().remove(handler);
    } catch (Throwable ignored) {
    }
  }

  @Override
  public void sendPacket(Object channel, Object packet) {
    if (channel == null || packet == null) {
      SU.error(SU.cs, new RuntimeException("§cFailed to send packet " + packet + " to channel " + channel), "SpigotLib", "gyurix");
      return;
    }
    if (packet instanceof WrappedPacket)
      packet = ((WrappedPacket) packet).getVanillaPacket();
    ((Channel) channel).pipeline().writeAndFlush(packet);
  }

  @Override
  public void unregisterServerChannelHandler() throws IllegalAccessException {
    removeHandler(cf.channel(), "SpigotLibServer");
  }


  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerJoin(PlayerJoinEvent e) {
    injectPlayer(e.getPlayer());
  }

  @EventHandler(priority = EventPriority.LOWEST)
  public void onPlayerLogin(PlayerLoginEvent e) {
    injectPlayer(e.getPlayer());
  }

  @EventHandler(priority = EventPriority.MONITOR)
  public void onPlayerQuit(final PlayerQuitEvent e) {
    final String pln = e.getPlayer().getName();
    SU.sch.scheduleSyncDelayedTask(pl, () -> {
      Player p = Bukkit.getPlayer(pln);
      if (p == null)
        channelLookup.remove(pln);
    });
  }

  public class ClientChannelHook extends ChannelDuplexHandler {
    public Player player;

    public void channelRead(ChannelHandlerContext ctx, Object packet) throws Exception {
      try {
        Channel channel = ctx.channel();
        PacketInEvent e = new PacketInEvent(channel, player, packet);
        if (e.getType() == PacketInType.LoginInStart) {
          GameProfile profile = (GameProfile) getGameProfile.get(packet);
          channelLookup.put(profile.getName(), channel);
        }
        dispatchPacketInEvent(e);
        packet = e.getPacket();
        if (!e.isCancelled())
          ctx.fireChannelRead(packet);
      } catch (Throwable e) {
        SU.error(SU.cs, e, "SpigotLib", "gyurix");
      }
    }

    public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
      try {
        PacketOutEvent e = new PacketOutEvent(ctx.channel(), player, packet);
        dispatchPacketOutEvent(e);
        packet = e.getPacket();
        if (!e.isCancelled())
          ctx.write(packet, promise);
      } catch (Throwable e) {
        SU.error(SU.cs, e, "SpigotLib", "gyurix");
      }
    }
  }

  public class ServerChannelHook extends ChannelInboundHandlerAdapter {
    public final ChannelHandler childHandler;

    public ServerChannelHook(ChannelHandler childHandler) {
      this.childHandler = childHandler;
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
      if (childHandler.getClass().getName().equals("lilypad.bukkit.connect.injector.NettyChannelInitializer"))
        Reflection.getField(childHandler.getClass(), "oldChildHandler").set(childHandler, oldHChildF.get(oldH));
      Channel c = (Channel) msg;
      c.pipeline().addLast("SpigotLibInit", new ChannelInboundHandlerAdapter() {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
          ChannelPipeline pipeline = ctx.pipeline();
          pipeline.remove("SpigotLibInit");
          pipeline.addBefore("packet_handler", "SpigotLib", new ClientChannelHook());
          ctx.fireChannelRead(o);
        }
      });
      ctx.fireChannelRead(msg);
    }
  }
}

