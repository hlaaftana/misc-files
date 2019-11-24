export asbool, elvis, find, joinurl

tonullable{T}(value, typ::Type{T}) = value != nothing ? Nullable{T}(value) : Nullable{T}()

asbool(any) = any != nothing || !isempty(any)
asbool(any, fieldname) = isdefined(any, symbol(fieldname)) && asbool(any.(symbol(fieldname)))
asbool(num::Number) = num != 0
asbool(nu::Nullable) = nu.isnull && asbool(nu.value)
asbool(bool::Bool) = bool
asbool(::Void) = false
elvis(cond, fall) = asbool(cond) ? cond : fall
elvis(thing, fieldname, fall) = asbool(thing, fieldname) ? thing.(symbol(fieldname)) : fall

function find(call, itr)
    for th in itr
        if asbool(call(th))
            return th
        end
    end
end

function joinurl(base::AbstractString, paths...)
    a = base
    for p in paths
        if endswith(base, "/") && startswith(p, "/")
            a = chop(base) * p
        elseif !endswith(base, "/") && !startswith(p, "/")
            a *= "/" * p
        else
            a *= p
        end
    end
    a
end