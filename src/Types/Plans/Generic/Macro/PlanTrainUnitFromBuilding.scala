package Types.Plans.Generic.Macro

import Development.Logger
import Startup.With
import Types.Plans.Generic.Allocation.{PlanAcquireCurrencyForUnit, PlanAcquireUnitsExactly}
import Types.Plans.Generic.Compound.PlanDelegateInSerial
import UnitMatchers.UnitMatchType
import bwapi.UnitType

class PlanTrainUnitFromBuilding(val traineeType:UnitType) extends PlanDelegateInSerial {
  
  val _currencyPlan = new PlanAcquireCurrencyForUnit(traineeType)
  val _trainerPlan = new PlanAcquireUnitsExactly(new UnitMatchType(traineeType.whatBuilds.first), 1)
  _children = List(_currencyPlan, _trainerPlan)
  
  var _trainer:Option[bwapi.Unit] = None
  var _trainee:Option[bwapi.Unit] = None
  
  override def isComplete(): Boolean = {
    _trainee.exists(p => p.exists && p.isCompleted)
  }
  
  override def execute() {
    _currencyPlan.isSpent = ! _trainee.isEmpty
  
    if (isComplete) {
      abort()
      return
    }
  
    super.execute()
  
    //Require all the resources
    if (!_children.forall(_.isComplete)) {
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
    // No		      No		    -	    Order
    // No		      Yes		    -	    Order (again; unexpected, but stuff happens)
    // Yes		    Yes		    No	  WTF   (what the heck is it training, then?)
    // Yes		    Yes		    Yes	  Wait
    
    if ( ! _trainer.contains(trainer)) {
      _reset()
    }
    
    val isTraining = ! trainer.getTrainingQueue.isEmpty
    val ordered = _trainer.nonEmpty
    
    if (ordered) {
      if (isTraining) {
        _trainee = With.ourUnits
          .filter(u =>
            u.getType == traineeType &&
            u.getX == trainer.getX &&
            u.getY == trainer.getY)
          .headOption
        
        if (_trainee.isEmpty) {
          Logger.warn(
            "Weird state: We ordered a %s at %s with an empty queue to build a %s, but there are no matching units.".format(
              trainer.getType,
              traineeType))
        }
      }
      else {
        _orderUnit(trainer)
      }
    }
    else {
      _orderUnit(trainer)
    }
  }
  
  def _orderUnit(trainer:bwapi.Unit) {
    _trainer = Some(trainer)
    trainer.train(traineeType)
  }
}
