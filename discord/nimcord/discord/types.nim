import misc, client, strutils, times, sequtils, json, tables

const
  cdnUrl*: string = "https://cdn.discordapp.com/$#/$#"
  discordEpoch*: uint64 = 1420070400000'u64

type
  Snowflake* = uint64
  Permissions* = int

  DiscordBase* = ref object of RootObj
    id*: Snowflake
    client*: Client

  User* = ref object of DiscordBase
    name*: string
    avatarHash*: string
    discrim*: string

  Guild* = ref object of DiscordBase
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
  Role* = ref object of DiscordBase
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
  GuildEmbed* = ref object of RootObj
    enabled: bool
    channelId: Snowflake
  VerificationLevel* = enum
    vlLow = 0, vlMedium = 1, vlHigh = 2
  IntegrationType* = enum
    itTwitch = "twitch", itYoutube = "youtube"
  Integration* = ref object of DiscordBase
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
  IntegrationAccount* = ref object of RootObj
    id*: string
    name*: string
  Emoji* = ref object of DiscordBase
    name*: string
    roleIds*: seq[Snowflake]
    guildId*: Snowflake
    requiresColons*: bool
    managed*: bool
  VoiceState* = ref object of DiscordBase
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
  PermissionOverwrite* = ref object of DiscordBase
    channelId*: string
    overwriteType*: OverwriteType
    allow*: Permissions
    deny*: Permissions
  ChannelType* = enum
    ctText = "text", ctVoice = "voice"
  Channel* = ref object of DiscordBase
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

  Message* = ref object of DiscordBase
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
  Embed* = ref object of RootObj
    title*: string
    embedType*: string
    description*: string
    url*: string
    thumbnail*: Thumbnail
    provider*: Provider
  Thumbnail* = ref object of RootObj
    url*: string
    proxyUrl*: string
    height*: int
    width*: int
  Provider* = ref object of RootObj
    name*: string
    url*: string
  Attachment* = ref object of DiscordBase
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