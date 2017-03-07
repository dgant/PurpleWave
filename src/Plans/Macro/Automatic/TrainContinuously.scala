package Plans.Macro.Automatic

import Startup.With
import Types.Buildable.{Buildable, BuildableUnit}
import bwapi.UnitType

class TrainContinuously(unitType:UnitType) extends AbstractBuildContinuously {
  
  override def _newBuild:Buildable = new BuildableUnit(unitType)
  override def _buildsRequired:Int =
      With.units.ours
        .filter(_.complete)
        .count(_.utype == unitType.whatBuilds.first) * unitType.whatBuilds.second
}
