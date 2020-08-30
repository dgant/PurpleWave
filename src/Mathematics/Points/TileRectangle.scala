package Mathematics.Points

import scala.collection.immutable

case class TileRectangle(
  startInclusive : Tile,
  endExclusive   : Tile) {

  def this() {
    this(new Tile(0), new Tile(0))
  }

  def this(tile: Tile) {
    this(tile, tile.add(1, 1))
  }

  if (endExclusive.x < startInclusive.x || endExclusive.y < startInclusive.y) {
    throw new Exception("Created an invalid (non-normalized) rectangle")
  }
  
  def add(x:Int, y:Int): TileRectangle =
    TileRectangle(
      startInclusive.add(x, y),
      endExclusive.add(x, y))
  
  def expand(x:Int, y:Int): TileRectangle =
    TileRectangle(
      startInclusive.add(-x, -y),
      endExclusive  .add( x,  y))
  
  def add(Tile: Tile): TileRectangle =
    add(Tile.x, Tile.y)

  def midPixel : Pixel = startPixel.midpoint(endPixel)
  def midpoint : Tile  = startInclusive.midpoint(endExclusive)
  
  def contains(x: Int, y: Int): Boolean =
    x >= startInclusive.x &&
    y >= startInclusive.y &&
    x < endExclusive.x &&
    y < endExclusive.y
  
  def contains(tile: Tile): Boolean = {
    contains(tile.x, tile.y)
  }

  def contains(pixel: Pixel): Boolean = {
    contains(pixel.x / 32, pixel.y / 32)
  }
  
  def intersects(otherRectangle: TileRectangle): Boolean = {
    containsRectangle(otherRectangle) || otherRectangle.containsRectangle(this)
  }
  
  private def containsRectangle(otherRectangle:TileRectangle):Boolean = {
    contains(otherRectangle.startInclusive)                                       ||
    contains(otherRectangle.endExclusive.subtract(1, 1))                          ||
    contains(otherRectangle.startInclusive.x, otherRectangle.endExclusive.y - 1)  ||
    contains(otherRectangle.endExclusive.x - 1, otherRectangle.startInclusive.y)
  }
  
  def startPixel      : Pixel = startInclusive.topLeftPixel
  def endPixel        : Pixel = endExclusive.topLeftPixel.subtract(1, 1)
  def topRightPixel   : Pixel = Pixel(endPixel.x, startPixel.y)
  def bottomleftPixel : Pixel = Pixel(startPixel.x, endPixel.y)
  
  lazy val cornerPixels: Array[Pixel] = Array(startPixel, topRightPixel, endPixel, bottomleftPixel)
  lazy val cornerTilesInclusive: Array[Tile] = Array(startInclusive, Tile(endExclusive.x - 1, startInclusive.y), endExclusive.subtract(1, 1), Tile(startInclusive.x, endExclusive.y - 1))
  
  lazy val tiles: immutable.IndexedSeq[Tile] =
    (0 until endExclusive.x - startInclusive.x).flatMap(x =>
      (0 until endExclusive.y - startInclusive.y).map(y =>
        Tile(startInclusive.x + x, startInclusive.y + y)))
  
  lazy val tilesSurrounding: Iterable[Tile] = {
    expand(1, 1).tiles.filterNot(contains)
  }
}
