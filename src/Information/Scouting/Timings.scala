package Information.Scouting

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, UnitInfo}
import Utilities.{?, CountMap}
import Utilities.Time.{Forever, GameTime}
import Utilities.UnitFilters.IsLandedBuilding

trait Timings {
  private val enemyCompletions  = new CountMap[UnitClass](Forever())
  private val enemyContacts     = new CountMap[UnitClass](Forever())

  def onUnitBirthTimings(unit: UnitInfo): Unit = {
    if (unit.isEnemy) {
      val latestStartFrame = With.frame - unit.unitClass.buildFrames + ?(unit.unitClass.isBuilding, unit.remainingCompletionFrames, 0)
      val provenTimings = unit.unitClass.buildUnitsEnabling.view ++ unit.unitClass.buildUnitsSpent ++ unit.unitClass.buildUnitsBorrowed
      // We could apply this recursively to be more thorough but I don't think we need to in practice
      provenTimings.foreach(enemyCompletions.reduceTo(_, latestStartFrame))
    }
  }

  protected def updateTimings(): Unit = {
    With.units.enemy.filter(_.visible).foreach(u => enemyCompletions.reduceTo(u.unitClass, u.completionFrame))
    With.units.enemy.filter(_.visible).foreach(u => enemyContacts   .reduceTo(u.unitClass, With.frame + rushFrames(u)))
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

  private def timestampedBuildings: Iterable[ForeignUnitInfo] = {
    With.units.enemy.filter(u => u.frameDiscovered < u.completionFrame)
  }

  def earliestCompletion(unitClass: UnitClass): Int = {
    enemyCompletions.get(unitClass).foreach(return _)
    var earliestCompletionFrame = Forever()
    if (unitClass == Protoss.DarkTemplar) {
      // Super-fast DT finishes 4:40. BetaStar demonstrates this on replay.
      earliestCompletionFrame                                       =  GameTime(4, 40)()
      if (With.fingerprints.twoGate())      earliestCompletionFrame += GameTime(1, 10)()
      if (With.fingerprints.dragoonRange()) earliestCompletionFrame += GameTime(0, 30)()
      timestampedBuildings.filter(Protoss.CyberneticsCore).foreach(u => earliestCompletionFrame = u.completionFrame + Protoss.CitadelOfAdun.buildFramesFull + Protoss.TemplarArchives.buildFramesFull + Protoss.DarkTemplar.buildFrames)
      timestampedBuildings.filter(Protoss.CitadelOfAdun)  .foreach(u => earliestCompletionFrame = u.completionFrame                                         + Protoss.TemplarArchives.buildFramesFull + Protoss.DarkTemplar.buildFrames)
      timestampedBuildings.filter(Protoss.TemplarArchives).foreach(u => earliestCompletionFrame = u.completionFrame                                                                                   + Protoss.DarkTemplar.buildFrames)
    }
    if (unitClass == Protoss.Observer) {
      // 13 Core Robo before range finishes at 4:05, but let's assume no Observers until they've given us cause to expect one
      val cloakedDebut = With.scouting.ourDebut(Terran.SpiderMine, Protoss.Arbiter, Protoss.DarkTemplar, Protoss.TemplarArchives, Protoss.ArbiterTribunal, Zerg.Lurker, Zerg.LurkerEgg)
      val roboCompletionFrame = Math.min(cloakedDebut         + Protoss.RoboticsFacility.buildFramesFull, enemyCompletions(Protoss.RoboticsFacility))
      val toryCompletionFrame = Math.min(roboCompletionFrame  + Protoss.Observatory.buildFramesFull,      enemyCompletions(Protoss.Observatory))
      earliestCompletionFrame = Math.min(toryCompletionFrame  + Protoss.Observer.buildFramesFull,         enemyCompletions(Protoss.Observer))
    }
    // TODO: Support other unit types (and support current types less maually)
    earliestCompletionFrame
  }

  def earliestArrival(unitClass: UnitClass): Int = {
    enemyContacts.get(unitClass).foreach(return _)
    earliestCompletion(unitClass) + rushFrames(unitClass)
  }
}
