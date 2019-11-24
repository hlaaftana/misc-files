import tables, htmlparser, xmltree

# deprecated entries don't have substitutes documented, they just mention it in the deprecated entry so you have to handle that

type
  VegasTypeKind = enum
    Enumeration, Class, Interface, Delegate, NimType
  VegasType = ref object
    name, desc: string
    case kind: VegasTypeKind
    of NimType:
      discard
    of Enumeration:
      members: seq[tuple[name, desc: string]]
    of Class, Interface:
      properties: seq[tuple[typ, name, desc: string]]
      constructors: seq[tuple[args: seq[tuple[typ, name: string]], desc: string]]
      methods, operators: seq[tuple[args: seq[tuple[typ, name: string]], typ, name, desc: string]]
    of Delegate:
      returnType: VegasType
      args: seq[tuple[typ, name: string]]

proc newNimType(name: string): VegasType = VegasType(name: name, kind: NimType)

var typeTable = {"Int32": newNimType("int32"), "Int64": newNimType("int64"),
  "UInt32": newNimType("uint32"), "UInt64": newNimType("uint64"),
  "Single": newNimType("float32"), "Double": newNimType("float64"),
  "Boolean": newNimType("boolean"), "Object": newNimType("JsObject"),
  "ICollection": newNimType("seq[JsObject]"), "String": newNimType("cstring")}.toTable