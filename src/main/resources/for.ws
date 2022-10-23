"
The for function expects the variable @i set to a positive number value.
The @body variable should be a valid include parameter.
"
( ^ for (
    ( @i = ( ( ! @i ) - 1 ) )
    ( # ( ! @body ) )
    ( ? ( ( ! @i ) > 0 )
        ( @ for )
    )
) )
