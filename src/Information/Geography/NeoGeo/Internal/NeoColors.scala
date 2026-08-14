package Information.Geography.NeoGeo.Internal

object NeoColors {
  object Hues {
    val Red           = 0
    val Orange        = 24
    val Yellow        = 64
    val Green         = 96
    val Teal          = 128
    val Blue          = 160
    val Indigo        = 192
    val Violet        = 224

    val allRainbow = Seq(Red, Orange, Yellow, Green, Teal, Blue, Indigo, Violet)
    val allContrasted = Seq(Green, Blue, Violet, Red, Yellow, Teal, Indigo, Orange)
    val eight: Seq[Int] = allRainbow
  }

}
