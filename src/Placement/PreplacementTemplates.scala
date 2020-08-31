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
      "-4xxx4xxx- " +
      "-xxxxxxxx- " +
      "-xxxxxxxx- " +
      "-HxxPxRxx- " +
      "xxxxxxxxx- " +
      "xxxxx----- "),
    new PreplacementTemplate().add(
      "---------- " +
      "-4xxx4xxx- " +
      "-xxxxxxxx- " +
      "-xxxxxxxx- " +
      "-RxxPxHxx- " +
      "-xxxxxxxxx " +
      "-----xxxxx ")
  )

  val gateways = Seq(
    new PreplacementTemplate().add(
      "xxx-------- " +
      "Px4xxx4xxx- " +
      "xxxxxxxxxx- " +
      "Pxxxxxxxxx- " +
      "xx4xxx4xxx- " +
      "Pxxxxxxxxx- " +
      "xxxxxxxxxx- " +
      "xxx-------- " ),
    new PreplacementTemplate().add(
      "xxx---- " +
      "Px4xxx- " +
      "xxxxxx- " +
      "Pxxxxx- " +
      "xx4xxx- " +
      "Pxxxxx- " +
      "xxxxxx- " +
      "xxx---- " ),
    new PreplacementTemplate().add(
      "---------- " +
      "-4xxx4xxx- " +
      "-xxxxxxxx- " +
      "-xxxxxxxx- " +
      "--xPxPxx-- " ),
    new PreplacementTemplate().add(
      "xx--------- " +
      "Px4xxx4xxx- " +
      "xxxxxxxxxx- " +
      "Pxxxxxxxxx- " +
      "xx--------- " ),
    new PreplacementTemplate().add(
      "-------- " +
      "-4xxxPx- " +
      "-xxxxxx- " +
      "-xxxxPx- " +
      "-4xxxxxx " +
      "-xxxxPxx " +
      "-xxxxxxx " +
      "------xx " ),
    new PreplacementTemplate().add(
      "xxx---- " +
      "Px4xxx- " +
      "xxxxxx- " +
      "xxxxxx- " +
      "xxx---- " ),
    new PreplacementTemplate().add(
      "PxPx- " +
      "xxxx- " +
      "4xxx- " +
      "xxxx- " +
      "xxxx- " +
      "----- " ),
    new PreplacementTemplate().add(
      "-PxPx " +
      "-xxxx " +
      "-4xxx " +
      "-xxxx " +
      "-xxxx " +
      "----- " )
  )

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
