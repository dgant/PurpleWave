package Placement

object PreplacementTemplates {

  val walkway = new PreplacementTemplate().add("-")

  val townhall = new PreplacementTemplate().add(
    "Txxx " +
    "xxxx " +
    "xxxx " )

  val initialLayouts = Seq(
    new PreplacementTemplate().add(
      "---------- " +
      "-GxxxGxxx- " +
      "-xxxxxxxx- " +
      "-xxxxxxxx- " +
      "xPxYxxRxx- " +
      "xxxxxxxxx- " +
      "xxxxxxx--- "),
    new PreplacementTemplate().add(
      "---------- " +
      "-GxxxGxxx- " +
      "-xxxxxxxx- " +
      "-xxxxxxxx- " +
      "xPxYxxRxx--- " +
      "xxxxxxRxxxxx " ))

  val gateways = Seq(
    new PreplacementTemplate().add(
      "xxx-------- " +
      "PxGxxxGxxx- " +
      "xxxxxxxxxx- " +
      "Pxxxxxxxxx- " +
      "xxGxxxGxxx- " +
      "Pxxxxxxxxx- " +
      "xxxxxxxxxx- " +
      "xxx-------- " ),
    new PreplacementTemplate().add(
      "xxx---- " +
      "PxGxxx- " +
      "xxxxxx- " +
      "Pxxxxx- " +
      "xxGxxx- " +
      "Pxxxxx- " +
      "xxxxxx- " +
      "xxx---- " ),
    new PreplacementTemplate().add(
      "---------- " +
      "-GxxxGxxx- " +
      "-xxxxxxxx- " +
      "-xxxxxxxx- " +
      "--xPxPxx-- " ),
    new PreplacementTemplate().add(
      "xx--------- " +
      "PxGxxxGxxx- " +
      "xxxxxxxxxx- " +
      "Pxxxxxxxxx- " +
      "xx--------- " ),
    new PreplacementTemplate().add(
      "-------- " +
      "-GxxxPx- " +
      "-xxxxxx- " +
      "-xxxxPx- " +
      "-Gxxxxxx " +
      "-xxxxPxx " +
      "-xxxxxxx " +
      "------xx " ))

  val tech = Seq(
    new PreplacementTemplate().add(
      "3xxPx3xx " +
      "xxxxxxxx " ),
    new PreplacementTemplate().add(
      "3xx " +
      "xxx " +
      "3xx " +
      "xxx " +
      "Pxx " +
      "xxx " ))

  val batterycannon = new PreplacementTemplate().add(
    "--------- " +
    "-BxxPxCx- " +
    "-xxxxxxx- " +
    "--------- " )
}
