package Planning.Plans.Protoss.GamePlans.Standard.PvP

import Macro.BuildRequests.RequestAtLeast
import Planning.Composition.UnitMatchers.UnitMatchWarriors
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Information.{Employing, SafeAtHome}
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.{Build, BuildOrder}
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Macro.Milestones._
import Planning.Plans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP.PvPOpen1GateReaverExpand

class PvPOpen1GateReaverExpand extends GameplanModeTemplate {
  
  override val activationCriteria : Plan      = new Employing(PvPOpen1GateReaverExpand)
  override val completionCriteria : Plan      = new MiningBasesAtLeast(2)
  override def emergencyPlans     : Seq[Plan] = Seq(new PvPIdeas.ReactToDarkTemplarEmergencies)
  override val aggression         : Double    = 0.8
  
  
  override val buildOrder = ProtossBuilds.OpeningZCoreZ
  override def buildPlans = Vector(
    
    new If(
      new UnitsAtLeast(2, Protoss.Reaver),
      new RequireMiningBases(2)),
    
    new TrainContinuously(Protoss.Reaver),
    new PvPIdeas.BuildDragoonsOrZealots,
    
    new If(
      new PvPIdeas.Crummy2GateDetection,
      new Build(RequestAtLeast(2, Protoss.Gateway))),
    new FlipIf(
      new And(
        new UnitsAtLeast(4, UnitMatchWarriors),
        new SafeAtHome),
      new Build(RequestAtLeast(3, Protoss.Gateway)),
      new Parallel(
        new BuildOrder(
          RequestAtLeast(1, Protoss.RoboticsFacility),
          RequestAtLeast(1, Protoss.RoboticsSupportBay),
          RequestAtLeast(1, Protoss.Reaver)),
        new RequireMiningBases(2)))
  )
}
