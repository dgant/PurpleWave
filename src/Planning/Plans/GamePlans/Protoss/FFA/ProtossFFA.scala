package Planning.Plans.Gameplans.Protoss.FFA

import Macro.Requests.Get
import Planning.Plan
import Planning.Plans.Compound.{If, Parallel}
import Planning.Plans.Gameplans.All.GameplanTemplate
import Planning.Plans.Macro.Automatic.{Pump, TechContinuously, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBases}
import Planning.Plans.Macro.Protoss.MeldArchons
import Planning.Plans.Scouting.ScoutOn
import Planning.Predicates.{Always, Predicate}
import Planning.Predicates.Milestones.{EnemyHasShownCloakedThreat, UnitsAtLeast, UpgradeComplete, UpgradeStarted}
import Utilities.UnitFilters.IsWarrior
import ProxyBwapi.Races.Protoss

class ProtossFFA extends GameplanTemplate {
  override val scoutPlan: Plan = new ScoutOn(Protoss.CyberneticsCore)
  
  override val buildOrder = Vector(
    Get(8,   Protoss.Probe),
    Get(Protoss.Pylon),
    Get(10, Protoss.Probe),
    Get(Protoss.Gateway),
    Get(12,  Protoss.Probe),
    Get(2, Protoss.Pylon),
    Get(13,  Protoss.Probe),
    Get(Protoss.Zealot),
    Get(15,  Protoss.Probe),
    Get(2, Protoss.Zealot),
    Get(2,   Protoss.Nexus))

  def doExpand: Predicate = new Always
  def expansionPlan = new Parallel(
    new If(UnitsAtLeast(4,  Protoss.Carrier,  complete = true), new RequireMiningBases(3)),
    new If(UnitsAtLeast(12, IsWarrior,    complete = true), new RequireMiningBases(3)),
    new If(UnitsAtLeast(6,  Protoss.Carrier,  complete = true), new RequireMiningBases(4)),
    new If(UnitsAtLeast(20, IsWarrior,    complete = true), new RequireMiningBases(4)))
  
  override def buildPlans: Seq[Plan] = Vector(
    new RequireMiningBases(2),
    new If(doExpand, expansionPlan),
    new Pump(Protoss.Observer, 2),
    new Pump(Protoss.Arbiter, 2),
    new Pump(Protoss.Carrier),
    new If(UnitsAtLeast(2, Protoss.Carrier), new UpgradeContinuously(Protoss.CarrierCapacity)),
    new If(UnitsAtLeast(2, Protoss.HighTemplar), new TechContinuously(Protoss.PsionicStorm)),
    new If(new EnemyHasShownCloakedThreat, new UpgradeContinuously(Protoss.ObserverSpeed)),
    new Pump(Protoss.DarkTemplar, 1),
    new Pump(Protoss.HighTemplar, 3),
    new If(UpgradeComplete(Protoss.ShuttleSpeed, 1, Protoss.Shuttle.buildFrames), new Pump(Protoss.Shuttle, 1)),
    new If(UpgradeComplete(Protoss.ZealotSpeed), new Pump(Protoss.Zealot, 6)),
    new UpgradeContinuously(Protoss.DragoonRange),
    new If(
      UpgradeStarted(Protoss.CarrierCapacity),
      new Pump(Protoss.Dragoon, 24),
      new Pump(Protoss.Dragoon, 24, maximumConcurrently = 2)),
    new Pump(Protoss.HighTemplar),
    new Pump(Protoss.Zealot),
    new MeldArchons(49),
    new Build(
      Get(Protoss.Assimilator),
      Get(Protoss.CyberneticsCore),
      Get(Protoss.DragoonRange),
      Get(2, Protoss.Gateway),
      Get(3, Protoss.Gateway),
      Get(2, Protoss.Assimilator),
      Get(2, Protoss.Stargate),
      Get(Protoss.FleetBeacon),
      Get(Protoss.AirDamage),
      Get(4, Protoss.Gateway)),
    new BuildGasPumps,
    new If(doExpand, new RequireMiningBases(3)),
    new Build(
      Get(Protoss.RoboticsFacility),
      Get(Protoss.Observatory)),
    new UpgradeContinuously(Protoss.AirDamage),
    new If(UpgradeComplete(Protoss.AirDamage, 3), new UpgradeContinuously(Protoss.AirArmor, 3)),
    new Build(
      Get(Protoss.CitadelOfAdun),
      Get(Protoss.ZealotSpeed),
      Get(Protoss.RoboticsSupportBay),
      Get(Protoss.ShuttleSpeed),
      Get(Protoss.Forge)),
    new UpgradeContinuously(Protoss.GroundDamage),
    new If(UpgradeComplete(Protoss.GroundDamage, 3), new UpgradeContinuously(Protoss.GroundArmor, 3)),
    new Build(
      Get(Protoss.TemplarArchives),
      Get(Protoss.PsionicStorm),
      Get(Protoss.ArbiterTribunal),
      Get(Protoss.Stasis),
      Get(15, Protoss.Gateway))
  )
}
