package Micro.Actions.Protoss

import Debugging.Visualizations.Forces
import Mathematics.Maff
import Mathematics.Physics.ForceMath
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Actions.Combat.Tactics.Potshot
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Micro.Heuristics.Potential
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object BeArbiter extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = Protoss.Arbiter(unit)
  
  protected def needsUmbrella(target: UnitInfo): Boolean = target.isNone(
    Protoss.Arbiter,
    Protoss.DarkTemplar,
    Protoss.Interceptor,
    Protoss.Observer)

  val cloakRadiusPixels: Int = 64
  override protected def perform(arbiter: FriendlyUnitInfo): Unit = {
    if (arbiter.matchups.pixelsEntangled > -16) {
      Retreat.delegate(arbiter)
      return
    }
    val goal                              = arbiter.team.map(_.vanguardAll()).getOrElse(arbiter.agent.destinationNext())
    val goalDistanceSquared               = arbiter.pixelDistanceSquared(goal)
    val seniorArbiters                    = arbiter.alliesSquad.filter(_.squadAge > arbiter.squadAge)
    val umbrellables                      = arbiter.alliesSquad.filter(needsUmbrella)
    val umbrellablesUncovered             = umbrellables.filterNot(u => seniorArbiters.exists(_.pixelDistanceCenter(u) < cloakRadiusPixels))
    val umbrellable                       = Maff.minBy(umbrellables)(_.pixelDistanceSquared(goal))
    arbiter.agent.station.set(umbrellable.map(_.pixel).getOrElse(arbiter.agent.destinationNext()))
    arbiter.agent.forces(Forces.travel)   = Potential.towardsDestination(arbiter)
    arbiter.agent.forces(Forces.spacing)  = Potential.hardAvoidThreatRange(arbiter)
    if (seniorArbiters.nonEmpty) {
      arbiter.agent.forces(Forces.threat) = ForceMath
        .sum(seniorArbiters.map(s => Potential.towardsUnit(arbiter, s, Math.min(0, arbiter.pixelDistanceCenter(s) - cloakRadiusPixels))))
        .clipAtMost(1.0)
    }
    MicroPathing.setWaypointForcefully(arbiter)
    if (arbiter.pixelDistanceCenter(arbiter.agent.destinationNext()) < 48 && arbiter.matchups.pixelsEntangled < 0) {
      Potshot.delegate(arbiter)
    }
    Commander.move(arbiter)
  }
}
