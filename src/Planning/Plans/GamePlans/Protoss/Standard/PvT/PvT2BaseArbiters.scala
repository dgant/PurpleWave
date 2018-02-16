package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, BuildCannonsAtNatural, RequireMiningBases}
import Planning.Plans.Predicates.Milestones.{OnGasPumps, IfOnMiningBases, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvT2BaseArbiter

class PvT2BaseArbiters extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvT2BaseArbiter)
  override val scoutExpansionsAt  = 60
  override val emergencyPlans     = Vector(new PvTIdeas.Require2BaseTech)
  override val priorityAttackPlan = new PvTIdeas.PriorityAttacks
  override val defaultAttackPlan  = new PvTIdeas.AttackRespectingMines
  
  override val buildPlans = Vector(
    new IfOnMiningBases(3, new BuildCannonsAtNatural(1)),
    new BuildCannonsAtExpansions(2),
    new If(
      new Or(
        new And(
          new UnitsAtLeast(6, UnitMatchWarriors),
          new SafeAtHome),
        new UnitsAtLeast(20, UnitMatchWarriors)),
      new RequireMiningBases(3)),
    new If(
      new Or(
        new And(
          new UnitsAtLeast(15, UnitMatchWarriors),
          new SafeAtHome),
        new UnitsAtLeast(40, UnitMatchWarriors)),
      new RequireMiningBases(4)),
    new FlipIf(
      new UnitsAtLeast(10, UnitMatchWarriors),
      new PvTIdeas.TrainArmy,
      new Parallel(
        new PvTIdeas.GetObserversForCloakedWraiths,
        new If(new UnitsAtLeast(2, Protoss.Arbiter), new Build(RequestTech(Protoss.Stasis))),
        new BuildOrder(
          RequestAtLeast(2, Protoss.Gateway),
          RequestAtLeast(1, Protoss.CitadelOfAdun),
          RequestAtLeast(1, Protoss.TemplarArchives),
          RequestAtLeast(1, Protoss.Stargate),
          RequestUpgrade(Protoss.ZealotSpeed),
          RequestAtLeast(4, Protoss.Gateway),
          RequestAtLeast(1, Protoss.ArbiterTribunal),
          RequestAtLeast(1, Protoss.Arbiter),
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.Observatory),
          RequestAtLeast(8, Protoss.Gateway)),
        new If(new UnitsAtLeast(2, Protoss.HighTemplar), new Build(RequestTech(Protoss.PsionicStorm))),
        new Build(RequestAtLeast(1, Protoss.Forge)),
        new UpgradeContinuously(Protoss.GroundDamage),
        new Build(RequestAtLeast(10, Protoss.Gateway)),
        new OnGasPumps(4, new Build(
          RequestUpgrade(Protoss.ArbiterEnergy),
          RequestAtLeast(2, Protoss.Stargate))))),
    new OnGasPumps(3, new UpgradeContinuously(Protoss.GroundArmor)),
    new IfOnMiningBases(2, new Build(RequestAtLeast(11, Protoss.Gateway))),
    new IfOnMiningBases(3, new Build(RequestAtLeast(16, Protoss.Gateway))),
    new IfOnMiningBases(4, new Build(RequestAtLeast(20, Protoss.Gateway)))
  )
}

