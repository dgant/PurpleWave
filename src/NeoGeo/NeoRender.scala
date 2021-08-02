package NeoGeo

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File

import javax.imageio.ImageIO

object NeoRender {
  def filepathBase = "./bwapi-data/write/"

  private def binary(boolean: Boolean): Color = {
    if (boolean) new Color(255, 255, 255) else new Color(0, 0, 0)
  }
  private def grayscale(int: Int): Color = {
    val v = Math.max(0, Math.min(255, int))
    new Color(v, v, v)
  }
  def apply(geo: NeoGeo): Unit = {
    renderWalks(geo, "walkable",      i => binary(geo.walkability(i)))
    renderWalks(geo, "altitude",      i => grayscale(64 * Math.min(1, geo.altitude(i)) + 191 * geo.altitude(i) / 64))
    renderTiles(geo, "buildable",     i => binary(geo.buildability(i)))
    renderTiles(geo, "groundheight",  i => grayscale(255 * geo.groundHeight(i) / 5))
    geo.directions.indices.foreach(d => renderWalks(geo, f"clearance-$d", i => grayscale(64 * Math.min(1, geo.altitude(i)) + 191 * geo.clearance(d)(i) / 64)))
  }

  def renderTiles(geo: NeoGeo, name: String, color: Int => Color): Unit = {
    render(geo, name, geo.tileWidth, geo.tileHeight, color)
  }

  def renderWalks(geo: NeoGeo, name: String, color: Int => Color): Unit = {
    render(geo, name, geo.walkWidth, geo.walkHeight, color)
  }

  private def render(geo: NeoGeo, name: String, width: Int, height: Int, color: Int => Color): Unit = {
    val image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    var x, y, i = 0
    while (y < height) {
      while (x < width) {
        image.setRGB(x, y, color(i).getRGB)
        x += 1
        i += 1
      }
      x = 0
      y += 1
    }
    ImageIO.write(image, "png", new File(f"$filepathBase${geo.mapNickname}-$name.png"))
  }
}
