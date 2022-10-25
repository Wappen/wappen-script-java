( # loop.ws )


( n = 0 )


( @condition = "
    ( ( ! n ) < 40 )
" )
( @body = "
    ( n = ( ( ! n ) + 1 ) )
" )
( @ while )


( @n = 10 )
( @body = "
    ( n = ( ( ! n ) + 1 ) )
" )
( @ for )


{ "We executed a few loops a total of" ( ! n ) "times" }
