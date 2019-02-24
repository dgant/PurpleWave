package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.EjectScout
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.{If, Or, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvPIdeas.AttackWithDarkTemplar
import Planning.Plans.Macro.Automatic.{CapGasWorkersAt, Pump, PumpWorkers}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyDarkTemplarLikely, SafeAtHome}
import Planning.Predicates.Strategy.Employing
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP3GateGoon

class PvP3GateGoon extends GameplanTemplate {
  
  override val activationCriteria : Predicate = new Employing(PvP3GateGoon)
  override val completionCriteria : Predicate = new Latch(new Or(new UnitsAtLeast(1, Protoss.RoboticsFacility), new UnitsAtLeast(5, Protoss.Gateway)))
  override def priorityAttackPlan : Plan = new AttackWithDarkTemplar
  override def attackPlan: Plan = new Trigger(new UnitsAtLeast(1, Protoss.Dragoon, complete = true), new PvPIdeas.AttackSafely)
  override def scoutPlan   : Plan = new ScoutOn(Protoss.Gateway)
  override def workerPlan  : Plan = NoPlan()
  override def placementPlan: Plan = new If(
    new BasesAtLeast(2),
    new ProposePlacement(new Blueprint(this, building = Some(Protoss.Pylon), preferZone = Some(With.geography.ourNatural.zone))))

  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new Trigger(new UnitsAtLeast(1, Protoss.Reaver), new PvPIdeas.ReactToFFE))
  
  override val buildOrder: Seq[BuildRequest] = ProtossBuilds.ThreeGateGoon

  override val buildPlans = Vector(
    new If(
      new UnitsAtMost(2, Protoss.Gateway),
      new CapGasWorkersAt(2)),

    new EjectScout,

    new If(new UnitsAtLeast(2, Protoss.DarkTemplar, complete = true), new RequireMiningBases(2)),

    new Pump(Protoss.Probe, 23),
    new BuildOrder(
      Get(8, Protoss.Dragoon),
      Get(2, Protoss.Nexus),
      Get(11, Protoss.Dragoon)),

    new If(
      new BasesAtLeast(2),
      new Parallel(
        new If(
          new Or(
            new SafeAtHome,
            new EnemyHasShown(Protoss.Forge),
            new EnemyDarkTemplarLikely),
          new Build(
            Get(Protoss.RoboticsFacility),
            Get(Protoss.Observatory))),
        new Build(Get(5, Protoss.Gateway)))),

    new PumpWorkers,
    new PvPIdeas.TrainArmy,
  )
}
