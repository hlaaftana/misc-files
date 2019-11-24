import sdl2, sdl2/image as sdlimage, opengl, opengl/glu, times, math

from sequtils import mapLiterals

type
  State = ref object
    done: bool
    numTicks: int
    window: WindowPtr
    context: GlContextPtr
    shaders: seq[cuint]
    vao, ebo, tex, pointbuf: cuint
    anchor: tuple[set: bool, x, y: cint]
    env: seq[tuple[x, y, z, s, t: cfloat]]
    indices: seq[array[3, cuint]]

proc loadTextures(state: State) =
  let surface = sdlimage.load("marina.png")
  if surface.isNil:
    echo "Image couldn't be loaded: ", sdl2.getError()
    quit 1

  var w = surface.w#.nextPowerOfTwo.cint
  var h = surface.h#.nextPowerOfTwo.cint
  var bpp: cint
  var Rmask, Gmask, Bmask, Amask: uint32
  if not pixelFormatEnumToMasks(SDL_PIXELFORMAT_ABGR8888, bpp,
    Rmask, Gmask, Bmask, Amask):
    quit "pixel format enum to masks " & $sdl2.getError()

  let newSurface = createRGBSurface(0, w, h, bpp,
    Rmask, Gmask, Bmask, Amask)

  discard surface.setSurfaceAlphaMod(0xFF)
  discard surface.setSurfaceBlendMode(BlendMode_None)

  blitSurface(surface, nil, newSurface, nil)

  glGenTextures(1, addr state.tex)
  glBindTexture(GL_TEXTURE_2D, state.tex)
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
  glTexImage2D(GL_TEXTURE_2D, 0, 4, w.GLsizei, h.GLsizei, 0, GL_RGBA, GL_UNSIGNED_BYTE, newSurface.pixels)
  freeSurface(surface)
  freeSurface(newSurface)

proc reshape(state: State, w, h: cint) =
  glViewport(0, 0, w, h)
  glMatrixMode(GL_PROJECTION)
  glLoadIdentity()
  glOrtho(0, w.GLfloat, 0, h.GLfloat, -1.0, 1.0)
  gluPerspective(45.0, 4 / 3, -1.0, 1.0)
  loadTextures(state)

proc loadEnvBuffer(state: State) =
  state.env = mapLiterals(@[
    (0.5, 0.5, 0.0, 1.0, 1.0),
    (0.5, -0.5, 0.0, 1.0, 0.0),
    (-0.5, -0.5, 0.0, 0.0, 0.0),
    (-0.5, 0.5, 0.0, 0.0, 1.0)], cfloat)
  state.indices = mapLiterals(@[
    [0, 1, 3],
    [1, 2, 3]], cuint)

  glGenVertexArrays(1, addr state.vao)

  glBindVertexArray(state.vao)

  var vbo: cuint
  glGenBuffers(1, addr vbo)

  glGenBuffers(1, addr state.pointbuf)

  glBindBuffer(GL_ARRAY_BUFFER, vbo)
  glBufferData(GL_ARRAY_BUFFER, state.env.len * 5 * sizeof(cfloat), addr state.env[0], GL_STATIC_DRAW)

  glGenBuffers(1, addr state.ebo)

  glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, state.ebo)
  glBufferData(GL_ELEMENT_ARRAY_BUFFER, state.indices.len * 3 * sizeof(cfloat), addr state.indices[0], GL_STATIC_DRAW)

  glVertexAttribPointer(0, 3, cGL_FLOAT, GL_FALSE, 5 * sizeof(cfloat), nil)
  glVertexAttribPointer(1, 2, cGL_FLOAT, GL_FALSE, 5 * sizeof(cfloat), nil)
  glEnableVertexAttribArray(0)
  glEnableVertexAttribArray(1)

proc compileShaders(state: State) =
  state.shaders = @[]
  var success: cint
  var infoLog = cast[cstring](create(char, 512))

  template shader(prog: cuint, kind: GLenum, source: string): cuint =
    let sh = glCreateShader(kind)
    glShaderSource(sh, 1, allocCStringArray([source]), nil)
    glCompileShader(sh)
    glGetShaderiv(sh, GL_COMPILE_STATUS, addr success)
    if success == 0:
      glGetShaderInfoLog(sh, 512, nil, infoLog)
      echo "Shader compilation failed"
      echo infoLog
      quit 1
    glAttachShader(prog, sh)
    sh

  template program(body) =
    var id = glCreateProgram()
    var vertexId, fragmentId: cuint
    template vertex(source: string) {.inject.} =
      bind id, vertexId
      vertexId = shader(id, GL_VERTEX_SHADER, source)
    template fragment(source: string) {.inject.} =
      bind id, fragmentId
      fragmentId = shader(id, GL_FRAGMENT_SHADER, source) 
    body
    glLinkProgram(id)
    glGetProgramiv(id, GL_LINK_STATUS, addr success)
    if success == 0:
      glGetProgramInfoLog(id, 512, nil, infoLog)
      echo "Shader linking compilation failed"
      echo cast[cstring](infoLog)
      quit 1
    glDeleteShader(vertexId)
    glDeleteShader(fragmentId)
    state.shaders.add(id)

  program:
    vertex """#version 330 core
layout (location = 0) in vec3 pos;
layout (location = 1) in vec2 texpos;

out vec3 rgbCol;
out vec2 texCoord;

void main() {
  gl_Position = vec4(pos, 1.0);
  rgbCol = vec3((pos.x + 1) / 2, (pos.y + 1) / 2, (pos.z + 1) / 2);
  texCoord = texpos;
}"""

    fragment """#version 330 core
in vec3 rgbCol;
in vec2 texCoord;

out vec4 color;

uniform sampler2D tex;

void main() {
  color = texture(tex, texCoord);
}"""

proc render(state: State) =
  glClearColor(0.2, 0.7, 0.8, 1.0)
  glClear(GL_COLOR_BUFFER_BIT)

  glUseProgram(state.shaders[0])

  when false:
    glBindBuffer(GL_ARRAY_BUFFER, state.pointbuf)

    var mouseX, mouseY: cint
    getMouseState(mouseX, mouseY)
    var windowWidth, windowHeight: cint
    getSize(state.window, windowWidth, windowHeight)
    var points = @[[(mouseX.cfloat / windowWidth.cfloat) * 2.0 - 1.0, (mouseY.cfloat / windowHeight.cfloat) * -2.0 + 1.0, -0.5]]
    if state.anchor.set:
      points.add([(state.anchor.x.cfloat / windowWidth.cfloat) * 2.0 - 1.0, (state.anchor.y.cfloat / windowHeight.cfloat) * -2.0 + 1.0, -0.5])
    const pointw = 0.07

  glBindTexture(GL_TEXTURE_2D, state.tex)
  glBindVertexArray(state.vao)

  var size: cint
  glGetBufferParameteriv(GL_ARRAY_BUFFER, GL_BUFFER_SIZE, addr size)
  glDrawElements(GL_TRIANGLES, GLsizei(size div sizeof(GLfloat)), GL_UNSIGNED_INT, nil)

  state.window.glSwapWindow()

proc listen(state: State) =
  var event = defaultEvent
  while event.pollEvent():
    case event.kind
    of QuitEvent:
      state.done = true
    of WindowEvent:
      let winev = event.window
      if winev.event == WindowEventResized:
        reshape(state, winev.data1, winev.data2)
    of MouseButtonUp:
      let ev = event.button
      case ev.button
      of ButtonLeft:
        state.anchor.set = false
      of ButtonRight:
        state.anchor = (true, ev.x, ev.y)
      else: discard
    else: discard

proc main() =
  discard sdl2.init(INIT_EVERYTHING)

  const
    firstWidth = 640
    firstHeight = 480

  var state: State
  new(state)
  state.window = createWindow("opengl test", 100, 100, firstWidth, firstHeight, SDL_WINDOW_OPENGL or SDL_WINDOW_RESIZABLE)
  state.context = state.window.glCreateContext()

  loadExtensions()
  glEnable(GL_POINT_SMOOTH)
  glPointSize(2.5)
  compileShaders(state)
  loadEnvBuffer(state)

  reshape(state, firstWidth, firstHeight)

  var lastFrameTime = cpuTime()

  while not state.done:
    listen(state)
    if cpuTime() - lastFrameTime >= (1 / 60):
      render(state)
      lastFrameTime = cpuTime()
      inc state.numTicks

  state.window.destroy()

main()