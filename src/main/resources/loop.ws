"
The for function expects the variable @n set to a positive number value.
The @body variable should be a valid include parameter.
"
( ^ for (
    ( ? ( ( @n = ( ( ! @n ) - 1 ) ) >= 0 )
        (
            ( # ( ! @body ) )
            ( @ for )
        )
    )
) )

"
The while function expects the variable @condition to be a bool-expression.
The @body variable should be a valid include parameter.
"

( ^ while (
    ( ? ( # ( ! @condition ) )
        (
            ( # ( ! @body ) )
            ( @ while )
        )
    )
) )
