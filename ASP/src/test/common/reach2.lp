#const width = 3.
dim(1..width).

1 { parent(X,Y,1,0),
	parent(X,Y,0,1),
	parent(X,Y,-1,0),
	parent(X,Y,0,-1)} 1 :- dim(X),dim(Y),(X,Y) != 1.
	
linked(1,1).
linked(X,Y) :- parent(X,Y,DX,DY),linked(X+DX,Y+DY).
:- dim(X,Y),not linked(X,Y).

vertical(X,Y) :- parent(X,Y,0, 1).
vertical(X,Y) :- parent(X,Y,0,-1).
#minimize {vertical(X,Y)}.