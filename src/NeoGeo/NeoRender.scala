package NeoGeo

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File

import NeoGeo.Internal.{NeoColors, NeoContinentBackend}
import javax.imageio.ImageIO

object NeoRender {
  def filepathBase = "./bwapi-data/write/"

  private def binary(boolean: Boolean): Color = {
    if (boolean) new Color(255, 255, 255) else new Color(0, 0, 0)
  }
  private def grayscale(int: Int): Color = {
    val v = NeoMath.clamp(int, 0, 255)
    new Color(v, v, v)
  }
  private def safeHV(h: Int, v: Int): Color = {
    NeoColors.hv(NeoMath.clamp(h, 0, 255), NeoMath.clamp(v, 0, 255))
  }
  private def safeHSV(h: Int, s: Int, v: Int): Color = {
    NeoColors.hsv(NeoMath.clamp(h, 0, 255), NeoMath.clamp(s, 0, 255), NeoMath.clamp(v, 0, 255))
  }
  private def walk64(geo: NeoGeo, i: Int): Int = if (geo.walkability(i)) 64 else 0

  def apply(geo: NeoGeo): Unit = {
    renderWalks(geo, "walkable",          i => binary(geo.walkability(i)))
    renderWalks(geo, "altitude",          i => grayscale(walk64(geo, i) + (191 * geo.altitude(i) / 64).toInt))
    renderWalks(geo, "continents",        i => if(geo.continentByWalk(i) == null) new Color(0, 0, 0) else geo.continentByWalk(i).asInstanceOf[NeoContinentBackend].color)
    renderWalks(geo, "clearanceMinWalks", i => safeHV(NeoColors.Hues.eight(geo.clearanceMinDir(i)), walk64(geo, i) + 191 * geo.clearanceMinWalks(i) / 64))
    renderWalks(geo, "clearanceMaxWalks", i => safeHV(NeoColors.Hues.eight(geo.clearanceMaxDir(i)), walk64(geo, i) + 191 * geo.clearanceMaxWalks(i) / 256))
    renderWalks(geo, "clearanceMin",      i => grayscale(walk64(geo, i) + 191 * geo.clearanceMinWalks(i) / 64))
    renderWalks(geo, "clearanceMax",      i => grayscale(walk64(geo, i) + 191 * geo.clearanceMaxWalks(i) / 256))
    renderTiles(geo, "buildable",         i => binary(geo.buildability(i)))
    renderTiles(geo, "groundheight",      i => grayscale(255 * geo.groundHeight(i) / 5))
    geo.directions.indices.foreach(d => renderWalks(geo, f"clearance-$d", i => grayscale(walk64(geo, i) + 191 * geo.clearance(d)(i) / 128)))
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
