package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.EjectScout
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound.{If, Or, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvPIdeas.AttackWithDarkTemplar
import Planning.Plans.Macro.Automatic.{CapGasWorkersAt, Pump, PumpWorkers}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Scouting.{ScoutForCannonRush, ScoutOn}
import Planning.Predicates.Compound.{And, Check, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyDarkTemplarLikely, SafeAtHome}
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvP3GateGoon, PvP3GateGoonCounter}

class PvP3GateGoon extends GameplanTemplate {
  
  override val activationCriteria : Predicate = new Employing(PvP3GateGoon, PvP3GateGoonCounter)
  override val completionCriteria : Predicate = new UnitsAtLeast(5, Protoss.Gateway)

  override def blueprints = Vector(
    new Blueprint(Protoss.Pylon,         placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 10.0)),
    new Blueprint(Protoss.ShieldBattery),
    new Blueprint(Protoss.Gateway,       placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 12.0)),
    new Blueprint(Protoss.Gateway,       placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 12.0)),
    new Blueprint(Protoss.Gateway,       placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 12.0)),
    new Blueprint(Protoss.Pylon,         placement = Some(PlacementProfiles.backPylon)),
    new Blueprint(Protoss.Pylon),
    new Blueprint(Protoss.Pylon),
    new Blueprint(Protoss.Pylon),
    new Blueprint(Protoss.Pylon),
    new Blueprint(Protoss.Pylon, requireZone = Some(With.geography.ourNatural.zone)))

  override def priorityAttackPlan : Plan = new AttackWithDarkTemplar
  override def attackPlan: Plan = new Trigger(
    new Or(
      new EnemyStrategy(With.fingerprints.gasSteal),
      new And(
        new UnitsAtLeast(1, Protoss.Dragoon, complete = true),
        new Not(new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway))),
      new UnitsAtLeast(3, Protoss.Dragoon, complete = true)),
    new PvPIdeas.AttackSafely)

  override def scoutPlan: Plan = new ScoutOn(Protoss.Gateway)
  override def workerPlan: Plan = NoPlan()

  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToGasSteal,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToProxyGateways,
    new PvPIdeas.ReactTo2Gate,
    new PvPIdeas.ReactToFFE,
    new ScoutForCannonRush)
  
  override val buildOrder: Seq[BuildRequest] = ProtossBuilds.ThreeGateGoon

  override val buildPlans = Vector(
    new If(
      new UnitsAtMost(2, Protoss.Gateway),
      new CapGasWorkersAt(2)),

    new EjectScout,

    new Pump(Protoss.Probe, 23),
    new BuildOrder(Get(8, Protoss.Dragoon)),
    new If(
      new And(
        new EnemyStrategy(With.fingerprints.fourGateGoon),
        new BasesAtMost(1)),
      new BuildOrder(
        Get(Protoss.CitadelOfAdun),
        Get(11, Protoss.Dragoon),
        Get(Protoss.TemplarArchives),
        Get(14, Protoss.Dragoon),
        Get(3, Protoss.DarkTemplar),
        Get(2, Protoss.Nexus)),
      new BuildOrder(
        Get(2, Protoss.Nexus),
        Get(11, Protoss.Dragoon))),


    new If(
      new BasesAtLeast(2),
      new Parallel(
        new If(
          new And(
            new Not(new EnemyStrategy(With.fingerprints.fourGateGoon)),
            new Or(
              new And(
                new SafeAtHome,
                new Check(() => ! With.geography.enemyBases.exists(b => b.isNaturalOf.isDefined && b.townHall.isDefined))),
              new EnemyHasShown(Protoss.Forge),
              new EnemyDarkTemplarLikely)),
          new Build(
            Get(Protoss.RoboticsFacility),
            Get(Protoss.Observatory))))),

    new PumpWorkers,
    new PvPIdeas.TrainArmy,
    new If(
      new Not(new EnemyStrategy(
        With.fingerprints.twoGate,
        With.fingerprints.robo,
        With.fingerprints.forgeFe,
        With.fingerprints.gatewayFe,
        With.fingerprints.nexusFirst)),
      new Build(
        Get(Protoss.RoboticsFacility),
        Get(Protoss.Observatory))),

    new Build(Get(5, Protoss.Gateway)),
  )
}
