package Placement.Generation

import Mathematics.Points.Directions
import Placement.Templating.Template
import bwapi.Race

object Templates {

  val walkway: Template = new Template().add("-")

  val townhall = new Template().add(
    "Hxxx",
    "xxxx",
    "xxxx")

  // Generic defense for town hall areas
  val bases = Seq(
    new Template()
      .withMineralDirection(Directions.Left) // Aim heavy defenses right
      .add(
        "xxxxxxCx",
        "xxxxxxxx",
        "xxxxxxCx",
        "Cxxxxxxx",
        "xx2x--Cx",
        "--xx--xx",
        "CxHxxxCx",
        "xxxxxxxx",
        "--xxxxCx",
        "Cx-Pxxxx",
        "xx-xxxCx",
        "xx-xxxxx"),
    new Template() // Aim heavy defenses left
      .withMineralDirection(Directions.Right)
      .add(
        "Cxxxxxxx",
        "xxxxxxxx",
        "Cxxxxxxx",
        "xxxxxxCx",
        "CxPx--xx",
        "xxxx----",
        "CxHxxxCx",
        "xxxxxxxx",
        "Cxxxxx--",
        "xx-2xxCx",
        "Cx-xxxxx",
        "xx-xxxxx"),
    new Template() // Aim heavy defenses up
      .withMineralDirection(Directions.Down)
      .add(
        "CxCxCxCxCxCx",
        "xxxxxxxxxxxx",
        "xxPxHxxx2xxx",
        "xxxxxxxxxxxx",
        "x---xxxx---x",
        "x-Cx-Cx-Cx-x",
        "x-xx-xx-xx-x"),
    new Template() // Aim heavy defenses Down
      .withMineralDirection(Directions.Up)
      .add(
        "x-Cx-Cx-Cx-x",
        "x-xx-xx-xx-x",
        "x---Hxxx---x",
        "xxPxxxxx2xxx",
        "xxxxxxxxxxxx",
        "CxCxCxCxCxCx",
        "xxxxxxxxxxxx"),
    new Template() // Generic low-footprint template in case we have issues fitting the others
      .add(
        "Cx-2x-Cx",
        "xx-xx-xx",
        "--Hxxx--",
        "2xxxxx2x",
        "xxxxxxxx",
        "Cx-Px-Cx",
        "xx-xx-xx"))

  // Default Protoss main base town hall layouts, though also good for Terran.
  // Most main bases have gas directly above the town hall position.
  // Exceptions in BASIL map pool: Neo Moon Glaive @ 12, Tau Cross @ 5
  //
  val mainBases = Seq(
    // This one is adapted from ASL13 RO8 Rain vs. Soulkey game 3 @ 10:53 (1:02:04 on Tastosis VOD).
    new Template()
      .withRaces(Race.Terran, Race.Protoss)
      .withMineralDirection(Directions.Right)
      .withGasDirection(Directions.Up)
      .add(
        "-3xxGxxxCx-",
        "-xxxxxxxxx-",
        "-3xxPx--Cx-",
        "-xxxxx--xx-",
        "-----------",
        "4xxxHxxx---",
        "xxxxxxxxCx-",
        "xxxxxxxxCx-"),
    // This one is adapted from ASL13 RO8 Rain vs. Soulkey game 2 @ 7:47 (36:48 on Tastosis VOD).
    new Template()
      .withRaces(Race.Terran, Race.Protoss)
      .withMineralDirection(Directions.Left)
      .withGasDirection(Directions.Up)
      .add(
        "x-Gxxxxxxx",
        "x-xxxxxxxx",
        "x---------",
        "x-Cx--Txxx",
        "x-xx--xxxx",
        "--HxxxAxxx",
        "Cxxxxxxxxx",
        "xxxxxxxxxx",
        "--CxPx3xxx",
        "--xxxxxxxx")) ++ bases // Default to generic base layouts if needed

  val initialLayouts = Seq(
    new Template().add(
      "----------",
      "-4xxx4xxx-",
      "-xxxxxxxx-",
      "-xxxxxxxx-",
      "-TxxPxRxx-",
      "xxxxxxxxx-",
      "xxxxx-----"),
    new Template().add(
      "----------",
      "-4xxx4xxx-",
      "-xxxxxxxx-",
      "-xxxxxxxx-",
      "-RxxPxTxx-",
      "-xxxxxxxxx",
      "-----xxxxx")
  )

  val gateways = Seq(
    new Template().add(
      "xxx--------",
      "2x4xxx4xxx-",
      "xxxxxxxxxx-",
      "Pxxxxxxxxx-",
      "xx4xxx4xxx-",
      "2xxxxxxxxx-",
      "xxxxxxxxxx-",
      "xxx--------" ),
    new Template().add(
      "xxx----",
      "2x4xxx-",
      "xxxxxx-",
      "Pxxxxx-",
      "xx4xxx-",
      "2xxxxx-",
      "xxxxxx-",
      "xxx---- " ),
    new Template().add(
      "----------",
      "-4xxx4xxx-",
      "-xxxxxxxx-",
      "-xxxxxxxx-",
      "--xPx2xx--" ),
    new Template().add(
      "xx---------",
      "2x4xxx4xxx-",
      "xxxxxxxxxx-",
      "Pxxxxxxxxx-",
      "xx---------" ),
    new Template().add(
      "--------",
      "-4xxxPx-",
      "-xxxxxx-",
      "-xxxx2x-",
      "-4xxxxxx",
      "-xxxxPxx",
      "-xxxxxxx",
      "------xx "),
    new Template().add(
      "xxx----",
      "Px4xxx-",
      "xxxxxx-",
      "xxxxxx-",
      "xxx---- "),
    new Template().add(
      "Px2x-",
      "xxxx-",
      "4xxx-",
      "xxxx-",
      "xxxx-",
      "----- "),
    new Template().add(
      "-Px2x",
      "-xxxx",
      "-4xxx",
      "-xxxx",
      "-xxxx",
      "----- ")
  )

  val tech = Seq(
    new Template().add(
      "3xxPx3xx",
      "xxxxxxxx"),
    new Template().add(
      "3xx",
      "xxx",
      "3xx",
      "xxx",
      "Pxx",
      "xxx"))

  val batterycannon = Seq(
    new Template()
      .withExitDirection(Directions.Up)
      .add(
      "--------",
      "-BxxPxCx",
      "-xxxxxxx"),
    new Template()
      .withExitDirection(Directions.Down)
      .add(
      "CxPxBxx-",
      "xxxxxxx-",
      "--------"),
    new Template()
      .withExitDirection(Directions.Left)
      .add(
      "----",
      "-Bxx",
      "-xxx",
      "-Pxx",
      "-xxx",
      "-Cxx",
      "-xxx"),
    new Template()
      .withExitDirection(Directions.Right)
      .add(
        "----",
        "Bxx-",
        "xxx-",
        "xPx-",
        "xxx-",
        "xCx-",
        "xxx-"))
}
