package Planning.Plans.Protoss.GamePlans.Standard.PvP

import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.Information.{Employing, SafeAtHome}
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones.{OnMiningBases, UnitsAtLeast}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP.PvPOneBaseReaverExpand

class PvPOneBaseReaverExpand extends GameplanModeTemplatePvP {
  
  override val activationCriteria : Plan      = new Employing(PvPOneBaseReaverExpand)
  override val completionCriteria : Plan      = new OnMiningBases(2)
  override def emergencyPlans     : Seq[Plan] = Seq(new PvPIdeas.ReactToDarkTemplarEmergencies)
  
  override def buildPlans = Vector(
    new FlipIf(
      new And(
        new UnitsAtLeast(5, UnitMatchWarriors),
        new SafeAtHome),
      new Parallel(
        new TrainContinuously(Protoss.Reaver, 4),
        new PvPIdeas.BuildDragoonsOrZealots,
        new Build(RequestAtLeast(3, Protoss.Gateway))),
      new Parallel(
        new Build(
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.RoboticsSupportBay)),
        new RequireMiningBases(2))))
}
