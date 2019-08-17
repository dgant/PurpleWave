package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Compound.Trigger
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Placement.BuildCannonsAtNatural
import Planning.Plans.Scouting.ScoutForCannonRush
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.Employing
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP1ZealotExpand

class PvP1ZealotExpand extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(PvP1ZealotExpand)
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(5, Protoss.Gateway))

  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactTo2Gate,
    new ScoutForCannonRush)

  override val buildOrder: Vector[BuildRequest] = Vector(
    Get(8, Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10, Protoss.Probe),
    Get(Protoss.Gateway),
    Get(12, Protoss.Probe),
    Get(2, Protoss.Pylon),
    Get(13, Protoss.Probe),
    Get(Protoss.Zealot),
    Get(15, Protoss.Probe),
    Get(2, Protoss.Zealot),
    Get(17, Protoss.Probe))

  override def buildPlans = Vector(
    new RequireMiningBases(2),
    new Pump(Protoss.Dragoon),
    new Pump(Protoss.Zealot),
    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore)),
    new Trigger(
      new UnitsAtLeast(2, Protoss.Pylon),
      new BuildCannonsAtNatural(2)),
    new UpgradeContinuously(Protoss.DragoonRange),
    new Build(
      Get(5, Protoss.Gateway),
      Get(2, Protoss.Assimilator))
  )
}
