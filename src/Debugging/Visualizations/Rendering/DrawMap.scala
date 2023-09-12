package Debugging.Visualizations.Rendering

import Debugging.Asciify
import Debugging.Visualizations.Colors
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import bwapi.Color

object DrawMap {
  
  def text(
    origin  : Pixel,
    text    : => String): Unit = {
    if (irrelevant(origin)) return
    With.game.drawTextMap(origin.bwapi, Asciify(text))
  }
  
  def line(
    start : Pixel,
    end   : Pixel,
    color : Color = Colors.DefaultGray): Unit = {
    if (irrelevant(start, end)) return
    With.game.drawLineMap(start.bwapi, end.bwapi, color)
  }
  
  def arrow(
    start : Pixel,
    end   : Pixel,
    color : Color = Colors.DefaultGray): Unit = {
    if (irrelevant(start, end)) return
    
    line(start, end, color)
    val angleBack = end.radiansTo(start)
    val angleDiff = Math.PI / 8
    triangle(
      end,
      end.radiateRadians(angleBack + angleDiff, 7),
      end.radiateRadians(angleBack - angleDiff, 7),
      color,
      solid = true)
  }

  def cross(
    at      : Pixel,
    radius  : Int,
    color   : Color = Colors.DefaultGray): Unit = {
    line(at.add(  -radius, -radius), at.add(   radius, radius), color)
    line(at.add(   radius, -radius), at.add(  -radius, radius), color)
  }
  
  def tile(
    tile: Tile,
    margin: Int = 0,
    color : Color = Colors.DefaultGray): Unit = {
    box(
      tile.topLeftPixel.add(margin, margin),
      tile.bottomRightPixel.subtract(margin, margin),
      color)
  }
  
  def box(
    start : Pixel,
    end   : Pixel,
    color : Color = Colors.DefaultGray,
    solid : Boolean = false): Unit = {
    if (irrelevant(start, end)) return

    With.game.drawBoxMap(start.bwapi, end.bwapi, color, solid)
  }
  
  def circle(
    center  : Pixel,
    radius  : Int,
    color   : Color = Colors.DefaultGray,
    solid   : Boolean = false): Unit = {
    if (radius <= 0) return
    if (irrelevant(
      center.add(-radius, -radius),
      center.add(radius, radius))) return
    With.game.drawCircleMap(center.bwapi, radius, color, solid)
  }
  
  def triangle(
    position1 : Pixel,
    position2 : Pixel,
    position3 : Pixel,
    color     : Color = Colors.DefaultGray,
    solid: Boolean = false): Unit = {
    if (irrelevant(position1, position2, position3)) return
    With.game.drawTriangleMap(position1.bwapi, position2.bwapi, position3.bwapi, color, solid)
  }
  
  def label(
    text            : => String,
    position        : Pixel,
    drawBackground  : Boolean = false,
    backgroundColor : Color = Colors.DefaultGray,
    drawBorder      : Boolean = false,
    borderColor     : Color = Colors.DefaultGray): Unit = {
    if (irrelevant(position)) return
    labelBox(Vector(text), position, drawBackground, backgroundColor, drawBorder, borderColor)
  }

  def labelDot(
    text            : => String,
    position        : Pixel,
    backgroundColor : Color = Colors.DefaultGray): Unit = {
    if (irrelevant(position)) return
    val width = 9 * text.length / 2
    val height = 11
    val radius = 1 + Math.max(width, height) / 2
    circle(position, radius, backgroundColor, solid = true)
    With.game.drawTextMap(position.x - width / 2, position.y - height / 2, Asciify(text))
  }
  
  def labelBox(
    textLines       : => TraversableOnce[String],
    position        : Pixel,
    drawBackground  : Boolean = false,
    backgroundColor : Color = Colors.DefaultGray,
    drawBorder      : Boolean = false,
    borderColor     : Color = Colors.DefaultGray): Unit = {

    if (irrelevant(position)) return
    
    val horizontalMargin = 2
    val estimatedTextWidth = (9 * textLines.map(_.length).max) / 2
    val boxWidth = estimatedTextWidth + (if (estimatedTextWidth > 0) 2 * horizontalMargin else 0)
    val boxHeight = 11 * textLines.size
    val textX = position.x - boxWidth/2
    val textY = position.y - boxHeight/2
    val boxX = textX - horizontalMargin
    val boxY = textY
    
    if (drawBackground) {
      With.game.drawBoxMap(
        boxX,
        boxY,
        boxX + boxWidth,
        boxY + boxHeight,
        backgroundColor,
        true)
    }
    if (drawBorder) {
      With.game.drawBoxMap(
        boxX - 1,
        boxY - 1,
        boxX + boxWidth + 1,
        boxY + boxHeight + 1,
        borderColor,
        false)
    }
    With.game.drawTextMap(
      textX,
      textY,
      textLines.map(Asciify.apply).mkString("\n"))
  }
  
  def tileRectangle(rectangle: TileRectangle, color: Color, margin: Int = 0): Unit = {
    if (irrelevant(rectangle.startPixel, rectangle.endPixel)) return
    With.game.drawBoxMap(rectangle.startPixel.subtract(margin, margin).bwapi, rectangle.endPixel.add(margin, margin).bwapi, color)
  }
  
  def polygon(pixels: Iterable[Pixel], color: Color = Colors.DefaultGray): Unit = {
    if (pixels.size < 2) return
    pixels.reduce((p1, p2) => { line(p1, p2, color); p2 })
    line(pixels.head, pixels.last, color)
  }
  
  def drawSkull(pixel: Pixel, colorDark: Color = Colors.DarkGray, colorBright: Color = Colors.BrightGray, f: Int = 1): Unit = {
    circle(pixel, 5 * f, colorBright,  solid = true)
    circle(pixel, 5 * f, colorDark,    solid = false)
    box(pixel.add(-3 * f, 0), pixel.add(1 + 3 * f, 6 * f), colorBright, solid = true)
    box(pixel.add(-3 * f, 0), pixel.add(1 + 3 * f, 6 * f), colorDark,   solid = false)
    box(pixel.add(-3 * f, 0), pixel.add(1 + 3 * f, 3 * f), colorBright, solid = true)

    if (f == 1) {
      box(pixel.add(-2 * f, -2 * f), pixel.add(0,     0), Color.Black, solid = true)
      box(pixel.add( 1 * f, -2 * f), pixel.add(3 * f, 0), Color.Black, solid = true)
    } else {
      circle(pixel.add(-2 * f, -1 * f), f, Color.Black, solid = true)
      circle(pixel.add( 2 * f, -1 * f), f, Color.Black, solid = true)
    }
    triangle(pixel.add(0, 1 * f), pixel.add(- f, 2 * f), pixel.add(f, 2 * f), Color.Black, solid = true)
    line(pixel.add(-1 * f, 4 * f), pixel.add(-1 * f, 6 * f - 1), colorDark)
    line(pixel.add( 1 * f, 4 * f), pixel.add( 1 * f, 6 * f - 1), colorDark)
  }

  def star(pixel: Pixel, radius: Int, color: Color, solid: Boolean = true): Unit = {
    // Totally fudging the math on this -- might be better if I spent 2 minutes thinking or Googling
    val a = Math.max(4, radius)
    val b = Math.max(2, a / 2)
    val c = Math.max(1, b / 2)
    triangle(pixel.add(-a, -c), pixel.add(a, -c), pixel.add(0, c), color, solid)
    triangle(pixel.add(0, -a),  pixel.add(0, c),  pixel.add(-b, b + 1), color, solid)
    triangle(pixel.add(0, -a),  pixel.add(0, c),  pixel.add( b, b + 1), color, solid)
  }

  def crosshair(pixel: Pixel, radius: Int, color: Color): Unit = {
    circle(pixel, radius, color)
    val r3 = radius * 3 / 2
    val r1 = radius / 2
    box(pixel.add( -1, -r3), pixel.add( 1,  -r1), color)
    box(pixel.add( -1,  r1), pixel.add( 1,   r3), color)
    box(pixel.add(-r3,  -1), pixel.add(-r1,   1), color)
    box(pixel.add( r1,  -1), pixel.add( r3,   1), color)
  }
  
  def irrelevant(points: Pixel*): Boolean = {
    ( ! points.exists(With.viewport.contains(_, 0))
      && ! With.viewport.contains(Maff.centroid(points)))
    // This is more accurate but is slower due to allocating a PixelRectangle
    // ! new PixelRectangle(points).expand(64, 64).intersects(With.viewport.area)
  }
}
