#const n=1000.

row(1..n).
col(1..n).

{queen(I,J):row(I),col(J)}.

:- queen(I,J1),queen(I,J2) ,J1 != J2.
:- queen(I1,J),queen(I2,J) ,I1 != I2.
:- queen(I,J), queen(II,JJ), (I,J) != (II,JJ),I-J == II-JJ.
:- queen(I,J), queen(II,JJ), (I,J) != (II,JJ),I+J == II+JJ.

{ queen(I,1..n) } == 1 :- I = 1..n.
{ queen (1..n,J) } == 1 :- J = 1..n.
:- { queen(D-J,J) }  >= 2, D =    2..2*n.
:- { queen(D+J,J) }  >= 2, D = 1-n..n-1.