import sdl2, state, util

proc lookRight*(room: Room, sprite: Sprite, n: int): (Tile, int) =
  result = (Air, sprite.x + n)
  for v in (sprite.y div 60)..((sprite.y + sprite.height - 1) div 60):
    let np = sprite.x + sprite.width - 1
    var h = np div 60 + 1
    let hmax = (np + n) div 60
    while h <= hmax:
      if h > 26 or (let tile = room.tiles[v * 27 + h]; tile != Air):
        let d = h * 60 - sprite.width
        if d < result[1]: result = (tile, d)
      inc h

proc lookLeft*(room: Room, sprite: Sprite, n: int): (Tile, int) =
  result = (Air, sprite.x - n)
  for v in (sprite.y div 60)..((sprite.y + sprite.height - 1) div 60):
    let np = sprite.x
    var h = np div 60 - 1
    let hmax = (np - n) div 60
    while h >= hmax:
      if h > 26 or (let tile = room.tiles[v * 27 + h]; tile != Air):
        let d = (h + 1) * 60
        if d > result[1]: result = (tile, d)
      dec h

proc lookDown*(room: Room, sprite: Sprite, n: int): (Tile, int) =
  result = (Air, sprite.y + n)
  for h in (sprite.x div 60)..((sprite.x + sprite.width - 1) div 60):
    let np = sprite.y + sprite.height - 1
    var v = np div 60 + 1
    let vmax = (np + n) div 60
    while v <= vmax:
      if v > 26 or (let tile = room.tiles[v * 27 + h]; tile != Air):
        let d = v * 60 - sprite.height
        if d < result[1]: result = (tile, d)
      inc v

proc lookUp*(room: Room, sprite: Sprite, n: int): (Tile, int) =
  result = (Air, sprite.y - n)
  for h in (sprite.x div 60)..((sprite.x + sprite.width - 1) div 60):
    let np = sprite.y
    var v = np div 60 - 1
    let vmax = (np - n) div 60
    while v >= vmax:
      if v > 26 or (let tile = room.tiles[v * 27 + h]; tile != Air):
        let d = (v + 1) * 60
        if d > result[1]: result = (tile, d)
      dec v

proc switchRoom*(state: State, world: openarray[ref Room], num: int, x, y: int, facing: Horizontal) =
  var player: Sprite
  if state.kind == Action and not state.room.isNil:
    for sp in state.room.sprites:
      if sp.kind == Player:
        player = sp
        break
  else:
    new(player)
  player.x = x
  player.y = y
  player.playerPin.exists = false
  player.playerFacing = facing
  state.roomNumber = num
  state.room.deepCopy(world[num])
  state.room.sprites.add(player)

proc collide*(game: Game, input: (Tile, int)): int =
  result = input[1]
  if input[0] == Death:
    game.state.switchRoom(game.world, game.checkpoint.room, game.checkpoint.x, game.checkpoint.y, Right)

proc update*(game: Game, state: StateKind) =
  var pauseBackground: TexturePtr
  let oldState = game.state
  if not oldState.isNil:
    case oldState.kind
    of Menu:
      oldState.menuTexture.destroy()
    of Pause:
      oldState.pauseBackground.destroy()
      oldState.pauseOverlay.destroy()
      if state == Action and not oldState.pauseOldState.isNil:
        game.state = oldState.pauseOldState
        return
    of Action, Edit:
      if state == Pause:
        let (w, h) = game.window.getSize
        let surf = createRGBSurface(0, w, h, 32, 0xff0000, 0x00ff00, 0x0000ff, 0xff000000u32)
        discard game.renderer.readPixels(nil, SDL_PIXEL_FORMAT_ARGB8888.cint, surf.pixels, surf.pitch)
        pauseBackground = createTextureFromSurface(game.renderer, surf)
        surf.destroy()
  let ns = State(kind: state)
  case ns.kind
  of Menu:
    ns.menuTexture = game.texture("res/menu.png")
  of Pause:
    ns.pauseBackground = pauseBackground
    ns.pauseOverlay = game.texture("res/menu.png")
    ns.pauseOldState = oldState
  of Action:
    ns.switchRoom(game.world, game.checkpoint.room, game.checkpoint.x, game.checkpoint.y, Right)
    ns.playerTexture = game.texture("res/menu.png")
  of Edit:
    ns.editRoomNumber = if oldState.kind == Action: oldState.roomNumber else: game.checkpoint.room
  game.state = ns

proc tick*(game: Game) =
  let st = game.state
  case st.kind
  of Menu, Pause: discard
  of Action:
    let room = st.room
    var bullet: Sprite
    var bulletIndex, bulletY: int
    for i, sp in room.sprites:
      case sp.kind
      of Player:
        bulletY = sp.y
        if not sp.playerPin.exists:
          if sp.playerVerticalSpeed == 0:
            if room[].tileAt(sp.x, sp.y + 120) == Air and ((sp.x - 1) mod 60 != 0 or room[].tileAt(sp.x + 60, sp.y + 120) == Air):
              sp.playerVerticalSpeed = 2
              if sp.playerJump == Thrown:
                sp.playerJump = Returning
          elif sp.playerVerticalSpeed > 0:
            bulletY = collide(game, lookDown(room[], sp, sp.playerVerticalSpeed))
            if bulletY - sp.y < sp.playerVerticalSpeed:
              sp.playerVerticalSpeed = 0
              sp.playerJump = Still
            elif sp.playerVerticalSpeed < 12:
              sp.playerVerticalSpeed += 3
            sp.y = bulletY
          else:
            bulletY = collide(game, lookUp(room[], sp, -sp.playerVerticalSpeed))
            sp.playerVerticalSpeed += 3
            sp.y = bulletY
        if sp.playerHorizontalSpeed > 0:
          let x =
            if sp.playerAccel == Right:
              collide(game, lookRight(room[], sp, sp.playerHorizontalSpeed))
            else:
              collide(game, lookLeft(room[], sp, sp.playerHorizontalSpeed))
          if sp.playerHorizontalSpeed < 15:
            sp.playerHorizontalSpeed += 10
          sp.x = x
        if sp.x == 0:
          st.switchRoom(game.world, st.roomNumber - 1, 26 * 60 - 1, sp.y, Left)
          return
        if sp.x >= 26 * 60:
          st.switchRoom(game.world, st.roomNumber + 1, 1, sp.y, Right)
          return
        if sp.y == 0:
          st.switchRoom(game.world, st.roomNumber - 11, sp.x, 26 * 60 - 1, sp.playerFacing)
          return
        if sp.y >= 25 * 60:
          st.switchRoom(game.world, st.roomNumber + 11, sp.x, 1, sp.playerFacing)
          return
      of Bullet:
        bulletIndex = i
        bullet = sp
        case sp.bulletFacing
        of Left: sp.x -= 15
        of Right: sp.x += 15
      of Pin:
        discard
    if not bullet.isNil:
      bullet.y = bulletY + 45
      if bullet.x notin 0..<(60 * 27):
        room.sprites.del(bulletIndex)
  of Edit: discard

proc keyPressed*(game: Game, ev: KeyboardEventPtr) =
  let st = game.state
  if ev.keysym.scancode == SDL_SCANCODE_D:
    game.dump()
    return
  case st.kind
  of Menu:
    if ev.keysym.scancode == SDL_SCANCODE_RETURN:
      game.update(Action)
  of Action:
    case ev.keysym.scancode
    of SDL_SCANCODE_RETURN:
      game.update(Pause)
    of SDL_SCANCODE_LEFT:
      for sp in st.room.sprites:
        if sp.kind == Player:
          sp.playerFacing = Left
          sp.playerAccel = Left
          sp.playerHorizontalSpeed = 5
    of SDL_SCANCODE_RIGHT:
      for sp in st.room.sprites:
        if sp.kind == Player:
          sp.playerFacing = Right
          sp.playerAccel = Right
          sp.playerHorizontalSpeed = 5
    of SDL_SCANCODE_Z:
      for sp in st.room.sprites:
        if sp.kind == Player:
          if sp.playerJump == Still:
            sp.playerPin.exists = false
            sp.playerJump = Thrown
            sp.playerVerticalSpeed = -30
    of SDL_SCANCODE_X:
      var
        player: Sprite
        bulletExists: bool
      for sp in st.room.sprites:
        if sp.kind == Player:
          player = sp
        elif sp.kind == Bullet:
          bulletExists = true
      if not bulletExists and not player.isNil:
        let face = player.playerFacing
        var bullet = Sprite(kind: Bullet, bulletFacing: face)
        bullet.x = player.x
        if face == Right:
          bullet.x += 60
        else:
          bullet.x -= 15
        bullet.y = player.y + 45
        st.room.sprites.add(bullet)
    of SDL_SCANCODE_A:
      for sp in st.room.sprites:
        if sp.kind == Player:
          if sp.playerPin.exists:
            sp.playerPin.exists = false
            sp.playerJump = Returning
          else:
            let pinSprite = Sprite(kind: Pin, x: sp.x + 10, y: sp.y)
            let
              left = lookLeft(st.room[], pinSprite, 180)
              right = lookRight(st.room[], pinSprite, 180)
            if Air notin {left[0], right[0]}:
              sp.playerPin = (true, left[1], right[1] + 60)
              sp.playerVerticalSpeed = 0
              sp.playerJump = Still
    of SDL_SCANCODE_DOWN:
      for sp in st.room.sprites:
        if sp.kind == Player:
          if sp.playerJump != Still:
            sp.playerVerticalSpeed = 24
          elif sp.playerPin.exists:
            sp.playerPin.exists = false
          else:
            sp.playerCrouching = true
    else: discard
  of Pause:
    case ev.keysym.scancode
    of SDL_SCANCODE_RETURN:
      game.update(Action)
    of SDL_SCANCODE_E:
      game.update(Edit)
    of SDL_SCANCODE_M:
      game.update(Menu)
    else: discard
  of Edit:
    case ev.keysym.scancode
    of SDL_SCANCODE_0:
      st.editSelectedTile = Air
    of SDL_SCANCODE_1:
      st.editSelectedTile = Solid
    of SDL_SCANCODE_2:
      st.editSelectedTile = Death
    of SDL_SCANCODE_RETURN:
      game.update(Pause)
    of SDL_SCANCODE_R:
      st.editAnchor.exists = false
    of SDL_SCANCODE_RIGHT:
      st.editRoomNumber += 1
      if game.world[st.editRoomNumber].isNil:
        game.world[st.editRoomNumber] = (ref Room)(sprites: @[])
    of SDL_SCANCODE_LEFT:
      st.editRoomNumber -= 1
      if game.world[st.editRoomNumber].isNil:
        game.world[st.editRoomNumber] = (ref Room)(sprites: @[])
    of SDL_SCANCODE_UP:
      st.editRoomNumber -= 11
      if game.world[st.editRoomNumber].isNil:
        game.world[st.editRoomNumber] = (ref Room)(sprites: @[])
    of SDL_SCANCODE_DOWN:
      st.editRoomNumber += 11
      if game.world[st.editRoomNumber].isNil:
        game.world[st.editRoomNumber] = (ref Room)(sprites: @[])
    else: discard

proc keyReleased*(game: Game, ev: KeyboardEventPtr) =
  let st = game.state
  if st.kind == Action:
    case ev.keysym.scancode
    of SDL_SCANCODE_LEFT:
      for sp in st.room.sprites:
        if sp.kind == Player and sp.playerAccel == Left:
          sp.playerHorizontalSpeed = 0
    of SDL_SCANCODE_RIGHT:
      for sp in st.room.sprites:
        if sp.kind == Player and sp.playerAccel == Right:
          sp.playerHorizontalSpeed = 0
    of SDL_SCANCODE_Z:
      for sp in st.room.sprites:
        if sp.kind == Player and sp.playerJump == Thrown:
          sp.playerVerticalSpeed = 0
    of SDL_SCANCODE_DOWN:
      for sp in st.room.sprites:
        if sp.kind == Player:
          sp.playerCrouching = false
    else: discard

proc mousePressed*(game: Game, ev: MouseButtonEventPtr) =
  let st = game.state
  if st.kind == Edit:
    let (ww, wh) = game.window.getSize
    let
      x = int(27 * ev.x.float / ww.float)
      y = int(27 * ev.y.float / wh.float)
    if ev.button == BUTTON_RIGHT:
      if st.editAnchor.exists:
        let X = if x < st.editAnchor.x: x..st.editAnchor.x else: st.editAnchor.x..x
        let Y = if y < st.editAnchor.y: y..st.editAnchor.y else: st.editAnchor.y..y
        for i in X:
          for j in Y:
            game.world[st.editRoomNumber].tiles[j * 27 + i] = st.editSelectedTile
        st.editAnchor.exists = false
      else:
        st.editAnchor = (true, x, y)
    elif ev.button == BUTTON_LEFT:
      game.world[st.editRoomNumber].tiles[y * 27 + x] = st.editSelectedTile