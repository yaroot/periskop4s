lazy val root = project
  .in(file("."))
  .aggregate(core)

val CatsEffectVersion = "3.3.7"
val Http4sVersion     = "0.23.10"
val JawnVersion       = "1.3.2"
val MUnitVersion      = "0.7.29"
val Specs2Version     = "4.10.5"

val commonWarts = Seq(
  Wart.AsInstanceOf,
  Wart.EitherProjectionPartial,
  Wart.Null,
  Wart.OptionPartial,
  Wart.Product,
  Wart.Return,
  // Wart.TraversableOps,
  Wart.TryPartial,
  Wart.Var
)

lazy val commonSettings = Seq(
  organization        := "periskop4s",
  scalaVersion        := "2.13.8",
  run / fork          := true,
  addCompilerPlugin("org.typelevel" % "kind-projector"     % "0.13.2" cross CrossVersion.full),
  addCompilerPlugin("com.olegpy"   %% "better-monadic-for" % "0.3.1" cross CrossVersion.binary),
  scalacOptions += "-Ymacro-annotations",
  scalacOptions += "-Xsource:3",
  Test / scalacOptions -= "-Xfatal-warnings",
  testFrameworks += new TestFramework("munit.Framework"),
  scalafmtOnCompile   := true,
  Global / cancelable := true,
  javaOptions ++= Seq(
    "-XX:+UseG1GC",
    "-Xmx600m",
    "-Xms600m",
    "-XX:SurvivorRatio=8",
    "-Duser.timezone=UTC"
  ),
  // Compile / wartremoverErrors := commonWarts,
  version ~= (_.replace('+', '-')),
  dynver ~= (_.replace('+', '-'))
)

lazy val core = project
  .in(file("core"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      /* "org.http4s"               %% "http4s-dsl"                   % Http4sVersion, */
      /* "org.http4s"               %% "http4s-circe"                 % Http4sVersion, */
      /* "org.http4s"               %% "http4s-prometheus-metrics"    % Http4sVersion, */
      /* "org.http4s"               %% "http4s-ember-server"          % Http4sVersion, */
      /* "org.typelevel"            %% "cats-effect"                  % CatsEffectVersion, */
      "org.typelevel" %% "jawn-parser" % JawnVersion,
      "org.typelevel" %% "jawn-ast"    % JawnVersion,
      "org.scalameta" %% "munit"       % MUnitVersion  % Test,
      "org.specs2"    %% "specs2-core" % Specs2Version % Test,
      "org.specs2"    %% "specs2-mock" % Specs2Version % Test
    )
  )
