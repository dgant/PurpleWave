package Performance.Tasks.Grids

import Information.Grids.AbstractGrid
import Lifecycle.With
import Performance.Tasks.AbstractTask

abstract class GridTask[T](grid: AbstractGrid[T]) extends AbstractTask {
  override protected def onRun(): Unit = grid.update()
}

class TaskGridAltitudeBonus     extends GridTask(With.grids.altitudeBonus)
class TaskGridBuildable         extends GridTask(With.grids.buildable)
class TaskGridBuildableTerrain  extends GridTask(With.grids.buildableTerrain)
class TaskGridChokepoints       extends GridTask(With.grids.chokepoints)
class TaskGridCreep             extends GridTask(With.grids.creep)
class TaskGridEnemyDetection    extends GridTask(With.grids.enemyDetection)
class TaskGridFriendlyDetection extends GridTask(With.grids.friendlyDetection)
class TaskGridFriendlyVision    extends GridTask(With.grids.friendlyVision)
class TaskGridMobilityBorder    extends GridTask(With.grids.mobilityBorder)
class TaskGridMobilityBuildings extends GridTask(With.grids.mobilityBuildings)
class TaskGridMobilityTerrain   extends GridTask(With.grids.mobilityTerrain)
class TaskGridPsi2x2and3x2      extends GridTask(With.grids.psi2Height)
class TaskGridPsi4x3            extends GridTask(With.grids.psi3Height)
class TaskGridUnits             extends GridTask(With.grids.units)
class TaskGridWalkable          extends GridTask(With.grids.walkable)
class TaskGridWalkableTerrain   extends GridTask(With.grids.walkableTerrain)
class TaskGridWalkableUnits     extends GridTask(With.grids.walkableUnits)