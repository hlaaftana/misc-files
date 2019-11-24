export Permissions

const PERM_CREATE_INSTANT_INVITE = 1
const PERM_KICK_MEMBERS = 2
const PERM_BAN_MEMBERS = 3
const PERM_ADMINISTRATOR = 4
const PERM_MANAGE_CHANNELS = 5
const PERM_MANAGE_GUILD = 6
const PERM_READ_MESSAGES = 10
const PERM_SEND_MESSAGES = 11
const PERM_SEND_TTS_MESSAGES = 12
const PERM_MANAGE_MESSAGES = 13
const PERM_EMBED_LINKS = 14
const PERM_ATTACH_FILES = 15
const PERM_READ_MESSAGE_HISTORY = 16
const PERM_MENTION_EVERYONE = 17
const PERM_CONNECT = 20
const PERM_SPEAK = 21
const PERM_MUTE_MEMBERS = 22
const PERM_DEAFEN_MEMBERS = 23
const PERM_MOVE_MEMBERS = 24
const PERM_USE_VAD = 25
const PERM_CHANGE_NICKNAME = 26
const PERM_MANAGE_NICKNAMES = 27
const PERM_MANAGE_ROLES = 28

type Permissions
    value::UInt64

    Permissions(fall) = new(UInt64(fall))
end

getbit(thing::Real, bit) = (thing << bit) >>> bit
getlastbit(thing::Real, bit) = getbit(thing, (sizeof(thing) * 8) - bit)
getperm(thing::Real, bit) = asbool(getlastbit(thing, bit))

caninvite(perms::Permissions) = getperm(perms, PERM_CREATE_INSTANT_INVITE)
cankick(perms::Permissions) = getperm(perms, PERM_KICK_MEMBERS)
canban(perms::Permissions) = getperm(perms, PERM_BAN_MEMBERS)
isadmin(perms::Permissions) = getperm(perms, PERM_ADMINISTRATOR)
canmanagechannels(perms::Permissions) = getperm(perms, PERM_MANAGE_CHANNELS)
canmanageguild(perms::Permissions) = getperm(perms, PERM_MANAGE_GUILD)
canreadmessages(perms::Permissions) = getperm(perms, PERM_READ_MESSAGES)
cansendmessages(perms::Permissions) = getperm(perms, PERM_SEND_MESSAGES)
cantts(perms::Permissions) = getperm(perms, PERM_SEND_TTS_MESSAGES)
canmanagemessages(perms::Permissions) = getperm(perms, PERM_MANAGE_MESSAGES)
canembed(perms::Permissions) = getperm(perms, PERM_EMBED_LINKS)
cansendfiles(perms::Permissions) = getperm(perms, PERM_ATTACH_FILES)
canreadhistory(perms::Permissions) = getperm(perms, PERM_READ_MESSAGE_HISTORY)
canmentioneveryone(perms::Permissions) = getperm(perms, PERM_MENTION_EVERYONE)
canconnect(perms::Permissions) = getperm(perms, PERM_CONNECT)
canspeak(perms::Permissions) = getperm(perms, PERM_SPEAK)
canmute(perms::Permissions) = getperm(perms, PERM_MUTE_MEMBERS)
candeafen(perms::Permissions) = getperm(perms, PERM_DEAFEN_MEMBERS)
canmovevoice(params::Permissions) = getperm(perms, PERM_MOVE_MEMBERS)
canpushtotalk(params::Permissions) = getperm(perms, PERM_USE_VAD)
canchangenick(params::Permissions) = getperm(perms, PERM_CHANGE_NICKNAME)
canmanagenicks(param::Permissions) = getperm(perms, PERM_MANAGE_NICKNAMES)
canmanageroles(param::Permissions) = getperm(perms, PERM_MANAGE_ROLES)