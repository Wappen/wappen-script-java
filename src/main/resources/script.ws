( # for.ws )

( o = 0 )
( @i = 10 )
( @body = "
    ( o = ( ( ! o ) + 1 ) )
" )
( @ for )

{ "We executed a for-loop" ( ! o ) "times" }
