package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Army.{DefendZones, EscortSettlers}
import Planning.Plans.Compound.{FlipIf, If, Parallel}
import Planning.Plans.GamePlans.Mode
import Planning.Plans.Information.{Employing, Never}
import Planning.Plans.Macro.Automatic.{MeldArchons, RequireSufficientSupply, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Scouting.ScoutExpansionsAt
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvT3BaseArbiter

class PvT3BaseArbiters extends Mode {
  
  description.set("PvT 3 Base Arbiter")
  
  override val activationCriteria: Plan = new Employing(PvT3BaseArbiter)
  override val completionCriteria: Plan = new Never
  
  children.set(Vector(
    new MeldArchons(40),
    new RequireSufficientSupply,
    new TrainWorkersContinuously(oversaturate = true),
    new BuildCannonsAtExpansions(2),
    new BuildGasPumps,
    new FlipIf(
      new UnitsAtLeast(24, UnitMatchWarriors),
      new Parallel(
        new PvTIdeas.TrainArmy,
        new RequireMiningBases(4)),
      new Parallel(
        new PvTIdeas.TrainObservers,
        new If(new UnitsAtLeast(2, Protoss.HighTemplar),  new Build(RequestTech(Protoss.PsionicStorm))),
        new If(new UnitsAtLeast(2, Protoss.Arbiter),      new Build(RequestTech(Protoss.Stasis))),
        new BuildOrder(
          RequestAtLeast(1, Protoss.Gateway),
          RequestAtLeast(1, Protoss.CyberneticsCore),
          RequestAtLeast(3, Protoss.Gateway),
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.Observatory),
          RequestAtLeast(5, Protoss.Gateway),
          RequestAtLeast(1, Protoss.CitadelOfAdun),
          RequestUpgrade(Protoss.ZealotSpeed),
          RequestAtLeast(8, Protoss.Gateway),
          RequestAtLeast(1, Protoss.TemplarArchives),
          RequestTech(Protoss.PsionicStorm),
          RequestAtLeast(1, Protoss.Stargate),
          RequestUpgrade(Protoss.HighTemplarEnergy),
          RequestAtLeast(1, Protoss.ArbiterTribunal),
          RequestUpgrade(Protoss.ArbiterEnergy),
          RequestAtLeast(2, Protoss.Forge)),
        new UpgradeContinuously(Protoss.GroundDamage),
        new UpgradeContinuously(Protoss.GroundArmor))),
    new RequireMiningBases(4),
    new Build(RequestAtLeast(20, Protoss.Gateway)),
    new RequireMiningBases(5),
    new DefendZones,
    new EscortSettlers,
    new ScoutExpansionsAt(100),
    new PvTIdeas.AttackWithDarkTemplar,
    new PvTIdeas.ContainSafely
  ))
}

