import sdl2 except init, quit
import sdl2/mixer, random, sequtils, times, math
import logic, present, state

proc main =
  if not sdl2.init(INIT_VIDEO or INIT_TIMER or INIT_EVENTS or INIT_AUDIO):
    quit "Couldn't initialize SDL, " & $getError()

  defer: sdl2.quit()

  if not setHint("SDL_RENDER_SCALE_QUALITY", "2"):
    quit "Couldn't set SDL render scale quality, " & $getError()

  var game: Game
  new(game)
  game.window = createWindow(title = "Kaizo demo",
    x = SDL_WINDOWPOS_CENTERED, y = SDL_WINDOWPOS_CENTERED,
    w = 540, h = 540, flags = SDL_WINDOW_SHOWN or SDL_WINDOW_RESIZABLE)
  if game.window.isNil:
    quit "Couldn't create window, " & $getError()
  game.renderer = game.window.createRenderer(index = -1,
    flags = Renderer_Accelerated or Renderer_PresentVsync)
  if game.renderer.isNil:
    quit "Couldn't create renderer, " & $getError()

  game.world[60] = (ref Room)(sprites: @[])
  for i in 0..26:
    game.world[60].tiles[i] = Solid
    game.world[60].tiles[27 * 26 + i] = Solid
    game.world[60].tiles[27 * i] = Solid

  game.world[61] = (ref Room)(sprites: @[])
  for i in 0..26:
    game.world[61].tiles[i] = Solid
    game.world[61].tiles[27 * 26 + i] = Solid
    game.world[61].tiles[27 * i + 26] = Solid
  game.world[61].tiles[25 * 27 + 13] = Solid
  game.world[61].tiles[23 * 27 + 15] = Death

  game.checkpoint = (room: 60, x: 13 * 60, y: 13 * 60)

  game.update(Menu)

  discard openAudio(0, 0, 2, 4096)

  var lastFrameTime = cpuTime()

  block mainLoop:
    while true:
      var event = defaultEvent
      while event.pollEvent():
        case event.kind
        of QuitEvent:
          break mainLoop
        of KeyDown:
          if not event.key.repeat:
            game.keyPressed(event.key)
        of KeyUp:
          game.keyReleased(event.key)
        of MouseButtonDown:
          game.mousePressed(event.button)
        of DropFile:
          game.world = readDump(readFile($event.drop.file))
          game.update(Menu)
        else: discard
      if cpuTime() - lastFrameTime >= (1 / 60):
        game.tick()
        inc game.numTicks
        game.renderer.setDrawColor(5, 125, 255, 255)
        game.renderer.clear()
        game.render()
        discard game.window.updateSurface()
        game.renderer.present()
        lastFrameTime = cpuTime()

  closeAudio()
  game.window.destroy()
  game.renderer.destroy()

when isMainModule: main()