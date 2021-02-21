package Micro.Actions.Protoss

import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Agency.Commander
import Planning.UnitMatchers.MatchBuilding
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object BeArbiter extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.is(Protoss.Arbiter)
  
  protected def needsUmbrella(target: UnitInfo): Boolean =
    ! target.isAny(
      Protoss.Arbiter,
      Protoss.DarkTemplar,
      Protoss.Interceptor,
      Protoss.Observer,
      MatchBuilding)

  val cloakRadiusPixels = 32 * 7
  override protected def perform(arbiter: FriendlyUnitInfo): Unit = {
    if (arbiter.matchups.pixelsOfEntanglement > -16) {
      Retreat.delegate(arbiter)
      return
    }
    val goal                  = arbiter.battle.map(_.us.vanguard()).getOrElse(arbiter.agent.destination)
    val goalDistanceSquared   = arbiter.pixelDistanceSquared(goal)
    val closerArbiters        = arbiter.battle.map(_.us.units.view.filter(_.is(Protoss.Arbiter)).filter(_.pixelDistanceSquared(goal) < goalDistanceSquared)).getOrElse(Iterable.empty).toVector
    val umbrellables          = arbiter.alliesSquadOrBattle.filter(needsUmbrella)
    val umbrellablesUncovered = umbrellables.filterNot(u => closerArbiters.exists(a => a.pixelDistanceCenter(u) < cloakRadiusPixels))
    val umbrellable           = ByOption.minBy(umbrellables)(_.pixelDistanceSquared(goal))
    arbiter.agent.toTravel    = umbrellable.map(_.pixel).orElse(arbiter.agent.toTravel)
    if (umbrellable.forall(arbiter.pixelDistanceCenter(_) < 32 * 3)) {
      Potshot.delegate(arbiter)
    }
    Commander.move(arbiter)
  }
}
