package Planning.Plans.GamePlans.Protoss.PvZ

import Macro.Requests.Get
import Planning.Plan
import Planning.Plans.Army.AttackAndHarass
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.PvZ.PvZIdeas.PvZRequireMiningBases
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.BuildGasPumps
import Planning.Plans.Placement.{BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Predicates.Compound.{And, Latch, Or}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.Employing
import Utilities.UnitFilters.IsWarrior
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.{PvZMidgame5GateGoon, PvZMidgame5GateGoonReaver}

class PvZ5GateGoon extends GameplanTemplate {

  override val activationCriteria = new Employing(PvZMidgame5GateGoon, PvZMidgame5GateGoonReaver)
  override val completionCriteria = new Latch(
    new Or(
      new UpgradeComplete(Protoss.ZealotSpeed),
      new UnitsAtLeast(3, Protoss.Reaver)))
  override def attackPlan: Plan = new Trigger(
    new UpgradeComplete(Protoss.DragoonRange),
    new AttackAndHarass,
    new PvZIdeas.ConditionalAttack)

  override def archonPlan: Plan = new PvZIdeas.MeldArchonsUntilStorm

  class LateTech extends Parallel(
    new Build(Get(Protoss.PsionicStorm)),
    new UpgradeContinuously(Protoss.GroundArmor),
    new UpgradeContinuously(Protoss.ObserverSpeed))
  
  override def emergencyPlans: Seq[Plan] = Seq(new PvZIdeas.ReactToLurkers)

  class NeedCorsairs extends And(
    new EnemiesAtLeast(3, Zerg.Mutalisk),
    new EnemiesAtMost(0, Zerg.Hydralisk))

  override def buildPlans: Seq[Plan] = Vector(
    new CapGasAt(400),
    new PvZIdeas.TakeSafeNatural,
    new PvZIdeas.AddEarlyCannons,
    new UpgradeContinuously(Protoss.DragoonRange),
    new Build(
      Get(Protoss.Gateway),
      Get(Protoss.Forge),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore)),
    new If(
      new UpgradeComplete(Protoss.DragoonRange, 1, Protoss.DragoonRange.upgradeFrames(1) / 2),
      new BuildGasPumps),
    new FlipIf(
      new SafeAtHome,
      new Parallel(
        new If(
          new Or(
            new And(
              new Employing(PvZMidgame5GateGoon),
              new UnitsAtLeast(18, IsWarrior)),
            new And(
              new Employing(PvZMidgame5GateGoonReaver),
              new UnitsAtLeast(4, Protoss.Reaver))),
          new PvZRequireMiningBases(3)),
        new If(
          new NeedCorsairs,
          new PumpRatio(Protoss.Corsair, 0, 12, Seq(Enemy(Zerg.Mutalisk, 0.6)))),
        new PumpShuttleAndReavers(4),
        new PumpRatio(Protoss.Dragoon, 0, 12, Seq(Enemy(Zerg.Mutalisk, 1.0))),
        new PumpRatio(Protoss.Zealot, 2, 12, Seq(Enemy(Zerg.Zergling, 0.25))),
        new Pump(Protoss.Dragoon)),
      new Trigger(
        new NeedCorsairs,
        new Parallel(
          new BuildGasPumps,
          new PumpRatio(Protoss.Stargate, 0, 2, Seq(Enemy(Zerg.Mutalisk, 0.2))),
          new UpgradeContinuously(Protoss.AirDamage)))),
    new Build(Get(5, Protoss.Gateway)),
    new BuildCannonsAtNatural(1),
    new If(
      new Employing(PvZMidgame5GateGoon),
      new Parallel(
        new UpgradeContinuously(Protoss.GroundDamage),
        new Build(
          Get(Protoss.CitadelOfAdun),
          Get(Protoss.TemplarArchives))),
      new Parallel(
        new Build(
          Get(Protoss.RoboticsFacility),
          Get(Protoss.Shuttle),
          Get(Protoss.RoboticsSupportBay),
          Get(Protoss.ShuttleSpeed)))),
    new BuildCannonsAtExpansions(5),
    new BuildCannonsAtNatural(2),

    // Transition
    new Trigger(
      new And(
        new UnitsAtLeast(5, Protoss.Gateway, complete = true),
        new Or(
          new Employing(PvZMidgame5GateGoon),
          new Latch(new UnitsAtLeast(1, Protoss.Reaver)))),
      new Parallel(
        new PvZRequireMiningBases(3),
        new Build(
          Get(Protoss.ZealotSpeed),
          Get(Protoss.Gateway, 12)),
        new Pump(Protoss.Zealot)))
  )
}
