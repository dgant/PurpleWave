package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Army.{ConsiderAttacking, DefendZones}
import Planning.Plans.Compound.{FlipIf, If, Parallel}
import Planning.Plans.GamePlans.Mode
import Planning.Plans.Information.Reactive.EnemyBio
import Planning.Plans.Information.{Employing, Never}
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{EnemyHasTech, UnitsAtLeast, UnitsAtMost}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.GamePlans.Standard.PvT.PvTIdeas.Require2BaseTech
import Planning.Plans.Scouting.ScoutExpansionsAt
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvT.PvT2BaseCarrier

class PvT2BaseCarriers extends Mode {
  
  description.set("PvT 2 Base Carriers")
  
  override val activationCriteria: Plan = new Employing(PvT2BaseCarrier)
  override val completionCriteria: Plan = new Never
  
  children.set(Vector(
    new Require2BaseTech,
    new RequireSufficientSupply,
    new TrainWorkersContinuously,
    new BuildCannonsAtExpansions(2),
    new If(
      new EnemyHasTech(Terran.WraithCloak),
      new Parallel(
        new Build(
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.Observatory)),
        new PvTIdeas.TrainObservers)),
    new If(
      new UnitsAtLeast(1, Protoss.Carrier, complete = true),
      new Build(RequestUpgrade(Protoss.CarrierCapacity))),
    new If(
      new UnitsAtLeast(12, Protoss.Zealot, complete = true),
      new Build(
        RequestAtLeast(1, Protoss.CitadelOfAdun),
        RequestUpgrade(Protoss.ZealotSpeed))),
    new FlipIf(
      new UnitsAtLeast(4, Protoss.Dragoon),
      new If(
        new UnitsAtMost(15, Protoss.Dragoon),
        new TrainContinuously(Protoss.Dragoon, 15),
        new TrainContinuously(Protoss.Zealot)),
      new TrainContinuously(Protoss.Carrier)),
    new BuildOrder(
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Stargate)),
    new If(
      new EnemyBio,
      new UpgradeContinuously(Protoss.AirArmor),
      new UpgradeContinuously(Protoss.AirDamage)),
    new Build(
      RequestAtLeast(1, Protoss.FleetBeacon),
      RequestAtLeast(2, Protoss.Stargate),
      RequestAtLeast(8, Protoss.Gateway)),
    new RequireMiningBases(3),
    new Build(RequestAtLeast(4, Protoss.Stargate)),
    new RequireMiningBases(4),
    new UpgradeContinuously(Protoss.AirDamage),
    new UpgradeContinuously(Protoss.AirArmor),
    new UpgradeContinuously(Protoss.GroundDamage),
    new DefendZones,
    new ScoutExpansionsAt(100),
    new ConsiderAttacking
  ))
}

