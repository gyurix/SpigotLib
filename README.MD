#SpigotLib

##Introduction

SpigotLib is a collection of several useful libraries and APIs
for Spigot plugin development. It's not only perfect for developers,
but it's also good for server owners, because they can use it's API
testing commands, which are really useful.

**Official download page:** https://www.spigotmc.org/resources/spigotlib.5925/

**JavaDoc:** https://gyurix.pro/javadoc/SpigotLib

##Compatibility

###Incompatible server versions:
1.8.0

###Compatible Spigot/PaperSpigot versions:
1.8.8; 1.9; 1.9.2; 1.9.4; 1.10; 1.10.1; 1.10.2; 1.11; 1.11.2; 1.12​; 1.12.2

Every here not listed server version should be either fully compatible
(similar or newer server versions) or minimally compatible (older server versions),
so even if you use the plugin on 1.7 or 1.6 servers,
you can still use some of it's features.

##Features

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

##Commands
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

##Permissions
- **spigotlib.debug** (for getting debug messages, like error reports in the chat)
- **spigotlib.command.commandname** (for using a SpigotLib command)

##Translations

- **Available:** English, German, Hungarian, Dutch

If you are able to, please translate this plugin to your language! In translation, you should keep the plugins colors, commands and variable names. You should only change the sentences in messages.

##Contact

Before complaining about something doesn't work, please CONTACT ME.

**Discord:** gyuriX#8413

**Skype:** gyurixdev

**Email:** barathgy@gmail.com

##​License

###SpigotLib is licensed under the GPL-3 license 

**Short overview:** https://tldrlegal.com/license/gnu-general-public-license-v3-(gpl-3)

**Full text:** https://github.com/gyurix/SpigotLib/blob/master/LICENSE.MD
