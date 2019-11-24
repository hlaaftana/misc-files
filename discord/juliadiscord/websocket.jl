using DandelionWebSockets
import JSON: json
import Requests: URI
import Libz

export OP_DISPATCH, OP_HEARTBEAT, OP_IDENTIFY, OP_PRESENCE, 
    OP_VOICE_STATE, OP_VOICE_PING, OP_RESUME, OP_RECONNECT, 
    OP_MEMBER_CHUNK, OP_SESSION_INVALID, OP_HELLO, OP_HEARTBEAT_ACK

export DiscordWSImpl

export on_message, heartbeat, start_heartbeat,
    identify, state_open#, connect

const OP_DISPATCH = 0
const OP_HEARTBEAT = 1 
const OP_IDENTIFY = 2 
const OP_PRESENCE = 3 
const OP_VOICE_STATE = 4 
const OP_VOICE_PING = 5 
const OP_RESUME = 6 
const OP_RECONNECT = 7 
const OP_MEMBER_CHUNK = 8 
const OP_SESSION_INVALID = 9
const OP_HELLO = 10
const OP_HEARTBEAT_ACK = 11

type DiscordWSImpl <: DiscordWS
    ws::WSClient
    client::DiscordClient
    seq::Integer
    stop_chan::Channel{Any}
end

function close(client::DiscordClient)
    take!(client.ws.stop_chan)
    stop(client.ws.ws)
end

function connect(client::DiscordClient)
    ws = WSClient()
    client.ws = DiscordWSImpl(ws, client, 0, Channel{Any}(1))
    wsconnect(ws, URI(joinurl(request_gateway(client), "?v=5")), client.ws)
end

Base.connect(client::DiscordClient) = connect(client)

function on_message(handler::DiscordWS, data::Dict)
    println(data)
    if data["op"] == 0
        handler.seq = data["s"]
        fire(handler.client, data["d"])
    elseif data["op"] == 10
        merge!(handler.client.cache, data["d"])
        start_heartbeat(handler, data["d"]["heartbeat_interval"] / 1000)
    end
end

function DandelionWebSockets.on_text(handler::DiscordWS, text::UTF8String)
    println("hohoho")
    on_message(handler, JSON.parse(text))
end

function DandelionWebSockets.on_binary(handler::DiscordWS, data::Vector{UInt8})
    println("hahaha")
    on_text(handler, Libz.inflate(data))
end

DandelionWebSockets.state_open(handler::DiscordWS) = identify(handler)
DandelionWebSockets.state_closed(handler::DiscordWS) = print("Closed !!! ")
DandelionWebSockets.state_closing(handler::DiscordWS) = print("Closing !!! ")

function heartbeat(handler::DiscordWS)
    send(handler, OP_HEARTBEAT, isdefined(handler, :seq) ? handler.seq : 0)
end

function start_heartbeat(handler::DiscordWS, interval::Real)
    @async begin
        while true
            heartbeat(handler)
            sleep(interval)
        end
    end
end

function identify(handler::DiscordWS)
    println("Connected__")
    data = Dict(
        "token" => handler.client.token,
        #"compress" => true,
        "large_threshold" => handler.client.largethreshold,
        "properties" => Dict(
            "browser" => "Discord.jl"
        )
    )
    println("I did the data")
    if (isdefined(handler.client, :shard))
        data.shard = handler.client.shard
    end
    println("Sending")
    send(handler, OP_IDENTIFY, data)
    println("Sent")
end