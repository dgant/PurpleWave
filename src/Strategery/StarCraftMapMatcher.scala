package Strategery

import Mathematics.Points.{Pixel, SpecificPoints}

object StarCraftMapMatcher {
  def clean(mapString: String): String = {
    mapString.toLowerCase
      .replaceAll(".scm", "")
      .replaceAll(".scx", "")
      .replaceAll("[^a-z]", "")
  }

  def clock(pixel: Pixel): String = {
    val output = Math.round(SpecificPoints.middle.radiansTo(pixel) * 12 / Math.PI / 2).toInt.toString
    if (output == "0") "12" else output
  }
}
