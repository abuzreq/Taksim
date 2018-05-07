#const width=10.
param("width",width).
dim(1..width).
tile((X,Y)) :- dim(X),dim(Y).

adj((X1,Y1),(X2,Y2)) :- tile((X1,Y1)),tile((X2,Y2)),|X2-X1| + |Y2-Y1| == 1.

start((1,1)).
finish((width,width)).

0 {sprite(T,wall;gem;altar)} 1 :- tile(T).

%:- not 1 {sprite(T,altar)} 1.
%:- not 1 {sprite(T,gem)} 1.