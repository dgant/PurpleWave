package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.{Attack, EjectScout}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Standard.PvZ.PvZIdeas.PvZRequireMiningBases
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.EnemyMutalisks
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.PvZMidgameNeoBisu

class PvZNeoBisu extends GameplanTemplate {

  override val activationCriteria = new Employing(PvZMidgameNeoBisu)
  override val completionCriteria = new Latch(new BasesAtLeast(3))
  override def archonPlan: Plan = new PvZIdeas.TemplarUpToEight
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
    new If(new UnitsAtLeast(1, Protoss.HighTemplar), new Build(Get(Protoss.PsionicStorm))),
    new If(new UnitsAtLeast(2, Protoss.Dragoon), new UpgradeContinuously(Protoss.DragoonRange)),
    new If(new TechComplete(Protoss.PsionicStorm), new PvZRequireMiningBases(3)),
    new PumpRatio(Protoss.Dragoon, 1, 8, Seq(Enemy(Zerg.Mutalisk, 1.0), Friendly(Protoss.Corsair, -1.0))),
    new Pump(Protoss.HighTemplar),
    new Pump(Protoss.Zealot),
    new Build(
      Get(Protoss.Gateway),
      Get(Protoss.Forge),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.Stargate),
      Get(2, Protoss.Assimilator),
      Get(Protoss.CitadelOfAdun)),
    new Pump(Protoss.Corsair, 6),
    new UpgradeContinuously(Protoss.AirDamage),
    new UpgradeContinuously(Protoss.GroundDamage),
    new UpgradeContinuously(Protoss.ZealotSpeed),
    new PumpRatio(Protoss.Stargate, 0, 2, Seq(Enemy(Zerg.Mutalisk, 1/5.0))),
    new Build(
      Get(Protoss.TemplarArchives),
      Get(Protoss.PsionicStorm),
      Get(6, Protoss.Gateway)),
    new PvZRequireMiningBases(3)
  )
}
