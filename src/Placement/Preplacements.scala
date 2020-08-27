package Placement

object Preplacements {
  val gateways21 = new PreplacementTemplate().add(
  "------------ " +
  "-GxxxPxGxxx- " +
  "-xxxxxxxxxx- " +
  "-xxxx--xxxx- " +
  "------------ " )

  val gateways12 = new PreplacementTemplate().add(
  "-------- " +
  "-GxxxPx- " +
  "-xxxxxx- " +
  "-xxxxPx- " +
  "-Gxxxxx- " +
  "-xxxxPx- " +
  "-xxxxxx- " +
  "-------- " )

  val tech21 = new PreplacementTemplate().add(
    "3xxPx3xx " +
    "xxxxxxxx " )

  val batterycannon = new PreplacementTemplate().add(
    "--------- " +
    "-BxxPxCx- " +
    "-xxxxxxx- " +
    "--------- " )
}
