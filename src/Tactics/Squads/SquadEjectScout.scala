package Tactics.Squads

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Performance.Cache
import Planning.Plans.Scouting.ScoutCleared
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers.{MatchAnd, MatchScoutCatcher}
import Planning.UnitPreferences.PreferClose
import Utilities.Time.Minutes

class SquadEjectScout extends Squad {
  override def toString: String = "Eject"

  val targetScout = new Cache(() => Maff.minBy(With.scouting.enemyScouts())(_.frameDiscovered))
  private val tilesToConsider = new Cache(() => With.scouting.basesToLookForEnemyScouts().view.flatMap(_.zone.tiles))
  private val destination = new Cache(() => targetScout()
    .map(_.pixel)
    .getOrElse(Maff.minBy(tilesToConsider())(With.grids.lastSeen.get).getOrElse(With.geography.home).center))

  private val scoutCleared = new ScoutCleared
  def launch() {
    if (With.frame > Minutes(8)()) return
    if (scoutCleared.apply) return
    if (targetScout().isEmpty) return

    lock.matcher = MatchAnd(MatchScoutCatcher, (unit) => unit.base.exists(With.scouting.basesToLookForEnemyScouts().contains) || unit.pixelsToGetInRange(targetScout().get) < 32)
    lock.counter = CountOne
    lock.preference = PreferClose(targetScout().get.pixel)
    lock.acquire()
  }
  
  override def run() {
    units.foreach(ejector => {
      ejector.intend(this, new Intention {
        toScoutTiles = if (targetScout().exists(_.likelyStillThere)) Seq.empty else tilesToConsider()
        toTravel = Some(destination())
        toAttack = if (ejector.matchups.targets.forall(targetScout().contains)) targetScout() else None
      })
    })
  }
}
