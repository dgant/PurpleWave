package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{FlipIf, If, Or, Parallel}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvPIdeas.TrainArmy
import Planning.Plans.Macro.Automatic.{Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Predicates.Compound.And
import Planning.Predicates.Economy.{GasAtLeast, MineralsAtLeast}
import Planning.Predicates.Milestones._
import Planning.Predicates.Reactive.SafeAtHome
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvPLateGame2BaseReaverCarrier_SpecificMaps, PvPLateGame2BaseReaverCarrier_SpecificOpponents}

class PvP2BaseReaverCarrier extends GameplanTemplate {

  override val activationCriteria = new Or(
    new Employing(PvPLateGame2BaseReaverCarrier_SpecificOpponents),
    new Employing(PvPLateGame2BaseReaverCarrier_SpecificMaps))

  override val removeMineralBlocksAt = 80
  override val emergencyPlans: Vector[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies
  )

  override val attackPlan = new If(
    new UnitsAtLeast(24, Protoss.Interceptor),
    new Attack)

  class TrainCarrierArmy extends Parallel(
    new FlipIf(
      new UnitsAtLeast(3, Protoss.Reaver),
      new Pump(Protoss.Reaver, 5),
      new Pump(Protoss.Carrier)),
    new If(
      new And(
        new UnitsAtLeast(2, Protoss.Stargate, complete = true),
        new GasAtLeast(200)),
      new PvPIdeas.PumpDragoonsAndZealots,
      new Pump(Protoss.Zealot)))

  override def buildPlans: Seq[Plan] = Vector(
    new RequireMiningBases(2),
    new If(
      new UnitsAtLeast(2, Protoss.Carrier),
      new UpgradeContinuously(Protoss.CarrierCapacity)),
    new Build(
      Get(2, Protoss.Gateway),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore)),
    new BuildGasPumps,
    new PvPIdeas.GetObserversIfDarkTemplarPossible,
    new FlipIf(
      new Or(
        new UnitsAtLeast(2, Protoss.Reaver, complete = true),
        new MineralsAtLeast(500)),
      new FlipIf(
        new Or(
          new SafeAtHome,
          new MineralsAtLeast(500)),
        new If(
          new UnitsAtMost(0, Protoss.FleetBeacon),
          new TrainArmy,
          new TrainCarrierArmy),
        new Build(
          Get(Protoss.DragoonRange),
          Get(Protoss.RoboticsFacility),
          Get(Protoss.RoboticsSupportBay))),
      new Build(
        Get(Protoss.Stargate),
        Get(Protoss.FleetBeacon),
        Get(2, Protoss.Stargate))),
    new If(
      new UpgradeComplete(Protoss.AirDamage, 3),
      new UpgradeContinuously(Protoss.AirArmor),
      new UpgradeContinuously(Protoss.AirDamage)),
    new Build(Get(5, Protoss.Gateway))
  )
}
