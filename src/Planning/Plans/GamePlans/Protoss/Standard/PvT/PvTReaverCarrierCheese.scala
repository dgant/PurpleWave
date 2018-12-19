package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.Attack
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.{Pump, PumpWorkers, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones._
import ProxyBwapi.Races.Protoss

class PvTReaverCarrierCheese extends GameplanModeTemplate {

  override val buildOrder             = ProtossBuilds.PvT13Nexus_NZ1GateCore
  override val defaultWorkerPlan      = new PumpWorkers(oversaturate = true)
  override val priorityAttackPlan     = new PvTIdeas.PriorityAttacks
  override def defaultScoutPlan       = new ScoutOn(Protoss.CyberneticsCore)
  override def defaultAttackPlan      = new Trigger(new UnitsAtLeast(2, Protoss.Reaver, complete = true), new Attack)

  override def buildPlans: Seq[Plan] = Vector(
    new Build(
      Get(Protoss.Gateway),
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore)),
    new RequireMiningBases(3),
    new BuildGasPumps,
    new Build(
      Get(Protoss.Stargate),
      Get(Protoss.RoboticsFacility),
      Get(Protoss.DragoonRange),
      Get(Protoss.FleetBeacon),
      Get(Protoss.RoboticsSupportBay),
      Get(2, Protoss.Stargate)),
    new If(
      new UpgradeComplete(Protoss.AirArmor, 3),
      new UpgradeContinuously(Protoss.AirDamage),
      new UpgradeContinuously(Protoss.AirArmor)),
    new Trigger(new UnitsAtLeast(2, Protoss.Carrier), new UpgradeContinuously(Protoss.CarrierCapacity)),
    new FlipIf(
      new UnitsAtLeast(3, Protoss.Reaver),
      new Pump(Protoss.Reaver, 5),
      new Pump(Protoss.Carrier)),
    new If(
      new GasAtLeast(200),
      new Pump(Protoss.Dragoon)),
    new Pump(Protoss.Zealot),
    new Build(Get(4, Protoss.Gateway)),
    new RequireMiningBases(4),
    new Build(Get(8, Protoss.Gateway)),
    new RequireMiningBases(5),
    new Build(Get(20, Protoss.Gateway)),
    new RequireMiningBases(7)
  )
}

