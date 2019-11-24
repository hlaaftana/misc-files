module Discord

const DISCORDJL_VERSION = v"0.0.0"
const DISCORDJL_GITHUB = "https://github.com/hlaaftana/Discord.jl"

export DISCORDJL_GITHUB, DISCORDJL_VERSION

include("util.jl")
include("permissions.jl")
include("data.jl")
include("client.jl")
include("websocket.jl")

end
