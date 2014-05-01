# Name is to be decided
*A process oriented game engine*

## Motivation & inspiration

Clojure, being a highly interactive, functional and generally awesome Lisp shows a lot of promise for being used in games and similar systems. That said, my earlier attempts trying to build a functional game engine in Clojure have not been as successful as I had hoped. While a lot of the bad properties of OO are definitely aleviated by FP, the rigidness of 'a world in an atom' is annoying and limiting during the development phase (when everything is working correctly it's great though, and the code is very clean).

Since all updating functions have to return a the new state, forgetting to do so has big consequenses and potentially ruins all the state built up inside the current session. This is not acceptable when a big appeal of using Clojure is the REPL experience. In general we want the game engine to be very robust so that typos and other simple programming errors do not force us to restart the whole application. When an error occurs the faulty part should pause while the rest of the system should try to function normally. After fixing the errors in the sub-system it should restart.

The most important inspiration for this approach is definitely Erlang, a programming language focused on robustness. The idea is to let every game entity (for example the player or each AI character) be represented by its own independent process. The same goes for different states in the game, like levels or different screens. If one of these processes throws an error it will stop and display its problem. When the programmer has fixed the bug she/he can tell the process (or any of its parent processes) to restart. Any state that the process had already built up will then be preserved and in most cases the system can just keep on going.

Ideally a process also has built-in strategies for restarting its child processes so that the application can be very robust even when not being controlled by the programmer. If for example one decorative item in the game starts throwing null pointer exceptions its probably more fun for the player if that object is restarted (or turned off) than that the whole application crashes.

## Supporting libraries

### Quil
For drawing, input, etc. Awesome in its own right!

### Core Async
To create the processes and build an Erlang-like environment for the game objects.

### Core Match
For pattern matching against messages sent to the game objects.

### Marginalia
For documentation.

## Usage

### Project.clj

```clojure
[???]
```

###