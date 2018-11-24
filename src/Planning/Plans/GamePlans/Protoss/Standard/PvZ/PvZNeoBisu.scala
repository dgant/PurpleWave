package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.{Attack, EjectScout}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Protoss.MeldArchons
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyMutalisks
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.PvZMidgameNeoBisu

class PvZNeoBisu extends GameplanModeTemplate {

  override val activationCriteria = new Employing(PvZMidgameNeoBisu)
  override val completionCriteria = new Latch(new MiningBasesAtLeast(3))
  override def defaultArchonPlan: Plan = new MeldArchons(49)
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
    new PumpMatchingRatio(Protoss.Corsair, 1, 12, Seq(Enemy(Zerg.Mutalisk, 1.0))),
    new If(
      new EnemyMutalisks,
      new UpgradeContinuously(Protoss.AirDamage)),
    new If(
      new UnitsAtLeast(1, Protoss.HighTemplar),
      new Build(Get(Protoss.PsionicStorm))),
    new If(
      new UnitsAtLeast(2, Protoss.Dragoon),
      new UpgradeContinuously(Protoss.DragoonRange)),
    new PumpMatchingRatio(Protoss.Dragoon, 1, 8, Seq(Enemy(Zerg.Mutalisk, 1.0))),
    new Pump(Protoss.HighTemplar),
    new Pump(Protoss.Zealot),
    new Build(
      Get(1, Protoss.Gateway),
      Get(1, Protoss.Assimilator),
      Get(1, Protoss.CyberneticsCore),
      Get(1, Protoss.Stargate),
      Get(2, Protoss.Assimilator),
      Get(1, Protoss.CitadelOfAdun)),
    new Pump(Protoss.Corsair),
    new UpgradeContinuously(Protoss.AirDamage),
    new UpgradeContinuously(Protoss.GroundDamage),
    new UpgradeContinuously(Protoss.ZealotSpeed),
    new Build(Get(Protoss.TemplarArchives)),
    new UpgradeContinuously(Protoss.DragoonRange),
    new Build(
      Get(Protoss.PsionicStorm),
      Get(4, Protoss.Gateway),
      Get(Protoss.RoboticsFacility),
      Get(Protoss.Observatory)),
    new Build(Get(6, Protoss.Gateway)),
    new RequireMiningBases(3)
  )
}
