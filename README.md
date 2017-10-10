# 2dgrid-game
A simple 2D tile based game framework. The scope of this project is to implement and optimize pathfinding algorithms on graphs.

## Getting Started
These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites
In order to compile and run the project you'll need to install additional dependencies manually.
First, you'll need to install maven for dependencies management and ant for building the whole project.
On debian based distro it will be:
```
$ sudo apt-get install maven ant
```

Then you'll have to fetch 2 additional dependency from the maven repository
```
$ mvn dependency:get -Dartifact=com.badlogicgames.gdx:gdx-ai:1.8.1
$ mvn dependency:get -Dartifact=commons-cli:commons-cli:1.4
```

### Installing

To clone the repository (install git if you don't have it already)

```
$ git clone http://github.com/n0tsaved/2dgrid-game
```
After cloning the repository you'll have to move under the cloned directory

```
$ cd 2dgrid-game
```
Now, to compile the project just run the following:

```
$ant -f 2dgrid-game.xml
```

### Running

To start the newly generated binary:
```
$ cd out/artifacts/2dgrid-game/
$ java -jar 2dgrid-game.jar
```
The currently available options are: 
```
-a;--algo {dijkstra, astar, theta, bidi, aastar}
-e;--entities [1-20]
-v;--verbose
-t;--show-trailmax
-b;--behaviour {chase, evade} (experimental: the user may experience glitches or crashes)
-h;--help 
```
