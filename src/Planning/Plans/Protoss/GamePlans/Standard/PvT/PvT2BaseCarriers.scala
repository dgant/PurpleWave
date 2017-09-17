package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plan
import Planning.Plans.Army.{ConsiderAttacking, DefendZones, EscortSettlers}
import Planning.Plans.Compound.{FlipIf, If}
import Planning.Plans.GamePlans.Mode
import Planning.Plans.Information.Reactive.EnemyBio
import Planning.Plans.Information.{Employing, Never}
import Planning.Plans.Macro.Automatic.{RequireSufficientSupply, TrainContinuously, TrainWorkersContinuously}
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, RequireMiningBases}
import Planning.Plans.Macro.Milestones.UnitsAtLeast
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
    new TrainWorkersContinuously,
    new BuildCannonsAtExpansions(2),
    new GetObserversForCloakedWraiths,
    new If(
      new UnitsAtLeast(1, Protoss.Carrier, complete = true),
      new Build(RequestUpgrade(Protoss.CarrierCapacity))),
    new If(
      new UnitsAtLeast(12, Protoss.Zealot, complete = true),
      new Build(
        RequestAtLeast(1, Protoss.CitadelOfAdun),
        RequestUpgrade(Protoss.ZealotSpeed))),
    new FlipIf(
      new UnitsAtLeast(8, Protoss.Dragoon),
      new PvTIdeas.TrainZealotsOrDragoons,
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
      RequestAtLeast(1, Protoss.Forge),
      RequestAtLeast(8, Protoss.Gateway)),
    new RequireMiningBases(3),
    new UpgradeContinuously(Protoss.AirDamage),
    new UpgradeContinuously(Protoss.AirArmor),
    new UpgradeContinuously(Protoss.GroundDamage),
    new RequireMiningBases(4),
    new Build(
      RequestAtLeast(12, Protoss.Gateway),
      RequestAtLeast(4, Protoss.Stargate)),
    new DefendZones,
    new EscortSettlers,
    new ScoutExpansionsAt(100),
    new ConsiderAttacking
  ))
}

