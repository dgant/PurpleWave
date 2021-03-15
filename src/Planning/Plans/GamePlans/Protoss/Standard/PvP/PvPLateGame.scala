package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Basic.WriteStatus
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{CapGasAt, PumpWorkers, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Placement.{BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Predicates.Compound._
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, EnemyBasesAtMost, SafeAtHome, SafeToMoveOut}
import Planning.Predicates.Strategy.{EnemyRecentStrategy, EnemyStrategy}
import Planning.UnitMatchers.{MatchOr, MatchWarriors}
import ProxyBwapi.Races.Protoss

class PvPLateGame extends GameplanTemplate {

  override val emergencyPlans: Vector[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush,
    new PvPIdeas.ReactToRoboAsDT,
    new PvPIdeas.ReactToArbiters)

  override val attackPlan: Plan = new PvPIdeas.AttackSafely
  override def archonPlan: Plan = new PvPIdeas.MeldArchonsPvP
  override def workerPlan: Plan = new PumpWorkers(maximumTotal = 25)

  val goingTemplar = new And(
    new Not(new EnemyStrategy(With.fingerprints.robo, With.fingerprints.dtRush)),
    new UnitsAtLeast(1, Protoss.CitadelOfAdun),
    new UnitsAtMost(0, Protoss.RoboticsFacility),
    new UnitsAtMost(0, Protoss.RoboticsSupportBay))

  val buildCannons = new And(
    // Templar are reasonably likely
    new Or(
      new EnemyHasShownCloakedThreat,
      new EnemyRecentStrategy(With.fingerprints.dtRush),
      new And(
        new EnemyBasesAtMost(1),
        new Not(new EnemyStrategy(With.fingerprints.robo, With.fingerprints.threeGateGoon, With.fingerprints.fourGateGoon)))),

    // We can't use a Robotics Facility
    // Note that this is deliberately placed after the Templar check so it only sticks at the point we're thinking about DTs
    new Sticky(new And(new UnitsAtMost(0, Protoss.RoboticsFacility), goingTemplar)))

  class EnemyPassiveOpening extends Or(
    new And(
      new Not(new EnemyStrategy(With.fingerprints.fourGateGoon)),
      new EnemyStrategy(With.fingerprints.robo, With.fingerprints.nexusFirst, With.fingerprints.forgeFe)),
    new EnemyBasesAtLeast(2))

  class AddEarlyTech extends If(
    goingTemplar,
    new Parallel(
      new Build(
        Get(Protoss.CitadelOfAdun),
        Get(2, Protoss.Gateway),
        Get(Protoss.TemplarArchives),
        Get(4, Protoss.Gateway),
        Get(2, Protoss.Assimilator),
        Get(5, Protoss.Gateway),
        Get(Protoss.ZealotSpeed),
        Get(Protoss.Forge),
        Get(Protoss.GroundDamage),
        Get(Protoss.PsionicStorm),
        Get(7, Protoss.Gateway),
        Get(Protoss.HighTemplarEnergy),
        Get(2, Protoss.GroundDamage))),
    new Parallel(
      new Build(Get(3, Protoss.Gateway)),
      new FlipIf(
        new And(
          new SafeAtHome,
          new Or(new UnitsAtLeast(1, Protoss.RoboticsFacility), new EnemyBasesAtLeast(2))),
        new Parallel(
          new Build(
            Get(4, Protoss.Gateway),
            Get(2, Protoss.Assimilator),
            Get(6, Protoss.Gateway))),
        new Build(
          Get(2, Protoss.Assimilator),
          Get(Protoss.RoboticsFacility),
          Get(Protoss.Observatory),
          Get(Protoss.RoboticsSupportBay),
          Get(4, Protoss.Gateway),
          Get(Protoss.ShuttleSpeed)))))

  class AddLateTech extends Parallel(
    new Build(
      Get(6, Protoss.Gateway),
      Get(Protoss.Forge),
      Get(Protoss.CitadelOfAdun),
      Get(Protoss.GroundDamage),
      Get(Protoss.ZealotSpeed),
      Get(Protoss.TemplarArchives)),
    new If(new Or(new UpgradeStarted(Protoss.GroundDamage, 3), new UnitsAtLeast(2, Protoss.Forge)), new UpgradeContinuously(Protoss.GroundArmor)),
    new Build(Get(Protoss.RoboticsFacility), Get(Protoss.Observatory)))

  class GetReactiveObservers extends If(
    new And(
      new EnemyHasShownCloakedThreat,
      new Or(
        new And(goingTemplar, new ReadyForThirdIfSafe),
        new Not(goingTemplar))),
    new Build(Get(Protoss.RoboticsFacility), Get(Protoss.Observatory), Get(Protoss.ObserverSpeed)))

  class ReadyForThirdIfSafe extends And(
    new Or(
      new Not(new EnemyHasShownCloakedThreat),
      new UnitsAtLeast(2, Protoss.Observer)),
    new Or(
      new And(
        goingTemplar,
        new UpgradeComplete(Protoss.ZealotSpeed),
        new TechComplete(Protoss.PsionicStorm),
        new UnitsAtLeast(6, Protoss.Gateway, complete = true)),
      new And(
        new Not(goingTemplar),
        new UnitsAtLeast(4, Protoss.Gateway, complete = true),
        new UnitsAtLeast(1, Protoss.Shuttle, complete = true),
        new UnitsAtLeast(2, Protoss.Reaver, complete = true))))

  class AddScalingTech extends Parallel(

    new If(
      new And(
        new Not(goingTemplar),
        new MiningBasesAtLeast(2),
        new UnitsAtLeast(24, Protoss.Dragoon),
        new UnitsAtLeast(5, Protoss.Gateway, complete = true)),
      new Build(Get(Protoss.CitadelOfAdun), Get(Protoss.ZealotSpeed))),
    new If(
      new And(
        new MiningBasesAtLeast(3),
        new UnitsAtLeast(24, MatchWarriors)),
      new Parallel(
        new Build(Get(Protoss.Forge)),
        new UpgradeContinuously(Protoss.GroundDamage),
        new Build(
          Get(Protoss.CitadelOfAdun),
          Get(Protoss.TemplarArchives),
          Get(Protoss.ZealotSpeed),
          Get(Protoss.PsionicStorm)))))

  class FinishDarkTemplarRush extends If(
    new And(new UnitsAtLeast(1, Protoss.TemplarArchives), new Not(new EnemyStrategy(With.fingerprints.robo))),
    new If(
      new Or(new EnemiesAtLeast(1, Protoss.Forge), new EnemiesAtLeast(1, Protoss.PhotonCannon), new EnemiesAtLeast(1, Protoss.RoboticsFacility)),
      new BuildOrder(Get(1, Protoss.DarkTemplar)),
      new BuildOrder(Get(2, Protoss.DarkTemplar))))

  class NeedToCutWorkersForGateways extends And(
    new MiningBasesAtLeast(2),
    new EnemyBasesAtMost(1),
    new UnitsAtMost(4, MatchOr(Protoss.Gateway, Protoss.RoboticsFacility, Protoss.RoboticsSupportBay)),
    new Not(new EnemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.forgeFe, With.fingerprints.gatewayFe, With.fingerprints.dtRush)))

  override def buildPlans: Seq[Plan] = Seq(
    new CapGasAt(500),
    new FinishDarkTemplarRush,
    new If(
      new UnitsAtLeast(1, Protoss.RoboticsSupportBay),
      new PumpWorkers(maximumConcurrently = 2),
      new If(
        new NeedToCutWorkersForGateways,
        new WriteStatus("GatewayCut"),
        new PumpWorkers(maximumConcurrently = 1))),

    new Build(Get(Protoss.Pylon), Get(Protoss.Gateway), Get(Protoss.Assimilator), Get(Protoss.CyberneticsCore), Get(Protoss.DragoonRange), Get(2, Protoss.Gateway)),

    // Detection
    new If(buildCannons, new BuildCannonsAtNatural(2)),
    new If(buildCannons, new BuildCannonsAtExpansions(1)),
    new GetReactiveObservers,

    // Expansions
    new RequireMiningBases(2),
    new If(new And(new ReadyForThirdIfSafe, new SafeToMoveOut), new RequireMiningBases(3)),

    // Meat
    new If(new UnitsAtLeast(1, Protoss.HighTemplar), new Build(Get(Protoss.PsionicStorm))),
    new PvPIdeas.TrainArmy,

    // Two-base transition
    new If(new NeedToCutWorkersForGateways, new Build(Get(5, Protoss.Gateway), Get(2, Protoss.Assimilator))),
    new AddEarlyTech,
    new BuildGasPumps,

    // Three-base (including likely mine-out)t ransition
    new RequireBases(3),
    new AddLateTech,
    new RequireMiningBases(3),
    new If(new MiningBasesAtLeast(3), new If(goingTemplar, new Build(Get(9, Protoss.Gateway)), new Build(Get(7, Protoss.Gateway)))),
    new RequireMiningBases(4),
    new If(new MiningBasesAtLeast(4), new If(goingTemplar, new Build(Get(14, Protoss.Gateway)), new Build(Get(12, Protoss.Gateway)))),
  )
}
