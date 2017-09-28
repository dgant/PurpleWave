package Planning.Plans.Protoss.GamePlans.Standard.PvT

import Macro.BuildRequests.{RequestAtLeast, RequestUpgrade}
import Planning.Plans.Compound.{And, If, Parallel}
import Planning.Plans.GamePlans.TemplateMode
import Planning.Plans.Information.Employing
import Planning.Plans.Information.Reactive.EnemyBio
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.{BuildCannonsAtExpansions, BuildCannonsAtNatural, RequireMiningBases}
import Planning.Plans.Macro.Milestones.{OnMiningBases, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvT.PvT2BaseCarrier

class PvT2BaseCarriers extends TemplateMode {
  
  override val activationCriteria   = new Employing(PvT2BaseCarrier)
  override val emergencyPlans       = Vector(new PvTIdeas.Require2BaseTech, new PvTIdeas.GetObserversForCloakedWraiths)
  
  override val buildPlans = Vector(
    new OnMiningBases(3, new BuildCannonsAtNatural(1)),
    new BuildCannonsAtExpansions(3),
    new If(new UnitsAtLeast(1, Protoss.Carrier, complete = true), new Build(RequestUpgrade(Protoss.CarrierCapacity))),
    new If(new UnitsAtLeast(4, Protoss.Carrier, complete = true), new RequireMiningBases(3)),
    new If(
      new And(
        new UnitsAtLeast(12, Protoss.Zealot),
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
    new UpgradeContinuously(Protoss.AirDamage),
    new UpgradeContinuously(Protoss.AirArmor),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Build(
      RequestAtLeast(1,   Protoss.TemplarArchives),
      RequestAtLeast(12,  Protoss.Gateway),
      RequestAtLeast(4,   Protoss.Stargate)))
}

