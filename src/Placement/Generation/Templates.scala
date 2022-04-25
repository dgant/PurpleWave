package Placement.Generation

import Placement.Templating.Template

object Templates {

  val walkway: Template = new Template().add("-")

  val townhall: Template = new Template().add(
    "Hxxx",
    "xxxx",
    "xxxx")

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

  val batterycannon: Template = new Template().add(
    "---------",
    "-BxxPxCx-",
    "-xxxxxxx-",
    "---------")
}
