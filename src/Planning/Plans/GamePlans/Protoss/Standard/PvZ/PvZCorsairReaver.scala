package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.{Attack, EjectScout}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Compound.{And, Latch}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyMutalisks
import Planning.Predicates.Strategy.Employing
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.{PvZLateGameCarrier, PvZMidgameCorsairReaverGoon, PvZMidgameCorsairReaverZealot}

class PvZCorsairReaver extends GameplanTemplate {

  override val activationCriteria = new Employing(PvZMidgameCorsairReaverZealot, PvZMidgameCorsairReaverGoon)
  override val completionCriteria = new Latch(new BasesAtLeast(3))
  override def archonPlan: Plan = new PvZIdeas.TemplarUpToEight
  override def attackPlan: Plan = new Attack(Protoss.Corsair)

  override def emergencyPlans: Seq[Plan] = Seq(new PvZIdeas.ReactToLurkers)

  override def buildPlans: Seq[Plan] = Vector(
    new EjectScout,
    new PvZIdeas.TakeSafeNatural,
    new PvZIdeas.AddEarlyCannons,
    // TODO: Skip reavers when doing 2-Base Goon Reaver
    new PumpRatio(Protoss.Corsair, 1, 12, Seq(Enemy(Zerg.Mutalisk, 1.0))),
    new PumpRatio(Protoss.Dragoon, 1, 8, Seq(Enemy(Zerg.Mutalisk, 1.0), Friendly(Protoss.Corsair, -1.0))),
    new PumpRatio(Protoss.Stargate, 0, 2, Seq(Enemy(Zerg.Mutalisk, 1/5.0))),
    new Trigger(
      new Or(
        new UnitsAtLeast(16, Protoss.Dragoon),
        new And(
          new UnitsAtLeast(30, UnitMatchWarriors),
          new UnitsAtLeast(2, Protoss.Reaver, complete = true),
          new UnitsAtLeast(1, Protoss.Shuttle, complete = true))),
      new Parallel(
        new Attack,
        new RequireMiningBases(3))),
    new If(
      new Employing(PvZLateGameCarrier),
      new PumpShuttleAndReavers(shuttleFirst = false),
      new PumpShuttleAndReavers),
    new If(
      new Or(
        new Employing(PvZMidgameCorsairReaverGoon),
        new UpgradeStarted(Protoss.ShuttleSpeed)),
      new Parallel(
        new UpgradeContinuously(Protoss.DragoonRange),
        new Pump(Protoss.Dragoon)),
      new Pump(Protoss.Zealot)),
    new BuildOrder(
      Get(Protoss.Gateway),
      Get(Protoss.Forge),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(2, Protoss.Assimilator),
      Get(Protoss.Stargate),
      Get(Protoss.RoboticsFacility)),
    new If(
      new Or(
        new EnemyMutalisks,
        new Employing(PvZLateGameCarrier)),
      new UpgradeContinuously(Protoss.AirDamage),
      new BuildOrder(Get(Protoss.Shuttle))),
    new BuildOrder(
      Get(Protoss.Corsair),
      Get(Protoss.RoboticsSupportBay),
      Get(Protoss.ShuttleSpeed)),
    new UpgradeContinuously(Protoss.GroundDamage),
    new If(
      new Employing(PvZMidgameCorsairReaverZealot),
      new Build(
        Get(Protoss.CitadelOfAdun),
        Get(Protoss.ZealotSpeed))),
    new Build(
      Get(6, Protoss.Gateway)),
  )
}
