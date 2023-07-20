package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Performance.Cache
import Planning.Predicates.MacroFacts
import ProxyBwapi.Races.Terran
import Utilities.?
import Utilities.Time.Minutes
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.IsWarrior
import Utilities.UnitPreferences.PreferClose

class SquadEjectScout extends Squad {
  override def toString: String = "Eject"

  val legalScouts = new Cache(() => With.scouting.enemyScouts().filter(u => u.visible || ! u.flying || ! u.tile.zone.island))
  val targetScout = new Cache(() => Maff.minBy(legalScouts())(_.frameDiscovered))
  private val tilesToConsider = new Cache(() => With.scouting.zonesToLookForEnemyScouts().view.flatMap(_.tiles).toSeq)
  private val destination = new Cache(() => targetScout()
    .map(_.pixel)
    .getOrElse(Maff.minBy(tilesToConsider())(With.grids.lastSeen.get).getOrElse(With.geography.home).center))

  def launch(): Unit = {
    if (With.frame > Minutes(8)())                                    return
    if (MacroFacts.scoutCleared)                                      return
    if (targetScout().isEmpty)                                        return
    if (With.geography.ourBases.exists(_.enemies.exists(IsWarrior)))  return

    lock.matcher = unit => (
      unit.canMove
      && unit.unitClass.attacksGround
      && (unit.topSpeed > Terran.SCV.topSpeed || unit.pixelRangeGround >= 32.0 * 4.0)
      && With.scouting.zonesToLookForEnemyScouts().exists(unit.zone==) || unit.pixelsToGetInRange(targetScout().get) < 32)
    lock.counter = CountOne
    lock.preference = PreferClose(targetScout().get.pixel)
    lock.acquire()
  }
  
  override def run(): Unit = {
    units.foreach(ejector =>
      ejector.intend(this)
        .setTravel(destination())
        .setAttack(?(ejector.matchups.targets.forall(targetScout().contains), targetScout(), None))
        .setScout(?(targetScout().exists(_.likelyStillThere), Seq.empty, tilesToConsider())))
  }
}
