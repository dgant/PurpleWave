package NeoGeo.Internal

import java.awt.Color

object NeoColors {
  object Hues {
    val Red           = 0
    val OrangeRed     = 12
    val Orange        = 24
    val OrangeYellow  = 42
    val Yellow        = 64
    val YellowGreen   = 80
    val Green         = 96
    val Sky           = 112
    val Teal          = 128
    val Sea           = 144
    val Blue          = 160
    val Indigo        = 192
    val Violet        = 224
    val rainbow = Seq(Green, Teal, Blue, Indigo, Violet, Red, Orange, Yellow)
    val contrasted = Seq(Red, Yellow, Teal, Indigo, Orange, Green, Blue, Violet, OrangeRed, Sky, OrangeYellow, Sea, YellowGreen)
  }

  // Via http://stackoverflow.com/questions/3018313/algorithm-to-convert-rgb-to-hsv-and-hsv-to-rgb-in-range-0-255-for-both
  def hsv(h: Int, s: Int, v: Int): Color = {
    if (s == 0) return new Color(v, v, v)
    val region = h/43
    val remainder = 6 * (h - 43 * region)
    val p = (v * (255 - s)) >> 8
    val q = (v * (255 - ((s * remainder) >> 8))) >> 8
    val t = (v * (255 - ((s * (255 - remainder)) >> 8))) >> 8
    region match {
      case 0 => new Color(v, t, p)
      case 1 => new Color(q, v, p)
      case 2 => new Color(p, v, t)
      case 3 => new Color(p, q, v)
      case 4 => new Color(t, p, v)
      case _ => new Color(v, p, q)
    }
  }

  def hv(h: Int, v: Int): Color = hsv(h, 255, v)

  val allContrasted: Array[Color] =
    Seq(255, 192, 128, 96).flatMap(s =>
      Hues.contrasted.flatMap(h =>
        (255 to 64 by -32).map(hsv(h, s, _)))).toArray
}
