package Mathematics.Points

import Mathematics.Maff

import scala.collection.SeqView

final case class TileRectangle(
  startInclusive : Tile,
  endExclusive   : Tile) {

  @inline def this() {
    this(new Tile(0), new Tile(0))
  }

  @inline def this(tile: Tile, width: Int, height: Int) {
    this(tile, tile.add(width, height))
  }

  @inline def this(tile: Tile) {
    this(tile, 1, 1)
  }

  @inline def this(included: Iterable[Tile]) {
    this(
      Tile(
        Maff.min(included.view.map(_.x)).getOrElse(0),
        Maff.min(included.view.map(_.y)).getOrElse(0)),
      Tile(
        Maff.max(included.view.map(_.x + 1)).getOrElse(0),
        Maff.max(included.view.map(_.y + 1)).getOrElse(0)))
  }

  @inline def this(included: Traversable[TileRectangle]) {
    this(
      Tile(
        Maff.min(included.view.map(_.startInclusive.x)).getOrElse(0),
        Maff.min(included.view.map(_.startInclusive.y)).getOrElse(0)),
      Tile(
        Maff.min(included.view.map(_.endExclusive.x)).getOrElse(0),
        Maff.min(included.view.map(_.endExclusive.y)).getOrElse(0)))
  }

  @inline def this(xStartInclusive: Int, yStartInclusive: Int, xEndExclusive: Int, yEndExclusive: Int) {
    this(Tile(xStartInclusive, yStartInclusive), Tile(xEndExclusive, yEndExclusive))
  }

  if (endExclusive.x < startInclusive.x) {
    throw new Exception("Created an invalid (non-normalized) rectangle")
  }
  
  @inline def add(x: Int, y: Int): TileRectangle =
    TileRectangle(
      startInclusive.add(x, y),
      endExclusive.add(x, y))
  
  @inline def expand(x: Int, y: Int): TileRectangle =
    TileRectangle(
      startInclusive.add(-x, -y),
      endExclusive  .add( x,  y))
  
  @inline def add(Tile: Tile): TileRectangle =
    add(Tile.x, Tile.y)

  @inline def center : Pixel = startPixel.midpoint(endPixel)
  @inline def midpoint : Tile  = startInclusive.midpoint(endExclusive)
  
  @inline def contains(x: Int, y: Int): Boolean =
    x >= startInclusive.x &&
    y >= startInclusive.y &&
    x < endExclusive.x &&
    y < endExclusive.y
  
  @inline def contains(tile: Tile): Boolean = {
    contains(tile.x, tile.y)
  }

  @inline def contains(pixel: Pixel): Boolean = {
    contains(pixel.x / 32, pixel.y / 32)
  }
  
  @inline def intersects(otherRectangle: TileRectangle): Boolean = {
    containsRectangle(otherRectangle) || otherRectangle.containsRectangle(this)
  }
  
  @inline private def containsRectangle(otherRectangle:TileRectangle):Boolean = {
    contains(otherRectangle.startInclusive)                                       ||
    contains(otherRectangle.endExclusive.subtract(1, 1))                          ||
    contains(otherRectangle.startInclusive.x, otherRectangle.endExclusive.y - 1)  ||
    contains(otherRectangle.endExclusive.x - 1, otherRectangle.startInclusive.y)
  }

  @inline def startPixel        : Pixel = startInclusive.topLeftPixel
  @inline def endPixel          : Pixel = endExclusive.topLeftPixel.subtract(1, 1)
  @inline def topRightPixel     : Pixel = Pixel(endPixel.x, startPixel.y)
  @inline def bottomleftPixel   : Pixel = Pixel(startPixel.x, endPixel.y)
  @inline def midPixel          : Pixel = startPixel.midpoint(endPixel)
  @inline def leftMiddlePixel   : Pixel = Pixel(startPixel.x, midPixel.y)
  @inline def rightMiddlePixel  : Pixel = Pixel(endPixel.x, midPixel.y)
  @inline def topCenterPixel    : Pixel = Pixel(midPixel.x, endPixel.y)
  @inline def bottomCenterPixel : Pixel = Pixel(midPixel.x, startPixel.y)
  
  @inline def cornerPixels: Array[Pixel] = Array(startPixel, topRightPixel, endPixel, bottomleftPixel)
  @inline def cornerTilesInclusive: Array[Tile] = Array(startInclusive, Tile(endExclusive.x - 1, startInclusive.y), endExclusive.subtract(1, 1), Tile(startInclusive.x, endExclusive.y - 1))
  
  @inline def tiles: SeqView[Tile, Seq[_]] =
    (0 until endExclusive.y - startInclusive.y).view.flatMap(y =>
      (0 until endExclusive.x - startInclusive.x).view.map(x =>
        Tile(startInclusive.x + x, startInclusive.y + y)))
  
  @inline def tilesSurrounding: Iterable[Tile] = expand(1, 1).tiles.filterNot(contains)
}
