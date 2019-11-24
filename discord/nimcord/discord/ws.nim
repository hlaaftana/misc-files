import websocket, nativesockets, asyncdispatch, pegs, sequtils, strutils,
  json, client, data, misc

proc identify*(client: Client) {.async.} =
  waitFor client.ws.sock.sendText($(%{
    "token": %client.token,
    "large_threshold": %(client.largeThreshold.vs(250)),
    "shard":
      if client.shardData == (0, 0):
        %(@[0, 1])
      else:
        %(@[client.shardData[0], client.shardData[1]])
  }), true)

proc connect*(client: Client, gatewayUrl: string) {.async.} =
  var
    withoutProtocol: string = gatewayUrl.replace(peg"wss?\:\/\/")
    split: seq[string] = toSeq(withoutProtocol.split('/', 2))
    host: string = split[0]
    path: string = "/" & split[1]
  client.ws = 
    waitFor(newAsyncWebsocket(host, Port 443,
      path, ssl = true))
  waitFor client.identify()