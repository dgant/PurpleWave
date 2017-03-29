package Debugging.Visualization

import bwapi.Color

object Colors {
  
  //Via http://stackoverflow.com/questions/3018313/algorithm-to-convert-rgb-to-hsv-and-hsv-to-rgb-in-range-0-255-for-both
  private def hsv(h:Int, s:Int, v:Int):Color = {
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
  
  private def hv(h:Int, v:Int):Color = hsv(h, 220, v)
  
  private val red = 0
  private val orange = 32
  private val yellow = 64
  private val green = 96
  private val teal = 128
  private val blue = 160
  private val indigo = 192
  private val violet = 224
  
  private val dark = 128
  private val medium = 160
  private val bright = 224
  private val neon = 255
    
  val DarkRed       = hv(red,     dark)
  val DarkOrange    = hv(orange,  dark)
  val DarkYellow    = hv(yellow,  dark)
  val DarkGreen     = hv(green,   dark)
  val DarkTeal      = hv(teal,    dark)
  val DarkBlue      = hv(blue,    dark)
  val DarkIndigo    = hv(indigo,  dark)
  val DarkViolet    = hv(violet,  dark)
  val DarkGray      = hsv(0, 0,   dark)
  val MediumRed     = hv(red,     medium)
  val MediumOrange  = hv(orange,  medium)
  val MediumYellow  = hv(yellow,  medium)
  val MediumGreen   = hv(green,   medium)
  val MediumTeal    = hv(teal,    medium)
  val MediumBlue    = hv(blue,    medium)
  val MediumIndigo  = hv(indigo,  medium)
  val MediumViolet  = hv(violet,  medium)
  val MediumGray    = hsv(0, 0,   medium)
  val DefaultGray   = MediumGray
  val BrightRed     = hv(red,     bright)
  val BrightOrange  = hv(orange,  bright)
  val BrightYellow  = hv(yellow,  bright)
  val BrightGreen   = hv(green,   bright)
  val BrightTeal    = hv(teal,    bright)
  val BrightBlue    = hv(blue,    bright)
  val BrightIndigo  = hv(indigo,  bright)
  val BrightViolet  = hv(violet,  bright)
  val BrightGray    = hsv(0, 0,   bright)
  val NeonRed       = hv(red,     neon)
  val NeonOrange    = hv(orange,  neon)
  val NeonYellow    = hv(yellow,  neon)
  val NeonGreen     = hv(green,   neon)
  val NeonTeal      = hv(teal,    neon)
  val NeonBlue      = hv(blue,    neon)
  val NeonIndigo    = hv(indigo,  neon)
  val NeonViolet    = hv(violet,  neon)
  val White         = hsv(0, 0,   neon)
  
}
