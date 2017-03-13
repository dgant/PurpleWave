package Plans.Macro.Automatic

import Startup.With
import Types.Buildable.{Buildable, BuildableUnit}
import bwapi.UnitType

class TrainContinuously(var unitType:UnitType = UnitType.None) extends AbstractBuildContinuously {
  
  override def _newBuild:Buildable = new BuildableUnit(unitType)
  override def _totalRequired:Int = {
    val now = With.units.ours.count(_.utype == unitType)
    val capacity = List(
      With.units.ours.filter(_.complete).count(_.utype == unitType.whatBuilds.first) * unitType.whatBuilds.second,
      With.game.self.minerals / Math.max(1, unitType.mineralPrice),
      With.game.self.gas / Math.max(1, unitType.gasPrice)
    )
    .min
    now + capacity
  }
    
}
