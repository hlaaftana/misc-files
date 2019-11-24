package hlaaftana.lip

import groovy.transform.CompileStatic

/*
fibonacci [n] =
  result [i, list] <- [0, 1] =
    match [i]
    [0, '[1]]
    [n, list]
    [result [next [i], do
      add [list] [plus map ['[1, 2], reverseNth [list]]]
      list]]

Assign
  Call
    Name fibonacci
    List
      Name n
  Assign
    Inject
      Call
        Name result
        List
          Name i
          Name list
      List
        Number 0
        Number 1
    Call
      Call
        Call
          Call
            Name match
            List
              Name i
          List
            Number 0
            Quote
              List
                Number 1
        List
          Name n
          Name list
      List
        Call
          Name result
          List
            Call
              Name next
              List
                Name i
            Do
              Call
                Call
                  Name add
                  List
                    Name list
                List
                  Call
                    Name plus
                    Call
                      Name map
                      List
                        Quote
                          List
                            Number 1
                            Number 2
                        Call
                          Name reverseNth
                          List
                            Name list
              Name list
*/

@CompileStatic
class LipParser {
	static Expression parse(String text) {

	}
}