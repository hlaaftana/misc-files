import times, os, strutils

if paramCount() == 0:
  echo("""Usage:
  .\smf2cta input [outputs]
    Takes each line as `x 0,0,0,0,0...` where x is the
    maximum x size for the layer and 0,0,0,0,0... is the
    layer code and turns it into a 2 dimensional array
    of integers. If outputs are given, the new text is
    written into those files otherwise to the input file.""")
  quit()

let inputFile = open(paramStr(1).string, fmRead)

let start = cpuTime()

var wr: seq[string] = @[]

for line in inputFile.lines:
  let
    wh = line.split(maxsplit = 2)
    x = wh[0].parseInt() div 20
    splitted = wh[1].split(',')
    y = splitted.len div x
  var
    i = 0
    list = newSeqOfCap[seq[string]](y)
  for t in splitted:
    if i mod x == 0:
      list.add(newSeqOfCap[string](x))
    list[^1].add(t)
    inc i
  wr.add($list)

echo "Done in ", (cpuTime() - start) * 1000, " milliseconds"

let outputFilenames = if paramCount() == 1: @[paramStr(1)]
                      else: commandLineParams()[1 .. ^1]

for fn in outputFilenames:
  fn.writeFile(wr.join("\n").replace("@"))
  echo "Successfully wrote to ", fn