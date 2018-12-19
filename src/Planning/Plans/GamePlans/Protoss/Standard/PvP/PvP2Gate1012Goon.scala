package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plans.Army.EjectScout
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvPIdeas.AttackWithDarkTemplar
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP2Gate1012Goon

class PvP2Gate1012Goon extends GameplanModeTemplate {

  override val activationCriteria: Predicate = new Employing(PvP2Gate1012Goon)
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(5, Protoss.Gateway))
  override def priorityAttackPlan: Plan = new AttackWithDarkTemplar
  override def defaultAttackPlan: Plan = new PvPIdeas.AttackSafely
  override val defaultScoutPlan: Plan = new ScoutOn(Protoss.Pylon)
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactToFFE)
  
  override val buildOrder = ProtossBuilds.TwoGate1012
  override def buildPlans = Vector(
    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.DragoonRange)),

    new If(new UnitsAtLeast(2, Protoss.DarkTemplar, complete = true), new RequireMiningBases(2)),
    new If(new UnitsAtLeast(2, Protoss.Reaver, complete = true), new RequireMiningBases(2)),

    new If(
      new EnemyStrategy(With.fingerprints.fourGateGoon),
      new Parallel(
        new EjectScout,
        new Build(Get(Protoss.CitadelOfAdun), Get(Protoss.TemplarArchives)))),

    new PvPIdeas.TrainArmy,
    new Build(Get(3, Protoss.Gateway)),

    new If(
      new SafeAtHome,
      new RequireMiningBases(2),
      new IfOnMiningBases(1, new Build(Get(Protoss.RoboticsFacility), Get(Protoss.RoboticsSupportBay)))),

    new Build(
      Get(1, Protoss.RoboticsFacility),
      Get(4, Protoss.Gateway),
      Get(1, Protoss.Observatory),
      Get(6, Protoss.Gateway),
      Get(1, Protoss.RoboticsSupportBay))
  )
}
