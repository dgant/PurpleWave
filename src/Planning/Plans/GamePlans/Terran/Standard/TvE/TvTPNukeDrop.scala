package Planning.Plans.GamePlans.Terran.Standard.TvE

import Macro.BuildRequests.Get
import Planning.Composition.UnitMatchers.{UnitMatchSiegeTank, UnitMatchWarriors}
import Planning.Plan
import Planning.Plans.Army.NukeBase
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import Planning.Plans.Macro.Automatic.Pump
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Milestones._
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.{Protoss, Terran}
import Strategery.Strategies.Terran.TvE.TvTPNukeDrop

class TvTPNukeDrop extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(TvTPNukeDrop)
  override def defaultAttackPlan = new If(
    new And(
      new UnitsAtLeast(40, UnitMatchWarriors),
      new MiningBasesAtLeast(2)),
    super.defaultAttackPlan)
  
  override def priorityAttackPlan: Plan = new NukeBase
  override def defaultDropPlan: Plan = NoPlan()
  
  override val buildOrder = Vector(
    Get(9, Terran.SCV),
    Get(1, Terran.SupplyDepot),
    Get(10, Terran.SCV),
    Get(1, Terran.Barracks),
    Get(11, Terran.SCV),
    Get(1, Terran.Refinery))
  
  override def defaultWorkerPlan: Plan = NoPlan()
  
  override def buildPlans: Seq[Plan] = Vector(
    new Pump(Terran.NuclearSilo),
    new Pump(Terran.NuclearMissile),
    new Pump(Terran.SCV),
    new Pump(Terran.Ghost, 2, 1),
    new Pump(Terran.Dropship, 2, 1),
    new If(
      new Or(
        new EnemiesAtLeast(1, Terran.Wraith),
        new EnemiesAtLeast(1, Protoss.Scout)),
      new Pump(Terran.Wraith),
      new Build(
        Get(Terran.WraithCloak),
        Get(2, Terran.Starport))
    ),
    new FlipIf(
      new And(
        new SafeAtHome,
        new UnitsAtLeast(20, UnitMatchWarriors)),
    new Parallel(
      new IfOnMiningBases(2, new Pump(Terran.ScienceVessel, 2, 1)),
      new Pump(Terran.SiegeTankUnsieged),
      new Pump(Terran.Marine)),
    new FlipIf(
      new And(
        new UnitsAtLeast(8, UnitMatchWarriors),
        new UnitsAtLeast(3, UnitMatchSiegeTank),
        new SafeAtHome),
      new Build(
        Get(1, Terran.Bunker),
        Get(1, Terran.Factory),
        Get(1, Terran.MachineShop),
        Get(1, Terran.Starport),
        Get(Terran.SiegeMode),
        Get(1, Terran.EngineeringBay),
        Get(1, Terran.MissileTurret),
        Get(1, Terran.ScienceFacility),
        Get(1, Terran.CovertOps),
        Get(1, Terran.Academy),
        Get(Terran.GhostCloak),
        Get(1, Terran.ControlTower),
        Get(2, Terran.Barracks),
        Get(Terran.GhostVisionRange)))),
    new Build(
      Get(2, Terran.CommandCenter),
      Get(2, Terran.Bunker),
      Get(2, Terran.MissileTurret)),
    new Build(
      Get(Terran.MarineRange),
      Get(2, Terran.EngineeringBay)),
    new UpgradeContinuously(Terran.BioDamage),
    new UpgradeContinuously(Terran.BioArmor),
    new Build(
      Get(Terran.Stim),
      Get(4, Terran.Factory),
      Get(4, Terran.MachineShop),
      Get(8, Terran.Barracks)),
    new RequireMiningBases(3),
    new Build(Get(15, Terran.Barracks))
  )
}
