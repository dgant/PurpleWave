package Debugging.Visualizations

import bwapi.Color

object Colors {
  
  //Via http://stackoverflow.com/questions/3018313/algorithm-to-convert-rgb-to-hsv-and-hsv-to-rgb-in-range-0-255-for-both
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
  
  private def hv(h: Int, v: Int): Color = hsv(h, 192, v)

  private val shadow    = 24
  private val midnight  = 48
  private val deep      = 96
  private val dark      = 128
  private val medium    = 160
  private val bright    = 192
  private val neon      = 255

  val rainbow: Array[Color] =
    (0 to 255).flatMap(r =>
      (0 to 255).flatMap(g =>
        (0 to 255).map(b =>
          new Color(r, g, b))))
      .toArray
  
  val ShadowRed       : Color = hv(Hues.Red,      shadow)
  val ShadowOrange    : Color = hv(Hues.Orange,   shadow)
  val ShadowYellow    : Color = hv(Hues.Yellow,   shadow)
  val ShadowGreen     : Color = hv(Hues.Green,    shadow)
  val ShadowTeal      : Color = hv(Hues.Teal,     shadow)
  val ShadowBlue      : Color = hv(Hues.Blue,     shadow)
  val ShadowIndigo    : Color = hv(Hues.Indigo,   shadow)
  val ShadowViolet    : Color = hv(Hues.Violet,   shadow)
  val ShadowGray      : Color = hsv(0, 0,         shadow)
  val MidnightRed     : Color = hv(Hues.Red,      midnight)
  val MidnightOrange  : Color = hv(Hues.Orange,   midnight)
  val MidnightYellow  : Color = hv(Hues.Yellow,   midnight)
  val MidnightGreen   : Color = hv(Hues.Green,    midnight)
  val MidnightTeal    : Color = hv(Hues.Teal,     midnight)
  val MidnightBlue    : Color = hv(Hues.Blue,     midnight)
  val MidnightIndigo  : Color = hv(Hues.Indigo,   midnight)
  val MidnightViolet  : Color = hv(Hues.Violet,   midnight)
  val MidnightGray    : Color = hsv(0, 0,         midnight)
  val DeepRed         : Color = hv(Hues.Red,      deep)
  val DeepOrange      : Color = hv(Hues.Orange,   deep)
  val DeepYellow      : Color = hv(Hues.Yellow,   deep)
  val DeepGreen       : Color = hv(Hues.Green,    deep)
  val DeepTeal        : Color = hv(Hues.Teal,     deep)
  val DeepBlue        : Color = hv(Hues.Blue,     deep)
  val DeepIndigo      : Color = hv(Hues.Indigo,   deep)
  val DeepViolet      : Color = hv(Hues.Violet,   deep)
  val DeepGray        : Color = hsv(0, 0,         deep)
  val DarkRed         : Color = hv(Hues.Red,      dark)
  val DarkOrange      : Color = hv(Hues.Orange,   dark)
  val DarkYellow      : Color = hv(Hues.Yellow,   dark)
  val DarkGreen       : Color = hv(Hues.Green,    dark)
  val DarkTeal        : Color = hv(Hues.Teal,     dark)
  val DarkBlue        : Color = hv(Hues.Blue,     dark)
  val DarkIndigo      : Color = hv(Hues.Indigo,   dark)
  val DarkViolet      : Color = hv(Hues.Violet,   dark)
  val DarkGray        : Color = hsv(0, 0,         dark)
  val MediumRed       : Color = hv(Hues.Red,      medium)
  val MediumOrange    : Color = hv(Hues.Orange,   medium)
  val MediumYellow    : Color = hv(Hues.Yellow,   medium)
  val MediumGreen     : Color = hv(Hues.Green,    medium)
  val MediumTeal      : Color = hv(Hues.Teal,     medium)
  val MediumBlue      : Color = hv(Hues.Blue,     medium)
  val MediumIndigo    : Color = hv(Hues.Indigo,   medium)
  val MediumViolet    : Color = hv(Hues.Violet,   medium)
  val MediumGray      : Color = hsv(0, 0,         medium)
  val DefaultGray     : Color = MediumGray
  val BrightRed       : Color = hv(Hues.Red,      bright)
  val BrightOrange    : Color = hv(Hues.Orange,   bright)
  val BrightYellow    : Color = hv(Hues.Yellow,   bright)
  val BrightGreen     : Color = hv(Hues.Green,    bright)
  val BrightTeal      : Color = hv(Hues.Teal,     bright)
  val BrightBlue      : Color = hv(Hues.Blue,     bright)
  val BrightIndigo    : Color = hv(Hues.Indigo,   bright)
  val BrightViolet    : Color = hv(Hues.Violet,   bright)
  val BrightGray      : Color = hsv(0, 0,         bright)
  val NeonRed         : Color = hv(Hues.Red,      neon)
  val NeonOrange      : Color = hv(Hues.Orange,   neon)
  val NeonYellow      : Color = hv(Hues.Yellow,   neon)
  val NeonGreen       : Color = hv(Hues.Green,    neon)
  val NeonTeal        : Color = hv(Hues.Teal,     neon)
  val NeonBlue        : Color = hv(Hues.Blue,     neon)
  val NeonIndigo      : Color = hv(Hues.Indigo,   neon)
  val NeonViolet      : Color = hv(Hues.Violet,   neon)
  val White           : Color = hsv(0, 0,         neon)
}
