package Information.Geography.Types

import Mathematics.Points.Pixel

object ChokeGeometry {
  // BWEM's neutral-blocker pseudo-chokes contain a single point, not two banks.
  // Find the narrowest terrain cross-section through that point at walk-tile resolution.
  def sides(center: Pixel, walkable: Pixel => Boolean): Vector[Pixel] = {
    (0 until 8).map(i => {
      val angle = i * Math.PI / 8
      Vector(-1, 1).map(sign => {
        (8 to 256 by 8).iterator
          .map(distance => center.radiateRadians(angle, sign * distance))
          .find(p => ! walkable(p))
          .getOrElse(center.radiateRadians(angle, sign * 256))
      })
    }).minBy(p => p.head.pixelDistanceSquared(p.last))
  }
}
