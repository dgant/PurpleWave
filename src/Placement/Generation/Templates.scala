package Placement.Generation

import Placement.Templating.Template

object Templates {

  val walkway: Template = new Template().add("-")

  val townhall: Template = new Template().add(
    "Txxx " +
    "xxxx " +
    "xxxx " )

  val initialLayouts = Seq(
    new Template().add(
      "---------- " +
      "-4xxx4xxx- " +
      "-xxxxxxxx- " +
      "-xxxxxxxx- " +
      "-HxxPxRxx- " +
      "xxxxxxxxx- " +
      "xxxxx----- "),
    new Template().add(
      "---------- " +
      "-4xxx4xxx- " +
      "-xxxxxxxx- " +
      "-xxxxxxxx- " +
      "-RxxPxHxx- " +
      "-xxxxxxxxx " +
      "-----xxxxx ")
  )

  val gateways = Seq(
    new Template().add(
      "xxx-------- " +
      "Px4xxx4xxx- " +
      "xxxxxxxxxx- " +
      "Pxxxxxxxxx- " +
      "xx4xxx4xxx- " +
      "Pxxxxxxxxx- " +
      "xxxxxxxxxx- " +
      "xxx-------- " ),
    new Template().add(
      "xxx---- " +
      "Px4xxx- " +
      "xxxxxx- " +
      "Pxxxxx- " +
      "xx4xxx- " +
      "Pxxxxx- " +
      "xxxxxx- " +
      "xxx---- " ),
    new Template().add(
      "---------- " +
      "-4xxx4xxx- " +
      "-xxxxxxxx- " +
      "-xxxxxxxx- " +
      "--xPxPxx-- " ),
    new Template().add(
      "xx--------- " +
      "Px4xxx4xxx- " +
      "xxxxxxxxxx- " +
      "Pxxxxxxxxx- " +
      "xx--------- " ),
    new Template().add(
      "-------- " +
      "-4xxxPx- " +
      "-xxxxxx- " +
      "-xxxxPx- " +
      "-4xxxxxx " +
      "-xxxxPxx " +
      "-xxxxxxx " +
      "------xx " ),
    new Template().add(
      "xxx---- " +
      "Px4xxx- " +
      "xxxxxx- " +
      "xxxxxx- " +
      "xxx---- " ),
    new Template().add(
      "PxPx- " +
      "xxxx- " +
      "4xxx- " +
      "xxxx- " +
      "xxxx- " +
      "----- " ),
    new Template().add(
      "-PxPx " +
      "-xxxx " +
      "-4xxx " +
      "-xxxx " +
      "-xxxx " +
      "----- " )
  )

  val tech = Seq(
    new Template().add(
      "3xxPx3xx " +
      "xxxxxxxx " ),
    new Template().add(
      "3xx " +
      "xxx " +
      "3xx " +
      "xxx " +
      "Pxx " +
      "xxx " ))

  val batterycannon: Template = new Template().add(
    "--------- " +
    "-BxxPxCx- " +
    "-xxxxxxx- " +
    "--------- " )
}
