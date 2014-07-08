Scrambl
=======

Scrambl is a scala + libgdx playground and game engine stub.

It runs on android, windows, linux and mac (probably).

Cool features: 2D hexagonal grid, customizable opengl shaders, flake generation algorithm.

Requirements
------------

To build, the following are required:

- Java 7

- ANDROID_HOME env var set. It needs only be properly set if actually building for android; otherwise any value will do.

- SBT

Usage
-----

Run on desktop: sbt desktop/run

Run on plugged android device: sbt android/start

Package desktop (cross platform) app: sbt assembly

Package android app: sbt android/package

Mains
-----

Several mains are available to run in on desktop:

- Roaming: the only 'completed' game. It implements the simplest gameplay (build a flake), intended as a proof of concept. It's the application actually packed in the android builds.

- Main: the original 'scrambl' game, left as stub for now, where a flake is scrambled and must be repaired.

- Flake explorer: a tool to test renderings and flake generation. Commands: TAB to toggle the gui, SPACE to regenerate flake, RIGHT and LEFT to navigate through predefined renderings, R to randomize rendering.

IDE
---

"sbt gen-idea" and "eclipse" should work fine.

Feature ideas / future improvements
-----------------------------------

- Improve test coverage

- Try scala improved collections such as Scalaxy or Scala Blitz

- Allow for full customization of renderings in "flake explorer", with export.

- Allow import of custom renderings and flakes.

- Add sounds, in the spirit of [the tone matrix](http://tonematrix.audiotool.com/), each step of a flake having its tone, a dying branch making dissonant sounds.

- Add a leader board and difficulty levels.

- Deploy to app store?

- Port engine to javascript/webgl.