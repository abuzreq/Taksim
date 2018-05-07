node(1..3).
edge(1,2).
edge(2,3).
col(r;g).
1{color(X,C):col(C)}1 :- node(X).
:- edge(X,Y), color(X,C),color(Y,C).