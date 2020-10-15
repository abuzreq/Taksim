edge(1,2).
edge(2,3).
edge(3,4).
edge(4,5).
edge(5,6).
edge(6,7).
edge(7,8).
edge(8,9).
edge(2,5).
edge(1,6).
edge(5,8).
edge(4,9).

:-not edge_q(1,2).
:-not edge_q(1,3).
:-not edge_q(2,4).
:-not edge_q(3,4).
:-edge_q(1,4).
:-edge_q(2,3).

