package Planning.Plans.GamePlans.Terran.FFA

import Lifecycle.With
import Macro.Requests.Get
import Planning.Plan
import Planning.Plans.Army.Aggression
import Planning.Plans.Basic.NoPlan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.All.GameplanTemplate
import Planning.Plans.Macro.Automatic.{Pump, UpgradeContinuously}
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBasesFFA}
import Planning.Predicates.Compound.{And, Check}
import Planning.Predicates.Economy.GasAtLeast
import Planning.Predicates.Milestones.{EnemyHasShownCloakedThreat, UnitsAtLeast}
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.UnitFilters.{IsTank, IsWarrior}

class TerranFFAMech extends GameplanTemplate {

  override val scoutPlan     : Plan  = NoPlan()
  
  private class UpgradeMech extends Parallel(
    new BuildGasPumps,
    new Build(
      Get(1, Terran.Factory),
      Get(2, Terran.Armory)),
    new UpgradeContinuously(Terran.MechDamage),
    new UpgradeContinuously(Terran.MechArmor),
    new Build(
      Get(1, Terran.Starport),
      Get(1, Terran.ScienceFacility)))
  
  private class UpgradeAir extends Parallel(
    new BuildGasPumps,
    new Build(
      Get(1, Terran.Factory),
      Get(1, Terran.Starport),
      Get(2, Terran.Armory)),
    new UpgradeContinuously(Terran.AirDamage),
    new UpgradeContinuously(Terran.AirArmor),
    new Build(Get(1, Terran.ScienceFacility)))
  
  private class BuildScienceFacilityForAddon(addon: UnitClass) extends Plan {
    override def onUpdate(): Unit = {
      val numberOfCovertOps         = With.units.countOurs(Terran.CovertOps)
      val numberOfPhysicsLab        = With.units.countOurs(Terran.PhysicsLab)
      val numberOfThisAddon         = if (addon == Terran.CovertOps) numberOfCovertOps else numberOfPhysicsLab
      val numberOfOtherAddon        = numberOfCovertOps + numberOfPhysicsLab - numberOfThisAddon
      val numberOfScienceFacilities = if (numberOfThisAddon > 0) 0 else 1 + numberOfOtherAddon
      With.scheduler.request(this, Get(numberOfScienceFacilities, Terran.ScienceFacility))
    }
  }
  
  override def attackPlan: Plan = new If(
    new UnitsAtLeast(20, IsWarrior),
    super.attackPlan)
  
  override lazy val buildOrder = Vector(
      Get(1,   Terran.CommandCenter),
      Get(9,   Terran.SCV),
      Get(1,   Terran.SupplyDepot),
      Get(11,  Terran.SCV),
      Get(1,   Terran.Barracks),
      Get(13,  Terran.SCV),
      Get(2,   Terran.Barracks),
      Get(14,  Terran.SCV),
      Get(2,   Terran.SupplyDepot))
  
  override def emergencyPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(1, Terran.Academy, complete = true),
      new Pump(Terran.Comsat, 2)),
    new If(
      new EnemyHasShownCloakedThreat,
      new Build(
        Get(1, Terran.ScienceVessel),
        Get(1, Terran.Refinery),
        Get(1, Terran.Academy),
        Get(1, Terran.EngineeringBay),
        Get(4, Terran.MissileTurret))),
    new If(
      new UnitsAtLeast(1, Terran.Ghost),
      new Pump(Terran.NuclearSilo))
  )
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new And(
        new UnitsAtLeast(20, IsWarrior),
        new GasAtLeast(1000)),
      new Parallel(
        new Build(Get(1, Terran.Factory)),
        new BuildScienceFacilityForAddon(Terran.CovertOps),
        new Build(Get(1, Terran.CovertOps)),
        new Pump(Terran.Ghost))
    ),
    new If(new UnitsAtLeast(1,  IsTank),    new Build(Get(Terran.SiegeMode))),
    new If(new UnitsAtLeast(12, IsWarrior),     new RequireMiningBasesFFA(2)),
    new If(new UnitsAtLeast(30, IsWarrior),     new RequireMiningBasesFFA(3)),
    new If(new UnitsAtLeast(40, IsWarrior),     new RequireMiningBasesFFA(4)),
    new If(new UnitsAtLeast(50, IsWarrior),     new RequireMiningBasesFFA(5)),
    new If(new UnitsAtLeast(30, IsWarrior),     new Pump(Terran.Dropship, 2)),
    new Pump(Terran.Comsat, 2),
    new Pump(Terran.Battlecruiser),
    new If(new UnitsAtLeast(1, Terran.NuclearMissile),  new Build(Get(Terran.GhostCloak))),
    new If(new UnitsAtLeast(1, Terran.NuclearMissile),  new Build(Get(Terran.GhostVisionRange))),
    new If(new UnitsAtLeast(3, Terran.Ghost),           new Build(Get(Terran.Lockdown))),
    new If(new UnitsAtLeast(8, Terran.Ghost),           new Build(Get(Terran.GhostEnergy))),
    new If(new UnitsAtLeast(1, Terran.Ghost),           new Pump(Terran.NuclearMissile)),
    new If(new UnitsAtLeast(3, Terran.Battlecruiser),   new Build(Get(Terran.Yamato))),
    new If(new UnitsAtLeast(6, Terran.Battlecruiser),   new Build(Get(Terran.BattlecruiserEnergy))),
    new If(new UnitsAtLeast(5, Terran.Battlecruiser),   new UpgradeAir),
    new If(new UnitsAtLeast(3, IsTank),     new UpgradeMech),
    new Pump(Terran.ScienceVessel, 2, 1),
    new Pump(Terran.SiegeTankUnsieged, 30),
    new Pump(Terran.Ghost, 5, 2),
    new If(
      new Check(() =>
        With.units.countOurs(Terran.Marine).toDouble /
        Math.max(
          1.0,
          With.units.countOurs(Terran.Medic))
        >= 4.0),
      new Pump(Terran.Medic, 20, 2)),
    new Pump(Terran.Marine),
    new Build(
      Get(1, Terran.Refinery),
      Get(1, Terran.Academy),
      Get(1, Terran.EngineeringBay),
      Get(Terran.Stim)),
    new UpgradeContinuously(Terran.BioDamage),
    new RequireMiningBasesFFA(2),
    new Build(
      Get(2, Terran.Bunker),
      Get(2, Terran.EngineeringBay),
      Get(Terran.MarineRange),
      Get(6, Terran.Barracks)),
    new UpgradeContinuously(Terran.BioArmor),
    new BuildGasPumps,
    new Build(
      Get(4, Terran.Factory),
      Get(10, Terran.Barracks),
      Get(8, Terran.Factory))
  )
}