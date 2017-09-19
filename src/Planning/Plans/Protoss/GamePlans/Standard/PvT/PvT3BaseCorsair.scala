package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Lifecycle.With
import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Army.{DefendZones, EscortSettlers}
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.Mode
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{MiningBasesAtLeast, UnitsAtLeast, UpgradeComplete}
import Planning.Plans.Scouting.ScoutExpansionsAt
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvT3BaseCorsair

class PvT3BaseCorsair extends Mode {
  
  description.set("PvT 3 Base Corsair")
  
  override val activationCriteria: Plan = new Employing(PvT3BaseCorsair)
  override val completionCriteria: Plan = new MiningBasesAtLeast(4)
  
  class TrainArmy extends Parallel(
    new PvTIdeas.TrainObservers,
    new FlipIf(
      new And(
        new UnitsAtLeast(18, Protoss.Dragoon),
        new Check(() => With.units.ours.count(_.is(Protoss.Dragoon)) > 4 * With.units.ours.count(_.is(Protoss.Corsair)))),
      new TrainContinuously(Protoss.Dragoon),
      new If(
        new UpgradeComplete(Protoss.CorsairEnergy, withinFrames = Protoss.Corsair.buildFrames),
        new TrainContinuously(Protoss.Corsair, 8))))
  
  class Build2BaseTech extends Parallel(
    new Build(
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(8, Protoss.Gateway)))
  
  class Build3BaseTech extends Parallel(
    new Build(
      RequestAtLeast(1, Protoss.Stargate),
      RequestAtLeast(1, Protoss.FleetBeacon),
      RequestUpgrade(Protoss.CorsairEnergy),
      RequestAtLeast(10, Protoss.Gateway),
      RequestAtLeast(2, Protoss.Stargate),
      RequestTech(Protoss.DisruptionWeb)))
  
  children.set(Vector(
    new PvTIdeas.Require2BaseTech,
    new RequireSufficientSupply,
    new TrainWorkersContinuously(oversaturate = true),
    new BuildCannonsAtExpansions(2),
    new If(new UnitsAtLeast(35, UnitMatchWarriors), new RequireMiningBases(4)),
    new FlipIf(
      new UnitsAtLeast(12, UnitMatchWarriors),
      new TrainArmy,
      new Parallel(
        new Build2BaseTech,
        new RequireMiningBases(3),
        new Build3BaseTech)),
    new DefendZones,
    new EscortSettlers,
    new ScoutExpansionsAt(80),
    new PvTIdeas.ContainSafely
  ))
}

