package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.{Attack, EjectScout}
import Planning.Plans.Compound.{If, Or, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.{CapGasAt, CapGasWorkersAt}
import Planning.Plans.Macro.Build.ProposePlacement
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.{BuildCannonsAtNatural, BuildCannonsInMain}
import Planning.Plans.Scouting.{ScoutForCannonRush, ScoutOn}
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyBasesAtMost
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitMatchers.UnitMatchWarriors
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP2GateGoon

class PvP2GateGoon extends GameplanTemplate {
  
  override val activationCriteria : Predicate = new Employing(PvP2GateGoon)
  override val completionCriteria : Predicate = new Latch(new UnitsAtLeast(5, Protoss.Gateway))

  override def blueprints = Vector(
    new Blueprint(this, building = Some(Protoss.Pylon),   placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 8.0)),
    new Blueprint(this, building = Some(Protoss.Gateway), placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 10.0)),
    new Blueprint(this, building = Some(Protoss.Gateway), placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 10.0)),
    new Blueprint(this, building = Some(Protoss.Pylon),   placement = Some(PlacementProfiles.backPylon)))

  override def placementPlan: Plan = new Parallel(
    super.placementPlan,
    new If(
      new Or(
        new BasesAtLeast(2),
        new And(
          new UnitsAtLeast(3, Protoss.Pylon),
          new Not(new EnemyStrategy(With.fingerprints.proxyGateway)))),
      new ProposePlacement(new Blueprint(this, building = Some(Protoss.Pylon), preferZone = Some(With.geography.ourNatural.zone)))))

  override def priorityAttackPlan: Plan = new Attack(Protoss.DarkTemplar)
  override def attackPlan: Plan = new If(
    new Or(
      new EnemyStrategy(With.fingerprints.gasSteal, With.fingerprints.nexusFirst, With.fingerprints.oneGateCore, With.fingerprints.robo, With.fingerprints.dtRush),
      new And(
        new Not(new EnemyStrategy(With.fingerprints.proxyGateway)),
        new UnitsAtLeast(3, Protoss.Dragoon, complete = true)),
      new UnitsAtLeast(7, Protoss.Dragoon, complete = true)),
    new PvPIdeas.AttackSafely)

  override def scoutPlan: Plan = new ScoutOn(Protoss.Zealot)

  override def emergencyPlans: Seq[Plan] = Vector(
    new PvPIdeas.ReactToGasSteal,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToFFE,
    new ScoutForCannonRush)
  
  override val buildOrder: Seq[BuildRequest] = ProtossBuilds.ZCoreZTwoGateGoon

  class GoDT extends Or(
    new UnitsAtLeast(1, Protoss.CitadelOfAdun),
    new And(
      new BasesAtMost(1),
      new EnemyStrategy(With.fingerprints.fourGateGoon),
      new Not(new EnemyStrategy(With.fingerprints.robo))))

  override val buildPlans = Vector(

    new If(new Not(new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway)), new EjectScout),

    new If(new GoDT, new CapGasAt(0, 350), new CapGasAt(0, 250)),
    new If(
      new UnitsAtMost(0, Protoss.CyberneticsCore),
      new CapGasWorkersAt(0),
      new Trigger(
        new UnitsAtLeast(1, Protoss.CyberneticsCore, complete = true),
        new If(
          new And(
            new UnitsAtMost(21, Protoss.Probe),
            new Not(new GoDT))),
          new CapGasWorkersAt(2))),

    new If(
      new Or(
        new EnemyStrategy(With.fingerprints.dtRush),
        new And(
          new BasesAtLeast(2),
          new EnemyBasesAtMost(1),
          new Not(new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.robo, With.fingerprints.nexusFirst, With.fingerprints.fourGateGoon)))),
      new Parallel(
        new BuildCannonsInMain(1),
        new BuildCannonsAtNatural(2))),

    new If(new EnemyStrategy(With.fingerprints.earlyForge), new RequireMiningBases(2)),

    new Build(Get(Protoss.DragoonRange)),

    new If(new EnemyStrategy(With.fingerprints.oneGateCore), new RequireMiningBases(2)),
    new If(new UnitsAtLeast(1, Protoss.DarkTemplar, complete = true), new RequireMiningBases(2)),
    new If(new UnitsAtLeast(12, UnitMatchWarriors), new RequireMiningBases(2)),

    new PvPIdeas.TrainArmy,

    new If(
      new EnemyStrategy(With.fingerprints.proxyGateway),
      new Build(Get(4, Protoss.Gateway)),
      new If(
        new GoDT,
        new Build(
          Get(Protoss.CitadelOfAdun),
          Get(Protoss.TemplarArchives)),
        new RequireMiningBases(2))),

    new If(
      new EnemyStrategy(With.fingerprints.dtRush),
      new Build(
        Get(2, Protoss.Assimilator),
        Get(Protoss.RoboticsFacility),
        Get(Protoss.Observatory))),

    new Build(
      Get(5, Protoss.Gateway),
      Get(2, Protoss.Assimilator)),

    new If(
      new Not(new EnemyStrategy(With.fingerprints.robo)),
      new BuildCannonsAtNatural(2))
  )
}
