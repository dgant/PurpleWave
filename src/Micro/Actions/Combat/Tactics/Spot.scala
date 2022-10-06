package Micro.Actions.Combat.Tactics

import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Agency.Commander
import Planning.Predicates.MacroFacts
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Spot extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.flying
    && ! unit.canAttack
    && ! unit.isAny(Terran.Dropship, Protoss.Shuttle)
    && unit.agent.passengers.isEmpty
    && unit.squad.exists(_.attackers.nonEmpty)
    && (unit.matchups.pixelsEntangled < -64
      || unit.totalHealth > 500
      || (unit.cloaked
        && ! MacroFacts.enemyHasShown(Terran.Comsat, Terran.SpellScannerSweep)
        && unit.matchups.enemyDetectorDeepest.forall(_.pixelsToSightRange(unit) > 96))))
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val from            = unit.team.map(_.centroidAir).getOrElse(unit.agent.destination)
    val toSpotEnemy     = best(from, unit, unit.enemiesSquad).orElse(best(from, unit, unit.enemiesBattle))
    val toSpot          = toSpotEnemy.map(_.pixel).getOrElse(unit.agent.destination)
    unit.agent.toTravel = Some(toSpot)
    Commander.move(unit)
  }

  def best(from: Pixel, spotter: FriendlyUnitInfo, enemies: Iterable[UnitInfo]): Option[UnitInfo] = {
    Maff.minBy(Maff.orElse(enemies.filterNot(_.visible), enemies))(_.pixelDistanceCenter(from))
  }
}
