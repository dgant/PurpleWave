package Information.Scouting

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.CountMap
import Utilities.Time.{Forever, GameTime}
import Utilities.UnitFilters.IsLandedBuilding

trait Timings {
  private val enemyContacts = new CountMap[UnitClass](Forever())

  protected def updateTimings(): Unit = {
    With.units.enemy
      .filter(_.visible)
      .filter(u => enemyContacts(u.unitClass) > With.frame)
      .foreach(u => enemyContacts.reduceTo(u.unitClass, With.frame + rushFrames(u)))
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
  def rushTargetAir   (from: Tile)      : Tile    = rushTargets.minBy(_.tileDistanceSquared(from.walkableTile))
  def rushTargetGround(from: Tile)      : Tile    = rushTargets.minBy(_.groundTiles(from.walkableTile))
  def rushTarget      (unit: UnitInfo)  : Tile    = rushTargets.minBy(unit.pixelDistanceTravelling)
  def rushDistance    (unit: UnitInfo)  : Double  = unit.pixelDistanceTravelling(rushTarget(unit))
  def rushDistance    (unit: UnitClass) : Double  = { val from = expectedOrigin(unit); (if (unit.isFlyer) from.pixelDistance(rushTargetAir(from)) else from.groundPixels(rushTargetGround(from))) }
  def rushFrames      (unit: UnitInfo)  : Int     = unit.framesToTravelPixels(rushDistance(unit))
  def rushFrames      (unit: UnitClass) : Int     = (rushDistance(unit) / unit.topSpeed).toInt

  def expectedArrival(unitClass: UnitClass): Int = enemyContacts(unitClass)

  def earliestArrival(unitClass: UnitClass): Int = {
    enemyContacts.get(unitClass).foreach(return _)
    var earliestCompletionFrame = Forever()
    if (unitClass == Protoss.DarkTemplar) {
      earliestCompletionFrame                                       =  GameTime(4, 40)()
      if (With.fingerprints.twoGate())      earliestCompletionFrame += GameTime(1, 10)()
      if (With.fingerprints.dragoonRange()) earliestCompletionFrame += GameTime(0, 30)()
    }

    earliestCompletionFrame + rushFrames(unitClass)
  }
}
