package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Compound.{If, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.BuildOrders.BuildOrder
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Milestones.UnitsAtLeast
import Planning.Predicates.Strategy.{Employing, StartPositionsAtLeast}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPRobo1012

class PvPRobo1012 extends GameplanTemplate {
  // Via https://tl.net/forum/bw-strategy/567442-pvp-bonyth-style-2-gate-3-zealot-21-gas-guide

  override val activationCriteria: Predicate = new Employing(PvPRobo1012)
  override val completionCriteria: Predicate = new UnitsAtLeast(1, Protoss.RoboticsFacility)

  override def scoutPlan: Plan = new If(new StartPositionsAtLeast(3), new ScoutOn(Protoss.Gateway, quantity = 2))
  override def attackPlan: Plan = new Trigger(new UnitsAtLeast(3, Protoss.Zealot, complete = true), new PvPIdeas.AttackSafely)

  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToGasSteal,
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush)

  override def buildOrder: Seq[BuildRequest] = Seq(
    Get(8, Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10, Protoss.Probe),
    Get(Protoss.Gateway),
    Get(12, Protoss.Probe),
    Get(2, Protoss.Gateway),
    Get(13, Protoss.Probe),
    Get(Protoss.Zealot),
    Get(2, Protoss.Pylon),
    Get(15, Protoss.Probe),
    Get(3, Protoss.Zealot),
    Get(Protoss.Assimilator),
    Get(17, Protoss.Probe),
    Get(Protoss.CyberneticsCore),
    Get(18, Protoss.Probe),
    Get(3, Protoss.Pylon),
    Get(20, Protoss.Probe),
    Get(2, Protoss.Dragoon),
    Get(Protoss.DragoonRange))

  override def buildPlans: Seq[Plan] = Seq(new BuildOrder(
    Get(6, Protoss.Dragoon),
    Get(4, Protoss.Zealot),
    Get(Protoss.RoboticsFacility)))
}
