package Information.Geography

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import Mathematics.Shapes.Spiral
import Performance.Cache
import Utilities.UnitFilters.IsTank

import scala.collection.mutable

trait GeographyCache {
  private def geo: Geography = With.geography
  
  protected val ourZonesCache           = new Cache(() => geo.zones.filter(_.isOurs))
  protected val ourBasesCache           = new Cache(() => geo.bases.filter(_.isOurs))
  protected val ourSettlementsCache     = new Cache(() => getSettlements)
  protected val enemyZonesCache         = new Cache(() => geo.zones.filter(_.isEnemy))
  protected val enemyBasesCache         = new Cache(() => geo.bases.filter(_.isEnemy))
  protected val ourTownHallsCache       = new Cache(() => geo.ourBases.flatMap(_.townHall))
  protected val ourNaturalCache = new Cache(() =>
    (if (geo.ourMain.isOurs) geo.ourMain.natural else None)
      .getOrElse(geo.bases.find(_.naturalOf.exists(_.isOurs))
        .getOrElse(geo.bases.minBy(_.townHallTile.groundPixels(geo.ourMain.townHallTile)))))

  protected lazy val zoneByTileCacheValid: Array[Zone] = geo.allTiles.map(tile => geo.zones.find(_.tiles.contains(tile)).getOrElse(getZoneForTile(tile)))
  protected lazy val baseByTileCacheValid: Array[Option[Base]] = geo.allTiles.map(getBaseForTile)

  protected val zoneByTileCacheInvalid: mutable.Map[Tile, Zone] = new mutable.HashMap[Tile, Zone] {
    override def default(key: Tile): Zone = { put(key, getZoneForTile(key)).get }
  }

  protected def getZoneForTile(tile: Tile): Zone =
    Maff
      .maxBy(
        Spiral
          .points(8)
          .map(point => {
            val neighbor = tile.add(point)
            if (neighbor.valid) geo.zones.find(_.tiles.contains(neighbor)) else None
          })
          .filter(_.isDefined)
          .map(z => z.get)
          .groupBy(x => x))(_._2.size)
      .map(_._1)
      .getOrElse(geo.zones.minBy(_.centroid.tileDistanceSquared(tile)))

  protected def getBaseForTile(tile: Tile): Option[Base] = tile.zone.bases.find(_.tiles.contains(tile))

  protected def getSettlements: Vector[Base] = (
    Vector.empty
      ++ With.geography.bases.view.filter(_.units.exists(u =>
      u.isOurs
        && u.unitClass.isBuilding
        && (u.unitClass.isTownHall || ! u.base.exists(_.townHallArea.intersects(u.tileArea)) // Ignore proxy base blockers
        )))
      ++ Vector(With.geography.ourNatural).filter(x =>
      With.strategy.isInverted
        && ! With.geography.ourMain.units.exists(_.unitClass.isStaticDefense)
        && With.units.ours.exists(u => u.complete && u.unitClass.ranged && (u.unitClass.canMove || u.is(IsTank)))
        && (With.units.existsEnemy(_.unitClass.ranged) || With.battles.globalHome.judgement.exists(_.shouldFight)))
      ++ With.units.ours
      .view
      .filter(_.intent.toBuild.exists(_.isTownHall))
      .flatMap(_.intent.toBuildTile.map(tile => tile.zone.bases.find(base => base.townHallTile == tile)))
      .flatten
      .filterNot(_.isOurs)
    ).distinct
}
