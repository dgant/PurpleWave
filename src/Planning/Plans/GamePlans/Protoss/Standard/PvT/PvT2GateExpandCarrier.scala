package Planning.Plans.GamePlans.Protoss.Standard.PvT

import Lifecycle.With
import Macro.BuildRequests.{BuildRequest, Get}
import Planning.Plan
import Planning.Plans.Compound.{If, Or, Parallel, Trigger}
import Planning.Plans.GamePlans.GameplanTemplate
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import Planning.Plans.Macro.Automatic.{Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Placement.BuildCannonsAtExpansions
import Planning.Plans.Scouting.{ScoutExpansionsAt, ScoutOn}
import Planning.Predicates.Compound.{And, Latch, Not}
import Planning.Predicates.Milestones._
import Planning.Predicates.Strategy.{Employing, EnemyStrategy}
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Protoss.PvT2GateRangeExpandCarrier

class PvT2GateExpandCarrier extends GameplanTemplate {

  override val activationCriteria = new Employing(PvT2GateRangeExpandCarrier)
  override def scoutExposPlan: Plan = new If(new BasesAtLeast(2), new ScoutExpansionsAt(80))

  override def scoutPlan: Plan = new ScoutOn(Protoss.CyberneticsCore)
  override def attackPlan: Plan = new Parallel(
    new PvTIdeas.AttackWithCarrierFleet,
    new If(
      new Or(
        new Not(new EnemyHasShown(Terran.SpiderMine)),
        new Not(new EnemyStrategy(With.fingerprints.twoFac)),
        new Latch(new UnitsAtLeast(4, Protoss.Carrier, complete = true))),
      super.attackPlan)
  )

  //override def buildOrder: Seq[BuildRequest] = ProtossBuilds.PvT2GateRangeExpand
  override def buildOrder: Seq[BuildRequest] = ProtossBuilds.PvT1015GateGoon

  override def buildPlans = Vector(
    new RequireMiningBases(2),
    new If(new UnitsAtLeast(4, Protoss.Carrier), new Build(Get(Protoss.CarrierCapacity))),
    new If(new UnitsAtLeast(4, Protoss.Carrier), new RequireMiningBases(3)),
    new If(new UnitsAtLeast(4, Protoss.Carrier), new Build(Get(Protoss.CitadelOfAdun), Get(Protoss.ZealotSpeed))),
    new Pump(Protoss.Carrier),
    new BuildCannonsAtExpansions(2),
    new If(
      new UnitsAtLeast(2, Protoss.Stargate),
      new If(
        new UpgradeComplete(Protoss.AirDamage, 3),
        new UpgradeContinuously(Protoss.AirArmor),
        new UpgradeContinuously(Protoss.AirDamage))),
    new UpgradeContinuously(Protoss.GroundDamage),
    new Pump(Protoss.Observer, 2),
    new Pump(Protoss.Dragoon),
    new If(
      new And(
        new EnemyStrategy(With.fingerprints.twoFac),
        new UnitsAtMost(0, Protoss.FleetBeacon)),
      new Build(Get(3, Protoss.Gateway))),
    new BuildGasPumps,
    new Build(
      Get(Protoss.Stargate),
      Get(Protoss.FleetBeacon)),
    new Trigger(
      new UnitsAtLeast(1, Protoss.FleetBeacon),
      new Parallel(
        new Build(Get(2, Protoss.Stargate)),
        new Build(Get(4, Protoss.Gateway)),
        new RequireMiningBases(3),
        new Build(Get(Protoss.CitadelOfAdun)),
        new Build(Get(Protoss.ZealotSpeed)),
        new Build(Get(3, Protoss.Stargate)),
        new Build(Get(6, Protoss.Gateway)),
        new Pump(Protoss.Zealot),
        new Build(Get(4, Protoss.Stargate)),
        new RequireMiningBases(4),
        new Build(Get(10, Protoss.Gateway))))
  )
}
