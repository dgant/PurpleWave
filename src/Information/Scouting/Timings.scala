package Information.Scouting

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.CountMap
import Utilities.Time.Forever
import Utilities.UnitFilters.IsLandedBuilding

trait Timings {
  private val enemyContacts = new CountMap[UnitClass](Forever())

  protected def updateTimings(): Unit = {
    With.units.enemy
      .filter(_.visible)
      .filter(u => enemyContacts(u.unitClass) > With.frame)
      .foreach(u => enemyContacts.reduceTo(u.unitClass, rushArrival(u)))
  }

  def expectedOrigin(unitClass: UnitClass): Tile = {
    var producers = With.units.enemy
      .filter(unitClass.whatBuilds._1)
      .map(u => u.tileTopLeft.add(0, u.unitClass.tileHeight).walkableTile)
      .toVector
    if (producers.isEmpty) producers = Vector(With.scouting.enemyHome.add(0, 3).walkableTile)
    producers.minBy(_.groundTiles(With.geography.home.walkableTile))
  }

  def rushTargets: Iterable[Tile] = Maff.orElse(With.units.ours.filter(IsLandedBuilding).map(_.tile.walkableTile), Seq(With.geography.home.walkableTile))
  def rushTargetAir   (from: Tile)    : Tile    = rushTargets.minBy(_.tileDistanceSquared(from.walkableTile))
  def rushTargetGround(from: Tile)    : Tile    = rushTargets.minBy(_.groundTiles(from.walkableTile))
  def rushTarget      (unit: UnitInfo): Tile    = rushTargets.minBy(unit.pixelDistanceTravelling)
  def rushDistance    (unit: UnitInfo): Double  = unit.pixelDistanceTravelling(rushTarget(unit))
  def rushArrival     (unit: UnitInfo): Int     = With.frame + unit.framesToTravelPixels(rushDistance(unit))

  def expectedArrival(unitClass: UnitClass): Int = enemyContacts(unitClass)

  def earliestArrival(unitClass: UnitClass): Int = {
    var output = expectedArrival(unitClass)



    output
  }
}
