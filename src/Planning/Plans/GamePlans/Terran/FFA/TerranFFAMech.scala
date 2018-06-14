package Planning.Plans.GamePlans.Terran.FFA

import Lifecycle.With
import Macro.BuildRequests.{GetAtLeast, GetTech, GetUpgrade}
import Planning.Composition.UnitMatchers.{UnitMatchSiegeTank, UnitMatchWarriors}
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.GamePlans.Terran.Situational.BunkersAtNatural
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.{BuildGasPumps, RequireMiningBasesFFA}
import Planning.Plans.Predicates.Milestones.{EnemyHasShownCloakedThreat, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import Planning.Plans.Predicates.Economy.GasAtLeast
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitClasses.UnitClass

class TerranFFAMech extends GameplanModeTemplate {
  
  override def defaultPlacementPlan : Plan  = new BunkersAtNatural(2)
  override val defaultScoutPlan     : Plan  = NoPlan()
  override val aggression                   = 0.8
  override val scoutExpansionsAt            = 150
  
  private class UpgradeMech extends Parallel(
    new BuildGasPumps,
    new Build(
      GetAtLeast(1, Terran.Factory),
      GetAtLeast(2, Terran.Armory)),
    new UpgradeContinuously(Terran.MechDamage),
    new UpgradeContinuously(Terran.MechArmor),
    new Build(
      GetAtLeast(1, Terran.Starport),
      GetAtLeast(1, Terran.ScienceFacility)))
  
  private class UpgradeAir extends Parallel(
    new BuildGasPumps,
    new Build(
      GetAtLeast(1, Terran.Factory),
      GetAtLeast(1, Terran.Starport),
      GetAtLeast(2, Terran.Armory)),
    new UpgradeContinuously(Terran.AirDamage),
    new UpgradeContinuously(Terran.AirArmor),
    new Build(GetAtLeast(1, Terran.ScienceFacility)))
  
  private class BuildScienceFacilityForAddon(addon: UnitClass) extends Plan {
    val build = new Build()
    override def onUpdate() {
      val numberOfCovertOps         = With.units.countOurs(Terran.CovertOps)
      val numberOfPhysicsLab        = With.units.countOurs(Terran.PhysicsLab)
      val numberOfThisAddon         = if (addon == Terran.CovertOps) numberOfCovertOps else numberOfPhysicsLab
      val numberOfOtherAddon        = numberOfCovertOps + numberOfPhysicsLab - numberOfThisAddon
      val numberOfScienceFacilities = if (numberOfThisAddon > 0) 0 else 1 + numberOfOtherAddon
      
      if (numberOfScienceFacilities > 0) {
        build.requests.set(Vector(GetAtLeast(numberOfScienceFacilities, Terran.ScienceFacility)))
      }
      build.update()
    }
  }
  
  override def defaultAttackPlan: Plan = new If(
    new UnitsAtLeast(20, UnitMatchWarriors),
    super.defaultAttackPlan)
  
  override lazy val buildOrder = Vector(
      GetAtLeast(1,   Terran.CommandCenter),
      GetAtLeast(9,   Terran.SCV),
      GetAtLeast(1,   Terran.SupplyDepot),
      GetAtLeast(11,  Terran.SCV),
      GetAtLeast(1,   Terran.Barracks),
      GetAtLeast(13,  Terran.SCV),
      GetAtLeast(2,   Terran.Barracks),
      GetAtLeast(14,  Terran.SCV),
      GetAtLeast(2,   Terran.SupplyDepot))
  
  override def emergencyPlans: Seq[Plan] = Vector(
    new If(
      new UnitsAtLeast(1, Terran.Academy, complete = true),
      new TrainContinuously(Terran.Comsat, 2)),
    new If(
      new EnemyHasShownCloakedThreat,
      new Build(
        GetAtLeast(1, Terran.ScienceVessel),
        GetAtLeast(1, Terran.Refinery),
        GetAtLeast(1, Terran.Academy),
        GetAtLeast(1, Terran.EngineeringBay),
        GetAtLeast(4, Terran.MissileTurret))),
    new If(
      new UnitsAtLeast(1, Terran.Ghost),
      new TrainContinuously(Terran.NuclearSilo))
  )
  
  override def buildPlans: Seq[Plan] = Vector(
    new If(
      new And(
        new UnitsAtLeast(20, UnitMatchWarriors),
        new GasAtLeast(1000)),
      new Parallel(
        new Build(GetAtLeast(1, Terran.Factory)),
        new BuildScienceFacilityForAddon(Terran.CovertOps),
        new Build(GetAtLeast(1, Terran.CovertOps)),
        new TrainContinuously(Terran.Ghost))
    ),
    new If(new UnitsAtLeast(1,  UnitMatchSiegeTank),    new Build(GetTech(Terran.SiegeMode))),
    new If(new UnitsAtLeast(12, UnitMatchWarriors),     new RequireMiningBasesFFA(2)),
    new If(new UnitsAtLeast(30, UnitMatchWarriors),     new RequireMiningBasesFFA(3)),
    new If(new UnitsAtLeast(40, UnitMatchWarriors),     new RequireMiningBasesFFA(4)),
    new If(new UnitsAtLeast(50, UnitMatchWarriors),     new RequireMiningBasesFFA(5)),
    new If(new UnitsAtLeast(30, UnitMatchWarriors),     new TrainContinuously(Terran.Dropship, 2)),
    new TrainContinuously(Terran.Comsat, 2),
    new TrainContinuously(Terran.Battlecruiser),
    new If(new UnitsAtLeast(1, Terran.NuclearMissile),  new Build(GetTech(Terran.GhostCloak))),
    new If(new UnitsAtLeast(1, Terran.NuclearMissile),  new Build(GetUpgrade(Terran.GhostVisionRange))),
    new If(new UnitsAtLeast(3, Terran.Ghost),           new Build(GetTech(Terran.Lockdown))),
    new If(new UnitsAtLeast(8, Terran.Ghost),           new Build(GetUpgrade(Terran.GhostEnergy))),
    new If(new UnitsAtLeast(1, Terran.Ghost),           new TrainContinuously(Terran.NuclearMissile)),
    new If(new UnitsAtLeast(3, Terran.Battlecruiser),   new Build(GetTech(Terran.Yamato))),
    new If(new UnitsAtLeast(6, Terran.Battlecruiser),   new Build(GetUpgrade(Terran.BattlecruiserEnergy))),
    new If(new UnitsAtLeast(5, Terran.Battlecruiser),   new UpgradeAir),
    new If(new UnitsAtLeast(3, UnitMatchSiegeTank),     new UpgradeMech),
    new TrainContinuously(Terran.ScienceVessel, 2, 1),
    new TrainContinuously(Terran.SiegeTankUnsieged, 30),
    new TrainContinuously(Terran.Ghost, 5, 2),
    new If(
      new Check(() =>
        With.units.countOurs(Terran.Marine).toDouble /
        Math.max(
          1.0,
          With.units.countOurs(Terran.Medic))
        >= 4.0),
      new TrainContinuously(Terran.Medic, 20, 2)),
    new TrainContinuously(Terran.Marine),
    new Build(
      GetAtLeast(1, Terran.Refinery),
      GetAtLeast(1, Terran.Academy),
      GetAtLeast(1, Terran.EngineeringBay),
      GetTech(Terran.Stim)),
    new UpgradeContinuously(Terran.BioDamage),
    new RequireMiningBasesFFA(2),
    new Build(
      GetAtLeast(2, Terran.Bunker),
      GetAtLeast(2, Terran.EngineeringBay),
      GetUpgrade(Terran.MarineRange),
      GetAtLeast(6, Terran.Barracks)),
    new UpgradeContinuously(Terran.BioArmor),
    new BuildGasPumps,
    new Build(
      GetAtLeast(4, Terran.Factory),
      GetAtLeast(10, Terran.Barracks),
      GetAtLeast(8, Terran.Factory))
  )
}