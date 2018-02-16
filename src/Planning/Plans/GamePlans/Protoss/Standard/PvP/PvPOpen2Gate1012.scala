package Planning.Plans.GamePlans.Protoss.Standard.PvP

import Macro.BuildRequests.RequestAtLeast
import Planning.Plan
import Planning.Plans.Compound._
import Planning.Plans.GamePlans.GameplanModeTemplate
import Planning.Plans.Predicates.Reactive.EnemyBasesAtLeast
import Planning.Plans.Predicates.{Employing, SafeAtHome, SafeToAttack}
import Planning.Plans.Macro.Automatic.TrainContinuously
import Planning.Plans.Macro.BuildOrders.Build
import Planning.Plans.Macro.Expanding.RequireMiningBases
import Planning.Plans.Predicates.Milestones.{EnemyUnitsAtLeast, MiningBasesAtLeast, UnitsAtLeast}
import Planning.Plans.GamePlans.Protoss.ProtossBuilds
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.PvP.PvPOpen2Gate1012

class PvPOpen2Gate1012 extends GameplanModeTemplate {
  
  override val activationCriteria : Plan      = new Employing(PvPOpen2Gate1012)
  override val completionCriteria : Plan      = new MiningBasesAtLeast(2)
  override def defaultAttackPlan  : Plan      = new PvPIdeas.AttackSafely
  override val scoutAt            : Int       = 12
  override def emergencyPlans     : Seq[Plan] = Seq(new PvPIdeas.ReactToDarkTemplarEmergencies)
  
  override val buildOrder = ProtossBuilds.OpeningTwoGate1012
  override def buildPlans = Vector(
  
    // We've got to expand eventually!
    new If(
      new UnitsAtLeast(2, Protoss.Reaver),
      new RequireMiningBases(2)),
    
    new FlipIf(
      new SafeAtHome,
      new Parallel(
        new TrainContinuously(Protoss.Reaver, 2),
        new PvPIdeas.BuildDragoonsOrZealots,
      new Parallel(
        
        // Try to bust or match enemy expansions
        new If(
          new EnemyBasesAtLeast(2),
          new If(
            new SafeToAttack,
            new Build(
              RequestAtLeast(1, Protoss.Assimilator),
              RequestAtLeast(1, Protoss.CyberneticsCore),
              RequestAtLeast(4, Protoss.Gateway)),
            new If(
              new SafeAtHome,
              new RequireMiningBases(2)))),
    
        // If they have goons, we need goons
        new If(
          new Or(
            new EnemyUnitsAtLeast(1, Protoss.Dragoon),
            new EnemyUnitsAtLeast(1, Protoss.CyberneticsCore, complete = true)),
          new Build(
            RequestAtLeast(1, Protoss.Assimilator),
            RequestAtLeast(1, Protoss.CyberneticsCore)))
      ))),
  
    new Build(
      RequestAtLeast(2, Protoss.Gateway),
      RequestAtLeast(1, Protoss.RoboticsFacility),
      RequestAtLeast(1, Protoss.RoboticsSupportBay)),
    
    new RequireMiningBases(2),
    new Build(RequestAtLeast(4, Protoss.Gateway))
  )
}
