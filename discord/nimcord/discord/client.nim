import data, websocket

type
  Client* = ref object of RootObj
    token*: string
    shardData*: tuple[shardId: int, numShards: int]
    largeThreshold*: int
    ws*: AsyncWebSocket
