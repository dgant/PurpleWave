package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Lifecycle.With
import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic.{PylonBlock, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireBases, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Predicates.Compound.{And, Not, Sticky}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyBasesAtLeast, EnemyBasesAtMost, SafeAtHome, SafeToMoveOut}
import Planning.Predicates.Strategy.{EnemyRecentStrategy, EnemyStrategy}
import Planning.UnitMatchers.{UnitMatchMobileDetectors, UnitMatchWarriors, UnitMatchWorkers}
import ProxyBwapi.Races.Protoss

class PvPLateGame extends GameplanTemplate {

  override val emergencyPlans: Vector[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies,
    new PvPIdeas.ReactToCannonRush)

  override def priorityAttackPlan: Plan = new PvPIdeas.AttackWithDarkTemplar
  override val attackPlan: Plan = new Parallel(
    new If(new EnemiesAtMost(0, UnitMatchMobileDetectors), new Attack(Protoss.DarkTemplar)),
    new PvPIdeas.AttackSafely)

  override def archonPlan: Plan = new PvPIdeas.MeldArchonsPvP

  val goingTemplar = new Not(new Sticky(new UnitsAtLeast(1, Protoss.RoboticsSupportBay)))

  val buildCannons = new And(
    // It's a good bet to do so; DT are the scariest threat
    new Or(
      new SafeAtHome,
      new EnemyHasShownCloakedThreat),

    // Templar are reasonably likely
    new Or(
      new EnemyRecentStrategy(With.fingerprints.dtRush),
      new ReadyForThirdIfSafe, // We're far enough along
      new And(
        new EnemyBasesAtMost(1),
        new Not(new EnemyStrategy(With.fingerprints.robo, With.fingerprints.fourGateGoon)))),

    // We can't use a Robotics Facility
    // Note that this is deliberately placed after the Templar check so it only sticks at the point we're thinking about DTs
    new Sticky(new UnitsAtMost(0, Protoss.RoboticsFacility)))

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
        Get(6, Protoss.Gateway),
        Get(2, Protoss.Assimilator)),
      new BuildGasPumps,
      new Build(
        Get(Protoss.ZealotSpeed),
        Get(Protoss.Forge),
        Get(Protoss.GroundDamage),
        Get(Protoss.PsionicStorm),
        Get(7, Protoss.Gateway),
        Get(Protoss.HighTemplarEnergy),
        Get(2, Protoss.GroundDamage),
        Get(10, Protoss.Gateway))),

    new Parallel(
      new Build(Get(3, Protoss.Gateway)),
      new If(new UnitsAtLeast(1, Protoss.RoboticsFacility), new BuildGasPumps),
      new FlipIf(
        new And(
          new SafeAtHome,
          new Or(
            new UnitsAtLeast(1, Protoss.RoboticsFacility),
            new EnemyBasesAtLeast(2))),
        new Build(
          Get(5, Protoss.Gateway),
          Get(2, Protoss.Assimilator)),
        new Build(
          Get(Protoss.RoboticsFacility),
          Get(Protoss.Observatory),
          Get(Protoss.RoboticsSupportBay),
          Get(Protoss.ShuttleSpeed))),
      new BuildGasPumps,
      new Build(
        Get(6, Protoss.Gateway),
        Get(Protoss.CitadelOfAdun),
        Get(Protoss.ZealotSpeed),
        Get(7, Protoss.Gateway),
        Get(Protoss.ScarabDamage))))

  class AddLateTech extends Parallel(
    new Build(Get(Protoss.Forge)),
    new If(new GasPumpsAtLeast(4), new Build(Get(2, Protoss.Forge))),
    new UpgradeContinuously(Protoss.GroundDamage),
    new If(new Or(new UpgradeStarted(Protoss.GroundDamage, 3), new UnitsAtLeast(2, Protoss.Forge)), new UpgradeContinuously(Protoss.GroundArmor)),
    new Build(Get(Protoss.RoboticsFacility), Get(Protoss.Observatory)))

  class AddLateGateways extends Parallel(
    new If(new MiningBasesAtLeast(2), new Build(Get(9, Protoss.Gateway))),
    new If(new MiningBasesAtLeast(3), new Build(Get(13, Protoss.Gateway))),
    new If(new MiningBasesAtLeast(4), new Build(Get(16, Protoss.Gateway))),
    new If(new MiningBasesAtLeast(5), new Build(Get(20, Protoss.Gateway))))

  class GetReactiveObservers extends If(
    new And(
      new EnemyHasShown(Protoss.DarkTemplar),
      new Or(new Not(goingTemplar), new ReadyForThirdIfSafe)),
    new Build(Get(Protoss.RoboticsFacility), Get(Protoss.Observatory), Get(Protoss.ObserverSpeed)))

  class ReadyForThirdIfSafe extends And(
    new Or(
      new Not(new EnemyHasShown(Protoss.DarkTemplar)),
      new UnitsAtLeast(2, Protoss.Observer)),
    new Or(
      new And(
        goingTemplar,
        new UpgradeComplete(Protoss.ZealotSpeed),
        new TechComplete(Protoss.PsionicStorm),
        new UnitsAtLeast(7, Protoss.Gateway)),
      new And(
        new Not(goingTemplar),
        new UnitsAtLeast(5, Protoss.Gateway, complete = true),
        new UnitsAtLeast(1, Protoss.Shuttle, complete = true),
        new UnitsAtLeast(1, Protoss.Reaver, complete = true))))

  class SafeForThird extends And(
    new SafeToMoveOut,
    new EnemyBasesAtLeast(3),
    new Or(
      new Not(new EnemyHasShown(Protoss.DarkTemplar)),
      new UnitsAtLeast(2, Protoss.Observer, complete = true)))

  class AddScalingTech extends Parallel(
    new If(
      new And(
        new MiningBasesAtLeast(2),
        new UnitsAtLeast(24, Protoss.Dragoon),
        new UnitsAtLeast(5, Protoss.Gateway, complete = true)),
      new Build(Get(Protoss.CitadelOfAdun), Get(Protoss.ZealotSpeed))),
    new If(new UnitsAtLeast(1, Protoss.HighTemplar), new Build(Get(Protoss.PsionicStorm))),
    new If(
      new And(
        new GasPumpsAtLeast(3),
        new UnitsAtLeast(24, UnitMatchWarriors)),
      new Parallel(
        new Build(Get(Protoss.Forge)),
        new UpgradeContinuously(Protoss.GroundDamage),
        new Build(
          Get(Protoss.CitadelOfAdun),
          Get(Protoss.TemplarArchives),
          Get(Protoss.ZealotSpeed),
          Get(Protoss.PsionicStorm)))))

  class FinishDarkTemplarRush extends If(
    new And(
      new UnitsAtLeast(1, Protoss.TemplarArchives),
      new UnitsAtMost(0, Protoss.Observer),
      new UnitsAtMost(0, Protoss.Observatory)),
    new BuildOrder(Get(2, Protoss.DarkTemplar)))

  override def buildPlans: Seq[Plan] = Seq(
    new FinishDarkTemplarRush,
    new Build(Get(Protoss.Pylon), Get(Protoss.Gateway), Get(Protoss.Assimilator), Get(Protoss.CyberneticsCore), Get(Protoss.DragoonRange), Get(2, Protoss.Gateway)),

    //  Detection
    new If(buildCannons, new BuildCannonsAtNatural(2)),
    new If(buildCannons, new BuildCannonsAtExpansions(1)),
    new GetReactiveObservers,

    // Expansions
    new If(new And(new MiningBasesAtLeast(2), new UnitsAtLeast(36, UnitMatchWorkers)), new PylonBlock),
    new RequireMiningBases(2),
    new If(new And(new ReadyForThirdIfSafe, new SafeForThird), new RequireBases(3)),
    new Trigger(
      new BasesAtLeast(3),
      new Parallel(
        new PvPIdeas.TakeBase2,
        new PvPIdeas.TakeBase3WithGateways,
        //new PvPIdeas.TakeBase4WithGateways
        )),

    new AddScalingTech,
    new PvPIdeas.TrainArmy,

    // Three-base transition
    new AddEarlyTech,
    new RequireBases(3),

    new FlipIf(
      new SafeAtHome,
      new AddLateGateways,
      new AddLateTech)
  )
}
