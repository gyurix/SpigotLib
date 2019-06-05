package gyurix.chat;

import com.google.common.collect.Lists;
import gyurix.json.JsonAPI;
import gyurix.json.JsonSettings;
import gyurix.spigotlib.ChatAPI;
import gyurix.spigotlib.SU;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;

import java.util.ArrayList;

import static gyurix.spigotutils.NullUtils.to0;

public class ChatTag {
  @JsonSettings(defaultValue = "false")
  public boolean bold, italic, underlined, strikethrough, obfuscated;
  public ChatClickEvent clickEvent;
  public ChatColor color;
  public ArrayList<ChatTag> extra;
  public ChatHoverEvent hoverEvent;
  public ChatScoreData score;
  public String text, translate, selector, insertion;
  public ArrayList<ChatTag> with;

  public ChatTag() {

  }

  public ChatTag(String in) {
    text = in;
  }

  public static ChatTag fromBaseComponent(BaseComponent comp) {
    ChatTag tag = new ChatTag();
    net.md_5.bungee.api.ChatColor color = comp.getColorRaw();
    if (color != null)
      tag.color = ChatColor.valueOf(color.name().toLowerCase());
    tag.obfuscated = to0(comp.isObfuscatedRaw());
    tag.bold = to0(comp.isObfuscatedRaw());
    tag.strikethrough = to0(comp.isStrikethroughRaw());
    tag.underlined = to0(comp.isUnderlinedRaw());
    tag.italic = to0(comp.isItalicRaw());
    if (comp instanceof TextComponent) {
      tag.text = ((TextComponent) comp).getText();
    } else if (comp instanceof TranslatableComponent) {
      TranslatableComponent tc = (TranslatableComponent) comp;
      tag.translate = tc.getTranslate();
      tag.with = new ArrayList<>();
      tc.getWith().forEach(c -> tag.with.add(ChatTag.fromBaseComponent(c)));
    }
    if (comp.getHoverEvent() != null)
      tag.hoverEvent = new ChatHoverEvent(comp.getHoverEvent());
    if (comp.getClickEvent() != null)
      tag.clickEvent = new ChatClickEvent(comp.getClickEvent());
    tag.insertion = comp.getInsertion();
    if (comp.getExtra() != null) {
      tag.extra = new ArrayList<>();
      for (BaseComponent c : comp.getExtra())
        tag.extra.add(ChatTag.fromBaseComponent(c));
    }
    return tag;
  }

  public static ChatTag fromBaseComponents(BaseComponent[] comps) {
    if (comps.length == 1)
      return fromBaseComponent(comps[0]);
    ChatTag tag = new ChatTag("");
    tag.extra = new ArrayList<>();
    for (BaseComponent c : comps)
      tag.extra.add(ChatTag.fromBaseComponent(c));
    return tag;
  }

  public static ChatTag fromColoredText(String colText) {
    colText = SU.optimizeColorCodes(colText);
    ArrayList<ChatTag> ctl = new ArrayList<>();
    ChatTag ct = new ChatTag();
    boolean col = false;
    StringBuilder sb = new StringBuilder();
    for (char c : colText.toCharArray()) {
      if (col) {
        ChatColor color = ChatColor.forId(c);
        if (color == ChatColor.reset)
          color = ChatColor.white;
        if (sb.length() != 0) {
          ct.text = sb.toString();
          sb.setLength(0);
          ctl.add(ct);
          ct = color.isFormat() ? ct.cloneFormat(new ChatTag()) : new ChatTag();
        }
        if (color.isFormat()) {
          switch (color) {
            case obfuscated:
              ct.obfuscated = true;
              break;
            case bold:
              ct.bold = true;
              break;
            case strikethrough:
              ct.strikethrough = true;
              break;
            case underline:
              ct.underlined = true;
              break;
            case italic:
              ct.italic = true;
              break;
          }
        } else {
          ct.color = color;
        }
        col = false;
      } else {
        if (c == '§')
          col = true;
        else
          sb.append(c);
      }
    }
    if (col)
      sb.append('§');
    ct.text = sb.toString();
    ctl.add(ct);
    return fromSeveralTag(ctl);
  }

  public static ChatTag fromExtraText(String extraText) {
    String[] parts = extraText.split("\\\\\\|");
    ArrayList<ChatTag> tags = new ArrayList<>();
    for (String part : parts) {
      String[] sa = part.split("\\\\-");
      ChatTag tag = fromColoredText(SU.optimizeColorCodes(sa[0]));
      for (int i = 1; i < sa.length; i++) {
        if (sa[i].length() > 0)
          tag.setExtra(sa[i].charAt(0), SU.optimizeColorCodes(sa[i].substring(1)));
      }
      tags.add(tag);
    }
    return fromSeveralTag(tags);
  }

  public static ChatTag fromICBC(Object icbc) {
    return JsonAPI.deserialize(ChatAPI.toJson(icbc), ChatTag.class);
  }

  public static ChatTag fromSeveralTag(ArrayList<ChatTag> ctl) {
    if (ctl.size() == 1)
      return ctl.iterator().next();
    ChatTag out = new ChatTag("");
    out.extra = ctl;
    return out;
  }

  public static String stripExtras(String extraText) {
    String[] parts = extraText.split("\\\\\\|");
    StringBuilder out = new StringBuilder();
    for (String p : parts) {
      out.append(p.split("\\\\-")[0]);
    }
    return out.toString();
  }

  public ChatTag cloneFormat(ChatTag target) {
    target.bold = bold;
    target.italic = italic;
    target.underlined = underlined;
    target.strikethrough = strikethrough;
    target.obfuscated = obfuscated;
    target.color = color;
    return target;
  }

  public String getFormatPrefix() {
    StringBuilder pref = new StringBuilder();
    if (color != null)
      pref.append('§').append(color.id);
    if (obfuscated)
      pref.append("§k");
    if (bold)
      pref.append("§l");
    if (strikethrough)
      pref.append("§m");
    if (underlined)
      pref.append("§n");
    if (italic)
      pref.append("§o");
    return pref.toString();
  }

  public boolean isSimpleText() {
    return translate == null && selector == null && insertion == null && with == null && score == null && extra == null &&
            !(bold == italic == underlined == strikethrough == obfuscated) && color == null && clickEvent == null && hoverEvent == null;
  }

  /**
   * Sets an extra data for this ChatTag. Available extra types:
   * # - Translation
   * \@ - Selector
   * A - Hover event, show achievement
   * D - Click event, twitch user data
   * E - Hover event, show entity
   * F - Click event, open file
   * I - Hover event, show item
   * P - Click event, change page
   * R - Click event, run command
   * S - Click event, suggest command
   * T - Hover event, show text
   * U - Click event, open url
   * W - Translate with
   * + - Insertion (on shift click)
   *
   * @param extraType The character representing the extra type
   * @param value     A String representing the value of this extra
   * @return This chat tag
   */
  public ChatTag setExtra(char extraType, String value) {
    switch (extraType) {
      case '#':
        translate = value;
        return this;
      case 'W':
        with.add(ChatTag.fromColoredText(value));
        return this;
      case '@':
        selector = value;
        return this;
      case '+':
        insertion = value;
        return this;
    }
    ChatHoverEventType he = ChatHoverEventType.forId(extraType);
    if (he != null) {
      hoverEvent = new ChatHoverEvent(he, value);
      return this;
    }
    ChatClickEventType ce = ChatClickEventType.forId(extraType);
    if (ce != null)
      clickEvent = new ChatClickEvent(ce, value);
    return this;
  }

  public BaseComponent toBaseComponent() {
    BaseComponent out = translate != null ? toTranslatableComponent() : toTextComponent();
    if (obfuscated)
      out.setObfuscated(obfuscated);
    if (bold)
      out.setBold(bold);
    if (strikethrough)
      out.setStrikethrough(strikethrough);
    if (underlined)
      out.setUnderlined(underlined);
    if (italic)
      out.setItalic(italic);
    if (color != null)
      out.setColor(color.toSpigotChatColor());
    if (clickEvent != null)
      out.setClickEvent(clickEvent.toSpigotClickEvent());
    if (hoverEvent != null)
      out.setHoverEvent(hoverEvent.toSpigotHoverEvent());
    if (insertion != null)
      out.setInsertion(insertion);
    if (extra != null)
      extra.forEach(e -> out.addExtra(e.toBaseComponent()));
    return out;
  }

  public BaseComponent[] toBaseComponents() {
    if (extra == null) {
      return new BaseComponent[]{toBaseComponent()};
    }
    BaseComponent[] baseComponents = new BaseComponent[extra.size()];
    for (int i = 0; i < extra.size(); ++i) {
      baseComponents[i] = extra.get(i).toBaseComponent();
    }
    return baseComponents;
  }

  public String toColoredString() {
    ArrayList<ChatTag> tags = extra == null ? Lists.newArrayList(this) : extra;
    StringBuilder out = new StringBuilder();
    for (ChatTag tag : tags) {
      if (tag.text != null) {
        out.append(tag.getFormatPrefix());
        out.append(tag.text);
      } else if (tag.translate != null) {
        out.append(tag.translate);
      }
    }
    return SU.optimizeColorCodes(out.toString());
  }

  public String toExtraString() {
    ArrayList<ChatTag> tags = extra == null ? Lists.newArrayList(this) : extra;
    StringBuilder out = new StringBuilder();
    for (ChatTag tag : tags) {
      if (tag.extra != null) {
        for (ChatTag t : tag.extra) {
          out.append("\\|").append(t.toExtraString());
        }
        continue;
      }
      out.append("\\|");
      out.append(tag.getFormatPrefix());
      if (tag.text != null)
        out.append(tag.text);
      if (tag.selector != null)
        out.append("\\-@").append(tag.selector);
      if (tag.translate != null)
        out.append("\\-#").append(tag.translate);
      if (tag.with != null) {
        StringBuilder sb2 = new StringBuilder();
        tag.with.forEach(w -> sb2.append(w.toColoredString()));
        out.append(SU.optimizeColorCodes(sb2.toString()));
      }
      if (tag.hoverEvent != null)
        out.append("\\-").append(tag.hoverEvent.action.id).append(tag.hoverEvent.value.toColoredString());
      if (tag.clickEvent != null)
        out.append("\\-").append(tag.clickEvent.action.id).append(tag.clickEvent.value);
      if (tag.insertion != null)
        out.append("\\-+").append(tag.insertion);
    }
    return out.substring(2);
  }

  public String toFormatlessString() {
    ArrayList<ChatTag> tags = extra == null ? Lists.newArrayList(this) : extra;
    StringBuilder out = new StringBuilder();
    for (ChatTag tag : tags) {
      if (tag.text != null)
        out.append(tag.text);
    }
    return out.toString();
  }

  public Object toICBC() {
    return ChatAPI.toICBC(JsonAPI.serialize(this));
  }

  @Override
  public String toString() {
    return JsonAPI.serialize(this);
  }

  private TextComponent toTextComponent() {
    return new TextComponent(text);
  }

  private TranslatableComponent toTranslatableComponent() {
    TranslatableComponent out = new TranslatableComponent(translate);
    with.forEach(t -> out.addWith(t.toBaseComponent()));
    return out;
  }
}

