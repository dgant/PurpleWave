package Placement.Generation

import Placement.Templating.Template

object TemplatesGeneric {

  val walkway: Template = new Template().from("-")

  val townhall: Template = new Template().from(
    "Hxxx",
    "xxxx",
    "xxxx")
}
