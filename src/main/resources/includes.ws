"
Include other wappen-script files using the # operator.
The code inside the file behind the path will be executed
as if it were pasted in place of the expression.
"

( # "script.ws" )

"
Using multi-line strings you can execute the code in a given string.
"
( # "
\"This is valid code!\"
( 23 * 3 )
" )
