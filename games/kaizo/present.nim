import sdl2, state, util, math

proc render*(game: Game) =
  let wsz = game.window.getSize()
  let (ww, wh) = wsz
  let st = game.state
  case st.kind
  of Menu:
    game.draw(st.menuTexture, rect(0, 0, ww, wh))
  of Action:
    let
      tw = ww.float / 27
      th = wh.float / 27
      twc = ceil(tw).cint
      thc = ceil(th).cint
      t2hc = ceil(2 * th).cint
    template convx(x: int): cint = cint(tw * x.float / 60)
    template convy(y: int): cint = cint(th * y.float / 60)
    for i, tile in st.room.tiles:
      if tile == Air: continue
      let
        x = i.cint mod 27
        y = i.cint div 27
      var sq = (cint(x.float * tw), cint(y.float * th), twc, thc)
      case tile
      of Air:
        game.renderer.setDrawColor(255, 255, 255, 255)
      of Solid:
        game.renderer.setDrawColor(0, 0, 0, 255)
      of Death:
        game.renderer.setDrawColor(255, 0, 0, 255)
      game.fill(sq)
    for sprite in st.room.sprites:
      case sprite.kind
      of Player:
        var dest = (convx(sprite.x), convy(sprite.y),
          twc, t2hc)
        if sprite.playerPin.exists:
          game.renderer.setDrawColor(60, 60, 60, 255)
          let y = convy(sprite.y + 10)
          game.renderer.drawLine(convx(sprite.playerPin.x1), y, convx(sprite.playerPin.x2), y)
        when false:
          game.renderer.setDrawColor(255, 0, 255, 255)
          game.fill(dest)
        game.renderer.copyEx(st.playerTexture, nil, addr dest, 0.0, nil, if sprite.playerFacing == Left: SDL_FLIP_HORIZONTAL else: SDL_FLIP_NONE)
      of Bullet:
        game.renderer.setDrawColor(255, 255, 0, 255)
        game.fill((convx(sprite.x), convy(sprite.y),
          cint(tw / 4), cint(th / 4)))
      of Pin:
        discard
  of Pause:
    game.draw(st.pauseBackground, rect(0, 0, ww, wh))
    game.draw(st.pauseOverlay, rect(0, 0, ww, wh))
  of Edit:
    let
      tw = ww.float / 27
      th = wh.float / 27
      twc = ceil(tw).cint
      thc = ceil(th).cint
    for i, tile in game.world[st.editRoomNumber].tiles:
      if tile == Air: continue
      let
        x = i.cint mod 27
        y = i.cint div 27
      var sq = (cint(x.float * tw), cint(y.float * th), twc, thc)
      if tile == Solid:
        game.renderer.setDrawColor(0, 0, 0, 255)
        game.fill(sq)
      elif tile == Death:
        game.renderer.setDrawColor(255, 0, 0, 255)
        game.fill(sq)
    if st.editAnchor.exists:
      var sq = (cint(st.editAnchor.x.float * tw), cint(st.editAnchor.y.float * th), twc, thc)
      game.renderer.setDrawColor(0, 255, 0, 100)
      game.fill(sq)