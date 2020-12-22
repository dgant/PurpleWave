package Micro.Actions.Protoss

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Tactics.Potshot
import Planning.UnitMatchers.UnitMatchBuilding
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object BeAnArbiter extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.is(Protoss.Arbiter)
  
  protected def needsUmbrella(target: UnitInfo): Boolean =
    ! target.isAny(
      Protoss.Arbiter,
      Protoss.DarkTemplar,
      Protoss.Interceptor,
      Protoss.Observer,
      UnitMatchBuilding)

  val cloakRadiusPixels = 32 * 7
  override protected def perform(arbiter: FriendlyUnitInfo): Unit = {
    if (arbiter.matchups.pixelsOfEntanglement > -16) {
      Retreat.delegate(arbiter)
      return
    }
    val goal                  = arbiter.battle.map(_.us.vanguard()).getOrElse(arbiter.agent.destination)
    val goalDistanceSquared   = arbiter.pixelDistanceSquared(goal)
    val closerArbiters        = arbiter.battle.map(_.us.units.view.filter(_.is(Protoss.Arbiter)).filter(_.pixelDistanceSquared(goal) < goalDistanceSquared)).getOrElse(Iterable.empty).toVector
    val umbrellables          = arbiter.immediateAllies.view.filter(needsUmbrella)
    val umbrellablesUncovered = umbrellables.filterNot(u => closerArbiters.exists(a => a.pixelDistanceCenter(u) < cloakRadiusPixels))
    val umbrellable           = ByOption.minBy(umbrellables)(_.pixelDistanceSquared(goal))
    arbiter.agent.toTravel    = umbrellable.map(_.pixelCenter).orElse(arbiter.agent.toTravel)
    if (umbrellable.forall(arbiter.pixelDistanceCenter(_) < 32 * 3)) {
      Potshot.delegate(arbiter)
    }
    With.commander.move(arbiter)
  }
}
