package Mathematics.Points

import Mathematics.Maff

case class PixelRectangle(
  startInclusive : Pixel,
  endExclusive   : Pixel) {

  def this() {
    this(Pixel(0, 0), Pixel(1, 1))
  }

  def this(pixel: Pixel) {
    this(pixel, pixel.add(1, 1))
  }

  def this(included: Iterable[Pixel]) {
    this(
      Pixel(
        Maff.min(included.view.map(_.x)).getOrElse(0),
        Maff.min(included.view.map(_.y)).getOrElse(0)),
      Pixel(
        Maff.max(included.view.map(_.x + 1)).getOrElse(0),
        Maff.max(included.view.map(_.y + 1)).getOrElse(0)))
  }

  if (endExclusive.x < startInclusive.x || endExclusive.y < startInclusive.y) {
    throw new Exception("Created an invalid (non-normalized) rectangle")
  }
  
  def add(x:Int, y:Int): PixelRectangle =
    PixelRectangle(
      startInclusive.add(x, y),
      endExclusive.add(x, y))
  
  def expand(x:Int, y:Int): PixelRectangle =
    PixelRectangle(
      startInclusive.add(-x, -y),
      endExclusive  .add( x,  y))
  
  def add(Pixel: Pixel): PixelRectangle =
    add(Pixel.x, Pixel.y)

  def midPixel : Pixel = startPixel.midpoint(endPixel)
  def midpoint : Pixel  = startInclusive.midpoint(endExclusive)
  
  def contains(x: Int, y: Int): Boolean =
    x >= startInclusive.x &&
    y >= startInclusive.y &&
    x < endExclusive.x &&
    y < endExclusive.y
  
  def contains(Pixel: Pixel): Boolean = {
    contains(Pixel.x, Pixel.y)
  }
  
  def intersects(otherRectangle: PixelRectangle): Boolean = {
    startInclusive.x < endExclusive.x &&
    startInclusive.y < endExclusive.y &&
    otherRectangle.startInclusive.x < otherRectangle.endExclusive.x &&
    otherRectangle.startInclusive.y < otherRectangle.endExclusive.y &&
    startInclusive.x < otherRectangle.endExclusive.x &&
    otherRectangle.startInclusive.x < endExclusive.x &&
    startInclusive.y < otherRectangle.endExclusive.y &&
    otherRectangle.startInclusive.y < endExclusive.y
  }
  
  def containsRectangle(otherRectangle: PixelRectangle): Boolean = {
    contains(otherRectangle.startInclusive)                                       ||
    contains(otherRectangle.endExclusive.subtract(1, 1))                          ||
    contains(otherRectangle.startInclusive.x, otherRectangle.endExclusive.y - 1)  ||
    contains(otherRectangle.endExclusive.x - 1, otherRectangle.startInclusive.y)
  }

  def nearestPoint(to: Pixel): Pixel = {
    Pixel(
      Maff.clamp(startInclusive.x, to.x, endExclusive.x - 1),
      Maff.clamp(startInclusive.y, to.y, endExclusive.y - 1))
  }
  
  def startPixel      : Pixel = startInclusive
  def endPixel        : Pixel = endExclusive.subtract(1, 1)
  def topRightPixel   : Pixel = Pixel(endPixel.x, startPixel.y)
  def bottomleftPixel : Pixel = Pixel(startPixel.x, endPixel.y)
  
  lazy val cornerPixels: Array[Pixel] = Array(startPixel, topRightPixel, endPixel, bottomleftPixel)
  
  lazy val pixelsEach32: Seq[Pixel] =
    (0 until endExclusive.x - startInclusive.x by 32).view.flatMap(x =>
      (0 until endExclusive.y - startInclusive.y by 32).view.map(y =>
        Pixel(startInclusive.x + x, startInclusive.y + y)))

  lazy val tiles: TileRectangle = new TileRectangle(startPixel.tile, endExclusive.subtract(1, 1).tile.add(1, 1))
}
