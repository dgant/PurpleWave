package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.{Attack, EjectScout}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyMutalisks
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.PvZMidgameNeoNeoBisu

class PvZNeoNeoBisu extends GameplanTemplate {

  override val activationCriteria = new Employing(PvZMidgameNeoNeoBisu)
  override val completionCriteria = new Latch(new BasesAtLeast(3))
  override def attackPlan: Plan = new Parallel(
    new Attack(Protoss.Corsair),
    new PvZIdeas.ConditionalAttack)

  override def emergencyPlans: Seq[Plan] = Seq(new PvZIdeas.ReactToLurkers)

  override def buildPlans: Seq[Plan] = Vector(
    new EjectScout,
    new PvZIdeas.TakeSafeNatural,
    new PvZIdeas.AddEarlyCannons,
    new PumpRatio(Protoss.Corsair, 1, 12, Seq(Enemy(Zerg.Mutalisk, 1.0))),
    new If(new EnemyMutalisks, new UpgradeContinuously(Protoss.AirDamage)),
    new If(new UnitsAtLeast(2, Protoss.Dragoon), new UpgradeContinuously(Protoss.DragoonRange)),
    new If(new UnitsAtLeast(1, Protoss.Arbiter, complete = true), new RequireMiningBases(3)),
    new PumpRatio(Protoss.Dragoon, 1, 8, Seq(Enemy(Zerg.Mutalisk, 1.0), Friendly(Protoss.Corsair, -1.0))),
    new UpgradeContinuously(Protoss.ArbiterEnergy),
    new Pump(Protoss.Arbiter),
    new Pump(Protoss.Zealot),
    new If(new UnitsAtLeast(1, Protoss.HighTemplar), new Build(Get(Protoss.PsionicStorm))),
    new Build(
      Get(Protoss.Gateway),
      Get(Protoss.Forge),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.Stargate),
      Get(2, Protoss.Assimilator),
      Get(Protoss.CitadelOfAdun)),
    new UpgradeContinuously(Protoss.GroundDamage),
    new UpgradeContinuously(Protoss.ZealotSpeed),
    new PumpRatio(Protoss.Stargate, 0, 2, Seq(Enemy(Zerg.Mutalisk, 1 / 5.0))),
    new Build(
      Get(Protoss.TemplarArchives),
      Get(Protoss.ArbiterTribunal),
      Get(6, Protoss.Gateway)),
    new Pump(Protoss.HighTemplar),
    new RequireMiningBases(3)
  )
}
