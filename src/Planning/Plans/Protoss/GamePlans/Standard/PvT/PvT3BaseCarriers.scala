package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Army.{DefendZones, EscortSettlers}
import Planning.Plans.Compound.{FlipIf, If, Parallel}
import Planning.Plans.GamePlans.Mode
import Planning.Plans.Information.{Employing, Never}
import Planning.Plans.Macro.Automatic.{MeldArchons, RequireSufficientSupply, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Milestones.UnitsAtLeast
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Scouting.ScoutExpansionsAt
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvT3BaseCarrier

class PvT3BaseCarriers extends Mode {
  
  description.set("PvT 3 Base Carriers")
  
  override val activationCriteria: Plan = new Employing(PvT3BaseCarrier)
  override val completionCriteria: Plan = new Never
  
  
  children.set(Vector(
    new MeldArchons(40),
    new RequireSufficientSupply,
    new TrainWorkersContinuously(oversaturate = true),
    new BuildCannonsAtExpansions(3),
    new BuildGasPumps,
    new If(new UnitsAtLeast(1, Protoss.Carrier), new Build(RequestUpgrade(Protoss.CarrierCapacity))),
    new FlipIf(
      new UnitsAtLeast(30, UnitMatchWarriors),
      new PvTIdeas.TrainArmy,
      new Parallel(
        new PvTIdeas.TrainObservers,
        new If(new UnitsAtLeast(2, Protoss.HighTemplar), new Build(RequestTech(Protoss.PsionicStorm))),
        new Build(
          RequestAtLeast(1, Protoss.Gateway),
          RequestAtLeast(1, Protoss.CyberneticsCore),
          RequestAtLeast(3, Protoss.Gateway),
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.Observatory),
          RequestAtLeast(5, Protoss.Gateway),
          RequestAtLeast(1, Protoss.CitadelOfAdun),
          RequestUpgrade(Protoss.ZealotSpeed),
          RequestAtLeast(6, Protoss.Gateway),
          RequestAtLeast(1, Protoss.Forge),
          RequestAtLeast(1, Protoss.TemplarArchives),
          RequestTech(Protoss.PsionicStorm),
          RequestAtLeast(1, Protoss.Stargate),
          RequestAtLeast(2, Protoss.CyberneticsCore),
          RequestAtLeast(1, Protoss.FleetBeacon)),
        new UpgradeContinuously(Protoss.AirDamage),
        new UpgradeContinuously(Protoss.AirArmor),
        new Build(RequestAtLeast(3, Protoss.Stargate)))),
    new RequireMiningBases(4),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(RequestAtLeast(20, Protoss.Gateway)),
    new RequireMiningBases(5),
    new UpgradeContinuously(Protoss.GroundArmor),
    new DefendZones,
    new EscortSettlers,
    new ScoutExpansionsAt(100),
    new PvTIdeas.AttackWithDarkTemplar,
    new PvTIdeas.ContainSafely
  ))
}

