package Micro.Actions.Protoss

import Mathematics.Maff
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}


object BeArbiter extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = Protoss.Arbiter(unit)
  
  protected def needsUmbrella(target: UnitInfo): Boolean =
    ! target.isAny(
      Protoss.Arbiter,
      Protoss.DarkTemplar,
      Protoss.Interceptor,
      Protoss.Observer)

  val cloakRadiusPixels: Int = 32 * 7
  override protected def perform(arbiter: FriendlyUnitInfo): Unit = {
    if (arbiter.matchups.pixelsOfEntanglement > -16) {
      Retreat.delegate(arbiter)
      return
    }
    val goal                  = arbiter.team.map(_.vanguardAll()).getOrElse(arbiter.agent.destination)
    val goalDistanceSquared   = arbiter.pixelDistanceSquared(goal)
    val closerArbiters        = arbiter.team.map(_.units.view.filter(Protoss.Arbiter).filter(_.pixelDistanceSquared(goal) < goalDistanceSquared)).getOrElse(Iterable.empty).toVector
    val umbrellables          = arbiter.alliesSquadOrBattle.filter(needsUmbrella)
    val umbrellablesUncovered = umbrellables.filterNot(u => closerArbiters.exists(a => a.pixelDistanceCenter(u) < cloakRadiusPixels))
    val umbrellable           = Maff.minBy(umbrellables)(_.pixelDistanceSquared(goal))
    arbiter.agent.toTravel    = umbrellable.map(_.pixel).orElse(arbiter.agent.toTravel)
    if (umbrellable.forall(arbiter.pixelDistanceCenter(_) < 32 * 3)) {
      Potshot.delegate(arbiter)
    }
    Commander.move(arbiter)
  }
}
