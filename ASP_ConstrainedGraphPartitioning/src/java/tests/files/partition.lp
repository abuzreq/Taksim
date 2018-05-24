#const n=0.
#const p=0.
#const r=0. % if r = 0, an additional partition(0) is added and used as a collector for removed nodes, contagiousness is not imposed on this partition

%node(0..n-1).

partition(r..p).
partition2(1..p).

1{belongs(N,P):partition(P)}1 :- node(N).

%n{belongs(N,P):partition(P),node(N)}n.  
%:- belongs(N1,P1),belongs(N2,P2),N1==N2,P1!=P2,partition(P1),partition(P2),node(N1),node(N2).




%N1 and N2 belong to the same partition
family(N1,N2,P) :- belongs(N1,P),belongs(N2,P),partition(P), node(N1), node(N2),N1 < N2,not family(N2,N1,P).


reach(X,Y) :- edge(X,Y),family(X,Y,P),partition(P).
reach(X,Z) :- reach(X,Y), reach(Y,Z).

%a partition is contagious if for any two nodes in it, they are reachable through edges that pass through nodes that are in turn in the partition

contiguous(P) :-  partition(P),reach(N1,N2):family(N1,N2,P),node(N1), node(N2) .
:- not contiguous(1..p).
edge_q(P1,P2) :- partition(P1),partition(P2),edge(N1,N2),P1 != P2,node(N1),node(N2),belongs(N1,P1),belongs(N2,P2).


%count(P,T) :- T = #count{N:belongs(N,P),node(N)},partition(P).


count_borders_supp(P1,P2,T):- T = #count{N1:
	belongs(N1,P1),belongs(N2,P2),
	edge(N1,N2),node(N1),node(N2)},
	partition(P1),partition(P2),P1 != P2,
	P1 != 0, P2 != 0.


count_borders(P1,P2,T) :- 
	T = T1+T2,
	count_borders_supp(P1,P2,T1),
	count_borders_supp(P2,P1,T2),
	partition(P1),partition(P2),P1<P2.

#minimize {T@1:
	count_borders(P1,P2,T),
	partition(P1),partition(P2)}.
	
%count_borders(P1,P2,T):- T = T1+T2,count_borders1(P1,P2,T1),count_borders2(P1,P2,T2).partition(P1),partition(P2).
%#minimize {(N1+N2)@1:partition(P1),partition(P2),edge(N1,N2),P1 != P2,node(N1),node(N2),belongs(N1,P1),belongs(N2,P2)}.





