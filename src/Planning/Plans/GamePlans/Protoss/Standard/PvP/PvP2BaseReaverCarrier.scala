package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound.{FlipIf, If, Or, Parallel}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.Standard.PvP.PvPIdeas.TrainArmy
import Planning.Plans.Macro.Automatic.{Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvPLateGame2BaseReaverCarrier_SpecificMaps, PvPLateGame2BaseReaverCarrier_SpecificOpponents}

class PvP2BaseReaverCarrier extends GameplanModeTemplate {

  override val activationCriteria = new Or(
    new Employing(PvPLateGame2BaseReaverCarrier_SpecificOpponents),
    new Employing(PvPLateGame2BaseReaverCarrier_SpecificMaps))

  override val scoutExpansionsAt = 150
  override val removeMineralBlocksAt = 80
  override val emergencyPlans: Vector[Plan] = Vector(
    new PvPIdeas.ReactToDarkTemplarEmergencies
  )

  override val defaultAttackPlan = new If(
    new UnitsAtMost(0, Protoss.FleetBeacon),
    new PvPIdeas.AttackSafely,
    new If(
      new UnitsAtLeast(24, Protoss.Interceptor),
      new Attack))

  class TrainCarrierArmy extends Parallel(
    new FlipIf(
      new UnitsAtLeast(3, Protoss.Reaver),
      new Pump(Protoss.Reaver, 5),
      new Pump(Protoss.Carrier)),
    new If(
      new GasAtLeast(200),
      new PvPIdeas.PumpDragoonsOrZealots,
      new Pump(Protoss.Zealot)))

  override def buildPlans: Seq[Plan] = Vector(
    new RequireMiningBases(2),
    new If(
      new UnitsAtLeast(2, Protoss.Carrier),
      new UpgradeContinuously(Protoss.CarrierCapacity)),
    new If(
      new UnitsAtMost(0, Protoss.FleetBeacon),
      new TrainArmy,
      new TrainCarrierArmy),
    new Build(
      Get(2, Protoss.Gateway),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore)),
    new BuildGasPumps,
    new Build(
      Get(Protoss.DragoonRange),
      Get(Protoss.RoboticsFacility)),
    new PvPIdeas.GetObserversIfDarkTemplarPossible,
    new Build(
      Get(Protoss.RoboticsSupportBay),
      Get(Protoss.Stargate),
      Get(Protoss.FleetBeacon),
      Get(2, Protoss.Stargate)),
    new If(
      new UpgradeComplete(Protoss.AirDamage, 3),
      new UpgradeContinuously(Protoss.AirArmor),
      new UpgradeContinuously(Protoss.AirDamage)),
    new Build(Get(5, Protoss.Gateway))
  )
}
