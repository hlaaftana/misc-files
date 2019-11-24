import math, sequtils, algorithm, times, streams

proc modInv(a0, b0: int): int =
  var (a, b, x0) = (a0, b0, 0)
  result = 1
  if b == 1: return
  while a > 1:
    result -= (a div b) * x0
    a = a mod b
    swap a, b
    swap x0, result
  if result < 0: result += b0

proc getStep(n: int): int {.inline.} =
 result = 1 + n*4 - int(n div 2)*2
 
proc primeFac(n: int): seq[int] =    
  result = @[]
  let maxq = int(floor(sqrt(float(n))))
  var d = 1
  var q = 2 or (n mod 2)
  while (q <= maxq) and (n mod q != 0):
    q = getStep(d)
    inc d
  if q <= maxq:
    result = concat(primeFac(q), primeFac(n div q), result)
  else: 
    result.add(n)
  result.sort(system.cmp)

var s = 0

var fs = newFileStream("cun", fmRead)
var start = cpuTime()

for n in 2..20_000_000:
  if (n and 16383) == 0:
    start = cpuTime() - start
    echo n, ", time: ", start

  var m = 1
  when true:
    let ps = block:
      let ui = fs.readUint8()
      echo ui
      let ln = ui shr 4
      let bits = ui and 0b1111
      var primes = newSeq[int](ln.int)
      for i in 0..<ln.int:
        primes[i] =
          case bits
          of 1: fs.readUint16().int
          of 2: fs.readUint8().int
          of 3: fs.readUint32().int
          else: raise newException(ValueError, "bit representation not recognized: " & $bits)
      primes
    echo ps
    if ps.len == 1 and ps[0] == n:
      for a in 2..<(n-1):
        if modInv(a, n) == a:
          m = a
    else:
      var lastSkips = newSeq[int](ps.len)
      for a in 2..<(n-1):
        var h = false
        for i in 0..<ps.len:
          if lastSkips[i] + ps[i] == a:
            lastSkips[i] = a
            h = true
        if not h and modInv(a, n) == a:
          m = a
  else:
    for cocan in 2..<(n-1):
      if gcd(cocan, n) == 1 and modInv(cocan, n) == cocan:
        m = cocan
  s += m

fs.close()

echo s