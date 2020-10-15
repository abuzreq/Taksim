# Taksim

This is the source code for Taksim, a system for procedurally game levels generation. The paper describing Taksim and explaining how is:

Abuzuraiq, A. M., Ferguson, A., & Pasquier, P. (2019, August). [Taksim: A Constrained Graph Partitioning Framework for Procedural Content Generation](https://abuzreq.netlify.app/pdfs/inproceedings/taksim_2019.pdf). In 2019 IEEE Conference on Games (CoG) (pp. 1-8). IEEE.

An accompanying presentation is accesible [Here](https://www.youtube.com/watch?v=Bu3m_7-3Tm4&list=FL-Z_nrHntYILlQn017zopHQ&index=2&t=2331s)

The basic idea is to partition a game space (e.g. a rectangular grid or a Voronoi diagram) such that the adjacency between the partitions are described by a constraint graph. This has many applications like generating a dungeon with a predetermined sequence of in-game missions or generating poltitical maps (like Risk) but being able to control which countried will be adjacent to which.
An earlier paper by myself introduced the concept and implemented it in A* search. The source code for that paper is also available in this repo [as well as seperarly in older repo.](https://github.com/abuzreq/ConstrainedGraphPartitioning). 

The new code in the repo involves using Answer Set Programming (or ASP) to achieve the same goal but with more speed and flexibility. I had an intention of refining the source code before publishing it, but that took a long time and I did not get the time for it. So here it is anyway. 


# Setup
The following are the intstructions for starting the project. I have tested these settings in [Eclipse IDE](https://www.eclipse.org/downloads/) both on a linux and windows operating systems. 

The repo contains two folders: *ASP_ContrainedGraph_Partitioning* and *ASP*. The first contains the A* and ASP implemenations and examples. The second contrains a libarary for briding the gap between the example <--and--> the ASP constraints.
Pre-conditions:
- Install Eclipse
- [Have Gradle Integration with Eclipse installed](https://www.vogella.com/tutorials/EclipseGradle/article.html)

Steps:
1. clone the repo
2. import the folder *ASP_ContrainedGraph_Partitioning* using the Import Existing Gradle option in Eclipse under File->Import->Gradle
3. import the folder *ASP* using the Import Existing Gradle option in Eclipse under File->Import->
By now you should have two projects in Eclipse: *ASP_ContrainedGraph_Partitioning* and *asp4j-master*
4. right-click on *ASP_ContrainedGraph_Partitioning* and select Grade -> Refresh Gradle Project
4.b if you faced this error "cannot nest ...." then [follow this link](https://stackoverflow.com/questions/39466094/eclipse-buildship-plugin-nesting-source-folders)

