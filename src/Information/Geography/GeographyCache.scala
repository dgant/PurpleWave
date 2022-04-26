package Information.Geography

import Information.Geography.Types.{Base, Zone}
import Lifecycle.With
import Mathematics.Maff
import Performance.Cache
import Utilities.UnitFilters.IsTank

trait GeographyCache extends GeographyBuilder {
  private def geo: Geography = With.geography

  protected val enemyBasesCache     : Cache[Vector[Base]]     = new Cache(() => geo.bases.filter(_.isEnemy))
  protected val ourZonesCache       : Cache[Vector[Zone]]     = new Cache(() => geo.zones.filter(_.isOurs))
  protected val ourBasesCache       : Cache[Vector[Base]]     = new Cache(() => geo.bases.filter(_.isOurs))
  protected val ourSettlementsCache : Cache[Vector[Base]]     = new Cache(() => getSettlements)

  protected val ourMainCache: Cache[Base] = new Cache(() =>
    Maff.orElse(
      geo.ourBases.filter(_.isStartLocation),
      geo.ourBases,
      geo.startBases).minBy(b => b.heart.groundTiles(With.self.startTile) + b.heart.tileDistanceFast(With.self.startTile) / 1000.0))

  protected val ourNaturalCache: Cache[Base] = new Cache(() =>
    geo.ourMain.natural.filter(_.naturalOf.exists(_.isOurs))
    .orElse(geo.bases.find(_.naturalOf.exists(_.isOurs)))
    .getOrElse(geo.bases.filterNot(geo.ourMain==).minBy(_.townHallTile.groundPixels(geo.ourMain.townHallTile))))

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
