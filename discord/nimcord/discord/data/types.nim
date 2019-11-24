import strutils, times, sequtils, json, tables,
  ../misc, ../client

const
  cdnUrl*: string = "https://cdn.discordapp.com/$#/$#"
  discordEpoch*: uint64 = 1420070400000'u64

type
  Snowflake* = uint64
  Permissions* = int

  DiscordCache* = ref object of RootObj
    resolvedMap*: Table

  DiscordBase* = ref object of RootObj
    client*: Client

  DiscordId* = ref object of DiscordBase
    id*: Snowflake

  User* = ref object of DiscordId
    name*: string
    avatarHash*: string
    discrim*: string

  Guild* = ref object of DiscordId
    name*: string
    iconHash*: string
    splashHash*: string
    ownerId*: Snowflake
    region*: string
    afkChannelId*: Snowflake
    afkTimeout*: int
    embedEnabled*: bool
    embedChannelId*: Snowflake
    verificationLevelNumber*: int
    members*: seq[Member]
    channels*: seq[Channel]
    voiceStates*: seq[VoiceState]
    roles*: seq[Role]
    emojis*: seq[Emoji]
    features*: seq[string]
    unavailable*: bool
  Role* = ref object of DiscordId
    name*: string
    color*: int
    hoist*: bool
    position*: int
    permissions*: Permissions
    managed*: bool
    mentionable*: bool
    guildId*: Snowflake
  Member* = ref object of User
    nick*: string
    roleIds*: seq[Snowflake]
    guildId*: Snowflake
    user*: User
    joinTimeString*: string
    mute*: bool
    deaf*: bool
  GuildEmbed* = ref object of DiscordBase
    enabled*: bool
    channelId*: Snowflake
  VerificationLevel* = enum
    vlLow = 0, vlMedium = 1, vlHigh = 2
  IntegrationType* = enum
    itTwitch = "twitch", itYoutube = "youtube"
  Integration* = ref object of DiscordId
    name*: string
    integrationType*: IntegrationType
    enabled*: bool
    syncing*: bool
    roleId*: Snowflake
    expireBehaviour*: int
    expireGracePeriod*: int
    user*: User
    account*: IntegrationAccount
    syncTimeString*: string
  IntegrationAccount* = ref object of DiscordBase
    id*: string
    name*: string
  Emoji* = ref object of DiscordId
    name*: string
    roleIds*: seq[Snowflake]
    guildId*: Snowflake
    requiresColons*: bool
    managed*: bool
  VoiceState* = ref object of DiscordId
    guildId*: Snowflake
    channelId*: Snowflake
    userId*: Snowflake
    sessionId*: Snowflake
    deaf*: bool
    mute*: bool
    selfDeaf*: bool
    selfMute*: bool
    suppress*: bool

  OverwriteType* = enum
    otRole = "role", otMember = "member"
  PermissionOverwrite* = ref object of DiscordId
    channelId*: string
    overwriteType*: OverwriteType
    allow*: Permissions
    deny*: Permissions
  ChannelType* = enum
    ctText = "text", ctVoice = "voice"
  Channel* = ref object of DiscordId
    case private*: bool:
    of true:
      user*: User
    else:
      name*: string
      permissionOverwrites*: seq[PermissionOverwrite]
      guildId*: Snowflake
      position*: int
    case chanType*: ChannelType
    of ctText:
      topic*: string
      lastMessageId*: Snowflake
    of ctVoice:
      bitrate*: int

  Message* = ref object of DiscordId
    channelId*: Snowflake
    author*: User
    content*: string
    timestampString*: string
    editTimestampString*: string
    tts*: bool
    mentionsEveryone*: bool
    mentions*: seq[User]
    attachments*: seq[Attachment]
    embeds*: seq[Embed]
    nonce*: int
  Embed* = ref object of DiscordBase
    title*: string
    embedType*: string
    description*: string
    url*: string
    thumbnail*: Thumbnail
    provider*: Provider
  Thumbnail* = ref object of DiscordBase
    url*: string
    proxyUrl*: string
    height*: int
    width*: int
  Provider* = ref object of DiscordBase
    name*: string
    url*: string
  Attachment* = ref object of DiscordId
    filename*: string
    size*: int
    url*: string
    proxyUrl*: string
    height*: int
    width*: int

proc resolveId*(thing: auto): Snowflake =
  return
    if thing is string:
      cast[Snowflake](parseInt(thing))
    elif thing is int:
      cast[Snowflake](thing)
    elif thing is DiscordBase:
      thing.id
    else:
      0

proc id*(thing: auto): Snowflake = resolveId(thing)

proc `==`*(self: DiscordBase, other: DiscordBase): bool =
  self.id == other.id

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
