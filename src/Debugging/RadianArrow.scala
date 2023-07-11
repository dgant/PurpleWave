package Debugging

import Mathematics.Maff

object RadianArrow {
  private val offset      : Double = Maff.twoPi / 8
  private val multiplier  : Double = 8 * Maff.inv2Pi

  def apply(r: Double, scale: Double = 1.0, emoji: Boolean = true): String = {
    val direction = (Maff.normalize0To2Pi(r + offset) * multiplier).toInt
    if (emoji) {
      if (scale < 0.5) direction match {
        case 0 => "\uD83E\uDC62"
        case 1 => "\uD83E\uDC66"
        case 2 => "\uD83E\uDC63"
        case 3 => "\uD83E\uDC67"
        case 4 => "\uD83E\uDC60"
        case 5 => "\uD83E\uDC64"
        case 6 => "\uD83E\uDC61"
        case 7 => "\uD83E\uDC65"
        case _ => "?"
      } else if (scale < 0.9) direction match {
        case 0 => "\uD83E\uDC6A"
        case 1 => "\uD83E\uDC6E"
        case 2 => "\uD83E\uDC6B"
        case 3 => "\uD83E\uDC6F"
        case 4 => "\uD83E\uDC68"
        case 5 => "\uD83E\uDC6C"
        case 6 => "\uD83E\uDC69"
        case 7 => "\uD83E\uDC6D"
        case _ => "?"
      } else if (scale < 1.1) direction match {
        case 0 => "\uD83E\uDC72"
        case 1 => "\uD83E\uDC76"
        case 2 => "\uD83E\uDC73"
        case 3 => "\uD83E\uDC77"
        case 4 => "\uD83E\uDC70"
        case 5 => "\uD83E\uDC74"
        case 6 => "\uD83E\uDC71"
        case 7 => "\uD83E\uDC75"
        case _ => "?"
      } else if (scale < 1.5) direction match {
        case 0 => "\uD83E\uDC7A"
        case 1 => "\uD83E\uDC7E"
        case 2 => "\uD83E\uDC7B"
        case 3 => "\uD83E\uDC7F"
        case 4 => "\uD83E\uDC78"
        case 5 => "\uD83E\uDC7C"
        case 6 => "\uD83E\uDC79"
        case 7 => "\uD83E\uDC7D"
        case _ => "?"
      } else direction match {
        case 0 => "\uD83E\uDC82"
        case 1 => "\uD83E\uDC86"
        case 2 => "\uD83E\uDC83"
        case 3 => "\uD83E\uDC87"
        case 4 => "\uD83E\uDC80"
        case 5 => "\uD83E\uDC84"
        case 6 => "\uD83E\uDC81"
        case 7 => "\uD83E\uDC85"
        case _ => "?"
      }
    } else {
      if (scale < 0.75) direction match {
        case 0 => "→"
        case 1 => "↘"
        case 2 => "↓"
        case 3 => "↙"
        case 4 => "←"
        case 5 => "↖"
        case 6 => "↑"
        case 7 => "↗"
        case _ => "?"
      } else if (scale < 1.25) direction match {
        case 0 => "⇒"
        case 1 => "⇘"
        case 2 => "⇓"
        case 3 => "⇙"
        case 4 => "⇐"
        case 5 => "⇖"
        case 6 => "⇑"
        case 7 => "⇗"
        case _ => "?"
      } else direction match {
        case 0 => "⇒⇒"
        case 1 => "⇘⇘"
        case 2 => "⇓⇓"
        case 3 => "⇙⇙"
        case 4 => "⇐⇐"
        case 5 => "⇖⇖"
        case 6 => "⇑⇑"
        case 7 => "⇗⇗"
        case _ => "??"
      }
    }
  }
}
