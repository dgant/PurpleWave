package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Army.{ConsiderAttacking, DefendZones, EscortSettlers}
import Planning.Plans.Compound.{And, If, Parallel}
import Planning.Plans.GamePlans.Mode
import Planning.Plans.Information.Reactive.EnemyBio
import Planning.Plans.Information.{Employing, Never}
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, BuildCannonsAtNatural, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{OnMiningBases, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Protoss.GamePlans.Standard.PvT.PvTIdeas.{GetObserversForCloakedWraiths, Require2BaseTech}
import Planning.Plans.Scouting.ScoutExpansionsAt
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvT2BaseCarrier

class PvT2BaseCarriers extends Mode {
  
  description.set("PvT 2 Base Carriers")
  
  override val activationCriteria: Plan = new Employing(PvT2BaseCarrier)
  override val completionCriteria: Plan = new Never
  
  children.set(Vector(
    new Require2BaseTech,
    new RequireSufficientSupply,
    new TrainWorkersContinuously(oversaturate = true),
    new OnMiningBases(3, new BuildCannonsAtNatural(1)),
    new BuildCannonsAtExpansions(3),
    new GetObserversForCloakedWraiths,
    new If(
      new UnitsAtLeast(1, Protoss.Carrier, complete = true),
      new Build(RequestUpgrade(Protoss.CarrierCapacity))),
    new If(
      new And(
        new UnitsAtLeast(8, Protoss.Zealot),
        new UnitsAtLeast(4, Protoss.Carrier)),
      new Build(
        RequestAtLeast(1, Protoss.CitadelOfAdun),
        RequestUpgrade(Protoss.ZealotSpeed))),
    new If(
      new UnitsAtLeast(1, Protoss.FleetBeacon),
      new Parallel(
        new TrainContinuously(Protoss.Carrier),
        new TrainContinuously(Protoss.Zealot)),
      new PvTIdeas.TrainZealotsOrDragoons),
    new BuildOrder(
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.Stargate)),
    new If(
      new UnitsAtLeast(4, Protoss.Carrier),
      new If(
        new EnemyBio,
        new UpgradeContinuously(Protoss.AirArmor),
        new UpgradeContinuously(Protoss.AirDamage))),
    new BuildOrder(
      RequestAtLeast(1, Protoss.FleetBeacon),
      RequestAtLeast(2, Protoss.Stargate),
      RequestAtLeast(1, Protoss.Forge),
      RequestAtLeast(5, Protoss.Gateway)),
    new RequireMiningBases(3),
    new UpgradeContinuously(Protoss.AirDamage),
    new UpgradeContinuously(Protoss.AirArmor),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(
      RequestAtLeast(1,   Protoss.TemplarArchives),
      RequestAtLeast(12,  Protoss.Gateway),
      RequestAtLeast(4,   Protoss.Stargate)),
    new RequireMiningBases(4),
    new DefendZones,
    new EscortSettlers,
    new ScoutExpansionsAt(80),
    new ConsiderAttacking
  ))
}

