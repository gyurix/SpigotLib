```

  ██████  ██▓███   ██▓  ▄████  ▒█████  ▄▄▄█████▓    ██▓     ██▓ ▄▄▄▄   
▒██    ▒ ▓██░  ██▒▓██▒ ██▒ ▀█▒▒██▒  ██▒▓  ██▒ ▓▒   ▓██▒    ▓██▒▓█████▄ 
░ ▓██▄   ▓██░ ██▓▒▒██▒▒██░▄▄▄░▒██░  ██▒▒ ▓██░ ▒░   ▒██░    ▒██▒▒██▒ ▄██
  ▒   ██▒▒██▄█▓▒ ▒░██░░▓█  ██▓▒██   ██░░ ▓██▓ ░    ▒██░    ░██░▒██░█▀  
▒██████▒▒▒██▒ ░  ░░██░░▒▓███▀▒░ ████▓▒░  ▒██▒ ░    ░██████▒░██░░▓█  ▀█▓
▒ ▒▓▒ ▒ ░▒▓▒░ ░  ░░▓   ░▒   ▒ ░ ▒░▒░▒░   ▒ ░░      ░ ▒░▓  ░░▓  ░▒▓███▀▒
░ ░▒  ░ ░░▒ ░      ▒ ░  ░   ░   ░ ▒ ▒░     ░       ░ ░ ▒  ░ ▒ ░▒░▒   ░ 
░  ░  ░  ░░        ▒ ░░ ░   ░ ░ ░ ░ ▒    ░           ░ ░    ▒ ░ ░    ░ 
      ░            ░        ░     ░ ░                  ░  ░ ░   ░      
                                                                     ░ 

```

# Introduction

SpigotLib is a collection of several useful libraries and APIs
for Spigot plugin development. It's not only perfect for developers,
but it's also good for server owners, because they can use it's API
testing commands, which are really useful.


|`Important links`|
|---|
[**Official Discord Group**](https://discord.gg/Jr3bKpq) |
[**Official Download Page**](https://www.spigotmc.org/resources/spigotlib.5925/) |
[**Online JavaDoc**](https://gyurix.github.io/SpigotLib/) |

# Why should you use SpigotLib?
If you are a **Minecraft server owner**, then you should use it for one of the following purposes:
1. You would like to use a plugin on your server which depends on it
2. For using some of it's commands by server owners, admins. You may even put them to a sign or an NPC with other plugins, what can be very helpful
3. For getting nice, colored and meaningful error reports encoded by the SpigotLibs CommandAPI instead of the old boring internal server error chat messages.

If you are a **Spigot plugin developer**, then you should use it for one of the following purposes:
1. For coding plugins up to **2x** faster
2. For using already perfectly working solutions for common plugin development problems
3. For hooking in to other plugins indirectly
4. To avoid the possibility of so many bugs appearing in your plugin
5. For coding plugins working on the packet level super easily

# Troubleshooting Issues

If the plugin does not work for you then please do not panic, do **NOT** leave a bad review, or a comment, just read and follow these steps:

0. **Before posting the new update I always fully test the plugin**, so it's verified that it works fine, which means that in the most of the cases it should also work at you without any issues. The only reason of malfunction can be the differences between my and your testing enviroment, so in the next points I will explain you what kind of testing enviroment differences exists

1. **Incorrect server version:** Please use the Spigots BuildTools to build yourself one of the following server versions: 1.8.8; 1.9; 1.9.2; 1.9.4; 1.10; 1.10.2; 1.11; 1.11.2; 1.12; 1.12.2; 1.13; 1.13.1; 1.14; 1.14.4; 1.15; 1.15.1

    For any other version there is no guarantee that SpigotLib will work without any issues. On 1.7.10 and older servers the SpigotLib works might only work in a limited "safe" mode, which means that only some of it's funtionalities will work, which does not directly depend on the Spigot API.

2. **Using other plugins which causes issues:**
 
    I will list you a few already known problematic plugins, of which some versions might cause issues:

  - **ChatControl plugins packet related features:** Please stop your server and disable these features in ChatControl plugins configuration
  
  - **ProtocolSupport, ProtocolLib, ViaVersion:** All of these plugins works with packets, so it's possible that some of them causes compatibility issues with SpigotLib. To resolve these issues it's suggested to make a new test server using just SpigotLib, and try to add these 3 plugins to it step by step. After these are added and working properly you may add the other plugins to your server. If they are not working, then check if you are using the correct versions of both of these plugins.

    I have tested and verified already, that using the right versions of ProtocolSupport, ProtocolLib and ViaVersion together with SpigotLib are working fine, however I believe, that the combination of these 4 plugins incorrectly causes the most of the problems.

3. **Remove old SpigotLib folder:** After each update it's suggested to remove the old SpigotLib folder for letting the plugin extract the latest configuration and language file from the new SpigotLib jar.

4. **Verify that SpigotLib works fine:** The easiest way to verify that SpigotLib works properly is the usage of the /sl command

5. **If you are absolutely sure about that none of the above mentioned points helped you:** Still do NOT leave a bad review or write a comment, just contact me and I will help you.


# Features

- **Animation API** (make your animations and run them easily)
- **BungeeAPI** (Communicate super easily with the BungeeCord or with other servers connected to BungeeCord)
- **ChatAPI** (for sending Action bar, Chat and System JSON messages really easy, using my really simple custom format)
- **CommandAPI** (make command simulations easily using this API. It provides commands like Title, Subtitle, ActionBar, Console, Chat, Message, e.t.c. And every command is executed through the VariableAPI, so you can also use variables in the commands. You can even create custom commands too)
- **Config management** (Don't waste your time with config loading / saving. This system simplifiest all your Config management. From now it works with MySQL too.)
- **EconomyAPI** (a simple but really useful EconomyAPI system with multiple balance types, Vault compatibility and bank support. Now it supports working with other economy management plugins too.)
- **ItemGuiAPI** (Make custom item GUIs easily, supports inventory layout management so you can set up the same item to several inventory slots easily)
- **JSON API** (Are you tired of using Gson, it's time to try out a way better JsonAPI system. This system works similiar to the configuration management, just with Jsons)
- **Language management API** (use per player selectable languages for your plugins language file)
- **Map API** (manage custom maps very easily and super fast on packet level)
- **Mojang API** (easily handle the requests and responses between your server and between the Mojangs official API)
- **MySQL API** (Never worry about MySQL connections anymore, just use this API to easily send MySQL commands and get the answers or just do some background/Async updates)
- **NBT API** (get, list and add custom NBT tags for entities and items)
- **TPS meter and crashreporter** (make a crash report from the per thread stack trace dumps, if the servers tps is too low)
- **TitleAPI** (for setting Titles/Subtitles, for players without version incompatibility)
- **PacketAPI** (I have created a brand new API for managing packets really easily and compatible between different Spigot versions)
- **PlayerFile** (A brilliant way of storing any kind of data about a player. It supports both FILE and MySQL backend.)
- **RomanNumsAPI** (A simple but powerful API for converting numbers to roman ones and vice versa)
- **ScoreboardAPI** (manage per player different NO FLICKER sidebars, tabbars and nametagbars easily)
- **VariableAPI** (for handling unlimited amount of variables cross plugins, with a really simple and easy to use way)
- **TimeAPI** (for showing times in chat easily)

# Commands
- **/sl cmd** [p:player] <command>
- **/sl debug** 
- **/sl item [give]** [p:player] [item]
- **/sl lang** [p:player] [language]
- **/sl migratetodb**
- **/sl perm** [p:player] [permission]
- **/sl reload** [p:player] <pf|lf|config>
- **/sl save** [p:player] <pf>
- **/sl vars** [p:player] [message]
- **/sl velocity** [p:player] <x> <y> <z>

# Permissions
- **spigotlib.debug** (for getting debug messages, like error reports in the chat)
- **spigotlib.command.commandname** (for using a SpigotLib command)

# Translations

- **Available:** English, German, Hungarian, Dutch (outdated, looking for new translators)

If you are able to, please translate this plugin to your language! In translation, you should keep the plugins colors, commands and variable names. You should only change the sentences in messages.

# Contact

Before complaining about something doesn't work, please CONTACT ME.

[**Discord Group**](https://discord.gg/Jr3bKpq)

**Author's Discord:** gyuriX#8413

**Skype:** gyurixdev

**[Email](mailto:barathgy@gmail.com?subject=SpigotLib&body=Hello%2C%0A%0AI%20need%20some%20support%20in%20using%20your%20SpigotLib%20plugin.%0A%0AMy%20server%20version%20is%3A%201.8.8%0A%0AI%20am%20using%20the%20following%20plugins%20on%20my%20server%3A%0A-%20SpigotLib%0A-%20Vault%0A-%20Essentials%0A%0AThanks%2C%0ASpigotMc-Username)**

# ​License

SpigotLib is licensed under the **GPL-3** license 

[Short overview](https://tldrlegal.com/license/gnu-general-public-license-v3-(gpl-3))
![](https://s6.postimg.cc/ipih76mi9/Spigot_Lib_License_Short.png)

[Full text](https://github.com/gyurix/SpigotLib/blob/master/LICENSE.MD)
