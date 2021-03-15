package Strategery

import Mathematics.Points.{Pixel, SpecificPoints}
import Mathematics.PurpleMath

object StarCraftMapMatcher {
  def clean(mapString: String): String = {
    mapString.toLowerCase
      .replaceAll(".scm", "")
      .replaceAll(".scx", "")
      .replaceAll("[^a-z]", "")
  }

  def clock(pixel: Pixel): String = {
    val output = Math.round(PurpleMath.normalize0To2Pi(SpecificPoints.middle.radiansTo(pixel)) * 6 / Math.PI).toInt.toString
    if (output == "0") "12" else output
  }
}
