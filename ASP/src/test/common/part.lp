partition(Clusters):-
	bagof(N,node(N),Nodes),delete(X,Nodes,RestOfNodes),
	partition([X],RestOfNodes,[],Clusters).
partition([]).