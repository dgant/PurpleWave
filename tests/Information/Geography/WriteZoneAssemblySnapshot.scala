package Debugging

import Lifecycle.With
import bwta.BWTA

import java.io.{File, PrintWriter}
import scala.collection.JavaConverters._

/** Test-only replacement for WriteMapCSV, overlaid in the diagnostic bot jar. */
object WriteMapCSV {
  private var written = false

  def apply(): Unit = {
    if (written) return
    written = true
    val geo = With.geography
    val regions = BWTA.getRegions.asScala.toVector
    val out = new PrintWriter(new File(With.bwapiData.write + "zone-assembly.tsv"))
    def row(values: Any*): Unit = out.println(values.mkString("\t"))
    row("MAP", With.mapFileName, With.game.mapHash, With.mapTileWidth, With.mapTileHeight)
    regions.zipWithIndex.foreach { case (r, i) =>
      row("REGION", i, r.getCenter.getX, r.getCenter.getY)
    }
    BWTA.getChokepoints.asScala.foreach { c =>
      row("CHOKE", regions.indexOf(c.getRegions.getLeft), regions.indexOf(c.getRegions.getRight),
        c.getWidth, c.getSides.getLeft.getX, c.getSides.getLeft.getY,
        c.getSides.getRight.getX, c.getSides.getRight.getY)
    }
    geo.zones.zipWithIndex.foreach { case (z, i) =>
      row("ZONE", i, regions.indexOf(z.bwemRegion), z.tiles.size)
    }
    geo.allTiles.foreach { t =>
      row("TILE", t.x, t.y, regions.indexOf(BWTA.getRegion(t.bwapi)), geo.zones.indexOf(t.zone),
        With.grids.walkableTerrain.get(t), With.game.isBuildable(t.bwapi), With.game.getGroundHeight(t.bwapi))
      require(t.zone.tiles.contains(t), "Zone tile lookup mismatch")
    }
    require(geo.zones.map(_.tiles.size).sum == geo.allTiles.size, "Zone partition mismatch")
    geo.bases.foreach { b =>
      row("BASE", b.townHallTile.x, b.townHallTile.y, geo.zones.indexOf(b.zone), b.isMain,
        b.natural.map(_.townHallTile.x).getOrElse(-1), b.natural.map(_.townHallTile.y).getOrElse(-1), b.tiles.size)
      require(b.tiles.forall(_.zone == b.zone), "Base tile lookup mismatch")
      require(b.zone.bases.contains(b), "Missing zone base")
    }
    geo.edges.foreach { e =>
      row("EDGE", geo.zones.indexOf(e.zones.head), geo.zones.indexOf(e.zones.last),
        e.sidePixels.head.x, e.sidePixels.head.y, e.sidePixels.last.x, e.sidePixels.last.y)
      require(e.zones.forall(_.edges.contains(e)), "Missing zone edge")
    }
    row("PASS", geo.zones.size, geo.edges.size, geo.bases.size, geo.mains.size)
    out.close()
  }
}
