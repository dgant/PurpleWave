package Utilities

import Mathematics.Points.{Pixel, SpecificPoints, Tile, TileRectangle}

case object EnrichPixel {
  
  implicit class EnrichedPixelCollection(positions: Traversable[Pixel]) {
    
    def minBound: Pixel =
      if (positions.isEmpty)
        SpecificPoints.middle
      else
        Pixel(
          positions.view.map(_.x).min,
          positions.view.map(_.y).min)
    
    def maxBound: Pixel =
      if (positions.isEmpty)
        SpecificPoints.middle
      else
        Pixel(
          positions.view.map(_.x).max,
          positions.view.map(_.y).max)
    
    def bottomLeftBound: Pixel =
      if (positions.isEmpty)
        SpecificPoints.middle
      else
        Pixel(
          positions.view.map(_.x).min,
          positions.view.map(_.y).max)
    
    def topRightBound: Pixel =
      if (positions.isEmpty)
        SpecificPoints.middle
      else
        Pixel(
          positions.view.map(_.x).max,
          positions.view.map(_.y).min)
    
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
  
  implicit class EnrichedTileCollection(positions: Traversable[Tile]) {
    
    def minBound: Tile =
      if (positions.isEmpty)
        SpecificPoints.tileMiddle
      else
        Tile(
          positions.view.map(_.x).min,
          positions.view.map(_.y).min)
    
    def maxBound: Tile =
      if (positions.isEmpty)
        SpecificPoints.tileMiddle
      else
        Tile(
          positions.view.map(_.x).max,
          positions.view.map(_.y).max)
    
    def centroid: Tile =
      if (positions.isEmpty)
        SpecificPoints.tileMiddle
      else
        Tile(
          positions.view.map(_.x).sum / positions.size,
          positions.view.map(_.y).sum / positions.size)
  }
}
