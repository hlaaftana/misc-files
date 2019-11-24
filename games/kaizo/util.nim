import sdl2, sdl2/image as sdlimage, state

template defaultVar*(name, value): untyped {.dirty.} = 
  var `name val` {.threadvar, gensym.}: type(value)
  template `name`*: type(value) =
    if `name val`.isNil:
      `name val` = value
    `name val`

template withSurface*(surf: SurfacePtr, body: untyped): untyped {.dirty.} =
  let it = surf
  if unlikely(it.isNil):
    quit "Surface was nil, error: " & $getError()
  body
  freeSurface(it)

template withSurface*(surf: SurfacePtr, name, body: untyped): untyped {.dirty.} =
  let `name` = surf
  if unlikely(`name`.isNil):
    quit "Surface was nil, error: " & $getError()
  body
  freeSurface(`name`)

proc toCstring*(c: char): cstring =
  var res = [c, '\0']
  result = cast[cstring](addr res)

template modsHeldDown*: bool =
  (getModState().cint and (
    KMOD_LCTRL.cint or KMOD_RCTRL.cint or
    KMOD_LSHIFT.cint or KMOD_RSHIFT.cint or
    KMOD_LALT.cint or KMOD_RALT.cint)) != 0

proc texture*(game: Game, image: cstring): TexturePtr =
  withSurface sdlimage.load(image):
    if unlikely(it.isNil):
      quit "Couldn't load texture " & $image & ", error: " & $getError()
    result = createTextureFromSurface(game.renderer, it)

template draw*(game: Game, texture: TexturePtr, src, dest: var Rect) =
  game.renderer.copy(texture, addr src, addr dest)

proc draw*(game: Game, texture: TexturePtr, src, dest: Rect) =
  var
    src = src
    dest = dest
  game.draw(texture, src, dest)

proc draw*(game: Game, texture: TexturePtr, dest: var Rect) =
  var w, h: cint
  texture.queryTexture(nil, nil, addr w, addr h)
  game.renderer.copy(texture, nil, addr dest)

proc draw*(game: Game, texture: TexturePtr, dest: Rect) =
  var dest = dest
  game.draw(texture, dest)

proc draw*(game: Game, texture: TexturePtr, x, y: cint) =
  var w, h: cint
  texture.queryTexture(nil, nil, addr w, addr h)
  var
    dest = rect(x, y, w, h)
  game.renderer.copy(texture, nil, addr dest)

template fill*(game: Game, dest: var Rect) =
  game.renderer.fillRect(addr dest)

proc fill*(game: Game, dest: Rect) =
  var
    dest = dest
  game.fill(dest)