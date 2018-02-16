package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.Employing
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, BuildCannonsAtNatural, RequireMiningBases}
import Planning.Plans.Predicates.Milestones.{MiningBasesAtLeast, IfOnMiningBases, UnitsAtLeast, UpgradeComplete}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvT3BaseCorsair

class PvT3BaseCorsairs extends GameplanModeTemplate {
  
  override val activationCriteria = new Employing(PvT3BaseCorsair)
  override val completionCriteria = new MiningBasesAtLeast(4)
  override val scoutExpansionsAt  = 60
  override val emergencyPlans     = Vector(new PvTIdeas.Require2BaseTech)
  override val defaultAttackPlan  = new PvTIdeas.AttackRespectingMines
  
  class TrainArmy extends Parallel(
    new PvTIdeas.TrainObservers,
    new FlipIf(
      new And(
        new UnitsAtLeast(15, Protoss.Dragoon),
        new Check(() => With.units.ours.count(_.is(Protoss.Dragoon)) > 4 * With.units.ours.count(_.is(Protoss.Corsair)))),
      new TrainContinuously(Protoss.Dragoon),
      new If(
        new UpgradeComplete(Protoss.CorsairEnergy, withinFrames = Protoss.Corsair.buildFrames),
        new TrainContinuously(Protoss.Corsair, 8))),
    new Build2BaseTech,
    new Build(RequestAtLeast(8, Protoss.Gateway)))
  
  class Build2BaseTech extends Parallel(
    new Build(
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(5, Protoss.Gateway)))
  
  class Build3BaseTech extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(8, Protoss.Gateway),
      RequestAtLeast(1, Protoss.FleetBeacon),
      RequestUpgrade(Protoss.CorsairEnergy)),
    new If(new UnitsAtLeast(1, Protoss.FleetBeacon, complete = true), new Build(RequestAtLeast(2, Protoss.Stargate))),
    new If(new UnitsAtLeast(2, Protoss.Corsair), new Build(RequestTech(Protoss.DisruptionWeb))),
    new Build(RequestAtLeast(12, Protoss.Gateway)))
  
  override val buildPlans = Vector(
    new PvTIdeas.Require2BaseTech,
    new IfOnMiningBases(3, new BuildCannonsAtNatural(1)),
    new BuildCannonsAtExpansions(2),
    new If(new UnitsAtLeast(35, UnitMatchWarriors), new RequireMiningBases(4)),
    new FlipIf(
      new UnitsAtLeast(12, UnitMatchWarriors),
      new PvTIdeas.TrainArmy,
      new Parallel(
        new Build2BaseTech,
        new RequireMiningBases(3),
        new Build3BaseTech)))
}

