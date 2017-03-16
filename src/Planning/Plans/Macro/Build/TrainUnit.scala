package Planning.Plans.Macro.Build

import Debugging.TypeDescriber
import Planning.Plans.Allocation.{LockCurrencyForUnit, LockUnits}
import Planning.Plan
import Startup.With
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.UnitMatchType
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class TrainUnit(val traineeType:UnitClass) extends Plan {
  
  val currency = new LockCurrencyForUnit(traineeType)
  val trainerPlan = new LockUnits {
    unitMatcher.set(new UnitMatchType(traineeType.whatBuilds._1))
    unitCounter.set(UnitCountOne)
  }
  
  private var trainer:Option[FriendlyUnitInfo] = None
  private var trainee:Option[FriendlyUnitInfo] = None
  private var lastOrderFrame = 0
  
  description.set("Train a " + TypeDescriber.unit(traineeType))
  
  override def isComplete: Boolean = trainee.exists(p => p.alive && p.complete)
  override def getChildren: Iterable[Plan] = List(currency, trainerPlan)
  override def onFrame() {
    if (isComplete) { return }
    
    getChildren.foreach(_.onFrame())
  
    //Require all the resources
    if (!getChildren.forall(_.isComplete)) {
      return
    }
  
    trainerPlan.units.headOption.foreach(requireTraining)
  }
  
  private def reset() {
    currency.isSpent = false
    trainer = None
    trainee = None
  }
  
  private def requireTraining(newTrainer:FriendlyUnitInfo) {
    
    // Training?	Ordered?	Unit?	Then
    // ---------- --------- ----- ----
    // Yes		    No		    -	    Wait
    // Yes		    Yes		    Yes	  Wait
    // Yes		    Yes		    No	  Wait (Unit should appear eventually)
    // No		      No		    -	    Order
    // No		      Yes		    -	    Order (again; unexpected, but stuff happens)
    
    if ( ! trainer.contains(newTrainer)) {
      reset()
    }
    
    val isTraining = newTrainer.trainingQueue.nonEmpty
    val ordered = trainer.nonEmpty
    
    if (isTraining) {
      if (ordered) {
        //TODO: Make sure we verify that the worker is *not complete* so, say, a wraith floating over a Starport doesn't get linked
        
        //Note that it's possible for a building to briefly have a worker type in the queue with no worker created.
        trainee = With.units.ours
          .filter(u =>
            u.utype == traineeType &&
            u.x == newTrainer.x &&
            u.y == newTrainer.y &&
            ! u.complete)
          .headOption
        
        //There seems to be a 1+ frame delay between the queue getting started
        //and the incomplete worker being created.
        //So don't freak out if the worker doesn't show up right away
      }
    }
    else {
      orderUnit(newTrainer)
    }
  }
  
  private def orderUnit(newTrainer:FriendlyUnitInfo) {
    trainer = Some(newTrainer)
    currency.isSpent = true
    newTrainer.baseUnit.train(traineeType.baseType)
    lastOrderFrame = With.frame
  }
}
