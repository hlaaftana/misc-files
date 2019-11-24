import sdl2, times, strutils, os

const unitsPerTile* = 60

type
  Tile* = enum
    Air
    Solid
    Death

  SpriteKind* = enum
    Player
    Pin
    Bullet

  Direction* = enum
    Left, Right, Up, Down

  Horizontal* = range[Left..Right]

  BoomerangState* = enum
    Still, Thrown, Returning

  Sprite* = ref object
    x*, y*: int
    case kind*: SpriteKind
    of Player:
      playerFacing*: Horizontal
      playerVerticalSpeed*: int
      playerJump*: BoomerangState
      playerAccel*: Horizontal
      playerHorizontalSpeed*: int
      playerCrouching*: bool
      playerPin*: tuple[exists: bool, x1, x2: int]
    of Pin:
      discard
    of Bullet:
      bulletFacing*: Horizontal

  Room* = object
    tiles*: array[27 * 27, Tile]
    sprites*: seq[Sprite]

  StateKind* = enum
    Menu
    Action
    Pause
    Edit

  State* = ref object
    case kind*: StateKind
    of Menu:
      menuTexture*: TexturePtr
    of Action:
      playerTexture*: TexturePtr
      roomNumber*: int
      room*: ref Room
    of Pause:
      pauseBackground*, pauseOverlay*: TexturePtr
      pauseOldState*: State
    of Edit:
      editRoomNumber*: int
      editSelectedTile*: Tile
      editAnchor*: tuple[exists: bool, x, y: int]

  Game* = ref object
    done*: bool
    numTicks*: int
    window*: WindowPtr
    renderer*: RendererPtr
    world*: array[11 * 11, ref Room]
    checkpoint*: tuple[room, x, y: int]
    state*: State

proc readDump*(data: string): array[11 * 11, ref Room] =
  var data = data
  var i = 0
  while i < data.len:
    let room = new Room
    result[data[i].int] = room
    inc i
    for ti in 0..<27*27:
      room.tiles[ti] = Tile(data[i])
      inc i
    room.sprites.newSeq(cast[ptr uint16](addr data[i])[].int)
    inc i, 2
    for sp in room.sprites.mitems:
      #let kind = SpriteKind(data[i])
      inc i

proc dump*(game: Game) =
  let fn = "dumps/dump_" & $int64(epochTime() * 1000)
  var strs = newSeq[string]()
  for i, room in game.world:
    if not room.isNil:
      var str = newString(1 + 27 * 27 + 2)
      var index = 0
      str[index] = i.char
      inc index
      for ti in 0..<27*27:
        str[index] = room.tiles[ti].char
        inc index
      strs.add(str)
  createDir("dumps")
  writeFile(fn, strs.join(""))

proc width*(sprite: Sprite): int =
  case sprite.kind
  of Player:
    result = unitsPerTile
  of Bullet:
    result = unitsPerTile div 4
  of Pin:
    result = unitsPerTile

proc height*(sprite: Sprite): int =
  case sprite.kind
  of Player:
    result = unitsPerTile * 2
  of Bullet:
    result = unitsPerTile div 4
  of Pin:
    result = 1

proc tileAt*(room: Room, x, y: int): Tile =
  room.tiles[(y div unitsPerTile) * 27 + (x div unitsPerTile)]