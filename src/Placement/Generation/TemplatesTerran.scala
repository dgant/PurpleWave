package Placement.Generation

import Mathematics.Points.Directions
import Placement.Access.PlaceLabels.{AnyProxy, DefendEntrance, DefendGround, ProxyGround, Tech}
import Placement.Templating.Template

object TemplatesTerran {

  val bases: Seq[Template] = Seq(
    new Template().from(
      "CM",
      "xxGxxxKxxx-",
      "CMxxxxxxxx-",
      "xxBNx-xxxx-",
      "--xxx------",
      "------CM",
      "CMHxxxxx",
      "xxxxxxxx",
      "--xxxxxx",
      "-CM-CM-",
      "-xx-xx-"),
    new Template().from(
      "xxxxGxxxCM",
      "Kxxxxxxxxx-",
      "xxxxBNx----",
      "xxxxxxx-CM-",
      "--------xx-",
      "xxCMHxxx---",
      "xxxxxxxxxx-",
      "xxx-xxxxxx-",
      "xxx-CMCM---",
      "xxx-xxxx-"),
    new Template().from(
      "CM-CM-Kxxx",
      "xx-xx-xxxx",
      "--Hxxxxxxx",
      "CMxxxxxx-",
      "xxxxxxxx-",
      "---------",
      "CMBNx-CM",
      "xxxxx-xx",
      "CMGxxxCM",
      "xxxxxxxx"),
    new Template().from(
      "xxCM-CM-CM",
      "xxxx-xx-xx-",
      "KxxxHxxx---",
      "xxxxxxxxxx-",
      "xxxxxxxxxx-",
      "-----------",
      "xxx-BNx-CM",
      "xxCMxxx-xx",
      "xxxxGxxxCM",
      "xxxxxxxxxx"),
    new Template().from(
      "CM-CM-CM",
      "xx-xx-xx-",
      "--Hxxx---",
      "CMxxxxxx-",
      "xxxxxxxx-",
      "---------",
      "CMBNx-CM",
      "xxxxx-xx"),
    new Template().from(
      "CMBNx-",
      "xxxxx-CM-",
      "------xx-",
      "CMHxxx---",
      "xxxxxxxx-",
      "--xxxxxx-",
      "CM-CM-CM",
      "xx-xx-xx-"),
    new Template().from(
      "BNx-",
      "xxx-CM-",
      "----xx-",
      "Hxxx---",
      "xxxxxx-",
      "xxxxxx-"),
    new Template().from(
      "----CM",
      "Hxxxxx-",
      "xxxxxx-",
      "xxxxxx-",
      "BNx----",
      "xxx-"),
    new Template().from(
      "Hxxx",
      "xxxxxx",
      "xxxxxx"))


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
      "------",
      "6xxxxx",
      "xxxxxx",
      "xxxxxx",
      "6xxxxx",
      "xxxxxx",
      "xxxxxx"),
    new Template().from(
      "------",
      "6xxxxx",
      "xxxxxx",
      "xxxxxx"))

  val supply: Seq[Template] = Seq(
    new Template()
      .from(
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
      "-YxxYxx-",
      "-xxxxxx-",
      "-YxxYxx-",
      "-xxxxxx-",
      "-YxxYxx-",
      "-xxxYxx-",
      "--------"),
    new Template().from(
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
