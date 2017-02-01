package Types.Plans.Generic.Macro

import Startup.With
import Types.Plans.Generic.Allocation.{PlanAcquireCurrencyForUnit, PlanAcquireUnitsExactly}
import Types.Plans.Generic.Compound.PlanDelegateInSerial
import UnitMatchers.UnitMatchType
import bwapi.UnitType

class PlanTrainUnitFromBuilding(
  val buildingType:UnitType,
  val traineeType:UnitType)
    extends PlanDelegateInSerial {
  
  val _currencyPlan = new PlanAcquireCurrencyForUnit(traineeType)
  val _trainerPlan = new PlanAcquireUnitsExactly(new UnitMatchType(traineeType.whatBuilds.first), 1)
  _children = List(_currencyPlan, _trainerPlan)
  
  var _orderedTrainer:Option[bwapi.Unit] = None
  var _trainee:Option[bwapi.Unit] = None
  
  override def isComplete(): Boolean = {
    _trainee.exists(p => p.exists &&  p.isCompleted)
  }
  
  override def execute() {
    _currencyPlan.isSpent = ! _orderedTrainer.isEmpty
    
    if (isComplete) {
      abort()
      return
    }
    
    super.execute()
    
    if ( ! _children.forall(_.isComplete)) {
      return
    }
    
    _trainerPlan.units.headOption.foreach(
      trainer => {
        if (_orderedTrainer.contains(trainer)) {
          if (_trainee.isEmpty) {
            _trainee = With.ourUnits.filter(u => u.getType == traineeType && ! u.isCompleted).headOption
          }
        }
        if (trainer.getTrainingQueue.isEmpty || _trainee.isEmpty) {
          val trainer = _trainerPlan.units.head
          trainer.build(traineeType)
          _orderedTrainer = Some(trainer)
        }
      })
  }
}
