module Hacclang
  class Script
    attr_accessor :environment
    attr_reader :indent_size
    attr_reader :indent_is_tab
    attr_reader :index
    attr_reader :line
    attr_reader :column

    def initialize
      @indent_size = 4
      @indent_is_tab = false

      @skip = 0
      @record_line = false

      @index = 0
      @line = 0
      @column = 0
      @current_value = nil
      @recording_command = false
      @recording_command_args = false
      @command_name = nil
      @command_args = nil

      @quote_char = nil
      @string_escaped = false

      @in_comment = false
    end

    def eval(code)
      code.each_line do |line|
        line.each_char do |c|
          if @skip > 0
            @skip -= 1
          elsif @recording_command

            if c[/[a-zA-Z_0-9]/]
              @command_name += c
            else
              @recording_command = false
              @recording_command_args = true
              @command_args = ""
            end

          elsif @recording_command_args

            if not ["\r", "\n"].include? c
              @command_args += c
            else
              @recording_command_args = false
            end

          elsif @quote_char

            if c == "\\"
              @string_escaped = true
            elsif c == @quote_char and not @string_escaped
              @quote_char = nil
              break
            else
              @current_value += c
            end

          elsif @in_comment

          elsif @column == 0

            if ["'", '"'].include? c
              @quote_char = c
              @current_value = ""
            elsif c[/[a-zA-Z_]/]
              @recording_command = true
              @command_name = c
            end

          end

          @column += 1
          @index += 1
        end


        @line += 1
        @index += 1
      end

      @current_value
    end
  end

  class Command
    attr_accessor :environment
    attr_reader :name
    attr_reader :parser

    def initialize(environment, name, &block)
      @name = name
      @environment = environment
      @parser = block
    end

    def call(args)
      parser.(args)
    end
  end

  class DdosTool
    attr_accessor :environment
    attr_reader :name
    attr_reader :code

    def initialize(environment, name, &block)
      @name = name
      @environment = environment
      @code = block
    end
  end

  class Environment
    attr_accessor :commands
    attr_accessor :ddos_tools
    attr_accessor :variables

    def initialize
      @variables = []

      @ddos_tools = []

      ddos_tool(:add){ |a, b| a + b }
      ddos_tool(:subtract){ |a, b| a - b }
      ddos_tool(:multiply){ |a, b| a * b }
      ddos_tool(:divide){ |a, b| a / b }
      ddos_tool(:modulo){ |a, b| a % b }
      ddos_tool(:power){ |a, b| a ** b }
    end

    def ddos_tool(name, &block)
      ddos_tools << DdosTool.new(self, name, &block)
    end
  end
end