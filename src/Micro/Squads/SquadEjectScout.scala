package Micro.Squads

import Lifecycle.With
import Micro.Agency.Intention
import Performance.Cache
import Planning.Plans.Scouting.ScoutCleared
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers.{MatchAnd, MatchScoutCatcher}
import Planning.UnitPreferences.PreferClose
import Utilities.{ByOption, Minutes}

class SquadEjectScout extends Squad {

  val targetScout = new Cache(() => ByOption.minBy(With.scouting.enemyScouts())(_.frameDiscovered))
  private val tilesToConsider = new Cache(() => With.scouting.basesToLookForEnemyScouts().view.flatMap(_.zone.tiles))
  private val destination = new Cache(() => targetScout()
    .map(_.pixel)
    .getOrElse(ByOption.minBy(tilesToConsider())(With.grids.lastSeen.get).getOrElse(With.geography.home).pixelCenter))

  private val scoutCleared = new ScoutCleared
  def recruit() {
    if (With.frame > Minutes(8)()) return
    if (scoutCleared.apply) return
    if (targetScout().isEmpty) return

    lock.matcher = MatchAnd(MatchScoutCatcher, (unit) => unit.base.exists(With.scouting.basesToLookForEnemyScouts().contains) || unit.pixelsToGetInRange(targetScout().get) < 32)
    lock.counter = CountOne
    lock.preference = PreferClose(targetScout().get.pixel)
    lock.acquire(this)
    addUnits(lock.units)
  }
  
  override def run() {
    units.foreach(ejector => {
      ejector.agent.intend(this, new Intention {
        toScoutTiles = if (targetScout().exists(_.likelyStillThere)) Seq.empty else tilesToConsider()
        toTravel = Some(destination())
        toAttack = if (ejector.matchups.targets.forall(targetScout().contains)) targetScout() else None
      })
    })
  }
}
