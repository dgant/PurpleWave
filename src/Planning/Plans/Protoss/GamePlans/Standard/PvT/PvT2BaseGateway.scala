package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Army.{DefendZones, EscortSettlers}
import Planning.Plans.Compound.If
import Planning.Plans.GamePlans.Mode
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.{MeldArchons, RequireSufficientSupply, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Scouting.ScoutExpansionsAt
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvT2BaseGateway

class PvT2BaseGateway extends Mode {
  
  description.set("PvT 2 Base Gateway")
  
  override val activationCriteria: Plan = new Employing(PvT2BaseGateway)
  override val completionCriteria: Plan = new MiningBasesAtLeast(3)
  
  children.set(Vector(
    new MeldArchons(40),
    new PvTIdeas.Require2BaseTech,
    new RequireSufficientSupply,
    new TrainWorkersContinuously(oversaturate = true),
    new BuildCannonsAtExpansions(2),
    new If(new UnitsAtLeast(30, UnitMatchWarriors), new RequireMiningBases(3)),
    new PvTIdeas.TrainArmy,
    new If(new UnitsAtLeast(1, Protoss.HighTemplar), new Build(RequestTech(Protoss.PsionicStorm))),
    new Build(
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(3, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Observatory),
      RequestAtLeast(4, Protoss.Gateway),
      RequestAtLeast(1, Protoss.CitadelOfAdun),
      RequestAtLeast(1, Protoss.TemplarArchives),
      RequestUpgrade(Protoss.ZealotSpeed),
      RequestAtLeast(6, Protoss.Gateway),
      RequestTech(Protoss.PsionicStorm),
      RequestUpgrade(Protoss.HighTemplarEnergy),
      RequestAtLeast(1, Protoss.Forge)),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(RequestAtLeast(8, Protoss.Gateway)),
    new RequireMiningBases(3),
    new DefendZones,
    new EscortSettlers,
    new ScoutExpansionsAt(100),
    new PvTIdeas.AttackWithDarkTemplar,
    new PvTIdeas.ContainSafely
  ))
}

