package Information.Geography.NeoGeo

import Mathematics.Maff
import Mathematics.Points.{Pixel, Points}

object MapIdentifier {
  def apply(mapString: String): String = {
    mapString.toLowerCase
      .replaceAll(".scm", "")
      .replaceAll(".scx", "")
      .replaceAll("kespa", "")
      .replaceAll("iccup", "")
      .replaceAll("neo", "")
      .replaceAll("new", "")
      .replaceAll("[^a-z]", "")
  }

  def clock(pixel: Pixel): String = {
    val output = Math.round(Maff.normalize0To2Pi(Points.middle.radiansTo(pixel)) * 6 / Math.PI).toInt.toString
    if (output == "0") "12" else output
  }
}
