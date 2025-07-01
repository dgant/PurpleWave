package Placement.Generation

import Mathematics.Points.Directions
import Placement.Access.PlaceLabels._
import Placement.Templating.Template

object TemplatesZerg {

  val expansions: Seq[Template] = Seq(
    new Template()
      .forExitDirection(Directions.Left)
      .from(
        "CN-",
        "xx-CM-CM-",
        "CN-xx-xx-",
        "xx-Hxxx--",
        "CN-xxxxCM",
        "xx-xxxxxx",
        "CN-------",
        "xx-CM-CM-",
        "CN-xx-xx-",
        "xx-",
        "---"),
    new Template()
      .forExitDirection(Directions.Left)
      .from(
        "xx-CM-CM-",
        "CN-xx-xx-",
        "xx-Hxxx--",
        "CN-xxxxCM",
        "xx-xxxxxx",
        "CN-------",
        "xx-CM-CM-",
        "CN-xx-xx-",
        "xx-",
        "---"),
    new Template()
      .forExitDirection(Directions.Left)
      .from(
        "CN-",
        "xx-CM-CM-",
        "CN-xx-xx-",
        "xx-Hxxx--",
        "CN-xxxxCM",
        "xx-xxxxxx",
        "CN-------",
        "xx-CM-CM-",
        "---xx-xx-"),
    new Template()
      .forExitDirection(Directions.Left)
      .from(
        "---CM-CM-",
        "CN-xx-xx-",
        "xx-Hxxx--",
        "CN-xxxxCM",
        "xx-xxxxxx",
        "CN-------",
        "xxxCM-CM-",
        "xxxxx-xx-"),
    new Template()
      .forExitDirection(Directions.Right)
      .from(
        "xxxxxx-CN",
        "xCM-CM-xx",
        "xxx-xx-CN",
        "--Hxxx-xx",
        "CMxxxx-CN",
        "xxxxxx-xx",
        "-------CN",
        "xCM-CM-xx",
        "xxx-xx-CN",
        "xxxxxx-xx",
        "xxxxxx---"),
    new Template()
      .forExitDirection(Directions.Right)
      .from(
        "xCM-CN-xx",
        "xxx-xx-CN",
        "--Hxxx-xx",
        "CMxxxx-CN",
        "xxxxxx-xx",
        "-------CN",
        "xCM-CM-xx",
        "xxx-xx-CN",
        "xxxxxx-xx",
        "xxxxxx---"),
    new Template()
      .forExitDirection(Directions.Right)
      .from(
        "xxxxxx-CN",
        "xCM-CM-xx",
        "xxx-xx-CN",
        "--Hxxx-xx",
        "CMxxxx-CN",
        "xxxxxx-xx",
        "-------CN",
        "xCM-CN-xx",
        "xxx-xx---"),
    new Template()
      .forExitDirection(Directions.Right)
      .from(
        "xCM-CN---",
        "xxx-xx-CN",
        "--Hxxx-xx",
        "CMxxxx-CN",
        "xxxxxx-xx",
        "-------CN",
        "xCM-CM-xx",
        "xxx-xx-CN"),
    new Template()
      .forExitDirection(Directions.Down)
      .from(
        "CM-CM-CM-",
        "xx-xx-xx-",
        "---Hxxx---",
        "CM-xxxx-CM",
        "xx-xxxx-xx",
        "-----------",
        "CNCNCNCNCN-",
        "xxxxxxxxxx-"),
    new Template()
      .forExitDirection(Directions.Down)
      .from(
        "CM-CM-CM-",
        "xx-xx-xx-",
        "---Hxxx---",
        "CM-xxxx-CM",
        "xx-xxxx-xx",
        "-----------",
        "CNCNCNCNCN-",
        "xxxxxxxxxx-"),
    new Template()
      .forExitDirection(Directions.Down)
      .from(
        "CM-CM-CM-",
        "xx-xx-xx-",
        "---Hxxx---",
        "CM-xxxx-CN",
        "xx-xxxx-xx",
        "---------",
        "CNCNCNCN-",
        "xxxxxxxx-"),
    new Template()
      .forExitDirection(Directions.Down)
      .from(
        "CM-CM-CM-",
        "xx-xx-xx-",
        "---Hxxx---",
        "CN-xxxx-CM",
        "xx-xxxx-xx",
        "-----------",
        "xxCNCNCNCN-",
        "xxxxxxxxxx-"),
    new Template()
      .forExitDirection(Directions.Down)
      .from(
        "xx-CM-CM-",
        "CM-xx-xx-",
        "xx-Hxxx--",
        "---xxxx-",
        "x--xxxx-",
        "x-------CN",
        "x-CNCNCNxx-",
        "x-xxxxxx"),

    new Template().from(
      "------",
      "-Hxxx-",
      "-xxxx-",
      "-xxxx-",
      "------"),
    new Template().from(
      "Hxxx",
      "xxxx",
      "xxxx"))

  val initialLayouts: Seq[Template] = Seq(
    new Template().from(
      "------------",
      "-WxxxFxxx---",
      "-xxxxxxxxxx-",
      "-xxxxxxxxxx-",
      "-WxxxFxxx---",
      "-xxxxxxxxxx-",
      "-xxxxxxxxxx-",
      "------------"),
    new Template().from(
      "------",
      "-Wxxx-----",
      "-xxxxFxxx---",
      "-xxxxxxxxxx-",
      "-Fxxxxxxxxx-",
      "-xxxxxx-----",
      "-xxxxxx-",
      "--------"))

  val production: Seq[Template] = Seq(
    new Template().from(
      "xxxx------",
      "xxxx-Fxxx-------",
      "-----xxxxxxFxxx---",
      "-Fxxxxxxxxxxxxxxx-",
      "-xxxxxxFxxxxxxxxx-",
      "-xxxxxxxxxxxx-----",
      "-------xxxxxx-",
      "xxxxxx--------"),
    new Template().from(
      "--------",
      "-Fxxx---",
      "-xxxxxx-",
      "-xxxxxx-",
      "-Fxxx---",
      "-xxxxxx-",
      "-xxxxxx-",
      "-Fxxx---",
      "-xxxxxx-",
      "-xxxxxx-",
      "-Fxxx---",
      "-xxxxxx-",
      "-xxxxxx-",
      "--------"),
    new Template().from(
      "--------",
      "-Fxxx---",
      "-xxxxxx-",
      "-xxxxxx-",
      "-Fxxx---",
      "-xxxxxx-",
      "-xxxxxx-",
      "-Fxxx---",
      "-xxxxxx-",
      "-xxxxxx-",
      "--------"),
    new Template().from(
      "--------",
      "-Fxxx---",
      "-xxxxxx-",
      "-xxxxxx-",
      "-Fxxx---",
      "-xxxxxx-",
      "-xxxxxx-",
      "--------"),
    new Template().from(
      "------------------",
      "-Fxxx--Fxxx--Fxxx---",
      "-xxxxxxxxxxxxxxxxxx-",
      "-xxxxxxxxxxxxxxxxxx-",
      "--------------------"),
    new Template().from(
      "------------",
      "-Fxxx--Fxxx---",
      "-xxxxxxxxxxxx-",
      "-xxxxxxxxxxxx-",
      "--------------"),
    new Template().from(
      "------",
      "-Fxxx---",
      "-xxxxxx-",
      "-xxxxxx-",
      "--------"))

  val tech6: Seq[Template] = Seq(
    new Template().from(
      "6xxxxx6xxxxx",
      "xxxxxxxxxxxx",
      "xxxxxxxxxxxx"),
    new Template().from(
      "--------",
      "-6xxxxx-",
      "-xxxxxx-",
      "-xxxxxx-",
      "-6xxxxx-",
      "-xxxxxx-",
      "-xxxxxx-",
      "--------"),
    new Template().from(
      "--------",
      "-6xxxxx-",
      "-xxxxxx-",
      "-xxxxxx-",
      "--------"))

  val supply: Seq[Template] = Seq(
    new Template()
      .from(
      "-----",
      "-Yxx-",
      "-xxx-",
      "-Yxx-",
      "-xxx-",
      "-Yxx-",
      "-xxx-",
      "-Yxx-",
      "-xxx-",
      "-Yxx-",
      "-xxx-",
      "-Yxx-",
      "-xxx-",
      "-----"),
    new Template().from(
      "-----",
      "-Yxx-",
      "-xxx-",
      "-Yxx-",
      "-xxx-",
      "-Yxx-",
      "-xxx-",
      "-Yxx-",
      "-xxx-",
      "-Yxx-",
      "-xxx-",
      "-----"),
    new Template().from(
      "-----",
      "-Yxx-",
      "-xxx-",
      "-Yxx-",
      "-xxx-",
      "-Yxx-",
      "-xxx-",
      "-Yxx-",
      "-xxx-",
      "-----"),
    new Template().from(
      "--------",
      "-YxxYxx-",
      "-xxxxxx-",
      "-YxxYxx-",
      "-xxxxxx-",
      "-YxxYxx-",
      "-xxxYxx-",
      "--------"),
    new Template().from(
      "--------",
      "-YxxYxx-",
      "-xxxxxx-",
      "-YxxYxx-",
      "-xxxYxx-",
      "--------"),
    new Template().from(
      "-Yxx-",
      "-xxx-",
      "-Yxx-",
      "-xxx-",
      "-Yxx-",
      "-xxx-",
      "-----"),
    new Template().from(
      "Yxx",
      "xxx",
      "Yxx",
      "xxx",
      "---"))
    .map(_.addLabels(Tech))

  val bunkerTurret: Seq[Template] = Seq(
    new Template()
      .forExitDirection(Directions.Left, Directions.Down)
      .from(
        "BxxCx",
        "xxxxx"),
    new Template()
      .forExitDirection(Directions.Right, Directions.Up)
      .from(
        "CxBxx",
        "xxxxx"),
    new Template()
      .forExitDirection(Directions.Up, Directions.Left)
      .from(
        "Bxx",
        "xxx",
        "Cx",
        "xx"),
    new Template()
      .forExitDirection(Directions.Down, Directions.Right)
      .from(
        "xCx",
        "xxx",
        "Bxx",
        "xxx"))
    .map(_.addLabels(DefendEntrance, DefendGround))

  val proxy2: Seq[Template] = Seq(
    new Template()
      .from(
        "----------",
        "-4xxx4xxx-",
        "-xxxxxxxx-",
        "-xxxxxxxx-",
        "----------"),
    new Template()
      .from(
        "------",
        "-4xxx-",
        "-xxxx-",
        "-xxxx-",
        "-4xxx-",
        "-xxxx-",
        "-xxxx-",
        "------"))
    .map(_.addLabels(AnyProxy, ProxyGround))
}
