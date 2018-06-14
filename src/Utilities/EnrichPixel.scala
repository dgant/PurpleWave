package Utilities

import Mathematics.Points.{Pixel, SpecificPoints, Tile, TileRectangle}

case object EnrichPixel {
  
  implicit class EnrichedPixelCollection(positions: Traversable[Pixel]) {
    def centroid: Pixel =
      if (positions.isEmpty)
        SpecificPoints.middle
      else
        Pixel(
          positions.view.map(_.x).sum / positions.size,
          positions.view.map(_.y).sum / positions.size)
  }
  
  implicit class EnrichedRectangleCollection(rectangles: Traversable[TileRectangle]) {
    def boundary: TileRectangle =
      TileRectangle(
        Tile(
          rectangles.map(_.startInclusive.x).min,
          rectangles.map(_.startInclusive.y).min),
        Tile(
          rectangles.map(_.endExclusive.x).max,
          rectangles.map(_.endExclusive.y).max))
  }
}
