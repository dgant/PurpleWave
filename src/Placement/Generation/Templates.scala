package Placement.Generation

import Mathematics.Points.Directions
import Placement.Access.PlaceLabels.{DefendEntrance, DefendGround, Defensive}
import Placement.Templating.Template
import bwapi.Race

object Templates {

  val walkway: Template = new Template().from("-")

  val townhall: Template = new Template().from(
    "Hxxx",
    "xxxx",
    "xxxx")

  // Generic defense for town hall areas
  val bases: Seq[Template] = Seq(
    /////////////////////////
    // Wide heavy defenses //
    /////////////////////////
    new Template() // Aim heavy defenses right
      .forMineralDirection(Directions.Left)
      .from(
        "xxxxxxCN",
        "xxxxxxxx",
        "xxxxxxCN",
        "CMUxxxxx",
        "xxPMNICN",
        "--xx--xx",
        "CMHxxxCN",
        "xxxxxxxx",
        "--xxxxCN",
        "CM-PNxxx",
        "xx-xxxCN",
        "xx-xxxxx"),
    new Template() // Aim heavy defenses left
      .forMineralDirection(Directions.Right)
      .from(
        "CNxxxxxx",
        "xxxxxxxx",
        "CNxxxxxx",
        "xxxxxxCM",
        "CNPMNIxx",
        "xxxx----",
        "CNHxxxCMI",
        "xxxxxxxx",
        "CNxxxx--",
        "xx-PN-CMU",
        "CN-xx-xx",
        "xx-xx-xx"),
    new Template() // Aim heavy defenses up
      .forMineralDirection(Directions.Down)
      .from(
        "CNCNCNCNCNCN",
        "xxxxxxxxxxxx",
        "xxPNHxxxPMNI",
        "xxxxxxxxxxxx",
        "x---xxxx---x",
        "x-CM-CMICMUx",
        "x-xx-xx-xx-x"),
    new Template() // Aim heavy defenses Down
      .forMineralDirection(Directions.Up)
      .from(
        "x-CM-CMICM-x",
        "x-xx-xx-xx-x",
        "x---Hxxx---x",
        "xxPMNIxxPNxx",
        "xxxxxxxxxxxx",
        "CNCNCNCNCNCN",
        "xxxxxxxxxxxx"),
    ///////////////////////////
    // Narrow heavy defenses //
    ///////////////////////////
    new Template() // Aim heavy defenses right
      .forMineralDirection(Directions.Left)
      .from(
        "xxxxxxCN",
        "CMxxxxxx",
        "xxPMNICN",
        "--xx--xx",
        "CMHxxxCNI",
        "xxxxxxxx",
        "CMUxxxCN",
        "xx----xx"),
    new Template() // Aim heavy defenses left
      .forMineralDirection(Directions.Right)
      .from(
        "CNxxxxxx",
        "xxxxxxxx",
        "CNPMNIxx",
        "xxxx--CMI",
        "CNHxxxxx",
        "xxxxxx--",
        "CNxxxxCM",
        "xx----xx"),
    new Template() // Aim heavy defenses up
      .forMineralDirection(Directions.Down)
      .from(
        "CNCNCNCN",
        "xxxxxxxx",
        "PNHxxxPMNI",
        "xxxxxxxx",
        "--xxxx--",
        "CM-CMICM",
        "xx-xx-xx"),
    new Template() // Aim heavy defenses Down
      .forMineralDirection(Directions.Up)
      .from(
        "CM-CMICM",
        "xx-xx-xx",
        "--Hxxx--",
        "PMNIxxPN",
        "xxxxxxxx",
        "CNCNCNCN",
        "xxxxxxxx"),
  ////////////////////////
  // Miniature defenses //
  ////////////////////////
    new Template() // Aim heavy defenses right
      .forMineralDirection(Directions.Left)
      .from(
        "CMPMNICN",
        "xxxx--xx",
        "--HxxxCN",
        "CMIxxxxx",
        "xxxxxxCN",
        "---xxxxx"),
    new Template() // Aim defenses left
      .forMineralDirection(Directions.Right)
      .from(
        "CNPMNICM",
        "xxxx--xx",
        "CNHxxx--",
        "xxxxxxCMI",
        "CNxxxxxx",
        "xxx-----"),
    new Template() // Aim defenses up
      .forMineralDirection(Directions.Down)
      .from(
        "CNCNCN",
        "xxxxxx",
        "HxxxPMNI",
        "xxxxxx",
        "xxxx--",
        "CMICM-",
        "xx-xx-"),
    new Template() // Aim defenses Down
      .forMineralDirection(Directions.Up)
      .from(
        "CM-CM-",
        "xx-xx-",
        "HxxxPMNI",
        "xxxxxx",
        "xxxx--",
        "CNCNCN",
        "xxxxxx"),
    ////////////////////////////////////
    // Very generic miniature defense //
    ////////////////////////////////////
    new Template()
      .from(
        "CD-PD-CD",
        "xx-xx-xx",
        "--Hxxx--",
        "CDxxxxCD",
        "xxxxxxxx",
        "CD-PD-CD",
        "xx-xx-xx"))

  // Default Protoss main base town hall layouts, though also good for Terran.
  // Most main bases have gas directly above the town hall position.
  // Exceptions in BASIL map pool: Neo Moon Glaive @ 12, Tau Cross @ 2, Tau Cross @ 5
  //
  val mainBases: Seq[Template] = Seq(
    // This one is adapted from ASL13 RO8 Rain vs. Soulkey game 3 @ 10:53 (1:02:04 on Tastosis VOD).
    new Template()
      .forRaces(Race.Protoss)
      .forMineralDirection(Directions.Right)
      .forGasDirection(Directions.Up)
      .from(
        "-TxxGxxxCMx",
        "-xxxxxxxxxx",
        "SIxxPM--CMU",
        "xxxxxx--xx-",
        "xxxx-------",
        "SIxxHxxx---",
        "xxxxxxxxCMI",
        "xxxxxxxxxx-"),
    // This one is adapted from ASL13 RO8 Rain vs. Soulkey game 2 @ 7:47 (36:48 on Tastosis VOD).
    new Template()
      .forRaces(Race.Protoss)
      .forMineralDirection(Directions.Left)
      .forGasDirection(Directions.Up)
      .from(
        "CMIxxxSIxx",
        "xxxxxxxxxx",
        "x-----xxxx",
        "x-PM--SIxx",
        "x-xx--xxxx",
        "--Hxxxxxxx",
        "CMxxxxTxxx",
        "xxxxxxxxxx",
        "--CM2UTxxx",
        "--xxxxxxxx")) ++ bases // Default to generic base layouts if needed


  val initialLayouts = Seq(
    // It's critical that the Gateways be on the bottom.
    // Gateway above Citadel-Pylon-Core can trap Dark Templar.
    new Template().from(
      "xxxxxx---",
      "xTxxPxRxx-",
      "xxxxxxxxx-",
      "x4xxx4xxx-",
      "-xxxxxxxx-",
      "-xxxxxxxx-",
      "----------"),
    new Template().from(
      "----xxxxxx",
      "-RxxPxTxxx",
      "-xxxxxxxxx",
      "-4xxx4xxxx",
      "-xxxxxxxx-",
      "-xxxxxxxx-",
      "----------")
  )

  val gateways = Seq(
    new Template().from(
      "xxx--------",
      "2x4xxx4xxx-",
      "xxxxxxxxxx-",
      "Pxxxxxxxxx-",
      "xx4xxx4xxx-",
      "2xxxxxxxxx-",
      "xxxxxxxxxx-",
      "xxx--------" ),
    new Template().from(
      "xxx----",
      "2x4xxx-",
      "xxxxxx-",
      "Pxxxxx-",
      "xx4xxx-",
      "2xxxxx-",
      "xxxxxx-",
      "xxx---- " ),
    new Template().from(
      "----------",
      "-4xxx4xxx-",
      "-xxxxxxxx-",
      "-xxxxxxxx-",
      "--xPx2xx--" ),
    new Template().from(
      "xx---------",
      "2x4xxx4xxx-",
      "xxxxxxxxxx-",
      "Pxxxxxxxxx-",
      "xx---------" ),
    new Template().from(
      "--------",
      "-4xxxPx-",
      "-xxxxxx-",
      "-xxxx2x-",
      "-4xxxxxx",
      "-xxxxPxx",
      "-xxxxxxx",
      "------xx "),
    new Template().from(
      "xxx----",
      "Px4xxx-",
      "xxxxxx-",
      "xxxxxx-",
      "xxx---- "),
    new Template().from(
      "Px2x-",
      "xxxx-",
      "4xxx-",
      "xxxx-",
      "xxxx-",
      "----- "),
    new Template().from(
      "-Px2x",
      "-xxxx",
      "-4xxx",
      "-xxxx",
      "-xxxx",
      "----- ")
  )

  val tech = Seq(
    new Template().from(
      "---------",
      "3xxPx3xx-",
      "xxxxxxxx-"),
    new Template().from(
      "----",
      "3xx-",
      "xxx-",
      "3xx-",
      "xxx-",
      "Pxx-",
      "xxx-"))

  val batterycannon = Seq(
    new Template()
      .addLabels(Defensive, DefendEntrance, DefendGround)
      .forExitDirection(Directions.Left)
      .from(
      "--------",
      "-BxxPxCx",
      "-xxxxxxx",
      "--------"),
    new Template()
      .addLabels(Defensive, DefendEntrance, DefendGround)
      .forExitDirection(Directions.Right)
      .from(
      "--------",
      "CxPxBxx-",
      "xxxxxxx-",
      "--------"),
    new Template()
      .addLabels(Defensive, DefendEntrance, DefendGround)
      .forExitDirection(Directions.Up)
      .from(
      "-----",
      "-Bxx-",
      "-xxx--",
      "-CxPx-",
      "-xxxx-"),
    new Template()
      .addLabels(Defensive, DefendEntrance, DefendGround)
      .forExitDirection(Directions.Down)
      .from(
        "-CxPx-",
        "-xxxx-",
        "-Bxx--",
        "-xxx-",
        "-----"))
}
