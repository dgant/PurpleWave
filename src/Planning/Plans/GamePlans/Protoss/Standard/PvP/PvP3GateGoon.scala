package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.{FlipIf, If, Or, Parallel}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.{Pump, PumpWorkers}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones.{EnemiesAtLeast, EnemyHasShown, MiningBasesAtLeast, UnitsAtLeast}
import Planning.Predicates.Reactive.{EnemyDarkTemplarLikely, SafeAtHome}
import Planning.Predicates.Strategy.Employing
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvPOpen3GateGoon

class PvP3GateGoon extends GameplanModeTemplate {
  
  override val activationCriteria : Predicate = new Employing(PvPOpen3GateGoon)
  override val completionCriteria : Predicate = new Latch(new Or(new UnitsAtLeast(1, Protoss.RoboticsFacility), new UnitsAtLeast(5, Protoss.Gateway)))
  override def defaultAttackPlan  : Plan = new PvPIdeas.AttackSafely
  override def defaultScoutPlan   : Plan = new ScoutOn(Protoss.CyberneticsCore)
  override def defaultWorkerPlan  : Plan = NoPlan()
  override def blueprints = Vector(
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon), preferZone = Some(With.geography.ourNatural.zone)))

  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactToFFE)
  
  override val buildOrder: Seq[BuildRequest] = ProtossBuilds.Opening_3GateDragoon
  
  override val buildPlans = Vector(

    // Vs. 4-Gates we need the extra Dragoons ASAP
    new If(
      new And(
        new MiningBasesAtLeast(2),
        new Not(new SafeAtHome)),
      new Build(Get(5, Protoss.Gateway))),

    new If(

      new SafeAtHome,
      new PumpWorkers(oversaturate = true),
      new PumpWorkers),

    new FlipIf(
      new And(
        new UnitsAtLeast(7, Protoss.Dragoon),
        new SafeAtHome),
      new Pump(Protoss.Dragoon),
      new Parallel(
        new If(
          new And(
            new SafeAtHome,
            new Or(
              new EnemyDarkTemplarLikely,
              new EnemiesAtLeast(1, Protoss.Forge),
              new EnemiesAtLeast(1, Protoss.PhotonCannon))),
            new Build(Get(1, Protoss.Forge))),
        new RequireMiningBases(2))),
    new If(
      new Or(
        new SafeAtHome,
        new EnemyHasShown(Protoss.Forge),
        new EnemyDarkTemplarLikely),
      new Build(Get(Protoss.RoboticsFacility)),
      new Build(Get(5, Protoss.Gateway)))
  )
}
