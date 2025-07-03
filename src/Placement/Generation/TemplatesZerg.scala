package Placement.Generation

import Mathematics.Points.Directions
import Placement.Access.PlaceLabels._
import Placement.Templating.Template

object TemplatesZerg {

  val mainBases: Seq[Template] = Seq(
    new Template()
      .forMineralDirection(Directions.Left)
      .from(
        "CMGxxx",
        "xxxxxxLxx",
        "JxCN--xxx",
        "xxxx----",
        "------Cx",
        "CMHxxxxx",
        "xxxxxx--",
        "--xxxxCx",
        "CM----xx",
        "xx-"),
    new Template()
      .forMineralDirection(Directions.Right)
      .from(
        "xxxGxxx",
        "LxxxxxxCM",
        "xxxCN--xx",
        "-Jxxx----",
        "-xx----Cx",
        "---Hxxxxx",
        "xCxxxxx--",
        "xXxxxxxCM",
        "x------xx"),
    new Template()
      .from(
        "-Cx--Cx-",
        "-xx--xx-",
        "---------",
        "---HxxxJx",
        "Lxxxxxxxx",
        "xxxxxxx--",
        "Cx-----Cx"),
    new Template()
      .from(
        "Cx--Cx-Cx",
        "xx--xx-Cx",
        "--Hxxx---",
        "JxxxxxLxx",
        "xxxxxxxxx",
        "Cx------Cx"),
    new Template().from(
      "Hxxx",
      "xxxx",
      "xxxx"))
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
        "x-------",
        "x-CNCNCNCN",
        "x-xxxxxxxx"),
    new Template()
      .forExitDirection(Directions.Right)
      .from(
        "-CM-CxCN",
        "-xx-xxxx",
        "--------",
        "-Hxxx-CN",
        "-xxxx-xx",
        "-xxxx-CN",
        "------xx",
        "-CM-CxCN",
        "-xx-xxxx"),
    new Template()
      .forExitDirection(Directions.Left)
      .from(
        "---Cx-CM-",
        "CN-xx-xx-",
        "xx-------",
        "CN-Hxxx-CM",
        "xx-xxxx---",
        "CN-xxxx-CM",
        "xx------xx",
        "CNCN-CM-",
        "xxxx-xx-"),
    new Template().from(
      "x-Cx-Cx",
      "--xx-xx",
      "CxHxxx-",
      "xxxxxxCx",
      "--xxxxxx",
      "Cx-----",
      "xx-"),
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

  val macroHatch: Seq[Template] = Seq(
    new Template().from(
      "------",
      "-Oxxx-",
      "-xxxx-",
      "-xxxx-",
      "------")
  .addLabels(GroundProduction))

  val tech: Seq[Template] = Seq(
    new Template().from(
      "3xx",
      "xxx"))
    .map(_.addLabels(Tech))
}
