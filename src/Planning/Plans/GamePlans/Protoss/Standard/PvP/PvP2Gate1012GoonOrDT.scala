package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Architecture.Heuristics.PlacementProfiles
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvPIdeas.AttackWithDarkTemplar
import Planning.Plans.Macro.Automatic.{CapGasAt, CapGasWorkersAt, Pump}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.BuildCannonsAtNatural
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import Planning.UnitCounters.UnitCountExactly
import Planning.UnitMatchers.{UnitMatchOr, UnitMatchWarriors}
import Planning.{Plan, Predicate}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvP2Gate1012DT, PvP2Gate1012Goon, PvP2Gate1012GoonCounter}

class PvP2Gate1012GoonOrDT extends GameplanTemplate {

  override val activationCriteria: Predicate = new Employing(PvP2Gate1012Goon, PvP2Gate1012GoonCounter, PvP2Gate1012DT)
  override val completionCriteria: Predicate = new Latch(new UnitsAtLeast(2, Protoss.Nexus))

  override def blueprints = Vector(
    new Blueprint(this, building = Some(Protoss.Pylon),         placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 8.0)),
    new Blueprint(this, building = Some(Protoss.Gateway),       placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 4.0)),
    new Blueprint(this, building = Some(Protoss.Gateway),       placement = Some(PlacementProfiles.defensive), marginPixels = Some(32.0 * 4.0)),
    new Blueprint(this, building = Some(Protoss.Pylon),         placement = Some(PlacementProfiles.backPylon)),
    new Blueprint(this, building = Some(Protoss.ShieldBattery)),
    new Blueprint(this, building = Some(Protoss.Gateway),       placement = Some(PlacementProfiles.defensive)),
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon)),
    new Blueprint(this, building = Some(Protoss.Pylon), requireZone = Some(With.geography.ourNatural.zone)))

  override def priorityAttackPlan: Plan = new Parallel(
    new If(
      new EnemyStrategy(With.fingerprints.proxyGateway),
      new Attack(Protoss.Zealot, UnitCountExactly(1))),
    new AttackWithDarkTemplar)

  override def attackPlan: Plan = new If(
    new Or(
      new Not(new EnemyStrategy(With.fingerprints.twoGate, With.fingerprints.proxyGateway)),
      new UnitsAtLeast(3, Protoss.Dragoon, complete = true)),
    new PvPIdeas.AttackSafely)

  override val scoutPlan: Plan = new ScoutOn(Protoss.Pylon)
  
  override def emergencyPlans: Seq[Plan] = Seq(
    new PvPIdeas.ReactToGasSteal,
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush)

  class GoDT extends And(
    new Or(
      new Employing(PvP2Gate1012DT),
      new EnemyStrategy(With.fingerprints.fourGateGoon, With.fingerprints.proxyGateway)),
    new Not(new EnemyStrategy(With.fingerprints.robo)))
  
  override val buildOrder: Vector[BuildRequest] = ProtossBuilds.TwoGate1012
  override def buildPlans = Vector(

    new CapGasAt(300),
    new If(
      new And(
        new UnitsAtMost(2, Protoss.Gateway),
        new Not(new GoDT)),
      new CapGasWorkersAt(2)),

    new If(
      new EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.proxyGateway, With.fingerprints.twoGate),
      new BuildOrder(Get(7, Protoss.Zealot))),

    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore)),

    new If(
      new Not(new GoDT),
      new Build(Get(Protoss.DragoonRange))),

    new If(
      new EnemyStrategy(With.fingerprints.forgeFe),
      new Build(
        Get(Protoss.RoboticsFacility),
        Get(Protoss.RoboticsSupportBay))),

    new If(
      new Or(
        new UnitsAtLeast(2, UnitMatchOr(Protoss.DarkTemplar, Protoss.Reaver), complete = true),
        new And(
          new UnitsAtLeast(12, UnitMatchWarriors),
          new UnitsAtLeast(1, Protoss.Forge),
          new SafeAtHome)),
      new RequireMiningBases(2)),

    new If(
      new GoDT,
      new Build(
        Get(Protoss.CitadelOfAdun),
        Get(Protoss.TemplarArchives))),

    new If(
      new And(
        new GoDT,
        new Not(new UpgradeStarted(Protoss.DragoonRange))),
      new Parallel(
        new Pump(Protoss.DarkTemplar, 4),
        new Pump(Protoss.Zealot),
        new Trigger(
          new UnitsAtLeast(1, Protoss.TemplarArchives, complete = true),
          new Build(Get(Protoss.DragoonRange))))),

    new FlipIf(
      new SafeAtHome,
      new PvPIdeas.TrainArmy,
      new Build(Get(3, Protoss.Gateway))),

    new If(
      new SafeAtHome,
      new RequireMiningBases(2)),

    new Build(
      Get(Protoss.Forge),
      Get(4, Protoss.Gateway),
      Get(2, Protoss.Nexus)),
    new BuildCannonsAtNatural(2),
  )
}
