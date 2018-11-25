package Planning.Plans.GamePlans.Protoss.Standard.PvZ

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic.{Enemy, Pump, PumpMatchingRatio, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.{BuildCannonsAtExpansions, BuildCannonsAtNatural}
import Planning.Predicates.Compound.Latch
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.Employing
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.Races.{Protoss, Zerg}
import Strategery.Strategies.Protoss.PvZMidgame5GateGoon

class PvZ5GateGoon extends GameplanModeTemplate {

  override val activationCriteria = new Employing(PvZMidgame5GateGoon)
  override val completionCriteria = new Latch(new UnitsAtLeast(1, Protoss.TemplarArchives))
  override def defaultAttackPlan: Plan = new Trigger(
    new UpgradeComplete(Protoss.DragoonRange),
    new Attack,
    new PvZIdeas.ConditionalAttack)

      
  class LateTech extends Parallel(
    new Build(Get(Protoss.PsionicStorm)),
    new UpgradeContinuously(Protoss.GroundArmor),
    new UpgradeContinuously(Protoss.ObserverSpeed))
  
  override def emergencyPlans: Seq[Plan] = Seq(new PvZIdeas.ReactToLurkers)
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(15,  UnitMatchWarriors),
      new RequireMiningBases(3)),
    new PvZIdeas.TakeSafeNatural,
    new PvZIdeas.AddEarlyCannons,
    new UpgradeContinuously(Protoss.DragoonRange),
    new PumpMatchingRatio(Protoss.Dragoon, 0, 12, Seq(Enemy(Zerg.Mutalisk, 1.0))),
    new PumpMatchingRatio(Protoss.Zealot, 2, 12, Seq(Enemy(Zerg.Zergling, 0.25))),
    new Pump(Protoss.Dragoon),
    new Build(
      Get(Protoss.Gateway),
      Get(Protoss.Forge),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(5, Protoss.Gateway)),
    new BuildGasPumps,
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(
      Get(Protoss.CitadelOfAdun),
      Get(Protoss.TemplarArchives)),
    new BuildCannonsAtExpansions(5),
    new BuildCannonsAtNatural(2),
    new RequireMiningBases(3),
    new Build(Get(Protoss.Gateway, 8))
  )
}
