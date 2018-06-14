package Planning.Plans.GamePlans.Terran.Standard.TvE

import Macro.BuildRequests.{Get, Tech, Upgrade}
import Planning.Composition.UnitMatchers.{UnitMatchSiegeTank, UnitMatchWarriors}
import Planning.Plan
import Planning.Plans.Army.NukeBase
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.{Employing, SafeAtHome}
import Planning.Plans.Macro.Automatic.TrainContinuously
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
    new TrainContinuously(Terran.NuclearSilo),
    new TrainContinuously(Terran.NuclearMissile),
    new TrainContinuously(Terran.SCV),
    new TrainContinuously(Terran.Ghost, 2, 1),
    new TrainContinuously(Terran.Dropship, 2, 1),
    new If(
      new Or(
        new EnemiesAtLeast(1, Terran.Wraith),
        new EnemiesAtLeast(1, Protoss.Scout)),
      new TrainContinuously(Terran.Wraith),
      new Build(
        Tech(Terran.WraithCloak),
        Get(2, Terran.Starport))
    ),
    new FlipIf(
      new And(
        new SafeAtHome,
        new UnitsAtLeast(20, UnitMatchWarriors)),
    new Parallel(
      new IfOnMiningBases(2, new TrainContinuously(Terran.ScienceVessel, 2, 1)),
      new TrainContinuously(Terran.SiegeTankUnsieged),
      new TrainContinuously(Terran.Marine)),
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
        Tech(Terran.SiegeMode),
        Get(1, Terran.EngineeringBay),
        Get(1, Terran.MissileTurret),
        Get(1, Terran.ScienceFacility),
        Get(1, Terran.CovertOps),
        Get(1, Terran.Academy),
        Tech(Terran.GhostCloak),
        Get(1, Terran.ControlTower),
        Get(2, Terran.Barracks),
        Upgrade(Terran.GhostVisionRange)))),
    new Build(
      Get(2, Terran.CommandCenter),
      Get(2, Terran.Bunker),
      Get(2, Terran.MissileTurret)),
    new Build(
      Upgrade(Terran.MarineRange),
      Get(2, Terran.EngineeringBay)),
    new UpgradeContinuously(Terran.BioDamage),
    new UpgradeContinuously(Terran.BioArmor),
    new Build(
      Tech(Terran.Stim),
      Get(4, Terran.Factory),
      Get(4, Terran.MachineShop),
      Get(8, Terran.Barracks)),
    new RequireMiningBases(3),
    new Build(Get(15, Terran.Barracks))
  )
}
