package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.{Attack, EjectScout}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.{EnemyHydralisks, EnemyMutalisks}
import Planning.Predicates.Strategy.Employing
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.PvZMidgameBisu

class PvZBisu extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvZMidgameBisu)
  override val completionCriteria = new Latch(new Or(new MiningBasesAtLeast(3), new TechComplete(Protoss.PsionicStorm)))
  override def defaultArchonPlan: Plan = new PvZIdeas.TemplarUpToEight
  override def defaultAttackPlan: Plan = new Parallel(
    new Attack(Protoss.Corsair),
    new Trigger(
      new UnitsAtLeast(1, Protoss.DarkTemplar),
      new Attack,
      new PvZIdeas.ConditionalAttack))

  override def emergencyPlans: Seq[Plan] = Seq(new PvZIdeas.ReactToLurkers)
  
  override def buildPlans: Seq[Plan] = Vector(
    new EjectScout,
    new Pump(Protoss.DarkTemplar, 3),
    new PvZIdeas.TakeSafeNatural,
    new PvZIdeas.AddEarlyCannons,
    new If(
      new UnitsAtLeast(1, Protoss.Stargate, complete = true),
      new If(
        new EnemyMutalisks,
        new Parallel(
          new PumpMatchingRatio(Protoss.Corsair, 1, 12, Seq(Enemy(Zerg.Mutalisk, 1.0))),
          new UpgradeContinuously(Protoss.AirDamage)),
        new If(
          new EnemyHydralisks,
          new Pump(Protoss.Corsair, 1),
          new Parallel(
            new Pump(Protoss.Corsair, 6),
            new UpgradeContinuously(Protoss.AirDamage))))),
    new If(
      new UnitsAtLeast(1, Protoss.HighTemplar),
      new Build(Get(Protoss.PsionicStorm))),
    new If(
      new UnitsAtLeast(2, Protoss.Dragoon),
      new UpgradeContinuously(Protoss.DragoonRange)),
    new If(
      new UnitsAtLeast(15, UnitMatchWarriors),
      new RequireMiningBases(3)),
    new PumpMatchingRatio(Protoss.Dragoon, 1, 8, Seq(Enemy(Zerg.Mutalisk, 1.0))),
    new Pump(Protoss.HighTemplar),
    new Pump(Protoss.Zealot),
    new Build(
      Get(1, Protoss.Gateway),
      Get(1, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore),
      Get(2, Protoss.Assimilator),
      Get(1, Protoss.Stargate),
      Get(1, Protoss.CitadelOfAdun)),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(
      Get(1, Protoss.TemplarArchives),
      Get(4, Protoss.Gateway),
      Get(Protoss.ZealotSpeed),
      Get(Protoss.PsionicStorm),
      Get(Protoss.DragoonRange),
      Get(8, Protoss.Gateway)),
    new RequireMiningBases(3)
  )
}
