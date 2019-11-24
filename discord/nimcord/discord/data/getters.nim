import types, strutils, times, sequtils,
  ../misc

proc timestampMs*(sf: Snowflake): uint64 =
  (sf shr 22) + discordEpoch

proc timestamp*(sf: Snowflake): TimeInterval =
  milliseconds(cast[int](sf.timestampMs))

proc joinTime*(member: Member): TimeInfo =
  parseDiscordJsonDate(member.joinTimeString)

proc syncTime*(integ: Integration): TimeInfo =
  parseDiscordJsonDate(integ.syncTimeString)

proc isText*(chan: Channel): bool =
  chan.chanType == ctText

proc isVoice*(chan: Channel): bool =
  chan.chanType == ctVoice

proc iconUrl*(guild: Guild): string =
  cdnUrl % ["icons", guild.iconHash & ".jpg"]

proc defaultChannel*(guild: Guild): Channel =
  guild.channels.find do (chan: Channel) -> bool:
    guild == chan

proc defaultRole*(guild: Guild): Role =
  guild.roles.find do (role: Role) -> bool:
    guild == role

proc textChannels*(guild: Guild): seq[Channel] =
  guild.channels.filter do (chan: Channel) -> bool:
    chan.chanType == ctText

proc voiceChannels*(guild: Guild): seq[Channel] =
  guild.channels.filter do (chan: Channel) -> bool:
    chan.chanType == ctVoice
