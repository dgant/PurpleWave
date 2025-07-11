package Placement.Generation

import Mathematics.Points.Directions
import Placement.Access.PlaceLabels.{AnyProxy, DefendEntrance, DefendGround, Defensive, ProxyGround}
import Placement.Templating.Template
import bwapi.Race

object TemplatesProtoss {

  val walkway: Template = new Template().from("-")

  val townhall: Template = new Template().from(
    "Hxxx",
    "xxxx",
    "xxxx")

  // Generic defense for town hall areas
  val expansions: Seq[Template] = Seq(
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
        "x---xxxxxxxxx",
        "x-TxxGxxxCMx",
        "--xxxxxxxxxx",
        "-SIxxPM--CMU",
        "-xxxxxx--xx-",
        "-xxxx-------",
        "-SIxxHxxx---",
        "-xxxxxxxxCMI",
        "-xxxxxxxxxx-",
        "----xxxxxxxx"),
    // This one is adapted from ASL13 RO8 Rain vs. Soulkey game 2 @ 7:47 (36:48 on Tastosis VOD).
    new Template()
      .forRaces(Race.Protoss)
      .forMineralDirection(Directions.Left)
      .forGasDirection(Directions.Up)
      .from(
        "xxxxxxxx---",
        "CMUxxxSIxx-",
        "xxxxxxxxxx-",
        "CM----xxxx-",
        "xxPM--SIxx-",
        "--xx--xxxx-",
        "CMHxxxxxxx-",
        "xxxxxxTxxx-",
        "--Uxxxxxxx-",
        "--CM2UTxxx-",
        "--xxxxxxxx-",
        "xxxxxxx----")) ++ expansions // Default to generic base layouts if needed

  val initialLayouts: Seq[Template] = Seq(
    // It's critical that the Gateways not have tech buildings below them.
    // Gateway above Citadel-Pylon-Core can trap Dark Templar.
    new Template()
      .forExitDirection(Directions.Left, Directions.Down)
      .from(
        "-------------",
        "-4xxx-3xxRxx-",
        "-xxxx-xxxxxx-", // Extra column is because Citadel right of that top Gateway can trap units
        "-xxxx-Px3Ix--",
        "-4xxx-xxxxx-",
        "-xxxx-2xTxx-",
        "-xxxx-xxxxx-",
        "------------"),
    new Template()
      .forExitDirection(Directions.Right, Directions.Up)
      .from(
        "------------",
        "-RxxTxx4xxx-",
        "-xxxxxxxxxx-",
        "--3IxPxxxxx-",
        "x-xxxxx4xxx-",
        "x-2x3xxxxxx-",
        "x-xxxxxxxxx-",
        "x-----------"),
    new Template()
      .from(
        "----------",
        "-RxxPxTxx-",
        "-xxxxxxxx-",
        "-4xxx4xxx-",
        "-xxxxxxxx-",
        "-xxxxxxxx-",
        "----------"),
    new Template()
      .from(
        "-RxxPxTxx",
        "-xxxxxxxx",
        "-4xxx4xxx",
        "-xxxxxxxx-",
        "-xxxxxxxx-",
        "----------"))

  val production: Seq[Template] = Seq(
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

  val tech: Seq[Template] = Seq(
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

  val batterycannon: Seq[Template] = Seq(
    // Super spacious
    new Template()
      .forExitDirection(Directions.Left)
      .from(
        "-------",
        "-BxxCx-",
        "-xxxxx-",
        "--PxCx-",
        "x-xxxx-",
        "x------"),
    new Template()
      .forExitDirection(Directions.Right)
      .from(
        "-------",
        "-CxBxx-",
        "-xxxxx-",
        "-CxPx--",
        "-xxxx-",
        "------"),
    new Template()
      .forExitDirection(Directions.Up)
      .from(
        "-------",
        "-BxxPx-",
        "-xxxxx-",
        "--CxCx-",
        "x-xxxx-",
        "x------"),
    new Template()
      .forExitDirection(Directions.Down)
      .from(
        "x------",
        "x-CxCx-",
        "--xxxx-",
        "-BxxPx-",
        "-xxxxx-",
        "-------"),
    // Spacious
    new Template()
      .forExitDirection(Directions.Left)
      .from(
      "--------",
      "-BxxPxCx",
      "-xxxxxxx",
      "--------"),
    new Template()
      .forExitDirection(Directions.Right)
      .from(
      "--------",
      "CxPxBxx-",
      "xxxxxxx-",
      "--------"),
    new Template()
      .forExitDirection(Directions.Up)
      .from(
      "-----",
      "-Bxx-",
      "-xxx--",
      "-CxPx-",
      "-xxxx-"),
    new Template()
      .forExitDirection(Directions.Down)
      .from(
        "-CxPx-",
        "-xxxx-",
        "-Bxx--",
        "-xxx-",
        "-----"),
    // Less spacious
    new Template()
      .forExitDirection(Directions.Left)
      .from(
        "-BxxCxPx",
        "-xxxxxxx",
        "--------"),
    new Template()
      .forExitDirection(Directions.Right)
      .from(
        "PxCxBxx-",
        "xxxxxxx-",
        "--------"),
    new Template()
      .forExitDirection(Directions.Up)
      .from(
        "----",
        "-Bxx",
        "-xxx",
        "-PxCx",
        "-xxxx"),
    new Template()
      .forExitDirection(Directions.Down)
      .from(
        "CxPx-",
        "xxxx-",
        "Bxx--",
        "xxx-",
        "----"),
    // No battery, directionless
    new Template()
      .from(
        "CxPx-",
        "xxxx-",
        "-----"),
    new Template()
      .from(
        "-----",
        "PxCx-",
        "xxxx-"),
    new Template()
      .from(
        "---",
        "Cx-",
        "xx-",
        "Px-",
        "xx-"),
    new Template()
      .from(
        "Px-",
        "xx-",
        "Cx-",
        "xx-",
        "---"))
    .map(_.addLabels(Defensive, DefendEntrance, DefendGround))

  val proxy4: Seq[Template] = Seq(
    new Template()
      .from(
        "x----------x",
        "x-4xxx4xxx-x",
        "x-xxxxxxxx-x",
        "--xxxxxxxx--",
        "-4xxxPI4xxx-",
        "-xxxxxxxxxx-",
        "-xxxx--xxxx-",
        "------------"),
    new Template()
      .from(
        "xxxx------xx",
        "xxxx-4xxx-xx",
        "-----xxxx-xx",
        "-4xxxxxxxx--",
        "-xxxxPI4xxx-",
        "-xxxxxxxxxx-",
        "---4xxxxxxx-",
        "xx-xxxx-----",
        "xx-xxxx-xxxx",
        "xx------xxxx"),
    new Template()
      .from(
        "------------------",
        "-4xxx4xxx4xxx4xxx-",
        "-xxxxxxxxxxxxxxxx-",
        "-xxxxxxxxxxxxxxxx-",
        "--------Px--------",
        "xxxxxxx-xx-xxxxxxx",
        "xxxxxxx----xxxxxxxx"))
    .map(_.addLabels(AnyProxy, ProxyGround))

  val proxy2: Seq[Template] = Seq(
    new Template()
      .from(
        "------xx",
        "-4xxx-xx",
        "-xxxx-xx",
        "-xxxx---",
        "-Px4xxx-",
        "-xxxxxx-",
        "---xxxx-",
        "x-------"),
    new Template()
      .from(
        "xx------",
        "xx-4xxx-",
        "xx-xxxx-",
        "---xxxx-",
        "-4xxxPx-",
        "-xxxxxx-",
        "-xxxx---",
        "------xx"),
    new Template()
      .from(
        "------xx",
        "-4xxx---",
        "-xxxxPx-",
        "-xxxxxx-",
        "--4xxx--",
        "x-xxxx-x",
        "x-xxxx-x",
        "x------x"),
    new Template()
      .from(
        "xx------",
        "---4xxx-",
        "-Pxxxxx-",
        "-xxxxxx-",
        "--4xxx--",
        "x-xxxx-x",
        "x-xxxx-x",
        "xx-----x"),
    new Template()
      .from(
        "------------",
        "-Px4xxx4xxx-",
        "-xxxxxxxxxx-",
        "---xxxxxxxx-",
        "xx----------"),
    new Template()
      .from(
        "------",
        "-4xxx-",
        "-xxxx-",
        "--Px--",
        "-4xxx-",
        "-xxxx-",
        "-xxxx-",
        "------"))
    .map(_.addLabels(AnyProxy, ProxyGround))
}
