package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Army.DefendZones
import Planning.Plans.Compound.{FlipIf, If, Parallel}
import Planning.Plans.GamePlans.Mode
import Planning.Plans.Information.Employing
import Planning.Plans.Macro.Automatic.{MeldArchons, RequireSufficientSupply, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{OnGasBases, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.GamePlans.Standard.PvT.PvTIdeas.Require2BaseTech
import Planning.Plans.Scouting.ScoutExpansionsAt
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvT2BaseArbiter

class PvT2BaseArbiters extends Mode {
  
  description.set("PvT 2 Base Arbiter")
  
  override val activationCriteria: Plan = new Employing(PvT2BaseArbiter)
  override val completionCriteria: Plan = new UnitsAtLeast(2, Protoss.Nexus, complete = true)
  
  children.set(Vector(
    new MeldArchons(40),
    new Require2BaseTech,
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new FlipIf(
      new UnitsAtLeast(20, UnitMatchWarriors),
      new PvTIdeas.TrainArmy,
      new Parallel(
        new BuildOrder(
          RequestAtLeast(2, Protoss.Gateway),
          RequestAtLeast(1, Protoss.CitadelOfAdun),
          RequestAtLeast(1, Protoss.TemplarArchives),
          RequestAtLeast(1, Protoss.Stargate),
          RequestAtLeast(4, Protoss.Gateway),
          RequestAtLeast(1, Protoss.ArbiterTribunal),
          RequestUpgrade(Protoss.ArbiterEnergy),
          RequestAtLeast(1, Protoss.Arbiter),
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.Observatory)
        ),
        new RequireMiningBases(3),
        new If(new UnitsAtLeast(2, Protoss.Arbiter), new Build(RequestTech(Protoss.Stasis))),
        new If(new UnitsAtLeast(2, Protoss.HighTemplar), new Build(RequestTech(Protoss.PsionicStorm))),
        new BuildOrder(RequestUpgrade(Protoss.ZealotSpeed)),
        new Build(RequestAtLeast(1, Protoss.Forge)),
        new UpgradeContinuously(Protoss.GroundDamage),
        new BuildCannonsAtExpansions(2),
        new Build(RequestAtLeast(10, Protoss.Gateway)),
        new OnGasBases(3, new Build(RequestAtLeast(2, Protoss.Stargate))),
        new RequireMiningBases(4),
        new Build(RequestAtLeast(20, Protoss.Gateway))
      )),
    new DefendZones,
    new ScoutExpansionsAt(100),
    new PvTIdeas.ContainSafely
  ))
}

