# DurabilityTime
Tool for comparing time taken with durability options

This program runs in a loop, upon each iteration it attempts to insert two documents into the cluster,
one using durability options and one without.  It then reports the amount of time taken for each,
and whether there were any exceptions.

Sample output

    Iteration      2    With Durability:        233 ms (       None ) Without Durability:         25 ms (       None )
    Iteration      4    With Durability:         53 ms (       None ) Without Durability:         25 ms (       None )
    Iteration      6    With Durability:         52 ms (       None ) Without Durability:         25 ms (       None )
    Iteration      8    With Durability:       6630 ms (       None ) Without Durability:         25 ms (       None )
    Iteration     10    With Durability:       5798 ms (       None ) Without Durability:         24 ms (       None )
    Iteration     12    With Durability:       5897 ms (       None ) Without Durability:         25 ms (       None )
    Iteration     14    With Durability:       5853 ms (       None ) Without Durability:         24 ms (       None )
    Iteration     16    With Durability:         50 ms (       None ) Without Durability:         26 ms (       None )
    Iteration     18    With Durability:       6157 ms (       None ) Without Durability:         25 ms (       None )
    Iteration     20    With Durability:         52 ms (       None ) Without Durability:         25 ms (       None )


