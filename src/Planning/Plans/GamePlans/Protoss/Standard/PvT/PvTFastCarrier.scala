package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Macro.BuildRequests.Get
import Planning.Plan
import Planning.Plans.Army.EjectScout
import Planning.Plans.Compound.{If, Or, Trigger}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Macro.Automatic._
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.BuildCannonsAtExpansions
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.Compound.{And, Not}
import Planning.Predicates.Economy.MineralsAtMost
import Planning.Predicates.Milestones.{MiningBasesAtLeast, UnitsAtLeast, UpgradeComplete, UpgradeStarted}
import Planning.Predicates.Never
import Planning.Predicates.Reactive.EnemyBio
import Planning.Predicates.Strategy.Employing
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss._

class PvTFastCarrier extends GameplanModeTemplate {
  override val activationCriteria     = new Employing(PvTFastCarrier)
  override val completionCriteria     = new Never //new Latch(new UnitsAtLeast(1, Protoss.FleetBeacon))
  override val defaultWorkerPlan      = new PumpWorkers(oversaturate = true)
  override val priorityAttackPlan     = new PvTIdeas.PriorityAttacks
  override def defaultScoutPlan: Plan = new ScoutOn(Protoss.Pylon)
  override val buildOrder = Vector(
    Get(8,  Protoss.Probe),
    Get(1,  Protoss.Pylon),
    Get(10, Protoss.Probe),
    Get(1,  Protoss.Gateway),
    Get(12, Protoss.Probe),
    Get(1,  Protoss.Assimilator),
    Get(13, Protoss.Probe),
    Get(1,  Protoss.Zealot),
    Get(14, Protoss.Probe),
    Get(2,  Protoss.Pylon),
    Get(15, Protoss.Probe),
    Get(1,  Protoss.CyberneticsCore),
    Get(18, Protoss.Probe),
    Get(Protoss.DragoonRange),
    Get(1,  Protoss.Dragoon),
    Get(21, Protoss.Probe),
    Get(2,  Protoss.Nexus))

  override def buildPlans: Seq[Plan] = Vector(
    new EjectScout,
    new Trigger(
      new MiningBasesAtLeast(3),
      initialBefore = new If(
        new MineralsAtMost(400),
        new Trigger(
          new And(
            new UpgradeStarted(Protoss.DragoonRange),
            new UnitsAtLeast(1, Protoss.Dragoon)),
          initialBefore = new CapGasWorkersAt(2),
          initialAfter = new CapGasWorkersAt(1)))),
    new If(
      new And(
        new Not(new UpgradeComplete(Protoss.AirArmor, 3)),
        new Or(
          new UpgradeComplete(Protoss.AirDamage, 3),
          new EnemyBio)),
      new UpgradeContinuously(Protoss.AirArmor),
      new UpgradeContinuously(Protoss.AirDamage)),
    new If(
      new Or(
        new UnitsAtLeast(1, Protoss.Carrier, complete = true),
        new UnitsAtLeast(3, Protoss.Carrier)),
      new UpgradeContinuously(Protoss.CarrierCapacity)),
    new If(
      new UnitsAtLeast(6, Protoss.Gateway),
      new Build(
        Get(1, Protoss.CitadelOfAdun),
        Get(Protoss.ZealotSpeed))),
    new Pump(Protoss.Carrier),
    new Trigger(
      new UnitsAtLeast(3, Protoss.Carrier),
      new BuildCannonsAtExpansions(1)),
    new Pump(Protoss.Dragoon, 6),
    new If(
      new UpgradeStarted(Protoss.ZealotSpeed),
      new Pump(Protoss.Zealot)),
    new Pump(Protoss.Dragoon),
    new RequireMiningBases(3),
    new Build(
      Get(1, Protoss.Stargate),
      Get(2, Protoss.Gateway)),
    new BuildGasPumps,
    new Build(
      Get(1, Protoss.FleetBeacon),
      Get(3, Protoss.Stargate)),
    new Build(Get(3, Protoss.Gateway)),
    new RequireMiningBases(4),
    new Build(Get(8, Protoss.Gateway)),
    new UpgradeContinuously(Protoss.Shields),
    new RequireMiningBases(5),
    new Build(Get(4, Protoss.Stargate)),
    new RequireMiningBases(6),
    new Build(Get(20, Protoss.Gateway))
  )
}
