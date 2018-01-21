package Planning.Plans.GamePlans.Terran.Standard.TvE

import Macro.BuildRequests.{RequestAtLeast, RequestTech, RequestUpgrade}
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound.{And, FlipIf, If}
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.{Employing, SafeAtHome}
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{IfOnMiningBases, UnitsAtLeast}
import Planning.Plans.Macro.Upgrades.UpgradeContinuously
import ProxyBwapi.Races.Terran
import Strategery.Strategies.Terran.TvE.TvTPNukeDrop

class TvTPNukeDrop extends GameplanModeTemplate {
  
  override val activationCriteria: Plan = new Employing(TvTPNukeDrop)
  
  override val buildOrder = Vector(
    RequestAtLeast(9, Terran.SCV),
    RequestAtLeast(1, Terran.SupplyDepot),
    RequestAtLeast(10, Terran.SCV),
    RequestAtLeast(1, Terran.Barracks),
    RequestAtLeast(11, Terran.SCV),
    RequestAtLeast(1, Terran.Refinery))
  
  override def buildPlans: Seq[Plan] = Vector(
    new TrainContinuously(Terran.NuclearMissile),
    new TrainContinuously(Terran.Ghost, 2, 1),
    new TrainContinuously(Terran.Dropship, 2, 1),
    new IfOnMiningBases(2, new TrainContinuously(Terran.ScienceVessel, 2, 1)),
    new TrainContinuously(Terran.SiegeTankUnsieged),
    new If(
      new UnitsAtLeast(30, UnitMatchWarriors),
      new TrainContinuously(Terran.Firebat, 8, 4)
    ),
    new TrainContinuously(Terran.Marine),
    new FlipIf(
      new And(
        new UnitsAtLeast(8, UnitMatchWarriors),
        new SafeAtHome),
      new Build(
        RequestAtLeast(1, Terran.Bunker),
        RequestAtLeast(1, Terran.Factory),
        RequestAtLeast(1, Terran.MachineShop),
        RequestAtLeast(1, Terran.Starport),
        RequestAtLeast(1, Terran.EngineeringBay),
        RequestAtLeast(1, Terran.MissileTurret),
        RequestAtLeast(1, Terran.ScienceFacility),
        RequestAtLeast(1, Terran.CovertOps),
        RequestAtLeast(1, Terran.Academy),
        RequestTech(Terran.GhostCloak),
        RequestAtLeast(1, Terran.ControlTower),
        RequestAtLeast(2, Terran.Barracks),
        RequestUpgrade(Terran.GhostVisionRange)),
      new Build(
        RequestAtLeast(2, Terran.CommandCenter),
        RequestAtLeast(2, Terran.Bunker),
        RequestAtLeast(2, Terran.MissileTurret))),
    new Build(
      RequestUpgrade(Terran.MarineRange),
      RequestAtLeast(2, Terran.EngineeringBay)),
    new UpgradeContinuously(Terran.BioDamage),
    new UpgradeContinuously(Terran.BioArmor),
    new Build(
      RequestTech(Terran.Stim),
      RequestAtLeast(4, Terran.Factory),
      RequestAtLeast(4, Terran.MachineShop),
      RequestAtLeast(8, Terran.Barracks)),
    new RequireMiningBases(3),
    new Build(RequestAtLeast(15, Terran.Barracks))
  )
}
