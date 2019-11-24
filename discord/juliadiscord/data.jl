const Snowflake = UInt64

export DISCORD_EPOCH, DISCORD_EPOCH, DEFAULT_AVATARS, DISCORD_DATE_FORMAT,
    NO_VERIF_LEVEL, LOW_VERIF_LEVEL, MEDIUM_VERIF_LEVEL, HIGH_VERIF_LEVEL

export User, Role, Member, PermissionOverwrite, DChannel, VoiceState, Guild,
    IntegrationAccount, Integration, Emoji, GuildEmbed, Thumbnail,
    Provider, Embed, Attachment, Message

export id, creationtime, snowflaketounix, avatarurl

const DISCORD_CDN_URL = "https://cdn.discordapp.com/"
const DISCORD_EPOCH = 142007400000
const DISCORD_DATE_FORMAT = Dates.DateFormat("y-m-dTH:M:S.s+00:00")
const DEFAULT_AVATARS = [
    "6debd47ed13483642cf09e832ed0bc1b",
    "322c936a8c8be1b803cd94861bdfa868",
    "dd4dbc0016779df1378e7812eabaa04d",
    "0e291f67c9274a1abdddeb3fd919cbaa",
    "1cbd08c76f8af6dddce02c5138971129"
]
const NO_VERIF_LEVEL = 0
const LOW_VERIF_LEVEL = 1
const MEDIUM_VERIF_LEVEL = 2
const HIGH_VERIF_LEVEL = 3

abstract type SnowflakeId
    id::Snowflake
end

snowflake(thing::SnowflakeId) = thing.id
snowflake(thing::AbstractString) = parse(Snowflake, thing)
snowflake(thing) = Snowflake(thing)
creationtime(thing::SnowflakeId) = DateTime(snowflaketounix(thing.id))
snowflaketounix(num::Snowflake) = (num >> 22) + DISCORD_EPOCH

type User <: SnowflakeId
    name::AbstractString
    discrim::Integer
    avatarhash::Nullable{AbstractString}

    function User(dict::Dict)
        user = new()
        user.id = snowflake(dict["id"])
        user.name = dict["username"]
        user.discrim = parse(Int, dict["discriminator"])
        user.avatarhash = dict["avatar"]
        user
    end
end

function avatarurl(user::User) 
    if user.avatarhash != nothing
        "$DISCORD_CDN_URL/avatars/$(user.id)/$(user.avatarhash).jpg"
    else
        DEFAULT_AVATARS[user.discrim % 5]
    end
end

Base.string(user::User) = "$(user.name)#$(user.discrim)"

type Role <: SnowflakeId
    name::AbstractString
    color::Integer
    hoist::Bool
    position::Integer
    permissions::Permissions
    managed::Bool
    mentionable::Bool
    guildid::Snowflake

    function Role(dict::Dict, guildid::Snowflake = 0)
        role = new()
        role.id = snowflake(dict["id"])
        role.name = dict["name"]
        role.color = dict["color"]
        role.hoist = dict["hoist"]
        role.position = dict["position"]
        role.permissions = Permissions(dict["permissions"])
        role.managed = dict["managed"]
        role.mentionable = dict["mentionable"]
        try
            role.guildid = elvis(guildid, snowflake(dict["guild_id"]))
        catch e
            error("Guild ID not satisfied for role. If you put it in a " +
                "dict, make sure it has the key \"guild_id\"")
        end
        role
    end
end

type Member <: SnowflakeId
    name::AbstractString
    discrim::Integer
    avatarhash::Nullable{AbstractString}
    nick::Nullable{AbstractString}
    roleids::Array{Snowflake}
    guildjoindate::DateTime
    mute::Bool
    deaf::Bool

    function Member(dict::Dict, guildid::Snowflake = 0)
        member = new()
        member.id = snowflake(dict["user"]["id"])
        member.name = dict["user"]["username"]
        member.discrim = parse(Int, dict["user"]["discriminator"])
        member.avatarhash = dict["user"]["avatar"]
        member.nick = haskey(dict, "nick") ? 
            Nullable{AbstractString}(dict["nick"]) :
            Nullable{AbstractString}()
        member.roleids = map(snowflake, dict["roles"])
        member.joindate = DateTime(dict["joined_at"], DISCORD_DATE_FORMAT)
        member.mute = dict["mute"]
        member.deaf = dict["deaf"]
        try
            member.guildid = elvis(guildid, dict["guild_id"])
        catch e
            error("Guild ID not satisfied for member. If you put it in a " + 
                "dict, make sure it has the key \"guild_id\"")
        end
        member
    end
end

type PermissionOverwrite <: SnowflakeId
    channeltype_::AbstractString
    allowed::Permissions
    denied::Permissions

    function PermissionOverwrite(dict::Dict, channelid::Snowflake = 0)
        overwrite = new()
        try
            overwrite.channelid = elvis(channelid, dict["channel_id"])
        catch e
            error("Channel ID not satisfied for overwrite. If you put it " +
                "in a dict, make sure it has the key \"channel_id\"")
        end
        overwrite.type_ = dict["type"]
        overwrite.allowed = Permissions(dict["allow"])
        overwrite.denied = Permissions(dict["deny"])
        overwrite
    end
end

type DChannel <: SnowflakeId
    private::Bool
    type_::AbstractString
    
    # Private
    users::Array{User}

    # Guild
    name::AbstractString
    permissionoverwrites::Array{PermissionOverwrite}
    guildposition::Integer

    # Text
    topic::AbstractString
    lastmessageid::Snowflake

    # Voice
    bitrate::Integer
    userlimit::Integer

    function DChannel(dict::Dict, guildid::Snowflake = 0)
        channel = new()
        channel.id = snowflake(dict["id"])
        channel.private = haskey(dict, "is_private") ? dict["is_private"] : false
        channel.type_ = channel.private ? "text" : dict["type"]
        if channel.private
            channel.user = User(dict["recipient"])
            channel.lastmessageid = snowflake(dict["last_message_id"])
        else
            try
                channel.guildid = elvis(guildid, snowflake(dict["guild_id"]))
            catch e
                error("Guild ID not satisfied for non-private channel. If you put it in a " +
                    "dict, make sure it has the key \"guild_id\"")
            end
            if channel.type_ == "voice"
                channel.bitrate = dict["bitrate"]
                channel.userlimit = dict["user_limit"]
            else
                channel.topic = dict["topic"]
                channel.lastmessageid = dict["last_message_id"]
            end
        end
        channel
    end
end

isprivate(channel::DChannel) = channel.private
istext(channel::DChannel) = channel.type_ == "text"
isvoice(channel::DChannel) = channel.type_ == "voice"

type VoiceState <: SnowflakeId
    channelusersessionid::AbstractString
    deaf::Bool
    mute::Bool
    selfdeaf::Bool
    selfmute::Bool
    suppress::Bool

    function VoiceState(dict::Dict, guildid::Snowflake = 0)
        state = new()
        state.id = state.userid = snowflake(dict["id"])
        state.channelid = snowflake(dict["channel_id"])
        state.sessionid = dict["session_id"]
        state.deaf = dict["deaf"]
        state.mute = dict["mute"]
        state.selfdeaf = dict["self_deaf"]
        state.selfmute = dict["self_mute"]
        state.suppress = dict["suppress"]
        try
            state.guildid = elvis(guildid, snowflake(dict["guild_id"]))
        catch e
            error("Guild ID not satisfied for voice state. If you put it in a " +
                "dict, make sure it has the key \"guild_id\"")
        end
        state
    end
end

type Guild <: SnowflakeId
    name::AbstractString
    iconhash::Nullable{AbstractString}
    region::AbstractString
    channels::Array{Channel}
    afkchannelid::Nullable{Snowflake}
    afkchannel::Nullable{DChannel}
    afktimeout::Integer
    embedenabled::Bool
    embedchannelid::Nullable{Snowflake}
    embedchannel::Nullable{DChannel}
    members::Array{Member}
    ownerowner::Member
    verificationlevel::Integer
    splashhash::AbstractString
    voicestates::Array{VoiceState}
    roles::Array{Role}
    features::Array{AbstractString}
    unavailable::Bool

    function Guild(dict::Dict)
        guild = new()
        guild.id = snowflake(dict["id"])
        guild.unavailable = dict["unavailable"]
        if !guild.unavailable
            guild.name = dict["name"]
            guild.iconhash = dict["icon"]
            guild.region = dict["region"]
            guild.channels = map((a) -> DChannel(a, guild.id), dict["channels"])
            guild.roles = map((a) -> Role(a, guild.id), dict["roles"])
            guild.members = map((a) -> Member(a, guild.id), dict["members"])
            guild.voicestates = map((a) -> VoiceState(a, guild.id), dict["voice_states"])
            guild.afkchannelid = haskey(dict, "afk_channel_id") ? 
                snowflake(dict["afk_channel_id"]) : 
                nothing
            guild.afktimeout = dict["afk_timeout"]
            guild.embedenabled = haskey(dict, "embed_enabled") ? 
                dict["embed_enabled"] :
                false
            guild.embedchannelid = haskey(dict, "embed_channel_id") ? 
                snowflake(dict["embed_channel_id"]) :
                nothing
            guild.ownerid = dict["owner_id"]
            guild.splashhash = dict["splash"]
            guild.features = dict["features"]
            guild.verificationlevel = dict["verification_level"]
        end
        guild
    end
end

afkchannel(guild::Guild) = find((a) -> a.id == guild.afkchannelid,
    guild.channels)

embedchannel(guild::Guild) = find((a) -> a.id == guild.embedchannelid,
    guild.channels)

owner(guild::Guild) = find((a) -> a.id == guild.ownerid, guild.members)

type IntegrationAccount
    id::AbstractString
    name::AbstractString

    function IntegrationAccount(dict::Dict)
        new(dict["id"], dict["name"])
    end
end

type Integration
    id::AbstractString
    name::AbstractString
    type_::AbstractString
    enabled::Bool
    syncing::Bool
    roleexpirebehaviour::Integer
    expiregraceperiod::Integer
    user::User
    account::IntegrationAccount

    function Integration(dict::Dict)
        new(dict["id"], dict["name"], dict["type"], dict["enabled"],
            dict["syncing"], dict["role_id"], dict["expire_behaviour"],
            dict["expire_grace_period"], User(dict["user"]),
            IntegrationAccount(dict["account"]), 
            DateTime(dict["synced_at"], DISCORD_DATE_FORMAT))
    end
end

type Emoji <: SnowflakeId
    name::AbstractString
    roleids::Array{Snowflake}
    guildmanaged::Bool
    requirescolons::Snowflake

    function Emoji(dict::Dict, guildid::Snowflake = 0)
        emoji = new()
        emoji.id = snowflake(dict["id"])
        emoji.name = dict["name"], 
        emoji.roleids = map(snowflake, dict["roles"])
        try
            emoji.guildid = elvis(guildid, snowflake(dict["guild_id"]))
        catch e
            error("Guild ID not satisfied for emoji. If you put it in a " +
                "dict, make sure it has the key \"guild_id\"")
        end
        emoji.requirescolons = dict["require_colons"]
        emoji.managed = dict["managed"]
        emoji
    end
end

type GuildEmbed
    enabled::Bool
    channelid::Snowflake

    GuildEmbed(dict::Dict) = new(dict["enabled"], snowflake(dict["channel_id"]))
end

type Thumbnail
    url::AbstractString
    proxyurl::AbstractString
    height::Integer
    width::Integer

    Thumbnail(dict::Dict) = new(dict["url"], dict["proxy_url"], dict["height"], dict["width"])
end

type Provider
    name::AbstractString
    url::AbstractString

    Provider(dict::Dict) = new(dict["name"], dict["url"])
end

type Embed
    title::AbstractString
    type_::AbstractString
    desc::AbstractString
    url::AbstractString
    thumbnail::Thumbnail
    provider::Provider

    Embed(dict::Dict) = new(dict["title"], dict["type"], dict["description"], dict["url"],
        Thumbnail(dict["thumbnail"]), Provider(dict["provider"]))
end

type Attachment
    filename::AbstractString
    size::Int
    url::AbstractString
    proxyurl::AbstractString
    height::Integer
    width::Integer

    Attachment(dict::Dict) = new(dict["filename"], dict["size"], dict["url"], dict["proxy_url"],
        dict["height"], dict["width"])
end

type Message <: SnowflakeId
    channelauthor::User
    content::AbstractString
    timestamp::DateTime
    edittimestamp::Nullable{DateTime}
    tts::Bool
    mentionseveryone::Bool
    mentions::Array{User}
    rolementions::Array{Role}
    attachments::Array{Attachment}
    embeds::Array{Embed}
    nonce::Nullable{AbstractString}

    function Message(dict::Dict) 
        message = new()
        message.id = snowflake(dict["id"])
        message.channelid = snowflake(dict["channel_id"])
        message.author = User(dict["author"])
        message.content = dict["content"]
        message.timestamp = DateTime(dict["timestamp"], DISCORD_DATE_FORMAT)
        message.edittimestamp = elvis(dict["edited_timestamp"], DateTime(dict["edited_timestamp"], 
            DISCORD_DATE_FORMAT))
        message.tts = dict["tts"]
        message.mentionseveryone = dict["mentions_everyone"]
        message.mentions = map(User, dict["mentions"])
        message.rolementions = map(Role, dict["mention_roles"])
        message.attachments = map(Attachment, dict["attachments"])
        message.embeds = map(Embed, dict["embeds"])
        message.nonce = haskey(dict, "nonce") ? dict["nonce"] : nothing
        message
    end
end

for t in [User, Role, Member, PermissionOverwrite, DChannel, VoiceState, Guild,
    IntegrationAccount, Integration, Emoji, GuildEmbed, Thumbnail,
    Provider, Embed, Attachment, Message]
    ts = Symbol(split(string(t), r"\.")[end])
    for fn in fieldnames(t)
        @eval function ($fn)(deleg::$ts)
            deleg.($fn)
        end
        #@eval export ($fn)
    end
end