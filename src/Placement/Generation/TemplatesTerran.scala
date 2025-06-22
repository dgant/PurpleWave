package Placement.Generation

import Mathematics.Points.Directions
import Placement.Templating.Template

object TemplatesTerran {

  val commandCenter: Seq[Template] = Seq(
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
      "--------"),
  )

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
      "6xxxxx",
      "xxxxxx",
      "xxxxxx",
      "6xxxxx",
      "xxxxxx",
      "xxxxxx"),
    new Template().from(
      "6xxxxx",
      "xxxxxx",
      "xxxxxx"))

  val supply: Seq[Template] = Seq(
    new Template().from(
      "Yxx",
      "xxx",
      "Yxx",
      "xxx",
      "Yxx",
      "xxx",
      "Yxx",
      "xxx",
      "Yxx",
      "xxx",
      "Yxx",
      "xxx"),
    new Template().from(
      "Yxx",
      "xxx",
      "Yxx",
      "xxx",
      "Yxx",
      "xxx",
      "Yxx",
      "xxx",
      "Yxx",
      "xxx"),
    new Template().from(
      "Yxx",
      "xxx",
      "Yxx",
      "xxx",
      "Yxx",
      "xxx",
      "Yxx",
      "xxx"),
    new Template().from(
      "YxxYxx",
      "xxxxxx",
      "YxxYxx",
      "xxxxxx",
      "YxxYxx",
      "xxxYxx"),
    new Template().from(
      "YxxYxx",
      "xxxxxx",
      "YxxYxx",
      "xxxYxx"),
    new Template().from(
      "Yxx",
      "xxx",
      "Yxx",
      "xxx",
      "Yxx",
      "xxx"),
    new Template().from(
      "Yxx",
      "xxx",
      "Yxx",
      "xxx",
      "Yxx",
      "xxx"))

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
        "Cx",
        "xx",
        "Bxx",
        "xxx"))
}
