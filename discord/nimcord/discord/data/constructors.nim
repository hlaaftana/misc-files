import json, types, getters, sequtils,
  ../misc, ../client

proc constructUser*(client: Client, obj: JsonNode): User =
  User(client: client, id: id(obj["id"].str), name: obj["username"].str,
    discrim: obj["discriminator"].str, avatarHash: obj["avatar"].str)

proc constructGuild*(client: Client, obj: JsonNode): Guild =
  return
    if obj["unavailable"].bval:
      Guild(client: client, id: id(obj["id"].str), unavailable: true)
    else:
      Guild(client: client, id: id(obj["id"].str), name: obj["name"].str,
        iconHash: obj["icon"].str, region: obj["region"].str,
        ownerId: id(obj["owner_id"].str), splashHash: obj["splash"].str,
        afkChannelId: id(obj["afk_channel_id"].str),
        afkTimeout: cast[int](obj["afk_timeout"].num), embedEnabled: obj["embed_enabled"].bval,
        embedChannelId: id(obj["embed_channel_id"].str),
        verificationLevelNumber: cast[int](obj["verification_level"].num),
        features: obj["features"].elems.map(proc(n: JsonNode): string = n.str))

proc constructNewGuild*(client: Client, obj: JsonNode) =
  g = constructGuild(client, obj)
  return
    if obj["unavailable"].bval:
      g
    else:
      g.members = obj["members"].elems.map()

proc getNick(obj: JsonNode): string =
  if obj.hasKey("nick"):
    obj["nick"].str
  else:
    nil

proc constructMember*(client: Client, obj: JsonNode,
                    guildId: Snowflake): Member =
  var user: User = constructUser(client, obj["user"])
  Member(client: client, id: user.id, name: user.name,
    discrim: user.discrim, avatarHash: user.avatarHash,
    nick: obj.getNick(),
    roleIds: obj["roles"].elems.map(proc(n: JsonNode): Snowflake = id(n.str)),
    user: user,
    joinTimeString: obj["joined_at"].str, mute: obj["mute"].bval,
    deaf: obj["deaf"].bval, guildId: guildId)

proc constructChannel*(obj: JsonNode, parentId: auto): Channel =
  Channel()