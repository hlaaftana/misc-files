require 'main.rb'

text = <<END
ping variable_name -l "hey what's up"

ddos_tool print(argument) ->
  echo argument

ddos print variable_name
END

Hacclang::eval(text)