import Requests
import JSON: json
import DandelionWebSockets: WebSocketHandler, send_text
export DISCORDJL_USERAGENT, DISCORD_API_URL, PTB_API_URL, CANARY_API_URL,
    eventalts

export DiscordWS, DiscordClient

export ptb, canary, fire, fulluseragent, headers, request,
    joinurl, send, eventname, listen, request_gateway

const DISCORDJL_USERAGENT = "DiscordBot ($DISCORDJL_GITHUB, $DISCORDJL_VERSION)"
const DISCORD_API_URL = "https://discordapp.com/api/"
const PTB_API_URL = "https://ptb.discordapp.com/api/"
const CANARY_API_URL = "https://canary.discordapp.com/api/"
eventalts = Dict(
    "message" => "message_create",
    "new_message" => "message_create",
    "channel" => "channel_create",
    "new_channel" => "channel_create",
    "guild" => "guild_create",
    "new_guild" => "guild_create",
    "role" => "guild_role_create",
    "new_role" => "guild_role_create",
    "ban" => "guild_ban_add",
    "unban" => "guild_ban_remove",
    "presence" => "presence_update",
    "new_member" => "guild_member_add",
    "member_join" => "guild_member_add",
    "user_join" => "guild_member_add",
    "member_joined" => "guild_member_add",
    "user_joined" => "guild_member_add",
    "member_updated" => "guild_member_update",
    "member_leave" => "guild_member_remove",
    "user_leave" => "guild_member_remove",
    "member_left" => "guild_member_remove",
    "user_left" => "guild_member_remove",
    "typing" => "typing_start"
)

abstract DiscordWS <: WebSocketHandler

function send(handler::DiscordWS, op::Int, d::Dict)
    send_text(handler.ws, json(Dict(
        "op" => op,
        "d" => d
    )))
end

type DiscordClient
    token::AbstractString
    bot::Bool
    baseurl::AbstractString
    largethreshold::Int
    shard::Tuple
    customuseragent::AbstractString
    cache::Dict
    listeners::Dict{AbstractString, Array}
    ws::DiscordWS

    function DiscordClient(; args...)
        client = new()
        client.bot = true
        client.baseurl = DISCORD_API_URL
        client.largethreshold = 250
        client.cache = Dict()
        client.listeners = Dict()
        for (k, v) in args
            client.(k) = v
        end
        client
    end

    DiscordClient(token::AbstractString; args...) = DiscordClient(token = token; args...)
    DiscordClient(token::AbstractString) = DiscordClient(token = token)
end

ptb(client::DiscordClient) = client.baseurl = PTB_API_URL
canary(client::DiscordClient) = client.baseurl = CANARY_API_URL

function eventname(name)
    name = lowercase(strip(string(name)))
    name = replace(name, "change", "update")
    name = replace(name, r"\s+", "_")
    name = haskey(eventalts, name) ? eventalts[name] : name
    uppercase(name)
end

function listen(client::DiscordClient, event, callable)
    ev = eventname(event)
    if haskey(client.listeners, ev)
        push!(client.listeners[ev], callable)
    else
        client.listeners[ev] = [callable]
    end
    callable
end

function fire(client::DiscordClient, event, data::Dict)
    ev = eventname(event)
    for l in get(client.listeners, ev, [])
        l(data)
    end
    for l in get(client.listeners, "ALL", [])
        l(ev, data)
    end
end

function fulluseragent(client::DiscordClient)
    if asbool(client, :customuseragent)
        "$DISCORDJL_USERAGENT $(client.customuseragent)"
    else
        DISCORDJL_USERAGENT
    end
end

function headers(client::DiscordClient, isGet::Bool = false)
    heads = Dict()
    if isdefined(client, :token)
        heads["Authorization"] = (client.bot ? "Bot " : "") * client.token
    end
    if !isGet
        heads["Content-Type"] = "application/json"
    end
    heads["User-Agent"] = fulluseragent(client)
    heads
end

function request(client::DiscordClient, method_name, url; args...)
    name = symbol(method_name)
    Requests.json(Requests.(name)(joinurl(client.baseurl, url); 
        headers = headers(client, name == :get), args...))
end

# HTTP STARTS HERE

request_gateway(client::DiscordClient) = request(client, "get", "gateway")["url"]