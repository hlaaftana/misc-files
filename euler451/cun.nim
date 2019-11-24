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

var fs = newFileStream("cun", fmWrite)
var start = cpuTime()

for n in 2..20_000_000:
  if (n and 16383) == 0:
    start = cpuTime() - start
    echo n, ", time: ", start
  when false:
    var m = 1
    when true:
      let ps = deduplicate(primeFac(n), true)
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
  else:
    let prms = deduplicate(primeFac(n), true)
    let mp = max(prms)
    if mp < high(uint8).int:
      fs.write(prms.len.uint8 shl 4 and 0b1000u8)
      for p in prms:
        fs.write(p.uint8)
    elif mp < high(uint16).int:
      fs.write(prms.len.uint8 shl 4 and 0b0100u8)
      for p in prms:
        fs.write(p.uint16)
    elif mp < high(uint32).int:
      fs.write(prms.len.uint8 shl 4 and 0b1100u8)
      for p in prms:
        fs.write(p.uint32)

fs.close()

echo s