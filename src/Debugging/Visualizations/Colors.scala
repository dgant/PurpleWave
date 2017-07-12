package Debugging.Visualizations

import bwapi.Color

object Colors {
  
  //Via http://stackoverflow.com/questions/3018313/algorithm-to-convert-rgb-to-hsv-and-hsv-to-rgb-in-range-0-255-for-both
  private def hsv(h: Int, s: Int, v: Int):Color = {
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
  
  private val red     = 0
  private val orange  = 32
  private val yellow  = 64
  private val green   = 96
  private val teal    = 128
  private val blue    = 160
  private val indigo  = 192
  private val violet  = 224
  
  private val midnight  = 48
  private val deep      = 96
  private val dark      = 128
  private val medium    = 160
  private val bright    = 192
  private val neon      = 255
  
  val MidnightRed     : Color = hv(red,     midnight)
  val MidnightOrange  : Color = hv(orange,  midnight)
  val MidnightYellow  : Color = hv(yellow,  midnight)
  val MidnightGreen   : Color = hv(green,   midnight)
  val MidnightTeal    : Color = hv(teal,    midnight)
  val MidnightBlue    : Color = hv(blue,    midnight)
  val MidnightIndigo  : Color = hv(indigo,  midnight)
  val MidnightViolet  : Color = hv(violet,  midnight)
  val MidnightGray    : Color = hsv(0, 0,   midnight)
  val DeepRed         : Color = hv(red,     deep)
  val DeepOrange      : Color = hv(orange,  deep)
  val DeepYellow      : Color = hv(yellow,  deep)
  val DeepGreen       : Color = hv(green,   deep)
  val DeepTeal        : Color = hv(teal,    deep)
  val DeepBlue        : Color = hv(blue,    deep)
  val DeepIndigo      : Color = hv(indigo,  deep)
  val DeepViolet      : Color = hv(violet,  deep)
  val DeepGray        : Color = hsv(0, 0,   deep)
  val DarkRed         : Color = hv(red,     dark)
  val DarkOrange      : Color = hv(orange,  dark)
  val DarkYellow      : Color = hv(yellow,  dark)
  val DarkGreen       : Color = hv(green,   dark)
  val DarkTeal        : Color = hv(teal,    dark)
  val DarkBlue        : Color = hv(blue,    dark)
  val DarkIndigo      : Color = hv(indigo,  dark)
  val DarkViolet      : Color = hv(violet,  dark)
  val DarkGray        : Color = hsv(0, 0,   dark)
  val MediumRed       : Color = hv(red,     medium)
  val MediumOrange    : Color = hv(orange,  medium)
  val MediumYellow    : Color = hv(yellow,  medium)
  val MediumGreen     : Color = hv(green,   medium)
  val MediumTeal      : Color = hv(teal,    medium)
  val MediumBlue      : Color = hv(blue,    medium)
  val MediumIndigo    : Color = hv(indigo,  medium)
  val MediumViolet    : Color = hv(violet,  medium)
  val MediumGray      : Color = hsv(0, 0,   medium)
  val DefaultGray     : Color = MediumGray
  val BrightRed       : Color = hv(red,     bright)
  val BrightOrange    : Color = hv(orange,  bright)
  val BrightYellow    : Color = hv(yellow,  bright)
  val BrightGreen     : Color = hv(green,   bright)
  val BrightTeal      : Color = hv(teal,    bright)
  val BrightBlue      : Color = hv(blue,    bright)
  val BrightIndigo    : Color = hv(indigo,  bright)
  val BrightViolet    : Color = hv(violet,  bright)
  val BrightGray      : Color = hsv(0, 0,   bright)
  val NeonRed         : Color = hv(red,     neon)
  val NeonOrange      : Color = hv(orange,  neon)
  val NeonYellow      : Color = hv(yellow,  neon)
  val NeonGreen       : Color = hv(green,   neon)
  val NeonTeal        : Color = hv(teal,    neon)
  val NeonBlue        : Color = hv(blue,    neon)
  val NeonIndigo      : Color = hv(indigo,  neon)
  val NeonViolet      : Color = hv(violet,  neon)
  val White           : Color = hsv(0, 0,   neon)
}
