import sbt._
import Keys._
import Defaults._

import sbtandroid.AndroidPlugin._

object Settings {
  lazy val desktopJarName = SettingKey[String]("desktop-jar-name", "name of JAR file for desktop")

  lazy val nativeExtractions = SettingKey[Seq[(String, NameFilter, File)]]("native-extractions", "(jar name partial, sbt.NameFilter of files to extract, destination directory)")

  lazy val common = Defaults.defaultSettings ++ Seq(
    version := "0.1",
    scalaVersion := "2.10.4",
    javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.7", "-target", "1.7"),
    scalacOptions ++= Seq("-encoding", "UTF-8", "-target:jvm-1.7"),
    javacOptions += "-Xlint",
    scalacOptions ++= Seq("-Xlint", "-Ywarn-dead-code", "-Ywarn-value-discard", "-unchecked", "-deprecation", "-feature"),
    libraryDependencies ++= Seq(
      "org.scalatest" % "scalatest_2.10" % "2.2.0" % "test"
    )
  )

  lazy val commonGdx = common ++ Seq(
    resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
    libraryDependencies ++= Seq(
      "com.badlogicgames.gdx" % "gdx" % "1.2.0"
      //,"com.typesafe.akka" %% "akka-actor" % "2.3.2"
    ),
    cancelable := true,
    proguardOptions <<= baseDirectory { (b) => Seq(
      scala.io.Source.fromFile(file("commonGdx/src/main/proguard.cfg")).getLines().map(_.takeWhile(_!='#')).filter(_!="").mkString("\n"), {
        val path = b/"src/main/proguard.cfg"
        if (path.exists()) {
          scala.io.Source.fromFile(b/"src/main/proguard.cfg").getLines().map(_.takeWhile(_!='#')).filter(_!="").mkString("\n")
        } else {
          ""
        }
      }
    )}
  )

  lazy val desktop = commonGdx ++ Seq(
    unmanagedResourceDirectories in Compile += file("commonGdx/src/main/resources"),
    fork in Compile := true,
    libraryDependencies ++= Seq(
      "net.sf.proguard" % "proguard-base" % "4.8" % "provided",
      "com.badlogicgames.gdx" % "gdx-backend-lwjgl" % "1.2.0",
      "com.badlogicgames.gdx" % "gdx-tools" % "1.2.0" % "compile",
      "com.badlogicgames.gdx" % "gdx-platform" % "1.2.0" classifier "natives-desktop"
    ),
    Tasks.assembly,
    desktopJarName := "scrambl"
  )

  lazy val android = commonGdx ++ Tasks.natives ++ Seq(
    versionCode := 0,
    keyalias := "change-me",
    platformName := "android-19",
    mainAssetsPath in Compile := file("commonGdx/src/main/resources"),
    unmanagedJars in Compile <+= libraryJarPath (p => Attributed.blank(p)) map( x=> x),
    libraryDependencies ++= Seq(
      "com.badlogicgames.gdx" % "gdx-backend-android" % "1.2.0",
      "com.badlogicgames.gdx" % "gdx-platform" % "1.2.0" % "natives" classifier "natives-armeabi",
      "com.badlogicgames.gdx" % "gdx-platform" % "1.2.0" % "natives" classifier "natives-armeabi-v7a"
    ),
    nativeExtractions <<= baseDirectory { base => Seq(
      ("natives-armeabi.jar", new ExactFilter("libgdx.so"), base / "lib" / "armeabi"),
      ("natives-armeabi-v7a.jar", new ExactFilter("libgdx.so"), base / "lib" / "armeabi-v7a")
    )}
  )
}

object Tasks {
  import java.io.{File => JFile}
  import Settings.desktopJarName
  import Settings.nativeExtractions

  lazy val extractNatives = TaskKey[Unit]("extract-natives", "Extracts native files")

  lazy val natives = Seq(
    ivyConfigurations += config("natives"),
    nativeExtractions := Seq.empty,
    extractNatives <<= (nativeExtractions, update) map { (ne, up) =>
      val jars = up.select(Set("natives"))
      ne foreach { case (jarName, fileFilter, outputPath) =>
        jars find(_.getName.contains(jarName)) map { jar =>
            IO.unzip(jar, outputPath, fileFilter)
        }
      }
    },
    compile in Compile <<= (compile in Compile) dependsOn extractNatives
  )

  lazy val assemblyKey = TaskKey[Unit]("assembly", "Assembly desktop using Proguard")

  lazy val assembly = assemblyKey <<= (fullClasspath in Runtime, // dependency to make sure compile finished
      target, desktopJarName, version, // data for output jar name
      proguardOptions, // merged proguard.cfg from commonGdx and desktop
      javaOptions in Compile, managedClasspath in Compile, // java options and classpath
      classDirectory in Compile, dependencyClasspath in Compile, update in Compile, // classes and jars to proguard
      streams) map { (c, target, name, ver, proguardOptions, options, cp, cd, dependencies, up, s) =>
    val provided = Set(up.select(Set("provided")):_*)
    val compile = Set(up.select(Set("compile")):_*)
    val runtime = Set(up.select(Set("runtime")):_*)
    val optional = Set(up.select(Set("optional")):_*)
    val onlyProvidedNames = provided -- compile -- runtime -- optional
    val (onlyProvided, withoutProvided) = dependencies.partition(cpe => onlyProvidedNames contains cpe.data)
    val exclusions = Seq("!META-INF/MANIFEST.MF", "!library.properties").mkString(",")
    val inJars = withoutProvided.map("\""+_.data.absolutePath+"\"("+exclusions+")").mkString(JFile.pathSeparator)
    val libraryJars = onlyProvided.map("\""+_.data.absolutePath+"\"").mkString(JFile.pathSeparator)
    val outfile = "\""+(target/"%s-%s.jar".format(name, ver)).absolutePath+"\""
    val classfiles = "\"" + cd.absolutePath + "\""
    val manifest = "\"" + file("desktop/src/main/manifest").absolutePath + "\""
    val proguard = options ++ Seq("-cp", Path.makeString(cp.files), "proguard.ProGuard") ++ proguardOptions ++ Seq(
      "-injars", classfiles,
      "-injars", inJars,
      "-injars", manifest,
      "-libraryjars", libraryJars,
      "-outjars", outfile)
   
    s.log.info("preparing proguarded assembly")
    s.log.debug("Proguard command:")
    s.log.debug("java "+proguard.mkString(" "))
    val exitCode = Process("java", proguard) ! s.log
    if (exitCode != 0) {
      sys.error("Proguard failed with exit code [%s]" format exitCode)
    } else {
      s.log.info("Output file: "+outfile)
    }
  }
}

object LibgdxBuild extends Build {
  lazy val common = Project(
    "common",
    file("common"),
    settings = Settings.common)

  lazy val commonGdx = Project(
    "commonGdx",
    file("commonGdx"),
    settings = Settings.commonGdx)
    .dependsOn(common)

  lazy val desktop = Project(
    "desktop",
    file("desktop"),
    settings = Settings.desktop)
    .dependsOn(commonGdx)

  lazy val android = AndroidProject(
    "android",
    file("android"),
    settings = Settings.android)
    .dependsOn(commonGdx)

  lazy val all = Project(
    "all-platforms",
    file("."),
    settings = Settings.commonGdx)
    .aggregate(common, desktop, android)
}
