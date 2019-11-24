module Hacclang
  def split(str, max = 0)
    list = [""]
    currentQuote = nil
    escaped = false
    str.each_char do |c|
      if list.size == max
        list[list.size - 1] += c
      else
        if currentQuote
          if ch == "\\"
            escaped = true
          end
          if ch == currentQuote and not escaped
            currentQuote = nil
          else
            list[list.size - 1] += ch
          end
        else
          if ['"', "'"].include? ch
            currentQuote = ch
          elsif ch.strip.empty?
            list += ""
          else 
            list[list.size - 1] += ch
          end
        end
      end
    end
    list
  end
end