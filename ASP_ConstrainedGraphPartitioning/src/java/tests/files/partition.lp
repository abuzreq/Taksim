#const n=0.
#const p=0.
#const r=0. % if r = 0, an additional partition(0) is added and used as a collector for removed nodes, contagiousness is not imposed on this partition

%node(0..n-1).

partition(r..p).

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
%:- contiguous(0).
edge_q(P1,P2) :- partition(P1),partition(P2),edge(N1,N2),P1 != P2,node(N1),node(N2),belongs(N1,P1),belongs(N2,P2).

%#minimize {(N1+N2)@1:partition(P1),partition(P2),edge(N1,N2),P1 != P2,node(N1),node(N2),belongs(N1,P1),belongs(N2,P2)}.
%#minimize {N: belongs(N,P),node(N),P==0}.


%TODO reduce redundancy in contains vs belongs,
%Constraints:
%(Hard const) requiring certain nodes to belong to partitions (:- belongs(1,1).)
%(Soft const) partition sizes: e.g. has a minimum , be balanced? ,follows a provided ratio (property-percentage const.)
%(Soft const) related to above, minimize removed nodes
% mapping constraint: using lots of node-in-partition constraints, 
%but with possibly as additional constraint to require the this partition doesn't grow any larger



