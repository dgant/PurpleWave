package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Compound.If
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen2Gate1012Goon

class PvP2Gate1012Goon extends GameplanModeTemplate {

  override val activationCriteria: Predicate = new Employing(PvPOpen2Gate1012Goon)
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(5, Protoss.Gateway))
  override def defaultAttackPlan: Plan = new PvPIdeas.AttackSafely
  override val defaultScoutPlan: Plan = new ScoutOn(Protoss.Pylon)
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactToFFE)
  
  override val buildOrder = ProtossBuilds.OpeningTwoGate1012FiveZealot
  override def buildPlans = Vector(
    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore)),
    new PvPIdeas.TrainArmy,
    new RequireMiningBases(2),
    new If(
      new EnemyStrategy(With.fingerprints.fourGateGoon),
      new Build(Get(6, Protoss.Gateway))),
    new Build(
      Get(1, Protoss.RoboticsFacility),
      Get(4, Protoss.Gateway),
      Get(1, Protoss.Observatory),
      Get(6, Protoss.Gateway),
      Get(1, Protoss.RoboticsSupportBay))
  )
}
