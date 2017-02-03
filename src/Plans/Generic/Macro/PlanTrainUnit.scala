package Plans.Generic.Macro

import Development.{Logger, TypeDescriber}
import Plans.Generic.Allocation.{PlanAcquireCurrencyForUnit, PlanAcquireUnitsExactly}
import Plans.Plan
import Startup.With
import Strategies.UnitMatchers.UnitMatchType
import bwapi.UnitType

class PlanTrainUnit(val traineeType:UnitType)
  extends Plan {
  
  val _currencyPlan = new PlanAcquireCurrencyForUnit(traineeType)
  val _trainerPlan = new PlanAcquireUnitsExactly {
    setUnitMatcher(new UnitMatchType(traineeType.whatBuilds.first))
  }
  
  var _trainer:Option[bwapi.Unit] = None
  var _trainee:Option[bwapi.Unit] = None
  
  override def describe(): Option[String] = {
    Some(TypeDescriber.describeUnitType(traineeType))
  }
  
  override def isComplete(): Boolean = {
    _trainee.exists(p => p.exists && p.isCompleted)
  }
  
  override def children(): Iterable[Plan] = {
    List(_currencyPlan, _trainerPlan)
  }
  
  override def onFrame() {
    children.foreach(_.onFrame())
  
    //Require all the resources
    if (!children.forall(_.isComplete)) {
      return
    }
  
    _trainerPlan.units.headOption.foreach(_requireTraining)
  }
  
  def _reset() {
    _currencyPlan.isSpent = false
    _trainer = None
    _trainee = None
  }
  
  def _requireTraining(trainer:bwapi.Unit) {
    
    // Training?	Ordered?	Unit?	Then
    // ---------- --------- ----- ----
    // Yes		    No		    -	    Wait
    // Yes		    Yes		    Yes	  Wait
    // Yes		    Yes		    No	  Wait (Unit should appear eventually)
    // No		      No		    -	    Order
    // No		      Yes		    -	    Order (again; unexpected, but stuff happens)
    
    if ( ! _trainer.contains(trainer)) {
      _reset()
    }
    
    val isTraining = ! trainer.getTrainingQueue.isEmpty
    val ordered = _trainer.nonEmpty
    
    if (isTraining) {
      if (ordered) {
        //Note that it's possible for a building to briefly have a unit type in the queue with no unit created.
        _trainee = With.ourUnits
          .filter(u =>
            u.getType == traineeType &&
            u.getX == trainer.getX &&
            u.getY == trainer.getY)
          .headOption
        
        //There seems to be a 1+ frame delay between the queue getting started
        //and the incomplete unit being created.
        //So don't freak out if the unit doesn't show up right away
      }
    }
    else {
      if (ordered) {
        Logger.warn("We ordered to train a unit, but it's not training.")
      }
      _orderUnit(trainer)
    }
  }
  
  def _orderUnit(trainer:bwapi.Unit) {
    _trainer = Some(trainer)
    _currencyPlan.isSpent = true
    trainer.train(traineeType)
  }
}
