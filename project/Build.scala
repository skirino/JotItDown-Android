import sbt._

import Keys._
import AndroidKeys._

object General {
  val settings = Defaults.defaultSettings ++ Seq(
    name                    := "JotItDown",
    version                 := "0.1",
    versionCode             := 0,
    scalaVersion            := "2.10.2",
    platformName in Android := "android-15",
    javaOptions             += "-Xmx1G",
    javacOptions           ++= Seq("-encoding", "UTF-8", "-source", "1.6", "-target", "1.6"),
    resolvers              ++= Seq(
      "ActionBarSherlock snapshots" at "http://r.jakewharton.com/maven/snapshot/",
      "spray"                       at "http://repo.spray.io/"
    )
  )

  val pgOptions = Seq(
    "-dontnote **",
    "-keep class scala.collection.immutable.StringLike { *; }",
    "-keep class scala.collection.SeqLike { public protected *; }",
    "-keep class     android.app.** { *; }",
    "-keep interface android.app.** { *; }",
    "-keep class     android.support.v4.app.** { *; }",
    "-keep interface android.support.v4.app.** { *; }",
    "-keep class     com.actionbarsherlock.** { *; }",
    "-keep interface com.actionbarsherlock.** { *; }"
  ).mkString(" ")

  val proguardSettings = Seq(
    useProguard in Android    := true,
    proguardOption in Android := pgOptions
  )

  lazy val fullAndroidSettings =
    General.settings                  ++
    AndroidProject.androidSettings    ++
    TypedResources.settings           ++
    proguardSettings                  ++
    AndroidManifestGenerator.settings ++
    AndroidMarketPublish.settings     ++ Seq(
      keyalias in Android  := "change-me",
      libraryDependencies ++= Seq(
        "org.scalatest"           %% "scalatest"        % "1.9.1" % "test",
        "org.scaloid"             %% "scaloid"          % "2.0-8",
        "com.actionbarsherlock"   %  "library"          % "4.0.0-SNAPSHOT" artifacts(Artifact("library", "apklib", "apklib")),
        "android"                 %  "compatibility-v4" % "r3-SNAPSHOT",
        "io.spray"                %% "spray-json"       % "1.2.5"
      )
    )
}

object AndroidBuild extends Build {
  lazy val main = Project(
    "JotItDown",
    file("."),
    settings = General.fullAndroidSettings
  )

  lazy val tests = Project(
    "tests",
    file("tests"),
    settings = General.settings            ++
               AndroidTest.androidSettings ++
               General.proguardSettings    ++ Seq(name := "JotItDownTests")
  ) dependsOn main
}
