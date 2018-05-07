#const n=0.
#const p=0.

node(0..n).
partition(0..p).


{belongs(N,P):partition(P),node(N)}.
:- not {belongs(N,P)} == n.

contains(P,N) :- belongs(N,P).
:- contains(P1,N1),contains(P2,N2),N1==N2,P1!=P2,partition(P1),partition(P2),node(N1),node(N2).


family(N1,N2,P) :- contains(P,N1),contains(P,N2),partition(P), node(N1), node(N2),N1 != N2,not family(N2,N1,P).

reach(X,Y) :- edge(X,Y),family(X,Y,P),partition(P).
reach(X,Z) :- reach(X,Y), reach(Y,Z).

contagious(P) :-  partition(P),reach(N1,N2):family(N1,N2,P),node(N1), node(N2) .
:- not contagious(1..p).

edge_q(P1,P2) :- partition(P1),partition(P2),edge(N1,N2),P1 != P2,node(N1),node(N2),belongs(N1,P1),belongs(N2,P2).
 
 
