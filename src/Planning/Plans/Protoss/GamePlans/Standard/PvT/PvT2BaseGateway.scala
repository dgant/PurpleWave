package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestTech}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Army.{DefendZones, EscortSettlers}
import Planning.Plans.Compound.{FlipIf, If, Parallel}
import Planning.Plans.GamePlans.Mode
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.Scouting.ScoutExpansionsAt
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvT2BaseGateway

class PvT2BaseGateway extends Mode {
  
  description.set("PvT 2 Base Gateway")
  
  override val activationCriteria: Plan = new Employing(PvT2BaseGateway)
  override val completionCriteria: Plan = new MiningBasesAtLeast(3)
  
  children.set(Vector(
    new PvTIdeas.Require2BaseTech,
    new RequireSufficientSupply,
    new TrainWorkersContinuously(oversaturate = true),
    new BuildCannonsAtExpansions(2),
    new If(new UnitsAtLeast(24, UnitMatchWarriors), new RequireMiningBases(3)),
    new FlipIf(
      new UnitsAtLeast(15, UnitMatchWarriors),
      new Parallel(
        new PvTIdeas.TrainArmy,
        new Build(
          RequestAtLeast(3, Protoss.Gateway),
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.Observatory),
          RequestAtLeast(5, Protoss.Gateway))),
      new PvTIdeas.Require3BaseTech),
    new RequireMiningBases(3),
    new Build(
      RequestTech(Protoss.PsionicStorm),
      RequestAtLeast(10, Protoss.Gateway)),
    new DefendZones,
    new EscortSettlers,
    new ScoutExpansionsAt(80),
    new PvTIdeas.AttackWithDarkTemplar,
    new PvTIdeas.ContainSafely
  ))
}

