package Micro.Squads

import Lifecycle.With
import Micro.Agency.Intention
import Performance.Cache
import Planning.Plans.Scouting.ScoutCleared
import Planning.UnitMatchers.{MatchAnd, MatchScoutCatcher}
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

    val scouts = With.scouting.enemyScouts()
    if (scouts.isEmpty) return

    // TODO: Lock one unit using this matcher
    MatchAnd(MatchScoutCatcher, (unit) => unit.base.exists(With.scouting.basesToLookForEnemyScouts().contains) || unit.pixelsToGetInRange(targetScout().get) < 32)
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
